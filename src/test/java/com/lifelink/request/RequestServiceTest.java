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
        request.setCurrentRadiusKm(5);
        when(requestRepository.findByStatus(RequestStatus.PENDING))
                .thenReturn(new ArrayList<>(List.of(request)));
        when(matchingEngine.findEligibleDonors(request, 15)).thenReturn(List.of());

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
}
