package com.lifelink.request;

import com.lifelink.request.dto.CreateRequestDto;
import com.lifelink.user.User;
import com.lifelink.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/requests")
@RequiredArgsConstructor
public class RequestController {

    private final RequestService requestService;
    private final RequestRepository requestRepository;
    private final UserRepository userRepository;
    private final GeometryFactory geometryFactory = new GeometryFactory();

    @PostMapping
    public ResponseEntity<?> createRequest(Authentication authentication, @RequestBody CreateRequestDto dto) {
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
        return ResponseEntity.ok(saved); // NOTE: DTO mapping is simplified here for brevity
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<?> cancelRequest(@PathVariable UUID id) {
        requestService.cancelRequest(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/fulfill")
    public ResponseEntity<?> fulfillRequest(@PathVariable UUID id) {
        requestService.fulfillRequest(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getRequest(@PathVariable UUID id) {
        return ResponseEntity.ok(requestRepository.findById(id).orElse(null)); // Should use DTO
    }
}
