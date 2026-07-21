package com.lifelink.matching;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CompatibilityMatrix {
    private static final Map<String, List<String>> CAN_RECEIVE_FROM = new HashMap<>();

    static {
        CAN_RECEIVE_FROM.put("O-", Collections.singletonList("O-"));
        CAN_RECEIVE_FROM.put("O+", Arrays.asList("O+", "O-"));
        CAN_RECEIVE_FROM.put("A-", Arrays.asList("A-", "O-"));
        CAN_RECEIVE_FROM.put("A+", Arrays.asList("A+", "A-", "O+", "O-"));
        CAN_RECEIVE_FROM.put("B-", Arrays.asList("B-", "O-"));
        CAN_RECEIVE_FROM.put("B+", Arrays.asList("B+", "B-", "O+", "O-"));
        CAN_RECEIVE_FROM.put("AB-", Arrays.asList("AB-", "A-", "B-", "O-"));
        CAN_RECEIVE_FROM.put("AB+", Arrays.asList("AB+", "AB-", "A+", "A-", "B+", "B-", "O+", "O-"));
    }

    public static List<String> getCompatibleDonors(String patientBloodType) {
        return CAN_RECEIVE_FROM.getOrDefault(patientBloodType.toUpperCase(), Collections.emptyList());
    }
}
