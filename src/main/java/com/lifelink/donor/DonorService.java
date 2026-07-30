package com.lifelink.donor;

import com.lifelink.donor.dto.DonorDto;
import com.lifelink.donor.dto.FcmTokenDto;
import com.lifelink.donor.dto.LocationDto;
import com.lifelink.user.User;
import com.lifelink.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DonorService {

    private final DonorRepository donorRepository;
    private final UserRepository userRepository;
    private final GeometryFactory geometryFactory = new GeometryFactory();

    /**
     * Retrieves the donor profile for the currently authenticated user.
     */
    @Transactional(readOnly = true)
    public DonorDto getMyProfile(String phone) {
        User user = findUserByPhone(phone);
        Donor donor = findDonorByUser(user);
        return mapToDto(donor, user);
    }

    /**
     * Updates the donor's GPS location.
     */
    @Transactional
    public DonorDto updateLocation(String phone, LocationDto dto) {
        User user = findUserByPhone(phone);
        Donor donor = findDonorByUser(user);

        Point location = buildPoint(dto.getLatitude(), dto.getLongitude());
        donor.setLocation(location);
        donor = donorRepository.save(donor);
        log.debug("Updated location for donor {} to ({}, {})", donor.getId(), dto.getLatitude(), dto.getLongitude());

        return mapToDto(donor, user);
    }

    /**
     * Registers or updates the FCM push notification token for the donor.
     * Called after login when the client receives a new FCM token.
     */
    @Transactional
    public void updateFcmToken(String phone, FcmTokenDto dto) {
        User user = findUserByPhone(phone);
        Donor donor = findDonorByUser(user);
        donor.setFcmToken(dto.getFcmToken());
        donorRepository.save(donor);
        log.info("FCM token updated for donor {}", donor.getId());
    }

    /**
     * Toggles the donor's active/available status.
     * Inactive donors are excluded from matching and notifications.
     */
    @Transactional
    public DonorDto setActiveStatus(String phone, boolean active) {
        User user = findUserByPhone(phone);
        Donor donor = findDonorByUser(user);
        donor.setIsActive(active);
        donor = donorRepository.save(donor);
        log.info("Donor {} active status set to {}", donor.getId(), active);
        return mapToDto(donor, user);
    }

    @Transactional
    public DonorDto verifyIdentity(String phone, com.lifelink.donor.dto.VerifyIdentityDto dto) {
        User user = findUserByPhone(phone);
        Donor donor = findDonorByUser(user);
        
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = md.digest(dto.getGovernmentId().getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            donor.setGovernmentIdHash(sb.toString());
            donor.setIdentityVerified(true);
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
        
        donor = donorRepository.save(donor);
        log.info("Donor {} identity verified", donor.getId());
        return mapToDto(donor, user);
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    private User findUserByPhone(String phone) {
        return userRepository.findByPhone(phone)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + phone));
    }

    private Donor findDonorByUser(User user) {
        return donorRepository.findByUserId(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Donor profile not found for user: " + user.getId()));
    }

    private Point buildPoint(double latitude, double longitude) {
        // JTS uses (X=longitude, Y=latitude)
        Point point = geometryFactory.createPoint(new Coordinate(longitude, latitude));
        point.setSRID(4326);
        return point;
    }

    public DonorDto mapToDto(Donor donor, User user) {
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
