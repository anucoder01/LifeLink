package com.lifelink.matching;

import com.lifelink.config.CacheNames;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Blood type compatibility matrix as a Spring-managed component.
 * The {@code getCompatibleDonors} result is cached in Redis because it is
 * called on every emergency request broadcast and the data is static.
 *
 * Lookup key = upper-cased blood type string (e.g. "A+").
 */
@Component
public class CompatibilityMatrix {

    private static final Map<String, List<String>> CAN_RECEIVE_FROM = new HashMap<>();

    static {
        CAN_RECEIVE_FROM.put("O-",  Collections.singletonList("O-"));
        CAN_RECEIVE_FROM.put("O+",  Arrays.asList("O+", "O-"));
        CAN_RECEIVE_FROM.put("A-",  Arrays.asList("A-", "O-"));
        CAN_RECEIVE_FROM.put("A+",  Arrays.asList("A+", "A-", "O+", "O-"));
        CAN_RECEIVE_FROM.put("B-",  Arrays.asList("B-", "O-"));
        CAN_RECEIVE_FROM.put("B+",  Arrays.asList("B+", "B-", "O+", "O-"));
        CAN_RECEIVE_FROM.put("AB-", Arrays.asList("AB-", "A-", "B-", "O-"));
        CAN_RECEIVE_FROM.put("AB+", Arrays.asList("AB+", "AB-", "A+", "A-", "B+", "B-", "O+", "O-"));
    }

    /**
     * Returns the list of donor blood types compatible with {@code patientBloodType}.
     * Result is cached in Redis with a 24-hour TTL (see RedisConfig).
     */
    @Cacheable(value = CacheNames.BLOOD_COMPATIBILITY, key = "#bloodType.toUpperCase()")
    public List<String> getCompatibleDonors(String bloodType) {
        return CAN_RECEIVE_FROM.getOrDefault(bloodType.toUpperCase(), Collections.emptyList());
    }
}
