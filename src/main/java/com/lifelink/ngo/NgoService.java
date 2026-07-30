package com.lifelink.ngo;

import com.lifelink.ngo.dto.CreateNgoDto;
import com.lifelink.ngo.dto.NgoDto;
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
public class NgoService {

    private final NgoRepository ngoRepository;
    private final GeometryFactory geometryFactory = new GeometryFactory();

    @Transactional(readOnly = true)
    public Page<NgoDto> getAll(Pageable pageable) {
        return ngoRepository.findAll(pageable).map(this::mapToDto);
    }

    @Transactional(readOnly = true)
    public NgoDto getById(UUID id) {
        return mapToDto(findOrThrow(id));
    }

    @Transactional
    public NgoDto create(CreateNgoDto dto) {
        Ngo ngo = new Ngo();
        updateEntityFromDto(ngo, dto);
        ngo.setVerified(false);
        ngo = ngoRepository.save(ngo);
        log.info("Registered new NGO '{}' with id {}", ngo.getName(), ngo.getId());
        return mapToDto(ngo);
    }

    @Transactional
    public NgoDto update(UUID id, CreateNgoDto dto) {
        Ngo ngo = findOrThrow(id);
        updateEntityFromDto(ngo, dto);
        ngo = ngoRepository.save(ngo);
        log.info("Updated NGO {}", id);
        return mapToDto(ngo);
    }

    @Transactional
    public NgoDto setVerified(UUID id, boolean verified) {
        Ngo ngo = findOrThrow(id);
        ngo.setVerified(verified);
        ngo = ngoRepository.save(ngo);
        log.info("NGO {} verified={}", id, verified);
        return mapToDto(ngo);
    }

    @Transactional
    public void delete(UUID id) {
        findOrThrow(id);
        ngoRepository.deleteById(id);
        log.info("Deleted NGO {}", id);
    }

    private Ngo findOrThrow(UUID id) {
        return ngoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("NGO not found: " + id));
    }

    private void updateEntityFromDto(Ngo entity, CreateNgoDto dto) {
        entity.setName(dto.getName());
        
        if (dto.getLatitude() != null && dto.getLongitude() != null) {
            Point point = geometryFactory.createPoint(new Coordinate(dto.getLongitude(), dto.getLatitude()));
            point.setSRID(4326);
            entity.setLocation(point);
        } else {
            entity.setLocation(null);
        }
        
        entity.setAddress(dto.getAddress());
        entity.setContactPhone(dto.getContactPhone());
        entity.setRegistrationNumber(dto.getRegistrationNumber());
        entity.setFocusAreas(dto.getFocusAreas());
    }

    private NgoDto mapToDto(Ngo entity) {
        NgoDto dto = new NgoDto();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        if (entity.getLocation() != null) {
            dto.setLatitude(entity.getLocation().getY());
            dto.setLongitude(entity.getLocation().getX());
        }
        dto.setVerified(entity.getVerified());
        dto.setAddress(entity.getAddress());
        dto.setContactPhone(entity.getContactPhone());
        dto.setRegistrationNumber(entity.getRegistrationNumber());
        dto.setFocusAreas(entity.getFocusAreas());
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }
}
