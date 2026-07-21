package com.lifelink;

import com.lifelink.donor.Donor;
import com.lifelink.matching.EligibilityUtil;
import com.lifelink.request.ComponentType;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EligibilityUtilTest {

    @Test
    void testWholeBloodEligibility() {
        Donor donor = new Donor();
        donor.setLastDonationDate(LocalDate.now().minusDays(80)); // not eligible (90 days required)
        assertFalse(EligibilityUtil.isEligible(donor, ComponentType.WHOLE_BLOOD));

        donor.setLastDonationDate(LocalDate.now().minusDays(95)); // eligible
        assertTrue(EligibilityUtil.isEligible(donor, ComponentType.WHOLE_BLOOD));
    }

    @Test
    void testPlateletsEligibility() {
        Donor donor = new Donor();
        donor.setLastDonationDate(LocalDate.now().minusDays(10)); // not eligible (14 days required)
        assertFalse(EligibilityUtil.isEligible(donor, ComponentType.PLATELETS));

        donor.setLastDonationDate(LocalDate.now().minusDays(15)); // eligible
        assertTrue(EligibilityUtil.isEligible(donor, ComponentType.PLATELETS));
    }

    @Test
    void testFirstTimeDonorEligible() {
        Donor donor = new Donor();
        donor.setLastDonationDate(null); // First time
        assertTrue(EligibilityUtil.isEligible(donor, ComponentType.WHOLE_BLOOD));
    }
}
