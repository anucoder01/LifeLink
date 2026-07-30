package com.lifelink.donor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lifelink.donor.dto.LiveLocationDto;
import com.lifelink.request.EmergencyRequest;
import com.lifelink.request.RequestRepository;
import com.lifelink.request.RequestResponse;
import com.lifelink.request.RequestResponseRepository;
import com.lifelink.request.RequestResponseStatus;
import com.lifelink.user.User;
import com.lifelink.user.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Tag(name = "Live Location", description = "Live tracking of accepted donors")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class LiveLocationController {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;
    private final DonorRepository donorRepository;
    private final RequestResponseRepository requestResponseRepository;
    private final RequestRepository requestRepository;

    private static final String LOCATION_PREFIX = "live_location:req:";

    @Operation(summary = "Push live location for an EN_ROUTE donor")
    @PostMapping("/donors/me/location/live")
    public ResponseEntity<Void> pushLiveLocation(
            Authentication authentication,
            @RequestParam UUID requestId,
            @RequestBody LiveLocationDto locationDto) {
        
        User user = userRepository.findByPhone(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Donor donor = donorRepository.findByUserId(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Donor not found"));

        RequestResponse response = requestResponseRepository.findByRequestIdAndDonorId(requestId, donor.getId())
                .orElseThrow(() -> new IllegalArgumentException("Response not found"));

        if (response.getStatus() != RequestResponseStatus.EN_ROUTE && response.getStatus() != RequestResponseStatus.ACCEPTED) {
            return ResponseEntity.badRequest().build();
        }

        locationDto.setDonorId(donor.getId().toString());
        locationDto.setTimestamp(LocalDateTime.now());

        String redisKey = LOCATION_PREFIX + requestId + ":donor:" + donor.getId();
        
        try {
            String json = objectMapper.writeValueAsString(locationDto);
            // Cache for 10 minutes, so it auto-expires if donor goes offline
            redisTemplate.opsForValue().set(redisKey, json, 10, TimeUnit.MINUTES);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize live location", e);
            return ResponseEntity.internalServerError().build();
        }

        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Poll live locations of all EN_ROUTE donors for a request")
    @GetMapping("/requests/{requestId}/live-locations")
    public ResponseEntity<List<LiveLocationDto>> getLiveLocations(
            Authentication authentication,
            @PathVariable UUID requestId) {
        
        User user = userRepository.findByPhone(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        EmergencyRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Request not found"));
        
        // Only requester can view live locations
        if (!request.getRequester().getId().equals(user.getId())) {
            return ResponseEntity.status(403).build();
        }

        // Find all keys matching this request
        String pattern = LOCATION_PREFIX + requestId + ":donor:*";
        var keys = redisTemplate.keys(pattern);
        List<LiveLocationDto> locations = new ArrayList<>();

        if (keys != null && !keys.isEmpty()) {
            for (String key : keys) {
                String json = redisTemplate.opsForValue().get(key);
                if (json != null) {
                    try {
                        LiveLocationDto dto = objectMapper.readValue(json, LiveLocationDto.class);
                        locations.add(dto);
                    } catch (JsonProcessingException e) {
                        log.error("Failed to deserialize live location from Redis key {}", key, e);
                    }
                }
            }
        }

        return ResponseEntity.ok(locations);
    }
}
