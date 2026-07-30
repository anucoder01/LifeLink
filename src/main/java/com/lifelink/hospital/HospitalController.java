package com.lifelink.hospital;

import com.lifelink.hospital.dto.CreateHospitalDto;
import com.lifelink.hospital.dto.HospitalDto;
import com.lifelink.hospital.dto.NearbyHospitalDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;
import com.lifelink.common.dto.PaginatedResponse;
import com.lifelink.common.util.PaginationUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Tag(name = "Hospital", description = "Hospital registration and geo-search")
@RestController
@RequestMapping("/api/v1/hospitals")
@RequiredArgsConstructor
public class HospitalController {

    private final HospitalService hospitalService;

    // -------------------------------------------------------------------------
    // CRUD
    // -------------------------------------------------------------------------

    /**
     * GET /api/v1/hospitals
     * Returns all hospitals with pagination.
     */
    @Operation(summary = "List all hospitals with pagination")
    @GetMapping
    public ResponseEntity<PaginatedResponse<HospitalDto>> getAll(
            @org.springdoc.core.annotations.ParameterObject Pageable pageable) {
        Page<HospitalDto> page = hospitalService.getAll(pageable);
        return ResponseEntity.ok(PaginationUtil.fromPage(page));
    }

    /**
     * GET /api/v1/hospitals/{id}
     * Returns a specific hospital.
     */
    @Operation(summary = "Get hospital by ID")
    @GetMapping("/{id}")
    public ResponseEntity<HospitalDto> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(hospitalService.getById(id));
    }

    /**
     * POST /api/v1/hospitals
     * Registers a new hospital (initially unverified).
     */
    @Operation(summary = "Register a new hospital")
    @PostMapping
    public ResponseEntity<HospitalDto> create(@Valid @RequestBody CreateHospitalDto dto) {
        HospitalDto created = hospitalService.create(dto);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    /**
     * PUT /api/v1/hospitals/{id}
     * Updates hospital name and location.
     */
    @Operation(summary = "Update hospital details")
    @PutMapping("/{id}")
    public ResponseEntity<HospitalDto> update(
            @PathVariable UUID id,
            @Valid @RequestBody CreateHospitalDto dto) {
        return ResponseEntity.ok(hospitalService.update(id, dto));
    }

    /**
     * PUT /api/v1/hospitals/{id}/verify?verified=true|false
     * Marks a hospital as verified or unverified (admin action).
     */
    @Operation(summary = "Set hospital verification status")
    @PutMapping("/{id}/verify")
    public ResponseEntity<HospitalDto> setVerified(
            @PathVariable UUID id,
            @RequestParam boolean verified) {
        return ResponseEntity.ok(hospitalService.setVerified(id, verified));
    }

    /**
     * DELETE /api/v1/hospitals/{id}
     */
    @Operation(summary = "Delete a hospital")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        hospitalService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // -------------------------------------------------------------------------
    // Geo-search
    // -------------------------------------------------------------------------

    /**
     * GET /api/v1/hospitals/nearby?lat=&lng=&radiusKm=
     * Finds hospitals within a given radius of the provided coordinates.
     */
    @Operation(summary = "Find hospitals within a radius")
    @GetMapping("/nearby")
    public ResponseEntity<PaginatedResponse<NearbyHospitalDto>> findNearby(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam(defaultValue = "10") double radiusKm,
            @org.springdoc.core.annotations.ParameterObject Pageable pageable) {
        Page<NearbyHospitalDto> page = hospitalService.findNearby(lat, lng, radiusKm, pageable);
        return ResponseEntity.ok(PaginationUtil.fromPage(page));
    }

    /**
     * GET /api/v1/hospitals/nearby/blood?lat=&lng=&radiusKm=&bloodType=&componentType=
     * Finds hospitals near the coordinates that have stock for the requested blood type + component.
     */
    @Operation(summary = "Find nearby hospitals with a specific blood type in stock")
    @GetMapping("/nearby/blood")
    public ResponseEntity<PaginatedResponse<NearbyHospitalDto>> findNearbyWithBlood(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam(defaultValue = "10") double radiusKm,
            @RequestParam String bloodType,
            @RequestParam String componentType,
            @org.springdoc.core.annotations.ParameterObject Pageable pageable) {
        Page<NearbyHospitalDto> page = hospitalService.findNearbyWithBlood(lat, lng, radiusKm, bloodType, componentType, pageable);
        return ResponseEntity.ok(PaginationUtil.fromPage(page));
    }
}
