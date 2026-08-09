package com.lifelink.donor;

import com.lifelink.donor.dto.DonorDto;
import com.lifelink.donor.dto.FcmTokenDto;
import com.lifelink.donor.dto.LocationDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Donor", description = "Donor profile and availability management")
@RestController
@RequestMapping("/api/v1/donors")
@RequiredArgsConstructor
public class DonorController {

    private final DonorService donorService;

    /**
     * GET /api/v1/donors/me
     * Returns the authenticated donor's profile.
     */
    @Operation(summary = "Get my donor profile")
    @GetMapping("/me")
    public ResponseEntity<DonorDto> getMe(Authentication authentication) {
        return ResponseEntity.ok(donorService.getMyProfile(authentication.getName()));
    }

    /**
     * PUT /api/v1/donors/me/location
     * Updates the authenticated donor's current GPS location.
     * Should be called periodically by the mobile client.
     */
    @Operation(summary = "Update my GPS location")
    @PutMapping("/me/location")
    public ResponseEntity<DonorDto> updateLocation(
            Authentication authentication,
            @Valid @RequestBody LocationDto locationDto) {
        return ResponseEntity.ok(donorService.updateLocation(authentication.getName(), locationDto));
    }

    /**
     * PUT /api/v1/donors/me/fcm-token
     * Registers or refreshes the FCM push notification token.
     * Must be called after login and whenever Firebase issues a new token.
     */
    @Operation(summary = "Register or refresh FCM push notification token")
    @PutMapping("/me/fcm-token")
    public ResponseEntity<Void> updateFcmToken(
            Authentication authentication,
            @Valid @RequestBody FcmTokenDto fcmTokenDto) {
        donorService.updateFcmToken(authentication.getName(), fcmTokenDto);
        return ResponseEntity.noContent().build();
    }

    /**
     * PUT /api/v1/donors/me/active?active=true|false
     * Toggles the donor's availability. Inactive donors are excluded from
     * all matching queries and receive no emergency notifications.
     */
    @Operation(summary = "Toggle donor availability (active/inactive)")
    @PutMapping("/me/active")
    public ResponseEntity<DonorDto> setActiveStatus(
            Authentication authentication,
            @RequestParam boolean active) {
        return ResponseEntity.ok(donorService.setActiveStatus(authentication.getName(), active));
    }

    @Operation(summary = "Verify donor identity with government ID")
    @PostMapping("/me/verify-identity")
    public ResponseEntity<DonorDto> verifyIdentity(
            Authentication authentication,
            @Valid @RequestBody com.lifelink.donor.dto.VerifyIdentityDto dto) {
        return ResponseEntity.ok(donorService.verifyIdentity(authentication.getName(), dto));
    }

    @Operation(summary = "Get full donation history (paginated)")
    @GetMapping("/me/history")
    public ResponseEntity<com.lifelink.common.dto.PaginatedResponse<com.lifelink.donor.dto.DonationHistoryDto>> getDonationHistory(
            Authentication authentication,
            @org.springdoc.core.annotations.ParameterObject org.springframework.data.domain.Pageable pageable) {
        org.springframework.data.domain.Page<com.lifelink.donor.dto.DonationHistoryDto> page = donorService.getDonationHistory(authentication.getName(), pageable);
        return ResponseEntity.ok(com.lifelink.common.util.PaginationUtil.fromPage(page));
    }
}

