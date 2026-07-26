package com.lifelink.request;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.UUID;

public interface RequestRepository extends JpaRepository<EmergencyRequest, UUID> {
    List<EmergencyRequest> findByStatus(RequestStatus status);
    Page<EmergencyRequest> findByRequesterId(UUID requesterId, Pageable pageable);
}
