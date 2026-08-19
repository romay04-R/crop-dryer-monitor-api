package com.example.cropdryer.controller;

import com.example.cropdryer.dto.ApiResponse;
import com.example.cropdryer.dto.ReadingRequest;
import com.example.cropdryer.dto.ReadingResponse;
import com.example.cropdryer.entity.Reading;
import com.example.cropdryer.service.ReadingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ReadingController {

    private final ReadingService service;
    @Value("${cropdryer.api.key:}")
    private String configuredApiKey;


    @PostMapping("/readings")
    public ResponseEntity<ApiResponse<ReadingResponse>> createReading(
            @RequestHeader(value = "X-API-Key", required = false) String apiKey,
            @Valid @RequestBody ReadingRequest request
    ) {
        if (configuredApiKey != null && !configuredApiKey.isBlank()) {
            if (apiKey == null || !configuredApiKey.equals(apiKey)) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or missing API key");
            }
        }

        Reading saved = service.saveFromRequest(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(ReadingResponse.fromEntity(saved)));
    }

    @GetMapping("/readings")
    public ResponseEntity<ApiResponse<List<ReadingResponse>>> listReadings(
            @RequestParam(name = "limit", defaultValue = "50") int limit,
            @RequestParam(name = "deviceId", required = false) String deviceId
    ) {
        if (limit <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "limit must be a positive integer");
        }
        List<Reading> readings = service.getRecent(limit, deviceId);
        List<ReadingResponse> data = readings.stream().map(ReadingResponse::fromEntity).collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.ok(data));
    }

    @GetMapping("/readings/latest")
    public ResponseEntity<?> latestReading(
            @RequestParam(name = "deviceId", required = false) String deviceId
    ) {
        Optional<Reading> latest = service.getLatest(deviceId);
        if (latest.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error("No readings found"));
        }
        return ResponseEntity.ok(ApiResponse.ok(ReadingResponse.fromEntity(latest.get())));
    }

    @GetMapping("/alerts")
    public ResponseEntity<ApiResponse<List<ReadingResponse>>> alerts(
            @RequestParam(name = "deviceId", required = false) String deviceId
    ) {
        List<Reading> alerts = service.getAlerts(deviceId);
        List<ReadingResponse> data = alerts.stream().map(ReadingResponse::fromEntity).collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.ok(data));
    }
}
