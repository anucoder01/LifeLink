package com.lifelink.bloodchain;

import com.lifelink.bloodchain.dto.AddVouchDto;
import com.lifelink.bloodchain.dto.InviteDetailsDto;
import com.lifelink.bloodchain.dto.VouchDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Blood Chain", description = "Manage trusted backup donor contacts (Blood Chain — Feature A)")
@RestController
@RequiredArgsConstructor
public class BloodChainController {

    private final BloodChainService bloodChainService;

    // =========================================================================
    // Vouch management — authenticated donors only
    // =========================================================================

    /**
     * GET /api/v1/donors/me/vouches
     * Returns the authenticated donor's list of trusted contacts (phone numbers masked).
     */
    @Operation(summary = "List my trusted backup donor contacts (paginated)")
    @GetMapping("/api/v1/donors/me/vouches")
    public ResponseEntity<com.lifelink.common.dto.PaginatedResponse<VouchDto>> getMyVouches(
            Authentication authentication,
            @org.springdoc.core.annotations.ParameterObject org.springframework.data.domain.Pageable pageable) {
        org.springframework.data.domain.Page<VouchDto> page = bloodChainService.getMyVouches(authentication.getName(), pageable);
        return ResponseEntity.ok(com.lifelink.common.util.PaginationUtil.fromPage(page));
    }

    /**
     * POST /api/v1/donors/me/vouches
     * Nominates a new trusted contact. Maximum 3 per donor.
     */
    @Operation(summary = "Nominate a trusted backup donor contact (max 3)")
    @PostMapping("/api/v1/donors/me/vouches")
    public ResponseEntity<VouchDto> addVouch(
            Authentication authentication,
            @Valid @RequestBody AddVouchDto dto) {
        return ResponseEntity.ok(bloodChainService.addVouch(authentication.getName(), dto));
    }

    /**
     * DELETE /api/v1/donors/me/vouches/{id}
     * Removes a nominated trusted contact.
     */
    @Operation(summary = "Remove a trusted contact from your Blood Chain")
    @DeleteMapping("/api/v1/donors/me/vouches/{id}")
    public ResponseEntity<Void> removeVouch(
            Authentication authentication,
            @PathVariable UUID id) {
        bloodChainService.removeVouch(authentication.getName(), id);
        return ResponseEntity.noContent().build();
    }

    // =========================================================================
    // Public invite flow — no auth required
    // =========================================================================

    /**
     * GET /api/v1/blood-chain/invite/{token}
     * Public endpoint. Validates an invite token and returns pre-fill data for the
     * registration form (contact name, phone, emergency summary).
     * Does NOT consume the token — token is consumed on successful registration.
     */
    @Operation(summary = "Validate a Blood Chain invite link (public)")
    @GetMapping("/api/v1/blood-chain/invite/{token}")
    public ResponseEntity<InviteDetailsDto> validateInvite(@PathVariable String token) {
        InviteDetailsDto details = bloodChainService.validateInviteToken(token);
        // Return 200 even for invalid tokens — let the client render the error message
        return ResponseEntity.ok(details);
    }
}
