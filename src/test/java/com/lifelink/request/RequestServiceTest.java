package com.lifelink.request;

import com.lifelink.bloodchain.BloodChainService;
import com.lifelink.donor.Donor;
import com.lifelink.donor.DonorRepository;
import com.lifelink.matching.MatchingEngine;
import com.lifelink.notification.FcmService;
import com.lifelink.user.User;
import com.lifelink.user.UserRepository;
import com.lifelink.webhook.WebhookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RequestServiceTest {

    @Mock private RequestRepository requestRepository;
    @Mock private RequestResponseRepository requestResponseRepository;
    @Mock private DonorRepository donorRepository;
    @Mock private UserRepository userRepository;
    @Mock private MatchingEngine matchingEngine;
    @Mock private FcmService fcmService;
    @Mock private BloodChainService bloodChainService;
    @Mock private RequestEventRepository requestEventRepository;
    @Mock private WebhookService webhookService;
    @Mock private com.lifelink.bloodbank.BloodBankRepository bloodBankRepository;
    @Mock private RequestSseService requestSseService;
    @Mock private com.lifelink.institution.HospitalForwardRepository hospitalForwardRepository;
    @Mock private com.lifelink.driver.DriverRepository driverRepository;
    @Mock private com.lifelink.hospital.HospitalRepository hospitalRepository;

    @InjectMocks
    private RequestService requestService;

    private EmergencyRequest request;
    private User requester;

    @BeforeEach
    void setUp() {
        requester = new User();
        requester.setId(UUID.randomUUID());
        
        request = new EmergencyRequest();
        request.setId(UUID.randomUUID());
        request.setRequester(requester);
        request.setStatus(RequestStatus.PENDING);
        request.setCurrentRadiusKm(5);
        request.setExpiresAt(LocalDateTime.now().plusHours(24));
        request.setComponentType(ComponentType.WHOLE_BLOOD);
        request.setUrgency(Urgency.HIGH);
        request.setBloodType("O+");
        org.locationtech.jts.geom.GeometryFactory gf = new org.locationtech.jts.geom.GeometryFactory();
        request.setLocation(gf.createPoint(new org.locationtech.jts.geom.Coordinate(77.5946, 12.9716)));
    }

    @Test
    void testFulfillRequest() {
        when(requestRepository.findById(request.getId())).thenReturn(Optional.of(request));
        
        Donor donor = new Donor();
        donor.setFcmToken("fcm123");
        
        RequestResponse response = new RequestResponse();
        response.setDonor(donor);
        response.setStatus(RequestResponseStatus.ACCEPTED);
        
        when(requestResponseRepository.findByRequestId(request.getId()))
                .thenReturn(List.of(response));

        requestService.fulfillRequest(request.getId());

        assertEquals(RequestStatus.FULFILLED, request.getStatus());
        verify(requestRepository, times(1)).save(request);
        verify(fcmService, times(1)).sendStandDownNotification("fcm123", request.getId().toString());
    }

    @Test
    void testCancelRequest() {
        when(requestRepository.findById(request.getId())).thenReturn(Optional.of(request));

        requestService.cancelRequest(request.getId());

        assertEquals(RequestStatus.CANCELLED, request.getStatus());
        verify(requestRepository, times(1)).save(request);
    }

    @Test
    void testAutoExpandRadius() {
        org.springframework.test.util.ReflectionTestUtils.setField(requestService, "maxRadiusKm", 50);
        request.setCurrentRadiusKm(5);
        when(requestRepository.findByStatus(RequestStatus.PENDING))
                .thenReturn(new ArrayList<>(List.of(request)));
        when(matchingEngine.findEligibleDonors(eq(request), anyInt())).thenReturn(List.of());

        requestService.autoExpandRadius();

        assertEquals(15, request.getCurrentRadiusKm());
        verify(requestRepository, times(1)).save(request);
    }

    @Test
    void testExpireRequests() {
        request.setExpiresAt(LocalDateTime.now().minusMinutes(5));
        
        when(requestRepository.findByStatus(RequestStatus.PENDING))
                .thenReturn(new ArrayList<>(List.of(request)));
        when(requestRepository.findByStatus(RequestStatus.IN_PROGRESS))
                .thenReturn(new ArrayList<>());

        requestService.expireRequests();

        assertEquals(RequestStatus.EXPIRED, request.getStatus());
        verify(requestRepository, times(1)).save(request);
    }

    @Test
    void testCreateRequest_HospitalInventoryFound() {
        com.lifelink.hospital.Hospital hospital = new com.lifelink.hospital.Hospital();
        hospital.setId(UUID.randomUUID());
        hospital.setName("Test Hospital");

        org.springframework.data.domain.Page<com.lifelink.hospital.Hospital> page = new org.springframework.data.domain.PageImpl<>(List.of(hospital));
        
        when(requestRepository.save(any())).thenReturn(request);
        when(hospitalRepository.findNearbyWithAvailableBlood(
            eq(request.getLocation()), eq(15000.0), eq(request.getBloodType()), eq(request.getComponentType().name()), any()
        )).thenReturn(page);

        EmergencyRequest result = requestService.createRequest(request);

        assertEquals(RequestStatus.FULFILLED, result.getStatus());
        verify(requestRepository, times(2)).save(any(EmergencyRequest.class));
        verify(matchingEngine, never()).findEligibleDonors(any(), anyInt());
    }

    @Test
    void testCreateRequest_NoHospitalInventory_TriggersDonorSearch() {
        org.springframework.data.domain.Page<com.lifelink.hospital.Hospital> page = new org.springframework.data.domain.PageImpl<>(List.of());
        
        when(requestRepository.save(any())).thenReturn(request);
        when(hospitalRepository.findNearbyWithAvailableBlood(
            eq(request.getLocation()), eq(15000.0), eq(request.getBloodType()), eq(request.getComponentType().name()), any()
        )).thenReturn(page);
        
        // Mock matching engine to return empty, triggering blood chain
        when(matchingEngine.findEligibleDonors(any(), anyInt())).thenReturn(List.of());

        EmergencyRequest result = requestService.createRequest(request);

        assertEquals(RequestStatus.PENDING, result.getStatus());
        verify(requestRepository, times(1)).save(any(EmergencyRequest.class));
        verify(matchingEngine, times(1)).findEligibleDonors(any(), anyInt());
    }
}
