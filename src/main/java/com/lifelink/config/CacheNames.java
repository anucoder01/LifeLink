package com.lifelink.config;

/**
 * Central registry of all cache name constants.
 * Use these in @Cacheable / @CacheEvict / @CachePut annotations
 * instead of raw string literals to prevent typos and make
 * refactoring safe.
 */
public final class CacheNames {

    private CacheNames() {}

    /** Full list of all hospitals. Evicted on any hospital write. */
    public static final String HOSPITALS_ALL = "hospitals-all";

    /** Geo-search results (hospitals within radius). Evicted on hospital write. */
    public static final String HOSPITALS_NEARBY = "hospitals-nearby";

    /**
     * Geo-search results filtered by blood type + component availability.
     * Evicted on hospital write OR inventory write.
     */
    public static final String HOSPITALS_NEARBY_BLOOD = "hospitals-nearby-blood";

    /**
     * Blood type compatibility lists from CompatibilityMatrix.
     * Static data — 24-hour TTL, never manually evicted.
     */
    public static final String BLOOD_COMPATIBILITY = "blood-compatibility";
}
