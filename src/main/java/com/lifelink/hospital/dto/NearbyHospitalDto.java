package com.lifelink.hospital.dto;

import lombok.Data;

import java.util.UUID;

/** Response DTO for a nearby hospital search result, including distance. */
@Data
public class NearbyHospitalDto {
    private UUID id;
    private String name;
    private double latitude;
    private double longitude;
    private Boolean verified;
    /** Straight-line distance from the query origin, in kilometres. */
    private double distanceKm;
}
