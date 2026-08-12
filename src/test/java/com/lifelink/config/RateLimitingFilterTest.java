package com.lifelink.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class RateLimitingFilterTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testAuthRateLimit() throws Exception {
        // 10 requests allowed, 11th should be 429
        for (int i = 0; i < 10; i++) {
            var result = mockMvc.perform(post("/api/v1/auth/login")
                    .header("X-Forwarded-For", "192.168.1.100")
                    .contentType("application/json")
                    .content("{\"email\":\"test@test.com\",\"password\":\"password\"}"))
                    .andReturn();
            org.junit.jupiter.api.Assertions.assertNotEquals(429, result.getResponse().getStatus(), "Status should not be 429 on first 10 requests");
        }

        mockMvc.perform(post("/api/v1/auth/login")
                .header("X-Forwarded-For", "192.168.1.100")
                .contentType("application/json")
                .content("{\"email\":\"test@test.com\",\"password\":\"password\"}"))
                .andExpect(status().isTooManyRequests());
    }

    @Test
    void testRequestRateLimit() throws Exception {
        // 5 requests allowed, 6th should be 429
        for (int i = 0; i < 5; i++) {
            var result = mockMvc.perform(post("/api/v1/requests")
                    .header("X-Forwarded-For", "192.168.1.101")
                    .contentType("application/json")
                    .content("{}"))
                    .andReturn();
            org.junit.jupiter.api.Assertions.assertNotEquals(429, result.getResponse().getStatus(), "Status should not be 429 on first 5 requests");
        }

        mockMvc.perform(post("/api/v1/requests")
                .header("X-Forwarded-For", "192.168.1.101")
                .contentType("application/json")
                .content("{}"))
                .andExpect(status().isTooManyRequests());
    }
}
