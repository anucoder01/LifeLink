package com.lifelink.request;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RequestResponseRepository extends JpaRepository<RequestResponse, UUID> {
    List<RequestResponse> findByRequestId(UUID requestId);
    Optional<RequestResponse> findByRequestIdAndDonorId(UUID requestId, UUID donorId);
}
