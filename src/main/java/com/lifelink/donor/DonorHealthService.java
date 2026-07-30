package com.lifelink.donor;

import com.lifelink.donor.dto.DonorHealthQuestionnaireDto;
import com.lifelink.user.User;
import com.lifelink.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DonorHealthService {

    private final DonorRepository donorRepository;
    private final UserRepository userRepository;
    private final DonorHealthQuestionnaireRepository healthRepository;

    @Transactional(readOnly = true)
    public DonorHealthQuestionnaireDto getHealthProfile(String phone) {
        User user = findUserByPhone(phone);
        Donor donor = findDonorByUser(user);
        
        DonorHealthQuestionnaire health = healthRepository.findById(donor.getId()).orElse(null);
        return mapToDto(health);
    }

    @Transactional
    public DonorHealthQuestionnaireDto updateHealthProfile(String phone, DonorHealthQuestionnaireDto dto) {
        User user = findUserByPhone(phone);
        Donor donor = findDonorByUser(user);
        
        DonorHealthQuestionnaire health = healthRepository.findById(donor.getId()).orElse(new DonorHealthQuestionnaire());
        
        if (health.getDonorId() == null) {
            health.setDonor(donor);
        }

        health.setHadRecentIllness(dto.getHadRecentIllness() != null ? dto.getHadRecentIllness() : health.getHadRecentIllness());
        health.setHadRecentSurgery(dto.getHadRecentSurgery() != null ? dto.getHadRecentSurgery() : health.getHadRecentSurgery());
        health.setOnMedication(dto.getOnMedication() != null ? dto.getOnMedication() : health.getOnMedication());
        health.setIsPregnant(dto.getIsPregnant() != null ? dto.getIsPregnant() : health.getIsPregnant());
        health.setHasRecentTattoos(dto.getHasRecentTattoos() != null ? dto.getHasRecentTattoos() : health.getHasRecentTattoos());
        health.setConsumedAlcoholRecently(dto.getConsumedAlcoholRecently() != null ? dto.getConsumedAlcoholRecently() : health.getConsumedAlcoholRecently());
        
        health = healthRepository.save(health);
        log.info("Health profile updated for donor {}", donor.getId());
        
        return mapToDto(health);
    }

    private User findUserByPhone(String phone) {
        return userRepository.findByPhone(phone)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + phone));
    }

    private Donor findDonorByUser(User user) {
        return donorRepository.findByUserId(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Donor profile not found for user: " + user.getId()));
    }

    private DonorHealthQuestionnaireDto mapToDto(DonorHealthQuestionnaire health) {
        if (health == null) return null;
        DonorHealthQuestionnaireDto dto = new DonorHealthQuestionnaireDto();
        dto.setHadRecentIllness(health.getHadRecentIllness());
        dto.setHadRecentSurgery(health.getHadRecentSurgery());
        dto.setOnMedication(health.getOnMedication());
        dto.setIsPregnant(health.getIsPregnant());
        dto.setHasRecentTattoos(health.getHasRecentTattoos());
        dto.setConsumedAlcoholRecently(health.getConsumedAlcoholRecently());
        dto.setLastUpdated(health.getLastUpdated());
        return dto;
    }
}
