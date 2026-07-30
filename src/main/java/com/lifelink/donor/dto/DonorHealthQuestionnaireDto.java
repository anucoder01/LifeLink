package com.lifelink.donor.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DonorHealthQuestionnaireDto {
    private Boolean hadRecentIllness;
    private Boolean hadRecentSurgery;
    private Boolean onMedication;
    private Boolean isPregnant;
    private Boolean hasRecentTattoos;
    private Boolean consumedAlcoholRecently;
    private LocalDateTime lastUpdated;
}
