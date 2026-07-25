package com.lifelink.config;

import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/**
 * Redis cache configuration.
 *
 * Cache names and their TTLs:
 * ┌──────────────────────────────┬─────────┬─────────────────────────────────────────────┐
 * │ Cache name                   │ TTL     │ What is cached                              │
 * ├──────────────────────────────┼─────────┼─────────────────────────────────────────────┤
 * │ hospitals-all                │  5 min  │ Full hospital list (invalidated on write)   │
 * │ hospitals-nearby             │  5 min  │ Geo-search results (lat/lng/radius key)     │
 * │ hospitals-nearby-blood       │  3 min  │ Nearby hospitals filtered by blood stock     │
 * │ blood-compatibility          │ 24 h    │ CompatibilityMatrix lookups (static data)   │
 * └──────────────────────────────┴─────────┴─────────────────────────────────────────────┘
 *
 * All caches use JSON serialization so values survive across deployments and
 * are human-readable in Redis. Keys are plain strings (no Java-serialized prefix).
 */
@Configuration
public class RedisConfig {

    // -------------------------------------------------------------------------
    // Default cache config — applied to all caches unless overridden
    // -------------------------------------------------------------------------

    @Bean
    public RedisCacheConfiguration defaultCacheConfiguration() {
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(5))
                .disableCachingNullValues()
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(
                                new StringRedisSerializer()))
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(
                                new GenericJackson2JsonRedisSerializer()));
    }

    // -------------------------------------------------------------------------
    // Per-cache TTL overrides
    // -------------------------------------------------------------------------

    @Bean
    public RedisCacheManagerBuilderCustomizer cacheManagerCustomizer() {
        return builder -> builder
                // Static blood compatibility data — almost never changes, long TTL
                .withCacheConfiguration(CacheNames.BLOOD_COMPATIBILITY,
                        defaultCacheConfiguration().entryTtl(Duration.ofHours(24)))

                // Geo-search results — slightly shorter because inventory changes
                .withCacheConfiguration(CacheNames.HOSPITALS_NEARBY_BLOOD,
                        defaultCacheConfiguration().entryTtl(Duration.ofMinutes(3)))

                // Full hospital list — same as default (5 min)
                .withCacheConfiguration(CacheNames.HOSPITALS_ALL,
                        defaultCacheConfiguration())

                // Nearby-only (no blood filter) — same as default (5 min)
                .withCacheConfiguration(CacheNames.HOSPITALS_NEARBY,
                        defaultCacheConfiguration());
    }
}
