package com.lifelink.donor;

import com.lifelink.donor.dto.DonorHealthQuestionnaireDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Donor Health", description = "Donor medical eligibility and health profiling")
@RestController
@RequestMapping("/api/v1/donors/me/health")
@RequiredArgsConstructor
public class DonorHealthController {

    private final DonorHealthService donorHealthService;

    @Operation(summary = "Get my health profile")
    @GetMapping
    public ResponseEntity<DonorHealthQuestionnaireDto> getHealthProfile(Authentication authentication) {
        DonorHealthQuestionnaireDto dto = donorHealthService.getHealthProfile(authentication.getName());
        if (dto == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(dto);
    }

    @Operation(summary = "Update my health profile")
    @PutMapping
    public ResponseEntity<DonorHealthQuestionnaireDto> updateHealthProfile(
            Authentication authentication,
            @RequestBody DonorHealthQuestionnaireDto dto) {
        return ResponseEntity.ok(donorHealthService.updateHealthProfile(authentication.getName(), dto));
    }
}
