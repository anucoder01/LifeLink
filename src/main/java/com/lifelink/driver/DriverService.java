package com.lifelink.driver;

import com.lifelink.driver.dto.DriverDto;
import com.lifelink.driver.dto.DriverRegistrationDto;
import com.lifelink.user.User;
import com.lifelink.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DriverService {

    private final DriverRepository driverRepository;
    private final UserRepository userRepository;
    private final GeometryFactory geometryFactory = new GeometryFactory();

    @Transactional
    public DriverDto registerDriver(String phone, DriverRegistrationDto dto) {
        User user = userRepository.findByPhone(phone)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (driverRepository.findByUserId(user.getId()).isPresent()) {
            throw new IllegalStateException("Driver already registered for this user");
        }

        Driver driver = new Driver();
        driver.setUser(user);
        driver.setVehicleType(dto.getVehicleType());
        
        Point location = geometryFactory.createPoint(new Coordinate(dto.getLongitude(), dto.getLatitude()));
        location.setSRID(4326);
        driver.setLocation(location);
        
        driverRepository.save(driver);

        return convertToDto(driver);
    }

    @Transactional
    public DriverDto updateAvailability(String phone, boolean isAvailable) {
        Driver driver = getDriverByPhone(phone);
        driver.setAvailable(isAvailable);
        driverRepository.save(driver);
        return convertToDto(driver);
    }

    @Transactional
    public DriverDto updateLocation(String phone, double lat, double lng) {
        Driver driver = getDriverByPhone(phone);
        Point location = geometryFactory.createPoint(new Coordinate(lng, lat));
        location.setSRID(4326);
        driver.setLocation(location);
        driverRepository.save(driver);
        return convertToDto(driver);
    }

    @Transactional(readOnly = true)
    public DriverDto getMyProfile(String phone) {
        return convertToDto(getDriverByPhone(phone));
    }

    private Driver getDriverByPhone(String phone) {
        User user = userRepository.findByPhone(phone)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return driverRepository.findByUserId(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Driver profile not found"));
    }

    private DriverDto convertToDto(Driver driver) {
        DriverDto dto = new DriverDto();
        dto.setId(driver.getId());
        dto.setName(driver.getUser().getName());
        dto.setPhone(driver.getUser().getPhone());
        dto.setAvailable(driver.isAvailable());
        dto.setVehicleType(driver.getVehicleType());
        dto.setVerified(driver.isVerified());
        return dto;
    }
}
