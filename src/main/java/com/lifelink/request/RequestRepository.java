package com.lifelink.request;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface RequestRepository extends JpaRepository<EmergencyRequest, UUID> {
    List<EmergencyRequest> findByStatus(RequestStatus status);
}
