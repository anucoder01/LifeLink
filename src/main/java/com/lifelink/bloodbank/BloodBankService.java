package com.lifelink.bloodbank;

import com.lifelink.bloodbank.dto.BloodBankDto;
import com.lifelink.bloodbank.dto.CreateBloodBankDto;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BloodBankService {

    private final BloodBankRepository bloodBankRepository;
    private final GeometryFactory geometryFactory = new GeometryFactory();

    @Transactional(readOnly = true)
    public Page<BloodBankDto> getAll(Pageable pageable) {
        return bloodBankRepository.findAll(pageable).map(this::mapToDto);
    }

    @Transactional(readOnly = true)
    public BloodBankDto getById(UUID id) {
        return mapToDto(findOrThrow(id));
    }

    @Transactional
    public BloodBankDto create(CreateBloodBankDto dto) {
        BloodBank bloodBank = new BloodBank();
        updateEntityFromDto(bloodBank, dto);
        bloodBank.setVerified(false);
        bloodBank = bloodBankRepository.save(bloodBank);
        log.info("Registered new blood bank '{}' with id {}", bloodBank.getName(), bloodBank.getId());
        return mapToDto(bloodBank);
    }

    @Transactional
    public BloodBankDto update(UUID id, CreateBloodBankDto dto) {
        BloodBank bloodBank = findOrThrow(id);
        updateEntityFromDto(bloodBank, dto);
        bloodBank = bloodBankRepository.save(bloodBank);
        log.info("Updated blood bank {}", id);
        return mapToDto(bloodBank);
    }

    @Transactional
    public BloodBankDto setVerified(UUID id, boolean verified) {
        BloodBank bloodBank = findOrThrow(id);
        bloodBank.setVerified(verified);
        bloodBank = bloodBankRepository.save(bloodBank);
        log.info("Blood bank {} verified={}", id, verified);
        return mapToDto(bloodBank);
    }

    @Transactional
    public void delete(UUID id) {
        findOrThrow(id);
        bloodBankRepository.deleteById(id);
        log.info("Deleted blood bank {}", id);
    }

    private BloodBank findOrThrow(UUID id) {
        return bloodBankRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Blood Bank not found: " + id));
    }

    private void updateEntityFromDto(BloodBank entity, CreateBloodBankDto dto) {
        entity.setName(dto.getName());
        Point point = geometryFactory.createPoint(new Coordinate(dto.getLongitude(), dto.getLatitude()));
        point.setSRID(4326);
        entity.setLocation(point);
        entity.setAddress(dto.getAddress());
        entity.setContactPhone(dto.getContactPhone());
        entity.setLicenseNumber(dto.getLicenseNumber());
        entity.setOperatingHours(dto.getOperatingHours());
    }

    private BloodBankDto mapToDto(BloodBank entity) {
        BloodBankDto dto = new BloodBankDto();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        if (entity.getLocation() != null) {
            dto.setLatitude(entity.getLocation().getY());
            dto.setLongitude(entity.getLocation().getX());
        }
        dto.setVerified(entity.getVerified());
        dto.setAddress(entity.getAddress());
        dto.setContactPhone(entity.getContactPhone());
        dto.setLicenseNumber(entity.getLicenseNumber());
        dto.setOperatingHours(entity.getOperatingHours());
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }
}
