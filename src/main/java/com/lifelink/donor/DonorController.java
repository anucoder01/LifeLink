package com.lifelink.donor;

import com.lifelink.donor.dto.DonorDto;
import com.lifelink.donor.dto.LocationDto;
import com.lifelink.user.User;
import com.lifelink.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/donors")
@RequiredArgsConstructor
public class DonorController {

    private final DonorRepository donorRepository;
    private final UserRepository userRepository;
    private final GeometryFactory geometryFactory = new GeometryFactory();

    @GetMapping("/me")
    public ResponseEntity<DonorDto> getMe(Authentication authentication) {
        User user = userRepository.findByPhone(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Donor donor = donorRepository.findByUserId(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Donor not found"));

        return ResponseEntity.ok(mapToDto(donor, user));
    }

    @PutMapping("/me/location")
    public ResponseEntity<DonorDto> updateLocation(Authentication authentication, @RequestBody LocationDto locationDto) {
        User user = userRepository.findByPhone(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Donor donor = donorRepository.findByUserId(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Donor not found"));

        Point location = geometryFactory.createPoint(new Coordinate(locationDto.getLongitude(), locationDto.getLatitude()));
        location.setSRID(4326);
        donor.setLocation(location);
        donor = donorRepository.save(donor);

        return ResponseEntity.ok(mapToDto(donor, user));
    }

    private DonorDto mapToDto(Donor donor, User user) {
        DonorDto dto = new DonorDto();
        dto.setId(donor.getId().toString());
        dto.setName(user.getName());
        dto.setBloodType(donor.getBloodType());
        if (donor.getLocation() != null) {
            dto.setLatitude(donor.getLocation().getY());
            dto.setLongitude(donor.getLocation().getX());
        }
        dto.setLastDonationDate(donor.getLastDonationDate());
        dto.setIsActive(donor.getIsActive());
        return dto;
    }
}
