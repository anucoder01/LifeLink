package com.lifelink.request;

import com.lifelink.request.dto.CreateRequestDto;
import com.lifelink.request.dto.RespondToRequestDto;
import com.lifelink.request.dto.RequestEventDto;
import com.lifelink.request.dto.RequestResponseDto;
import com.lifelink.common.dto.PaginatedResponse;
import com.lifelink.common.util.PaginationUtil;
import com.lifelink.user.User;
import com.lifelink.user.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    private final RequestSseService requestSseService;
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

    @Operation(summary = "List my emergency requests (paginated)")
    @GetMapping
    public ResponseEntity<PaginatedResponse<EmergencyRequest>> getMyRequests(
            Authentication authentication,
            @org.springdoc.core.annotations.ParameterObject Pageable pageable) {
        Page<EmergencyRequest> page = requestService.getRequestsByRequester(authentication.getName(), pageable);
        return ResponseEntity.ok(PaginationUtil.fromPage(page));
    }

    @Operation(summary = "Get responses for an emergency request (paginated)")
    @GetMapping("/{id}/responses")
    public ResponseEntity<PaginatedResponse<RequestResponseDto>> getRequestResponses(
            Authentication authentication,
            @PathVariable UUID id,
            @org.springdoc.core.annotations.ParameterObject Pageable pageable) {
        Page<RequestResponseDto> page = requestService.getRequestResponses(id, authentication.getName(), pageable)
                .map(this::convertToDto);
        return ResponseEntity.ok(PaginationUtil.fromPage(page));
    }

    @Operation(summary = "Get event history log for an emergency request (paginated)")
    @GetMapping("/{id}/events")
    public ResponseEntity<PaginatedResponse<RequestEventDto>> getRequestEvents(
            Authentication authentication,
            @PathVariable UUID id,
            @org.springdoc.core.annotations.ParameterObject Pageable pageable) {
        Page<RequestEventDto> page = requestService.getRequestEvents(id, authentication.getName(), pageable)
                .map(this::convertToDto);
        return ResponseEntity.ok(PaginationUtil.fromPage(page));
    }

    @Operation(summary = "Listen to real-time status updates via SSE")
    @GetMapping(value = "/{id}/stream", produces = org.springframework.http.MediaType.TEXT_EVENT_STREAM_VALUE)
    public org.springframework.web.servlet.mvc.method.annotation.SseEmitter streamRequestStatus(
            Authentication authentication,
            @PathVariable UUID id) {
        // We verify the user has access to the request first
        requestService.getRequestEvents(id, authentication.getName(), Pageable.ofSize(1));
        return requestSseService.subscribe(id, authentication.getName());
    }

    @Operation(summary = "Update response status of donor (EN_ROUTE, DONATED, NO_SHOW)")
    @PutMapping("/{id}/response-status")
    public ResponseEntity<Void> updateResponseStatus(
            Authentication authentication,
            @PathVariable UUID id,
            @RequestParam com.lifelink.request.RequestResponseStatus status) {
        requestService.updateResponseStatus(id, authentication.getName(), status);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Request a volunteer driver for transportation")
    @PostMapping("/{id}/request-driver")
    public ResponseEntity<Void> requestDriver(
            Authentication authentication,
            @PathVariable UUID id) {
        requestService.requestDriver(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }

    private RequestResponseDto convertToDto(RequestResponse entity) {
        RequestResponseDto dto = new RequestResponseDto();
        dto.setId(entity.getId());
        dto.setDonorId(entity.getDonor().getId());
        
        RequestResponseStatus status = entity.getStatus();
        if (status == RequestResponseStatus.ACCEPTED || status == RequestResponseStatus.EN_ROUTE || status == RequestResponseStatus.DONATED) {
            if (entity.getDonor().getUser() != null) {
                dto.setDonorName(entity.getDonor().getUser().getName());
                dto.setDonorPhone(entity.getDonor().getUser().getPhone());
            }
        } else {
            dto.setDonorName("Anonymous Donor");
            dto.setDonorPhone(null);
        }
        
        dto.setStatus(entity.getStatus().name());
        dto.setRespondedAt(entity.getRespondedAt());
        return dto;
    }

    private RequestEventDto convertToDto(RequestEvent entity) {
        RequestEventDto dto = new RequestEventDto();
        dto.setId(entity.getId());
        dto.setEventType(entity.getEventType());
        dto.setMessage(entity.getMessage());
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }
    @Operation(summary = "Get shareable public link for emergency request")
    @GetMapping("/{id}/share")
    public ResponseEntity<com.lifelink.request.dto.EmergencyRequestShareDto> getShareableLink(@PathVariable UUID id) {
        EmergencyRequest request = requestService.getById(id);
        com.lifelink.request.dto.EmergencyRequestShareDto dto = com.lifelink.request.dto.EmergencyRequestShareDto.builder()
                .id(request.getId().toString())
                .bloodType(request.getBloodType())
                .componentType(request.getComponentType().name())
                .urgency(request.getUrgency().name())
                .status(request.getStatus().name())
                .latitude(request.getLocation().getY())
                .longitude(request.getLocation().getX())
                .build();
        return ResponseEntity.ok(dto);
    }
}
