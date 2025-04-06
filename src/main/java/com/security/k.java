//package com.security;
//import io.github.bucket4j.*;
//import org.springframework.stereotype.Component;
//import org.springframework.web.servlet.HandlerInterceptor;
//
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import java.time.Duration;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.concurrent.ConcurrentMap;
//
//@Component
//public class RateLimitInterceptor implements HandlerInterceptor {
//
//    private final ConcurrentMap<String, Bucket> cache = new ConcurrentHashMap<>();
//
//    @Override
//    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
//        String ip = request.getRemoteAddr();
//        Bucket bucket = cache.computeIfAbsent(ip, k -> newBucket());
//
//        if (bucket.tryConsume(1)) {
//            return true;
//        } else {
//            response.setStatus(429);
//            response.getWriter().write("Too many requests. Try again later.");
//            return false;
//        }
//    }
//
//    private Bucket newBucket() {
//        return Bucket4j.builder()
//                .addLimit(Bandwidth.classic(10, Refill.intervally(10, Duration.ofMinutes(1))))
//                .build();
//    }
//}
