package com.lifelink.hospital;

import com.lifelink.user.Role;
import com.lifelink.user.User;
import com.lifelink.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class HospitalAccessService {
    private final UserRepository userRepository;
    @Transactional(readOnly = true)
    public User assertCanAccess(String phone, UUID hospitalId) {
        User user = userRepository.findByPhone(phone).orElseThrow(() -> new AccessDeniedException("User not found"));
        if (user.getRole() == Role.SUPER_ADMIN) return user;
        if (user.getRole() != Role.HOSPITAL_ADMIN || user.getHospital() == null || !user.getHospital().getId().equals(hospitalId)) {
            throw new AccessDeniedException("User is not assigned to this hospital");
        }
        return user;
    }
}
