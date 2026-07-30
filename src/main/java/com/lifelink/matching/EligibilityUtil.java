package com.lifelink.matching;

import com.lifelink.donor.Donor;
import com.lifelink.request.ComponentType;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class EligibilityUtil {

    public static boolean isEligible(Donor donor, ComponentType componentType) {
        // Health Questionnaire Checks
        if (donor.getHealthQuestionnaire() != null) {
            com.lifelink.donor.DonorHealthQuestionnaire health = donor.getHealthQuestionnaire();
            // Hard blocks for donation
            if (Boolean.TRUE.equals(health.getIsPregnant()) ||
                Boolean.TRUE.equals(health.getConsumedAlcoholRecently()) ||
                Boolean.TRUE.equals(health.getHasRecentTattoos()) ||
                Boolean.TRUE.equals(health.getHadRecentSurgery())) {
                return false;
            }
        }

        if (donor.getLastDonationDate() == null) {
            return true;
        }

        long daysSinceLastDonation = ChronoUnit.DAYS.between(donor.getLastDonationDate(), LocalDate.now());

        return switch (componentType) {
            case WHOLE_BLOOD -> daysSinceLastDonation >= 90;
            case PLATELETS -> daysSinceLastDonation >= 14;
            case PLASMA -> daysSinceLastDonation >= 28;
        };
    }
}
