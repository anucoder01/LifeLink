package com.lifelink.hospital;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.lifelink.config.CacheNames;
import com.lifelink.hospital.dto.CreateHospitalDto;
import com.lifelink.hospital.dto.HospitalDto;
import com.lifelink.hospital.dto.NearbyHospitalDto;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class HospitalService {

    private static final double METERS_PER_KM = 1_000.0;

    private final HospitalRepository hospitalRepository;
    private final GeometryFactory geometryFactory = new GeometryFactory();

    // -------------------------------------------------------------------------
    // CRUD
    // -------------------------------------------------------------------------

    /**
     * Returns all hospitals.
     * Cached for 5 minutes; evicted on any hospital write.
     */
    @Cacheable(value = CacheNames.HOSPITALS_ALL, key = "#pageable.pageNumber + '-' + #pageable.pageSize + '-' + #pageable.sort.toString()")
    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<HospitalDto> getAll(org.springframework.data.domain.Pageable pageable) {
        return hospitalRepository.findAll(pageable)
                .map(this::mapToDto);
    }

    /**
     * Returns a single hospital by ID.
     */
    @Transactional(readOnly = true)
    public HospitalDto getById(UUID id) {
        return mapToDto(findOrThrow(id));
    }

    /**
     * Registers a new hospital. The hospital starts as unverified.
     * Evicts all hospital-related caches so stale lists are not returned.
     */
    @Caching(evict = {
        @CacheEvict(value = CacheNames.HOSPITALS_ALL, allEntries = true),
        @CacheEvict(value = CacheNames.HOSPITALS_NEARBY, allEntries = true),
        @CacheEvict(value = CacheNames.HOSPITALS_NEARBY_BLOOD, allEntries = true)
    })
    @Transactional
    public HospitalDto create(CreateHospitalDto dto) {
        Hospital hospital = new Hospital();
        hospital.setName(dto.getName());
        hospital.setLocation(buildPoint(dto.getLatitude(), dto.getLongitude()));
        hospital.setAddress(dto.getAddress());
        hospital.setContactPhone(dto.getContactPhone());
        hospital.setVerified(false);
        hospital = hospitalRepository.save(hospital);
        log.info("Registered new hospital '{}' with id {}", hospital.getName(), hospital.getId());
        return mapToDto(hospital);
    }

    /**
     * Updates hospital name and/or location.
     */
    @Caching(evict = {
        @CacheEvict(value = CacheNames.HOSPITALS_ALL, allEntries = true),
        @CacheEvict(value = CacheNames.HOSPITALS_NEARBY, allEntries = true),
        @CacheEvict(value = CacheNames.HOSPITALS_NEARBY_BLOOD, allEntries = true)
    })
    @Transactional
    public HospitalDto update(UUID id, CreateHospitalDto dto) {
        Hospital hospital = findOrThrow(id);
        hospital.setName(dto.getName());
        hospital.setLocation(buildPoint(dto.getLatitude(), dto.getLongitude()));
        hospital.setAddress(dto.getAddress());
        hospital.setContactPhone(dto.getContactPhone());
        hospital = hospitalRepository.save(hospital);
        log.info("Updated hospital {}", id);
        return mapToDto(hospital);
    }

    /**
     * Toggles the verified status of a hospital.
     * Only an admin should be able to call this in a production setup.
     */
    @Caching(evict = {
        @CacheEvict(value = CacheNames.HOSPITALS_ALL, allEntries = true),
        @CacheEvict(value = CacheNames.HOSPITALS_NEARBY, allEntries = true),
        @CacheEvict(value = CacheNames.HOSPITALS_NEARBY_BLOOD, allEntries = true)
    })
    @Transactional
    public HospitalDto setVerified(UUID id, boolean verified) {
        Hospital hospital = findOrThrow(id);
        hospital.setVerified(verified);
        hospital = hospitalRepository.save(hospital);
        log.info("Hospital {} verified={}", id, verified);
        return mapToDto(hospital);
    }

    /**
     * Deletes a hospital and all of its inventory records (cascade).
     */
    @Caching(evict = {
        @CacheEvict(value = CacheNames.HOSPITALS_ALL, allEntries = true),
        @CacheEvict(value = CacheNames.HOSPITALS_NEARBY, allEntries = true),
        @CacheEvict(value = CacheNames.HOSPITALS_NEARBY_BLOOD, allEntries = true)
    })
    @Transactional
    public void delete(UUID id) {
        findOrThrow(id);
        hospitalRepository.deleteById(id);
        log.info("Deleted hospital {}", id);
    }

    // -------------------------------------------------------------------------
    // Geo-query
    // -------------------------------------------------------------------------

    /**
     * Finds hospitals within {@code radiusKm} kilometres of the given coords.
     * Key is rounded to 4 decimal places (~11 m precision) to maximise cache hits.
     *
     * @param latitude  origin latitude
     * @param longitude origin longitude
     * @param radiusKm  search radius in kilometres (default 10 if <= 0)
     */
    @Cacheable(
        value = CacheNames.HOSPITALS_NEARBY,
        key = "T(java.lang.Math).round(#latitude*10000)+':'+T(java.lang.Math).round(#longitude*10000)+':'+#radiusKm"
    )
    @Transactional(readOnly = true)
    public List<NearbyHospitalDto> findNearby(double latitude, double longitude, double radiusKm) {
        double radiusMeters = (radiusKm > 0 ? radiusKm : 10.0) * METERS_PER_KM;
        Point origin = buildPoint(latitude, longitude);

        return hospitalRepository.findWithinRadius(origin, radiusMeters)
                .stream()
                .map(h -> mapToNearbyDto(h, origin))
                .toList();
    }

    /**
     * Finds hospitals near the given point that have stock for the requested
     * blood type and component.
     */
    @Cacheable(
        value = CacheNames.HOSPITALS_NEARBY_BLOOD,
        key = "T(java.lang.Math).round(#latitude*10000)+':'+T(java.lang.Math).round(#longitude*10000)+':'+#radiusKm+':'+#bloodType+':'+#componentType"
    )
    @Transactional(readOnly = true)
    public List<NearbyHospitalDto> findNearbyWithBlood(
            double latitude, double longitude, double radiusKm,
            String bloodType, String componentType) {

        double radiusMeters = (radiusKm > 0 ? radiusKm : 10.0) * METERS_PER_KM;
        Point origin = buildPoint(latitude, longitude);

        return hospitalRepository.findNearbyWithAvailableBlood(
                        origin, radiusMeters, bloodType.toUpperCase(), componentType.toUpperCase())
                .stream()
                .map(h -> mapToNearbyDto(h, origin))
                .toList();
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    private Hospital findOrThrow(UUID id) {
        return hospitalRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Hospital not found: " + id));
    }

    private Point buildPoint(double latitude, double longitude) {
        Point point = geometryFactory.createPoint(new Coordinate(longitude, latitude));
        point.setSRID(4326);
        return point;
    }

    /** Euclidean-approximation distance in km between two JTS Points (good enough for UI display). */
    private double distanceKm(Point a, Point b) {
        // We use the PostGIS GEOGRAPHY type which stores in meters; here we do a
        // simple approximation for the DTO field only — the query itself uses the accurate DB function.
        final double EARTH_RADIUS_KM = 6371.0;
        double lat1 = Math.toRadians(a.getY()), lat2 = Math.toRadians(b.getY());
        double dLat = lat2 - lat1;
        double dLon = Math.toRadians(b.getX() - a.getX());
        double sinDLat = Math.sin(dLat / 2);
        double sinDLon = Math.sin(dLon / 2);
        double c = sinDLat * sinDLat + Math.cos(lat1) * Math.cos(lat2) * sinDLon * sinDLon;
        return 2 * EARTH_RADIUS_KM * Math.asin(Math.sqrt(c));
    }

    public HospitalDto mapToDto(Hospital h) {
        HospitalDto dto = new HospitalDto();
        dto.setId(h.getId());
        dto.setName(h.getName());
        if (h.getLocation() != null) {
            dto.setLatitude(h.getLocation().getY());
            dto.setLongitude(h.getLocation().getX());
        }
        dto.setVerified(h.getVerified());
        dto.setAddress(h.getAddress());
        dto.setContactPhone(h.getContactPhone());
        dto.setCreatedAt(h.getCreatedAt());
        return dto;
    }

    private NearbyHospitalDto mapToNearbyDto(Hospital h, Point origin) {
        NearbyHospitalDto dto = new NearbyHospitalDto();
        dto.setId(h.getId());
        dto.setName(h.getName());
        if (h.getLocation() != null) {
            dto.setLatitude(h.getLocation().getY());
            dto.setLongitude(h.getLocation().getX());
            dto.setDistanceKm(Math.round(distanceKm(origin, h.getLocation()) * 100.0) / 100.0);
        }
        dto.setVerified(h.getVerified());
        return dto;
    }
}
