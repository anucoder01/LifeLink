package com.lifelink.user;

import com.lifelink.user.dto.ChangePasswordDto;
import com.lifelink.user.dto.UpdateProfileDto;
import com.lifelink.user.dto.UserProfileDto;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // -------------------------------------------------------------------------
    // Read
    // -------------------------------------------------------------------------

    /**
     * Returns the profile of the currently authenticated user.
     *
     * @param phone the phone number used as the JWT subject / Spring Security principal name
     */
    @Transactional(readOnly = true)
    public UserProfileDto getMe(String phone) {
        return mapToDto(findByPhoneOrThrow(phone));
    }

    // -------------------------------------------------------------------------
    // Write
    // -------------------------------------------------------------------------

    /**
     * Partially updates the authenticated user's profile.
     * Only {@code name} and {@code email} can be changed; phone and role are immutable.
     * Null/blank fields in the DTO are ignored (patch semantics).
     */
    @Transactional
    public UserProfileDto updateMe(String phone, UpdateProfileDto dto) {
        User user = findByPhoneOrThrow(phone);

        if (StringUtils.hasText(dto.getName())) {
            user.setName(dto.getName().strip());
        }

        // Email: allow explicit null to clear, but validate uniqueness if setting a new one
        if (dto.getEmail() != null) {
            String newEmail = dto.getEmail().isBlank() ? null : dto.getEmail().strip().toLowerCase();
            final var userId = user.getId(); // captured before user is reassigned below
            if (newEmail != null && !newEmail.equals(user.getEmail())) {
                userRepository.findByEmail(newEmail).ifPresent(existing -> {
                    if (!existing.getId().equals(userId)) {
                        throw new IllegalStateException("Email address is already in use: " + newEmail);
                    }
                });
            }
            user.setEmail(newEmail);
        }

        user = userRepository.save(user);
        log.info("Profile updated for user {}", user.getId());
        return mapToDto(user);
    }

    /**
     * Changes the authenticated user's password after verifying their current one.
     *
     * @throws IllegalArgumentException if the current password is wrong
     */
    @Transactional
    public void changePassword(String phone, ChangePasswordDto dto) {
        User user = findByPhoneOrThrow(phone);

        if (!passwordEncoder.matches(dto.getCurrentPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }

        user.setPasswordHash(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(user);
        log.info("Password changed for user {}", user.getId());
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    private User findByPhoneOrThrow(String phone) {
        return userRepository.findByPhone(phone)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + phone));
    }

    public UserProfileDto mapToDto(User user) {
        UserProfileDto dto = new UserProfileDto();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setPhone(user.getPhone());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());
        dto.setCreatedAt(user.getCreatedAt());
        return dto;
    }
}
