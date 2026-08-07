package com.sih.roadassistant.security;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitingFilter implements Filter {
     private static final int MAX_BUCKET_CAPACITY = 20;
     private static final double REFILL_RATE = 1.0;

     private final Map<String, TokenBucket> limiters = new ConcurrentHashMap<>();

     @Override
     public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
         throws IOException, ServletException {
         HttpServletRequest httpRequest = (HttpServletRequest) request;
         HttpServletResponse httpResponse = (HttpServletResponse) response;

         String ipAddress = httpRequest.getRemoteAddr();
         TokenBucket bucket = limiters.computeIfAbsent(ipAddress, k -> new TokenBucket());
         if (bucket.tryConsume()) {
             chain.doFilter(request, response);
         } else {
             httpResponse.setStatus(429);
             httpResponse.setContentType("application/json");
             httpResponse.getWriter().write("{\"error\":\"Too many requests. Rate Limit exceeded.\"}");
         }
     }

     private static class TokenBucket {
          private double tokens = MAX_BUCKET_CAPACITY;
          private long lastRefillTimestamp = System.currentTimeMillis();

          public synchronized boolean tryConsume() {
              refill();
              if (tokens >= 1.0) {
                  tokens -= 1.0;
                  return true;
              }
              return false;
          }

          private void refill() {
              long now = System.currentTimeMillis();
              double deltaSeconds = (now - lastRefillTimestamp) / 1000.0;
              tokens = Math.min(MAX_BUCKET_CAPACITY, tokens + (deltaSeconds * REFILL_RATE));
              lastRefillTimestamp = now;
          }
     }
}
