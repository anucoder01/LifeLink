package com.lifelink.ngo;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface NgoRepository extends JpaRepository<Ngo, UUID> {
}
