package com.lifelink.auth;

import com.lifelink.auth.dto.AuthRequest;
import com.lifelink.auth.dto.AuthResponse;
import com.lifelink.auth.dto.RegisterRequest;
import com.lifelink.donor.Donor;
import com.lifelink.donor.DonorRepository;
import com.lifelink.user.Role;
import com.lifelink.user.User;
import com.lifelink.user.UserRepository;
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

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final TokenProvider tokenProvider;
    private final UserRepository userRepository;
    private final DonorRepository donorRepository;
    private final PasswordEncoder passwordEncoder;
    private final GeometryFactory geometryFactory = new GeometryFactory();

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getPhone(), request.getPassword())
        );

        String token = tokenProvider.generateToken(authentication);
        return ResponseEntity.ok(new AuthResponse(token));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
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
}
