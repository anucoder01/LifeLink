package com.lifelink;

import com.lifelink.donor.Donor;
import com.lifelink.donor.DonorRepository;
import com.lifelink.matching.MatchingEngine;
import com.lifelink.notification.FcmService;
import com.lifelink.request.*;
import com.lifelink.user.Role;
import com.lifelink.user.User;
import com.lifelink.user.UserRepository;
import com.lifelink.webhook.WebhookSubscriptionRepository;
import com.lifelink.webhook.WebhookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
@Transactional
public class RequestServicePaginationAndWebhookTest {

    @Autowired
    private RequestService requestService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DonorRepository donorRepository;

    @Autowired
    private RequestResponseRepository requestResponseRepository;

    @Autowired
    private RequestEventRepository requestEventRepository;

    @MockBean
    private MatchingEngine matchingEngine;

    @MockBean
    private FcmService fcmService;

    @MockBean
    private WebhookService webhookService;

    private User requester;
    private User donorUser;
    private Donor donor;
    private final GeometryFactory geometryFactory = new GeometryFactory();

    @BeforeEach
    void setUp() {
        requester = new User();
        requester.setName("Alice");
        requester.setPhone("9999999991");
        requester.setEmail("alice_test@example.com");
        requester.setPasswordHash("hash");
        requester.setRole(Role.REQUESTER);
        requester = userRepository.save(requester);

        donorUser = new User();
        donorUser.setName("Bob");
        donorUser.setPhone("9999999992");
        donorUser.setEmail("bob_test@example.com");
        donorUser.setPasswordHash("hash");
        donorUser.setRole(Role.DONOR);
        donorUser = userRepository.save(donorUser);

        donor = new Donor();
        donor.setUser(donorUser);
        donor.setBloodType("O+");
        Point donorLoc = geometryFactory.createPoint(new Coordinate(12.9716, 77.5946));
        donorLoc.setSRID(4326);
        donor.setLocation(donorLoc);
        donor.setIsActive(true);
        donor.setFcmToken("fcm-token-bob");
        donor = donorRepository.save(donor);
    }

    @Test
    void testCreateRequestLogsEventAndTriggersWebhook() {
        Point reqLoc = geometryFactory.createPoint(new Coordinate(12.9716, 77.5946));
        reqLoc.setSRID(4326);

        EmergencyRequest request = new EmergencyRequest();
        request.setRequester(requester);
        request.setBloodType("O+");
        request.setComponentType(ComponentType.WHOLE_BLOOD);
        request.setUrgency(Urgency.NORMAL);
        request.setLocation(reqLoc);

        Mockito.when(matchingEngine.findEligibleDonors(any(), anyInt()))
                .thenReturn(Collections.singletonList(donor));

        EmergencyRequest saved = requestService.createRequest(request);

        assertNotNull(saved.getId());
        assertEquals(RequestStatus.PENDING, saved.getStatus());

        // Verify event was logged
        List<RequestEvent> events = requestEventRepository.findAll();
        assertFalse(events.isEmpty());
        assertTrue(events.stream().anyMatch(e -> e.getEventType().equals("CREATED")));

        // Verify webhook dispatch was triggered
        verify(webhookService, times(1)).dispatchEvent(eq(requester.getId()), any());
    }

    @Test
    void testDonorResponsesAndTransitions() {
        Point reqLoc = geometryFactory.createPoint(new Coordinate(12.9716, 77.5946));
        reqLoc.setSRID(4326);

        EmergencyRequest request = new EmergencyRequest();
        request.setRequester(requester);
        request.setBloodType("O+");
        request.setComponentType(ComponentType.WHOLE_BLOOD);
        request.setUrgency(Urgency.NORMAL);
        request.setLocation(reqLoc);

        Mockito.when(matchingEngine.findEligibleDonors(any(), anyInt()))
                .thenReturn(Collections.singletonList(donor));

        EmergencyRequest saved = requestService.createRequest(request);

        // Accept request
        requestService.respondToRequest(saved.getId(), donorUser.getPhone(), RequestResponseStatus.ACCEPTED);

        // Verify request moved to IN_PROGRESS
        EmergencyRequest updatedRequest = requestService.getById(saved.getId());
        assertEquals(RequestStatus.IN_PROGRESS, updatedRequest.getStatus());

        // Verify response status updated
        RequestResponse response = requestResponseRepository.findByRequestIdAndDonorId(saved.getId(), donor.getId()).orElseThrow();
        assertEquals(RequestResponseStatus.ACCEPTED, response.getStatus());

        // Transition status to EN_ROUTE
        requestService.updateResponseStatus(saved.getId(), donorUser.getPhone(), RequestResponseStatus.EN_ROUTE);
        response = requestResponseRepository.findByRequestIdAndDonorId(saved.getId(), donor.getId()).orElseThrow();
        assertEquals(RequestResponseStatus.EN_ROUTE, response.getStatus());

        // Transition status to DONATED
        requestService.updateResponseStatus(saved.getId(), donorUser.getPhone(), RequestResponseStatus.DONATED);
        response = requestResponseRepository.findByRequestIdAndDonorId(saved.getId(), donor.getId()).orElseThrow();
        assertEquals(RequestResponseStatus.DONATED, response.getStatus());

        // Verify event log size
        Page<RequestEvent> eventsPage = requestService.getRequestEvents(saved.getId(), requester.getPhone(), PageRequest.of(0, 10));
        assertTrue(eventsPage.getTotalElements() >= 4); // CREATED, DONOR_RESPONDED (ACCEPTED), DONOR_RESPONDED (EN_ROUTE), DONOR_RESPONDED (DONATED)
    }

    @Test
    void testPaginationQueries() {
        Point reqLoc = geometryFactory.createPoint(new Coordinate(12.9716, 77.5946));
        reqLoc.setSRID(4326);

        EmergencyRequest request = new EmergencyRequest();
        request.setRequester(requester);
        request.setBloodType("O+");
        request.setComponentType(ComponentType.WHOLE_BLOOD);
        request.setUrgency(Urgency.NORMAL);
        request.setLocation(reqLoc);

        Mockito.when(matchingEngine.findEligibleDonors(any(), anyInt()))
                .thenReturn(Collections.singletonList(donor));

        EmergencyRequest saved = requestService.createRequest(request);

        // Test getRequestsByRequester pagination
        Page<EmergencyRequest> reqsPage = requestService.getRequestsByRequester(requester.getPhone(), PageRequest.of(0, 10));
        assertEquals(1, reqsPage.getTotalElements());

        // Test getRequestResponses pagination
        Page<RequestResponse> respPage = requestService.getRequestResponses(saved.getId(), requester.getPhone(), PageRequest.of(0, 10));
        assertEquals(1, respPage.getTotalElements());
    }
}
