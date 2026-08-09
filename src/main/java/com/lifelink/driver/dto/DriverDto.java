package com.lifelink.driver.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class DriverDto {
    private UUID id;
    private String name;
    private String phone;
    private boolean isAvailable;
    private String vehicleType;
    private boolean verified;
}
