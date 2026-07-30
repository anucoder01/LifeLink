package com.lifelink.bloodbank;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface BloodBankRepository extends JpaRepository<BloodBank, UUID> {
}
