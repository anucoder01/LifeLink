package com.lifelink.donor;

import com.lifelink.donor.dto.DonorConsentDto;
import com.lifelink.user.User;
import com.lifelink.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DonorConsentService {

    private final DonorRepository donorRepository;
    private final UserRepository userRepository;
    private final DonorConsentRepository consentRepository;

    @Transactional(readOnly = true)
    public DonorConsentDto getConsentProfile(String phone) {
        User user = findUserByPhone(phone);
        Donor donor = findDonorByUser(user);
        
        DonorConsent consent = consentRepository.findById(donor.getId()).orElse(new DonorConsent());
        return mapToDto(consent);
    }

    @Transactional
    public DonorConsentDto updateConsentProfile(String phone, DonorConsentDto dto) {
        User user = findUserByPhone(phone);
        Donor donor = findDonorByUser(user);
        
        DonorConsent consent = consentRepository.findById(donor.getId()).orElse(new DonorConsent());
        
        if (consent.getDonorId() == null) {
            consent.setDonor(donor);
        }

        consent.setShareLocation(dto.getShareLocation() != null ? dto.getShareLocation() : consent.getShareLocation());
        consent.setAllowEmergencyNotifications(dto.getAllowEmergencyNotifications() != null ? dto.getAllowEmergencyNotifications() : consent.getAllowEmergencyNotifications());
        consent.setAllowCampNotifications(dto.getAllowCampNotifications() != null ? dto.getAllowCampNotifications() : consent.getAllowCampNotifications());
        consent.setShowNameOnPublicImpactBoard(dto.getShowNameOnPublicImpactBoard() != null ? dto.getShowNameOnPublicImpactBoard() : consent.getShowNameOnPublicImpactBoard());
        
        consent = consentRepository.save(consent);
        log.info("Consent profile updated for donor {}", donor.getId());
        
        return mapToDto(consent);
    }

    private User findUserByPhone(String phone) {
        return userRepository.findByPhone(phone)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + phone));
    }

    private Donor findDonorByUser(User user) {
        return donorRepository.findByUserId(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Donor profile not found for user: " + user.getId()));
    }

    private DonorConsentDto mapToDto(DonorConsent consent) {
        if (consent == null) return null;
        DonorConsentDto dto = new DonorConsentDto();
        dto.setShareLocation(consent.getShareLocation());
        dto.setAllowEmergencyNotifications(consent.getAllowEmergencyNotifications());
        dto.setAllowCampNotifications(consent.getAllowCampNotifications());
        dto.setShowNameOnPublicImpactBoard(consent.getShowNameOnPublicImpactBoard());
        dto.setLastUpdated(consent.getLastUpdated());
        return dto;
    }
}
