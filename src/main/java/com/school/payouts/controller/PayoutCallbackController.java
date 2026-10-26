
package com.school.payouts.controller;

import com.school.payouts.config.B2cConfig;
import com.school.payouts.service.PayoutResultService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tools.jackson.databind.JsonNode;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class PayoutCallbackController {
    private static final Map<String, Object> ACK = Map.of("ResultCode", 0, "ResultDesc", "Accepted");

    private final B2cConfig config;
    private final PayoutResultService results;

    @PostMapping("/b2c/result/{secret}")
    public ResponseEntity<Map<String, Object>> result(@PathVariable String secret, @RequestBody JsonNode payload) {
        if (!secretMatches(secret)) return ResponseEntity.notFound().build();   // reveal nothing
        results.onResult(payload);
        return ResponseEntity.ok(ACK);
    }

    @PostMapping("/b2c/timeout/{secret}")
    public ResponseEntity<Map<String, Object>> timeout(@PathVariable String secret, @RequestBody JsonNode payload) {
        if (!secretMatches(secret)) return ResponseEntity.notFound().build();
        results.onTimeout(payload);
        return ResponseEntity.ok(ACK);
    }

    private boolean secretMatches(String provided) {
        String expected = config.getCallbackSecret() == null ? "" : config.getCallbackSecret();
        return !expected.isBlank() && MessageDigest.isEqual(
                provided.getBytes(StandardCharsets.UTF_8), expected.getBytes(StandardCharsets.UTF_8));
    }
}