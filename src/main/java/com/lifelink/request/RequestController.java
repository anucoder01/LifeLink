package com.lifelink.request;

import com.lifelink.request.dto.CreateRequestDto;
import com.lifelink.request.dto.RespondToRequestDto;
import com.lifelink.user.User;
import com.lifelink.user.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Emergency Requests", description = "Create and manage blood emergency requests")
@RestController
@RequestMapping("/api/v1/requests")
@RequiredArgsConstructor
public class RequestController {

    private final RequestService requestService;
    private final UserRepository userRepository;
    private final GeometryFactory geometryFactory = new GeometryFactory();

    /**
     * POST /api/v1/requests
     * Creates a new emergency blood request and immediately broadcasts to nearby eligible donors.
     */
    @Operation(summary = "Create a new emergency blood request")
    @PostMapping
    public ResponseEntity<?> createRequest(
            Authentication authentication,
            @Valid @RequestBody CreateRequestDto dto) {

        User user = userRepository.findByPhone(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        EmergencyRequest request = new EmergencyRequest();
        request.setRequester(user);
        request.setBloodType(dto.getBloodType());
        request.setComponentType(dto.getComponentType());
        request.setUrgency(dto.getUrgency());

        Point location = geometryFactory.createPoint(new Coordinate(dto.getLongitude(), dto.getLatitude()));
        location.setSRID(4326);
        request.setLocation(location);

        EmergencyRequest saved = requestService.createRequest(request);
        return ResponseEntity.ok(saved);
    }

    /**
     * POST /api/v1/requests/{id}/respond
     * Allows an authenticated donor to ACCEPT or DECLINE a blood request notification.
     * Donor must have been previously notified (i.e., appear in request_responses).
     */
    @Operation(summary = "Respond to an emergency request (ACCEPTED or DECLINED)")
    @PostMapping("/{id}/respond")
    public ResponseEntity<Void> respondToRequest(
            Authentication authentication,
            @PathVariable UUID id,
            @Valid @RequestBody RespondToRequestDto dto) {

        requestService.respondToRequest(id, authentication.getName(), dto.getStatus());
        return ResponseEntity.noContent().build();
    }

    /**
     * PUT /api/v1/requests/{id}/cancel
     * Cancels a PENDING or IN_PROGRESS request. Typically called by the requester.
     */
    @Operation(summary = "Cancel an active request")
    @PutMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelRequest(@PathVariable UUID id) {
        requestService.cancelRequest(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * PUT /api/v1/requests/{id}/fulfill
     * Marks a request as FULFILLED. Notifies all pending/accepted donors to stand down.
     */
    @Operation(summary = "Mark a request as fulfilled")
    @PutMapping("/{id}/fulfill")
    public ResponseEntity<Void> fulfillRequest(@PathVariable UUID id) {
        requestService.fulfillRequest(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * GET /api/v1/requests/{id}
     * Returns request details by ID.
     */
    @Operation(summary = "Get request details by ID")
    @GetMapping("/{id}")
    public ResponseEntity<?> getRequest(@PathVariable UUID id) {
        return ResponseEntity.ok(requestService.getById(id));
    }
}

