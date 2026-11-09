package com.lifelink.donor;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Service
@RequiredArgsConstructor
public class DonorRecoveryService {

    private final DonorRecoveryLogRepository recoveryLogRepository;
    private final DonorRepository donorRepository;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LogRecoveryDto {
        private UUID donorId;
        @Min(1) @Max(7) private int dayNumber;
        @PositiveOrZero @Max(10000) private int waterMlLogged;
        @Pattern(regexp = "EXCELLENT|GOOD|TIRED|DIZZY|COMPLICATION") private String feelingStatus;
        @Size(max = 500)
        private String notes;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecoveryDayPlanDto {
        private int dayNumber;
        private String title;
        private String nutritionAdvice;
        private int recommendedWaterMl;
        private int loggedWaterMl;
        private String feelingStatus;
        private boolean completed;
        private String safetyAlert;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DonorRecoveryStatusDto {
        private UUID donorId;
        private String lastDonationDate;
        private int currentDay;
        private int totalHydrationMl;
        private String overallStatus;
        private List<RecoveryDayPlanDto> timeline;
    }

    private static final String[] DAY_TITLES = {
            "Day 1 check-in", "Day 2 check-in", "Day 3 check-in", "Day 4 check-in",
            "Day 5 check-in", "Day 6 check-in", "Day 7 check-in"
    };
    private static final String FOLLOW_DONATION_CENTER_GUIDANCE =
            "Follow the after-donation instructions provided by your donation center. Contact them for medical advice.";

    @Transactional
    public DonorRecoveryStatusDto logProgress(LogRecoveryDto dto) {
        Donor donor = donorRepository.findById(dto.getDonorId())
                .orElseThrow(() -> new IllegalArgumentException("Donor not found"));

        LocalDate donationDate = donor.getLastDonationDate() != null ? donor.getLastDonationDate() : LocalDate.now();
        DonorRecoveryLog log = recoveryLogRepository.findByDonorIdAndDonationDateAndDayNumber(dto.getDonorId(), donationDate, dto.getDayNumber())
                .orElseGet(() -> DonorRecoveryLog.builder()
                        .donor(donor)
                        .donationDate(donationDate)
                        .dayNumber(dto.getDayNumber())
                        .build());

        log.setWaterMlLogged(Math.max(log.getWaterMlLogged(), dto.getWaterMlLogged()));
        if (dto.getFeelingStatus() != null) {
            log.setFeelingStatus(dto.getFeelingStatus());
        }
        if (dto.getNotes() != null) {
            log.setNotes(dto.getNotes());
        }
        log.setLoggedAt(LocalDateTime.now());
        recoveryLogRepository.save(log);

        return getRecoveryStatus(dto.getDonorId());
    }

    @Transactional(readOnly = true)
    public DonorRecoveryStatusDto getRecoveryStatus(UUID donorId) {
        Donor donor = donorRepository.findById(donorId)
                .orElseThrow(() -> new IllegalArgumentException("Donor not found"));

        LocalDate donationDate = donor.getLastDonationDate() != null ? donor.getLastDonationDate() : LocalDate.now().minusDays(1);
        int currentDay = (int) (LocalDate.now().toEpochDay() - donationDate.toEpochDay() + 1);
        currentDay = Math.min(Math.max(currentDay, 1), 7);

        List<DonorRecoveryLog> logs = recoveryLogRepository.findByDonorIdAndDonationDateOrderByDayNumberAsc(donorId, donationDate);
        int totalWater = logs.stream().mapToInt(DonorRecoveryLog::getWaterMlLogged).sum();

        List<RecoveryDayPlanDto> timeline = new ArrayList<>();
        boolean hasComplication = false;

        for (int day = 1; day <= 7; day++) {
            int finalDay = day;
            DonorRecoveryLog log = logs.stream().filter(l -> l.getDayNumber() == finalDay).findFirst().orElse(null);

            String feeling = log != null ? log.getFeelingStatus() : "PENDING";
            int waterLogged = log != null ? log.getWaterMlLogged() : 0;
            boolean completed = log != null && waterLogged >= 1500;

            String alert = null;
            if ("DIZZY".equals(feeling) || "COMPLICATION".equals(feeling)) {
                alert = "You reported a symptom. Contact your donation center or a medical professional for guidance. Seek urgent care for severe symptoms.";
                hasComplication = true;
            }

            timeline.add(RecoveryDayPlanDto.builder()
                    .dayNumber(day)
                    .title(DAY_TITLES[day - 1])
                    .nutritionAdvice(FOLLOW_DONATION_CENTER_GUIDANCE)
                    .recommendedWaterMl(2500)
                    .loggedWaterMl(waterLogged)
                    .feelingStatus(feeling)
                    .completed(completed)
                    .safetyAlert(alert)
                    .build());
        }

        String overall = hasComplication ? "ATTENTION_NEEDED" : "CHECK_IN_ONLY";

        return DonorRecoveryStatusDto.builder()
                .donorId(donorId)
                .lastDonationDate(donationDate.toString())
                .currentDay(currentDay)
                .totalHydrationMl(totalWater)
                .overallStatus(overall)
                .timeline(timeline)
                .build();
    }
}
