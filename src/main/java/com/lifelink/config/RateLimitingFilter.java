package com.lifelink.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitingFilter implements Filter {

    private final Map<String, Bucket> authBuckets = new ConcurrentHashMap<>();
    private final Map<String, Bucket> requestBuckets = new ConcurrentHashMap<>();

    private Bucket createAuthBucket() {
        Bandwidth limit = Bandwidth.classic(10, Refill.greedy(10, Duration.ofMinutes(1)));
        return Bucket.builder().addLimit(limit).build();
    }

    private Bucket createRequestBucket() {
        Bandwidth limit = Bandwidth.classic(5, Refill.greedy(5, Duration.ofMinutes(1)));
        return Bucket.builder().addLimit(limit).build();
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        String path = httpRequest.getRequestURI();
        String method = httpRequest.getMethod();
        String ip = getClientIP(httpRequest);

        if (path.startsWith("/api/v1/auth") || path.startsWith("/api/v1/blood-chain/invite")) {
            Bucket bucket = authBuckets.computeIfAbsent(ip, k -> createAuthBucket());
            if (!bucket.tryConsume(1)) {
                httpResponse.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                httpResponse.setHeader("Retry-After", "60");
                httpResponse.getWriter().write("Too many requests. Please try again later.");
                return;
            }
        } else if (path.equals("/api/v1/requests") && "POST".equalsIgnoreCase(method)) {
            Bucket bucket = requestBuckets.computeIfAbsent(ip, k -> createRequestBucket());
            if (!bucket.tryConsume(1)) {
                httpResponse.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                httpResponse.setHeader("Retry-After", "60");
                httpResponse.getWriter().write("Too many requests. Please try again later.");
                return;
            }
        }

        chain.doFilter(request, response);
    }

    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }
}
