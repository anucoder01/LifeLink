package com.lifelink.auth;

import com.lifelink.auth.dto.*;
import com.google.firebase.auth.FirebaseToken;
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
    private final com.lifelink.bloodbank.BloodBankRepository bloodBankRepository;
    private final com.lifelink.ngo.NgoRepository ngoRepository;
    private final PasswordEncoder passwordEncoder;
    private final BloodChainService bloodChainService;
    private final OtpService otpService;
    private final FirebaseAuthService firebaseAuthService;
    private final GeometryFactory geometryFactory = new GeometryFactory();

    @Operation(summary = "Login with phone and password")
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getPhone(), request.getPassword())
        );

        String token = tokenProvider.generateToken(authentication);
        String refreshToken = tokenProvider.generateRefreshToken(authentication);
        return ResponseEntity.ok(new AuthResponse(token, refreshToken));
    }

    @Operation(summary = "Refresh access token")
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@Valid @RequestBody RefreshRequest request) {
        String refreshToken = request.getRefreshToken();
        
        if (tokenProvider.validateToken(refreshToken) && tokenProvider.isRefreshToken(refreshToken)) {
            Authentication authentication = tokenProvider.getAuthentication(refreshToken);
            String newToken = tokenProvider.generateToken(authentication);
            String newRefreshToken = tokenProvider.generateRefreshToken(authentication);
            return ResponseEntity.ok(new AuthResponse(newToken, newRefreshToken));
        }
        
        return ResponseEntity.status(401).body("Invalid refresh token");
    }

    @Operation(summary = "Send OTP to phone number")
    @PostMapping("/otp/send")
    public ResponseEntity<?> sendOtp(@Valid @RequestBody OtpSendRequest request) {
        otpService.sendOtp(request.getPhone());
        return ResponseEntity.ok("OTP sent successfully");
    }

    @Operation(summary = "Verify OTP and login")
    @PostMapping("/otp/verify")
    public ResponseEntity<?> verifyOtp(@Valid @RequestBody OtpVerifyRequest request) {
        if (!otpService.verifyOtp(request.getPhone(), request.getOtp())) {
            return ResponseEntity.status(401).body("Invalid or expired OTP");
        }

        User user = userRepository.findByPhone(request.getPhone())
                .orElse(null);

        if (user == null) {
            // Depending on requirements, we could auto-register or return a specific status.
            return ResponseEntity.status(404).body("User not found. Please register first.");
        }

        Authentication authentication = new UsernamePasswordAuthenticationToken(
            user.getPhone(), null, java.util.Collections.singletonList(
                new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + user.getRole().name())));
        String token = tokenProvider.generateToken(authentication);
        String refreshToken = tokenProvider.generateRefreshToken(authentication);

        return ResponseEntity.ok(new AuthResponse(token, refreshToken));
    }

    @Operation(summary = "Login with Google Sign-In")
    @PostMapping("/google")
    public ResponseEntity<?> googleSignIn(@Valid @RequestBody GoogleSignInRequest request) {
        FirebaseToken decodedToken = firebaseAuthService.verifyIdToken(request.getIdToken());
        if (decodedToken == null) {
            return ResponseEntity.status(401).body("Invalid Google ID Token");
        }

        String email = decodedToken.getEmail();
        if (email == null) {
            return ResponseEntity.badRequest().body("Google account must have an email");
        }

        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            // Auto-register google users as REQUESTERs for now, since they didn't provide blood type.
            user = new User();
            user.setName(decodedToken.getName());
            user.setEmail(email);
            // Generate a placeholder phone since it's required (or adapt DB schema)
            user.setPhone("GGL-" + decodedToken.getUid()); 
            user.setPasswordHash(passwordEncoder.encode(decodedToken.getUid())); // dummy password
            user.setRole(Role.REQUESTER);
            user = userRepository.save(user);
        }

        Authentication authentication = new UsernamePasswordAuthenticationToken(
            user.getPhone(), null, java.util.Collections.singletonList(
                new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + user.getRole().name())));
        String token = tokenProvider.generateToken(authentication);
        String refreshToken = tokenProvider.generateRefreshToken(authentication);

        return ResponseEntity.ok(new AuthResponse(token, refreshToken));
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
        } else if (request.getRole() == Role.BLOOD_BANK_ADMIN) {
            com.lifelink.bloodbank.BloodBank bloodBank = new com.lifelink.bloodbank.BloodBank();
            bloodBank.setUser(user);
            bloodBank.setName(request.getInstitutionName() != null ? request.getInstitutionName() : request.getName());
            bloodBank.setAddress(request.getAddress());
            bloodBank.setContactPhone(request.getContactPhone());
            bloodBank.setLicenseNumber(request.getLicenseOrRegistrationNumber());
            bloodBank.setOperatingHours(request.getOperatingHours());
            if (request.getLatitude() != null && request.getLongitude() != null) {
                Point location = geometryFactory.createPoint(new Coordinate(request.getLongitude(), request.getLatitude()));
                location.setSRID(4326);
                bloodBank.setLocation(location);
            } else {
                return ResponseEntity.badRequest().body("Latitude and longitude are required for Blood Bank registration");
            }
            bloodBankRepository.save(bloodBank);
        } else if (request.getRole() == Role.NGO_ADMIN) {
            com.lifelink.ngo.Ngo ngo = new com.lifelink.ngo.Ngo();
            ngo.setUser(user);
            ngo.setName(request.getInstitutionName() != null ? request.getInstitutionName() : request.getName());
            ngo.setAddress(request.getAddress());
            ngo.setContactPhone(request.getContactPhone());
            ngo.setRegistrationNumber(request.getLicenseOrRegistrationNumber());
            ngo.setFocusAreas(request.getFocusAreas());
            if (request.getLatitude() != null && request.getLongitude() != null) {
                Point location = geometryFactory.createPoint(new Coordinate(request.getLongitude(), request.getLatitude()));
                location.setSRID(4326);
                ngo.setLocation(location);
            }
            ngoRepository.save(ngo);
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
