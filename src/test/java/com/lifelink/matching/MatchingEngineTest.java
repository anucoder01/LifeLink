package com.lifelink.matching;

import com.lifelink.donor.Donor;
import com.lifelink.donor.DonorRepository;
import com.lifelink.request.ComponentType;
import com.lifelink.request.EmergencyRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class MatchingEngineTest {

    @Mock
    private DonorRepository donorRepository;

    @Mock
    private CompatibilityMatrix compatibilityMatrix;

    @InjectMocks
    private MatchingEngine matchingEngine;

    private GeometryFactory geometryFactory = new GeometryFactory();
    private EmergencyRequest request;
    private Donor donor1;
    private Donor donor2;

    @BeforeEach
    void setUp() {
        Point loc = geometryFactory.createPoint(new Coordinate(77.5946, 12.9716));
        loc.setSRID(4326);

        request = new EmergencyRequest();
        request.setBloodType("A+");
        request.setComponentType(ComponentType.WHOLE_BLOOD);
        request.setLocation(loc);

        donor1 = new Donor();
        donor1.setBloodType("O+");
        // Ensure donor1 is eligible (no recent donations)
        donor1.setLastDonationDate(java.time.LocalDate.now().minusDays(100));
        
        donor2 = new Donor();
        donor2.setBloodType("A+");
        // Ensure donor2 is NOT eligible (donated whole blood yesterday)
        donor2.setLastDonationDate(java.time.LocalDate.now().minusDays(1));
    }

    @Test
    void testFindEligibleDonors_FiltersByEligibility() {
        when(compatibilityMatrix.getCompatibleDonors("A+")).thenReturn(List.of("A+", "A-", "O+", "O-"));
        when(donorRepository.findEligibleDonorsWithinRadius(any(Point.class), eq(5000.0), any(List.class)))
                .thenReturn(List.of(donor1, donor2));

        List<Donor> result = matchingEngine.findEligibleDonors(request, 5);

        // Only donor1 is eligible
        assertEquals(1, result.size());
        assertEquals("O+", result.get(0).getBloodType());
    }

    @Test
    void testFindEligibleDonors_NoCompatibleTypes() {
        when(compatibilityMatrix.getCompatibleDonors("A+")).thenReturn(List.of());

        List<Donor> result = matchingEngine.findEligibleDonors(request, 5);

        assertEquals(0, result.size());
    }
}
