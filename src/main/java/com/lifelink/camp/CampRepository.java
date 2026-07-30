package com.lifelink.camp;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface CampRepository extends JpaRepository<Camp, UUID> {
    
    @Query("SELECT c FROM Camp c WHERE c.isActive = true AND c.startTime <= :now AND c.endTime >= :now")
    List<Camp> findActiveCamps(LocalDateTime now);
}
