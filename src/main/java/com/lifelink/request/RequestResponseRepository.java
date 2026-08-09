package com.lifelink.request;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RequestResponseRepository extends JpaRepository<RequestResponse, UUID> {
    List<RequestResponse> findByRequestId(UUID requestId);
    Page<RequestResponse> findByRequestId(UUID requestId, Pageable pageable);
    Optional<RequestResponse> findByRequestIdAndDonorId(UUID requestId, UUID donorId);
    List<RequestResponse> findByDonorIdAndStatus(UUID donorId, RequestResponseStatus status);
    Page<RequestResponse> findByDonorIdAndStatus(UUID donorId, RequestResponseStatus status, Pageable pageable);
}
