package com.lifelink.donor;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GamificationService {

    private final DonorGamificationRepository gamificationRepository;
    private final DonorBadgeRepository badgeRepository;
    private final DonorRepository donorRepository;
    private final DonorConsentRepository consentRepository;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BadgeDto {
        private String key;
        private String name;
        private String icon;
        private LocalDateTime earnedAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GamificationProfileDto {
        private UUID donorId;
        private String donorName;
        private String bloodType;
        private int xp;
        private int level;
        private int currentStreak;
        private int totalDonations;
        private int xpToNextLevel;
        private List<BadgeDto> badges;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LeaderboardEntryDto {
        private int rank;
        private String donorName;
        private String bloodType;
        private int level;
        private int xp;
        private int totalDonations;
        private int badgeCount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CertificateDto {
        private String certificateId;
        private String donorName;
        private String bloodType;
        private int totalDonations;
        private String issuedDate;
        private String verificationHash;
        private String signatureTitle;
    }

    @Transactional
    public GamificationProfileDto getGamificationProfile(UUID donorId) {
        Donor donor = donorRepository.findById(donorId)
                .orElseThrow(() -> new IllegalArgumentException("Donor not found with ID: " + donorId));

        DonorGamification stats = gamificationRepository.findByDonorId(donorId)
                .orElseGet(() -> gamificationRepository.save(DonorGamification.builder()
                        .donor(donor)
                        .xp(100) // 100 bonus signup XP
                        .level(1)
                        .currentStreak(1)
                        .totalDonations(0)
                        .build()));

        List<DonorBadge> badges = badgeRepository.findByDonorId(donorId);
        int nextLevelXp = stats.getLevel() * 250;
        int xpToNext = Math.max(0, nextLevelXp - stats.getXp());

        return GamificationProfileDto.builder()
                .donorId(donor.getId())
                .donorName(donor.getUser() != null ? donor.getUser().getName() : "Anonymous Lifesaver")
                .bloodType(donor.getBloodType())
                .xp(stats.getXp())
                .level(stats.getLevel())
                .currentStreak(stats.getCurrentStreak())
                .totalDonations(stats.getTotalDonations())
                .xpToNextLevel(xpToNext)
                .badges(badges.stream().map(b -> BadgeDto.builder()
                        .key(b.getBadgeKey())
                        .name(b.getBadgeName())
                        .icon(b.getBadgeIcon())
                        .earnedAt(b.getEarnedAt())
                        .build()).collect(Collectors.toList()))
                .build();
    }

    @Transactional
    public GamificationProfileDto recordDonation(UUID donorId, boolean isCritical) {
        Donor donor = donorRepository.findById(donorId)
                .orElseThrow(() -> new IllegalArgumentException("Donor not found"));

        DonorGamification stats = gamificationRepository.findByDonorId(donorId)
                .orElseGet(() -> DonorGamification.builder().donor(donor).build());

        int earnedXp = isCritical ? 150 : 100;
        stats.setXp(stats.getXp() + earnedXp);
        stats.setTotalDonations(stats.getTotalDonations() + 1);
        stats.setCurrentStreak(stats.getCurrentStreak() + 1);
        stats.setLastDonationAt(LocalDateTime.now());

        // Calculate Level (1 Level per 250 XP)
        int newLevel = (stats.getXp() / 250) + 1;
        stats.setLevel(newLevel);

        gamificationRepository.save(stats);

        // Award Badges
        checkAndAwardBadge(donor, "FIRST_DONATION", "First Lifesaver", "❤️", stats.getTotalDonations() >= 1);
        checkAndAwardBadge(donor, "SILVER_HERO", "Silver Hero", "🥈", stats.getTotalDonations() >= 3);
        checkAndAwardBadge(donor, "GOLDEN_DONOR", "Golden Donor", "👑", stats.getTotalDonations() >= 5);
        checkAndAwardBadge(donor, "STREAK_MASTER", "Streak Champion", "🔥", stats.getCurrentStreak() >= 3);
        if (isCritical) {
            checkAndAwardBadge(donor, "CRITICAL_RESPONDER", "Emergency Hero", "🚨", true);
        }

        return getGamificationProfile(donorId);
    }

    private void checkAndAwardBadge(Donor donor, String key, String name, String icon, boolean condition) {
        if (condition && !badgeRepository.existsByDonorIdAndBadgeKey(donor.getId(), key)) {
            badgeRepository.save(DonorBadge.builder()
                    .donor(donor)
                    .badgeKey(key)
                    .badgeName(name)
                    .badgeIcon(icon)
                    .earnedAt(LocalDateTime.now())
                    .build());
        }
    }

    @Transactional(readOnly = true)
    public List<LeaderboardEntryDto> getLeaderboard() {
        List<DonorGamification> list = gamificationRepository.findTopDonorsByXp();
        List<LeaderboardEntryDto> leaderboard = new ArrayList<>();
        int rank = 1;

        for (DonorGamification g : list) {
            boolean nameVisible = consentRepository.findById(g.getDonor().getId())
                    .map(c -> Boolean.TRUE.equals(c.getShowNameOnPublicImpactBoard())).orElse(false);
            String name = nameVisible && g.getDonor().getUser() != null ? g.getDonor().getUser().getName() : "Anonymous Donor";
            int badgeCount = badgeRepository.findByDonorId(g.getDonor().getId()).size();

            leaderboard.add(LeaderboardEntryDto.builder()
                    .rank(rank++)
                    .donorName(name)
                    .level(g.getLevel())
                    .xp(g.getXp())
                    .totalDonations(g.getTotalDonations())
                    .badgeCount(badgeCount)
                    .build());
            if (rank > 20) break; // Top 20
        }
        return leaderboard;
    }

    @Transactional(readOnly = true)
    public CertificateDto generateCertificate(UUID donorId) {
        Donor donor = donorRepository.findById(donorId)
                .orElseThrow(() -> new IllegalArgumentException("Donor not found"));

        DonorGamification stats = gamificationRepository.findByDonorId(donorId).orElse(null);
        int total = stats != null ? stats.getTotalDonations() : 1;

        String name = donor.getUser() != null ? donor.getUser().getName() : "Valued Donor";
        String certId = "LFL-CERT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String hash;
        try {
            byte[] digest = java.security.MessageDigest.getInstance("SHA-256")
                    .digest((name + certId + donor.getBloodType()).getBytes(java.nio.charset.StandardCharsets.UTF_8));
            hash = java.util.HexFormat.of().withUpperCase().formatHex(digest);
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is unavailable", e);
        }

        return CertificateDto.builder()
                .certificateId(certId)
                .donorName(name)
                .bloodType(donor.getBloodType())
                .totalDonations(total)
                .issuedDate(LocalDateTime.now().toLocalDate().toString())
                .verificationHash("SHA-256-" + hash)
                .signatureTitle("National Emergency Blood Alliance & LifeLink Network")
                .build();
    }
}
