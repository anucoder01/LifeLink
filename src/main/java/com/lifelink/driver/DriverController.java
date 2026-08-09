package com.lifelink.driver;

import com.lifelink.driver.dto.DriverDto;
import com.lifelink.driver.dto.DriverRegistrationDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Driver", description = "Volunteer driver management")
@RestController
@RequestMapping("/api/v1/drivers")
@RequiredArgsConstructor
public class DriverController {

    private final DriverService driverService;

    @Operation(summary = "Register as a volunteer driver")
    @PostMapping("/register")
    @PreAuthorize("hasRole('DRIVER') or hasRole('DONOR')")
    public ResponseEntity<DriverDto> registerDriver(
            Authentication authentication,
            @Valid @RequestBody DriverRegistrationDto dto) {
        return ResponseEntity.ok(driverService.registerDriver(authentication.getName(), dto));
    }

    @Operation(summary = "Update driver availability status")
    @PutMapping("/me/availability")
    public ResponseEntity<DriverDto> updateAvailability(
            Authentication authentication,
            @RequestParam boolean isAvailable) {
        return ResponseEntity.ok(driverService.updateAvailability(authentication.getName(), isAvailable));
    }

    @Operation(summary = "Update driver live location")
    @PutMapping("/me/location")
    public ResponseEntity<DriverDto> updateLocation(
            Authentication authentication,
            @RequestParam double lat,
            @RequestParam double lng) {
        return ResponseEntity.ok(driverService.updateLocation(authentication.getName(), lat, lng));
    }

    @Operation(summary = "Get current driver profile")
    @GetMapping("/me")
    public ResponseEntity<DriverDto> getMyProfile(Authentication authentication) {
        return ResponseEntity.ok(driverService.getMyProfile(authentication.getName()));
    }
}
