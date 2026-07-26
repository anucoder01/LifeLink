package com.lifelink;

import com.lifelink.matching.CompatibilityMatrix;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CompatibilityMatrixTest {

    private final CompatibilityMatrix matrix = new CompatibilityMatrix();

    @Test
    void testONegativeCanOnlyReceiveFromONegative() {
        List<String> compatible = matrix.getCompatibleDonors("O-");
        assertEquals(1, compatible.size());
        assertTrue(compatible.contains("O-"));
    }

    @Test
    void testABPositiveCanReceiveFromAll() {
        List<String> compatible = matrix.getCompatibleDonors("AB+");
        assertEquals(8, compatible.size());
        assertTrue(compatible.contains("O-"));
        assertTrue(compatible.contains("A+"));
        assertTrue(compatible.contains("B-"));
        assertTrue(compatible.contains("AB+"));
    }

    @Test
    void testAPositive() {
        List<String> compatible = matrix.getCompatibleDonors("A+");
        assertEquals(4, compatible.size());
        assertTrue(compatible.contains("A+"));
        assertTrue(compatible.contains("A-"));
        assertTrue(compatible.contains("O+"));
        assertTrue(compatible.contains("O-"));
    }
}
