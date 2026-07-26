package com.lifelink.request;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface RequestEventRepository extends JpaRepository<RequestEvent, UUID> {
    Page<RequestEvent> findByRequestIdOrderByCreatedAtAsc(UUID requestId, Pageable pageable);
}
