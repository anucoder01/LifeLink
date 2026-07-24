package com.lifelink.auth;

import com.lifelink.auth.dto.AuthRequest;
import com.lifelink.auth.dto.AuthResponse;
import com.lifelink.auth.dto.RegisterRequest;
import com.lifelink.bloodchain.BloodChainService;
import com.lifelink.bloodchain.dto.InviteDetailsDto;
import com.lifelink.donor.Donor;
import com.lifelink.donor.DonorRepository;
import com.lifelink.user.Role;
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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Auth", description = "Authentication and registration")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final TokenProvider tokenProvider;
    private final UserRepository userRepository;
    private final DonorRepository donorRepository;
    private final PasswordEncoder passwordEncoder;
    private final BloodChainService bloodChainService;
    private final GeometryFactory geometryFactory = new GeometryFactory();

    @Operation(summary = "Login with phone and password")
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getPhone(), request.getPassword())
        );

        String token = tokenProvider.generateToken(authentication);
        return ResponseEntity.ok(new AuthResponse(token));
    }

    @Operation(summary = "Register a new user")
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        if (userRepository.findByPhone(request.getPhone()).isPresent()) {
            return ResponseEntity.badRequest().body("Phone number is already in use");
        }

        User user = new User();
        user.setName(request.getName());
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());

        userRepository.save(user);

        if (request.getRole() == Role.DONOR) {
            Donor donor = new Donor();
            donor.setUser(user);
            donor.setBloodType(request.getBloodType());
            if (request.getLatitude() != null && request.getLongitude() != null) {
                // PostGIS uses Longitude, Latitude for Point
                Point location = geometryFactory.createPoint(new Coordinate(request.getLongitude(), request.getLatitude()));
                location.setSRID(4326);
                donor.setLocation(location);
            }
            donorRepository.save(donor);
        }

        return ResponseEntity.ok("User registered successfully");
    }

    /**
     * POST /api/v1/auth/register/invited?token={inviteToken}
     * Special registration flow for contacts invited via the Blood Chain.
     * The invite token pre-fills phone number; the registrant only needs to supply
     * name, password, blood type, and location. On success the token is consumed.
     */
    @Operation(summary = "Register via a Blood Chain invite link (public)")
    @PostMapping("/register/invited")
    public ResponseEntity<?> registerViaInvite(
            @RequestParam String token,
            @Valid @RequestBody RegisterRequest request) {

        InviteDetailsDto invite = bloodChainService.validateInviteToken(token);
        if (!invite.isValid()) {
            return ResponseEntity.badRequest().body(invite.getInvalidReason());
        }

        // Force the phone from the invite — prevents registering a different number
        request.setPhone(invite.getContactPhone());
        request.setRole(Role.DONOR); // Invited contacts are always registering as donors

        if (userRepository.findByPhone(request.getPhone()).isPresent()) {
            // Already registered — just consume token and return success
            bloodChainService.consumeInviteToken(token);
            return ResponseEntity.ok("You are already registered. Please log in.");
        }

        User user = new User();
        user.setName(request.getName() != null ? request.getName() : invite.getContactName());
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.DONOR);
        userRepository.save(user);

        Donor donor = new Donor();
        donor.setUser(user);
        donor.setBloodType(request.getBloodType());
        if (request.getLatitude() != null && request.getLongitude() != null) {
            org.locationtech.jts.geom.Point location = geometryFactory.createPoint(
                    new org.locationtech.jts.geom.Coordinate(request.getLongitude(), request.getLatitude()));
            location.setSRID(4326);
            donor.setLocation(location);
        }
        donorRepository.save(donor);

        bloodChainService.consumeInviteToken(token);
        return ResponseEntity.ok("Registration successful! You are now part of the Blood Chain.");
    }
}
