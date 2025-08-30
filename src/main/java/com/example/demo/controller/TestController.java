package com.example.demo.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequestMapping("/api")
public class TestController {

    // 각 API별 동시 처리 제한
    private final Semaphore api1Semaphore = new Semaphore(1000);
    private final Semaphore api2Semaphore = new Semaphore(1000);
    private final Semaphore api3Semaphore = new Semaphore(200);
    private final Semaphore api4Semaphore = new Semaphore(600);

    @GetMapping("/1")
    public ResponseEntity<Map<String, Object>> api1() {
        String requestId = UUID.randomUUID().toString().substring(0, 8);

        try {
            if (!api1Semaphore.tryAcquire(1, TimeUnit.SECONDS)) {
                log.warn("API-1 rejected: {}", requestId);
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                        .body(Map.of("error", "Too many requests"));
            }

            try {
                Thread.sleep(50); // 간단한 처리 시뮬레이션

                log.info("API-1 success: {}", requestId);
                return ResponseEntity.ok(Map.of(
                        "api", "1",
                        "requestId", requestId,
                        "available", api1Semaphore.availablePermits()
                ));

            } finally {
                api1Semaphore.release();
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Interrupted"));
        }
    }

    @PostMapping("/2")
    public ResponseEntity<Map<String, Object>> api2() {
        String requestId = UUID.randomUUID().toString().substring(0, 8);

        try {
            if (!api2Semaphore.tryAcquire(1, TimeUnit.SECONDS)) {
                log.warn("API-2 rejected: {}", requestId);
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                        .body(Map.of("error", "Too many requests"));
            }

            try {
                Thread.sleep(50);

                log.info("API-2 success: {}", requestId);
                return ResponseEntity.ok(Map.of(
                        "api", "2",
                        "requestId", requestId,
                        "available", api2Semaphore.availablePermits()
                ));

            } finally {
                api2Semaphore.release();
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Interrupted"));
        }
    }

    @PutMapping("/3")
    public ResponseEntity<Map<String, Object>> api3() {
        String requestId = UUID.randomUUID().toString().substring(0, 8);

        try {
            if (!api3Semaphore.tryAcquire(1, TimeUnit.SECONDS)) {
                log.warn("API-3 rejected: {}", requestId);
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                        .body(Map.of("error", "Too many requests"));
            }

            try {
                Thread.sleep(100); // 병목 구간 - 더 긴 처리 시간

                log.info("API-3 success: {}", requestId);
                return ResponseEntity.ok(Map.of(
                        "api", "3",
                        "requestId", requestId,
                        "available", api3Semaphore.availablePermits(),
                        "bottleneck", true
                ));

            } finally {
                api3Semaphore.release();
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Interrupted"));
        }
    }

    @DeleteMapping("/4")
    public ResponseEntity<Map<String, Object>> api4() {
        String requestId = UUID.randomUUID().toString().substring(0, 8);

        try {
            if (!api4Semaphore.tryAcquire(1, TimeUnit.SECONDS)) {
                log.warn("API-4 rejected: {}", requestId);
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                        .body(Map.of("error", "Too many requests"));
            }

            try {
                Thread.sleep(50);

                log.info("API-4 success: {}", requestId);
                return ResponseEntity.ok(Map.of(
                        "api", "4",
                        "requestId", requestId,
                        "available", api4Semaphore.availablePermits(),
                        "complete", true
                ));

            } finally {
                api4Semaphore.release();
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Interrupted"));
        }
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> status() {
        return ResponseEntity.ok(Map.of(
                "api1", Map.of("max", 1000, "available", api1Semaphore.availablePermits()),
                "api2", Map.of("max", 1000, "available", api2Semaphore.availablePermits()),
                "api3", Map.of("max", 200, "available", api3Semaphore.availablePermits()),
                "api4", Map.of("max", 600, "available", api4Semaphore.availablePermits())
        ));
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("UP");
    }
}