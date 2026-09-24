package com.sprintflow.backend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprintflow.backend.dto.dashboard.ProjectDashboardResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;


import java.time.Duration;

@Configuration
public class RedisConfig {

    @Bean
    public RedisCacheManager cacheManager(
            RedisConnectionFactory redisConnectionFactory,
            ObjectMapper objectMapper) {

        RedisCacheConfiguration cacheConfiguration =
                RedisCacheConfiguration.defaultCacheConfig()
                        .entryTtl(Duration.ofMinutes(10))
                        .serializeValuesWith(
                                RedisSerializationContext.SerializationPair
                                        .fromSerializer(
                                                new GenericJackson2JsonRedisSerializer(objectMapper)
                                        )
                        );

        // GenericJackson2JsonRedisSerializer(ObjectMapper) writes no @class type id, so a cached
        // value can only come back as LinkedHashMap. Binding this cache to its declared type
        // deserializes it as ProjectDashboardResponse instead.
        RedisCacheConfiguration projectDashboardConfiguration =
                cacheConfiguration.serializeValuesWith(
                        RedisSerializationContext.SerializationPair
                                .fromSerializer(
                                        new Jackson2JsonRedisSerializer<>(
                                                ProjectDashboardResponse.class
                                        )
                                )
                );

        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(cacheConfiguration)
                .withCacheConfiguration("projectDashboard", projectDashboardConfiguration)
                .build();
    }
}