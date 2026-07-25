package com.lifelink.user;

import com.lifelink.user.dto.ChangePasswordDto;
import com.lifelink.user.dto.UpdateProfileDto;
import com.lifelink.user.dto.UserProfileDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Tag(name = "User Profile", description = "Authenticated user profile management")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * GET /api/v1/users/me
     * Returns the authenticated user's own profile.
     * Never exposes passwordHash.
     */
    @Operation(summary = "Get my profile")
    @GetMapping("/me")
    public ResponseEntity<UserProfileDto> getMe(Authentication authentication) {
        return ResponseEntity.ok(userService.getMe(authentication.getName()));
    }

    /**
     * PUT /api/v1/users/me
     * Updates the authenticated user's name and/or email.
     * Patch semantics: null/blank fields are ignored.
     * Phone and role cannot be changed after registration.
     */
    @Operation(summary = "Update my profile (name, email)")
    @PutMapping("/me")
    public ResponseEntity<UserProfileDto> updateMe(
            Authentication authentication,
            @Valid @RequestBody UpdateProfileDto dto) {
        return ResponseEntity.ok(userService.updateMe(authentication.getName(), dto));
    }

    /**
     * PUT /api/v1/users/me/password
     * Changes the authenticated user's password.
     * Requires the current password for verification — prevents unauthorized changes
     * if a session token is stolen.
     */
    @Operation(summary = "Change my password (requires current password)")
    @PutMapping("/me/password")
    public ResponseEntity<Void> changePassword(
            Authentication authentication,
            @Valid @RequestBody ChangePasswordDto dto) {
        userService.changePassword(authentication.getName(), dto);
        return ResponseEntity.noContent().build();
    }
}
