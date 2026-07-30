package com.lifelink.donor;

import com.lifelink.donor.dto.DonorConsentDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Donor Consent", description = "Donor privacy and consent management")
@RestController
@RequestMapping("/api/v1/donors/me/consent")
@RequiredArgsConstructor
public class DonorConsentController {

    private final DonorConsentService donorConsentService;

    @Operation(summary = "Get my consent profile")
    @GetMapping
    public ResponseEntity<DonorConsentDto> getConsentProfile(Authentication authentication) {
        DonorConsentDto dto = donorConsentService.getConsentProfile(authentication.getName());
        return ResponseEntity.ok(dto);
    }

    @Operation(summary = "Update my consent profile")
    @PutMapping
    public ResponseEntity<DonorConsentDto> updateConsentProfile(
            Authentication authentication,
            @RequestBody DonorConsentDto dto) {
        return ResponseEntity.ok(donorConsentService.updateConsentProfile(authentication.getName(), dto));
    }
}
