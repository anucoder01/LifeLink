package com.lifelink.bloodchain;

import com.lifelink.bloodchain.dto.AddVouchDto;
import com.lifelink.bloodchain.dto.InviteDetailsDto;
import com.lifelink.bloodchain.dto.VouchDto;
import com.lifelink.donor.Donor;
import com.lifelink.donor.DonorRepository;
import com.lifelink.notification.SmsService;
import com.lifelink.request.EmergencyRequest;
import com.lifelink.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BloodChainService {

    /** Maximum number of contacts a donor can vouch for. */
    private static final int MAX_VOUCHES = 3;

    /** Token expiry window in hours. */
    private static final int TOKEN_EXPIRY_HOURS = 72;

    private final DonorVouchRepository vouchRepository;
    private final BloodChainInviteTokenRepository tokenRepository;
    private final DonorRepository donorRepository;
    private final UserRepository userRepository;
    private final SmsService smsService;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    // =========================================================================
    // Vouch Management
    // =========================================================================

    /**
     * Returns all vouched contacts for the authenticated donor (phone numbers masked).
     */
    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<VouchDto> getMyVouches(String phone, org.springframework.data.domain.Pageable pageable) {
        Donor donor = findDonorByPhone(phone);
        return vouchRepository.findAllByDonorId(donor.getId(), pageable)
                .map(this::mapToVouchDto);
    }

    /**
     * Adds a new vouched contact for the authenticated donor.
     * Enforces the max-3 limit and prevents duplicate phone numbers.
     */
    @Transactional
    public VouchDto addVouch(String phone, AddVouchDto dto) {
        Donor donor = findDonorByPhone(phone);

        if (vouchRepository.countByDonorId(donor.getId()) >= MAX_VOUCHES) {
            throw new IllegalStateException(
                    "You can nominate at most " + MAX_VOUCHES + " trusted contacts.");
        }

        if (vouchRepository.existsByDonorIdAndContactPhone(donor.getId(), dto.getContactPhone())) {
            throw new IllegalArgumentException(
                    "This phone number is already in your trusted contacts list.");
        }

        DonorVouch vouch = new DonorVouch();
        vouch.setDonor(donor);
        vouch.setContactName(dto.getContactName());
        vouch.setContactPhone(dto.getContactPhone());
        vouch = vouchRepository.save(vouch);

        log.info("Donor {} added vouch for contact phone ending in ...{}",
                donor.getId(), dto.getContactPhone().substring(Math.max(0, dto.getContactPhone().length() - 4)));

        return mapToVouchDto(vouch);
    }

    /**
     * Removes a vouched contact. Only the owning donor can delete their own vouches.
     */
    @Transactional
    public void removeVouch(String phone, UUID vouchId) {
        Donor donor = findDonorByPhone(phone);
        DonorVouch vouch = vouchRepository.findById(vouchId)
                .orElseThrow(() -> new IllegalArgumentException("Vouch not found: " + vouchId));

        if (!vouch.getDonor().getId().equals(donor.getId())) {
            throw new IllegalStateException("You can only remove your own trusted contacts.");
        }

        vouchRepository.delete(vouch);
        log.info("Donor {} removed vouch {}", donor.getId(), vouchId);
    }

    // =========================================================================
    // Blood Chain Trigger — called by RequestService when max radius yields 0 donors
    // =========================================================================

    /**
     * Triggered when the matching engine exhausts the 30 km radius with zero donors found.
     * Sends an SMS invite to all vouched contacts of every eligible donor in the system
     * who has not yet been reached. This is the "Blood Chain" activation.
     *
     * @param request the unsatisfied emergency request
     */
    @Transactional
    public void activateBloodChain(EmergencyRequest request) {
        log.warn("BLOOD CHAIN ACTIVATED for request {} — no donors found within 30km radius.",
                request.getId());

        // Collect ALL active donors (regardless of radius) who haven't already been notified
        List<Donor> allActiveDonors = donorRepository.findAll()
                .stream()
                .filter(Donor::getIsActive)
                .toList();

        int totalSmsSent = 0;

        for (Donor donor : allActiveDonors) {
            List<DonorVouch> vouches = vouchRepository.findAllByDonorId(donor.getId());
            for (DonorVouch vouch : vouches) {
                // Don't send duplicate SMS for the same (request, phone) pair
                if (tokenRepository.existsByRequestIdAndContactPhone(
                        request.getId(), vouch.getContactPhone())) {
                    continue;
                }

                String token = generateToken();
                BloodChainInviteToken inviteToken = new BloodChainInviteToken();
                inviteToken.setToken(token);
                inviteToken.setRequest(request);
                inviteToken.setContactPhone(vouch.getContactPhone());
                inviteToken.setContactName(vouch.getContactName());
                inviteToken.setExpiresAt(LocalDateTime.now().plusHours(TOKEN_EXPIRY_HOURS));
                tokenRepository.save(inviteToken);

                String inviteLink = baseUrl + "/api/v1/blood-chain/invite/" + token;
                String smsBody = buildSmsMessage(vouch, request, inviteLink);
                smsService.send(vouch.getContactPhone(), smsBody);
                totalSmsSent++;
            }
        }

        log.info("Blood Chain: sent {} SMS invites for request {}", totalSmsSent, request.getId());
    }

    // =========================================================================
    // Invite Token — used by the public registration flow
    // =========================================================================

    /**
     * Validates an invite token and returns pre-fill data for the registration form.
     * Does NOT consume the token — that happens at registerViaInvite().
     */
    @Transactional(readOnly = true)
    public InviteDetailsDto validateInviteToken(String token) {
        InviteDetailsDto dto = new InviteDetailsDto();
        return tokenRepository.findByToken(token)
                .map(t -> {
                    if (t.getUsed()) {
                        dto.setValid(false);
                        dto.setInvalidReason("This invite link has already been used.");
                    } else if (t.getExpiresAt().isBefore(LocalDateTime.now())) {
                        dto.setValid(false);
                        dto.setInvalidReason("This invite link has expired.");
                    } else {
                        dto.setValid(true);
                        dto.setContactName(t.getContactName());
                        dto.setContactPhone(t.getContactPhone());
                        dto.setEmergencySummary(buildEmergencySummary(t.getRequest()));
                    }
                    return dto;
                })
                .orElseGet(() -> {
                    dto.setValid(false);
                    dto.setInvalidReason("Invalid invite link.");
                    return dto;
                });
    }

    /**
     * Marks an invite token as used after the contact registers.
     * Called by AuthController after successful registration via invite.
     */
    @Transactional
    public void consumeInviteToken(String token) {
        tokenRepository.findByToken(token).ifPresent(t -> {
            t.setUsed(true);
            tokenRepository.save(t);
            log.info("Invite token consumed: ...{}", token.substring(token.length() - 8));
        });
    }

    // =========================================================================
    // Scheduled Cleanup
    // =========================================================================

    /**
     * Runs daily at 3 AM. Deletes expired + unused tokens older than 7 days to keep the table clean.
     */
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void cleanupExpiredTokens() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(7);
        tokenRepository.deleteExpiredUnusedTokens(cutoff);
        log.info("Blood Chain: expired invite token cleanup complete.");
    }

    // =========================================================================
    // Private helpers
    // =========================================================================

    private Donor findDonorByPhone(String phone) {
        return userRepository.findByPhone(phone)
                .flatMap(user -> donorRepository.findByUserId(user.getId()))
                .orElseThrow(() -> new IllegalArgumentException("Donor not found for phone: " + phone));
    }

    private String generateToken() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return HexFormat.of().formatHex(bytes); // 64-char hex string
    }

    private String buildSmsMessage(DonorVouch vouch, EmergencyRequest request, String inviteLink) {
        return String.format(
                "Hi %s, a trusted friend said you might help in an emergency. " +
                "Blood type %s is urgently needed nearby. " +
                "Register as a donor (takes 2 mins): %s " +
                "Reply STOP to never receive these messages.",
                vouch.getContactName(),
                request.getBloodType(),
                inviteLink
        );
    }

    private String buildEmergencySummary(EmergencyRequest request) {
        return String.format("%s blood needed urgently (%s)",
                request.getBloodType(),
                request.getUrgency().name().toLowerCase());
    }

    /**
     * Masks a phone number for display: first 4 chars + **** + last 2 chars.
     * e.g. "9876543210" → "9876****10"
     */
    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) return "***";
        int len = phone.length();
        return phone.substring(0, 4) + "****" + phone.substring(len - 2);
    }

    private VouchDto mapToVouchDto(DonorVouch vouch) {
        VouchDto dto = new VouchDto();
        dto.setId(vouch.getId());
        dto.setContactName(vouch.getContactName());
        dto.setContactPhoneMasked(maskPhone(vouch.getContactPhone()));
        dto.setCreatedAt(vouch.getCreatedAt());
        return dto;
    }
}
