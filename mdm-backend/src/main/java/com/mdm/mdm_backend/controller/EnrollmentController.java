package com.mdm.mdm_backend.controller;

import com.mdm.mdm_backend.model.dto.EnrollRequest;
import com.mdm.mdm_backend.model.dto.EnrollmentRequest;
import com.mdm.mdm_backend.model.entity.Device;
import com.mdm.mdm_backend.service.EnrollmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    private String generateDevToken() {
        String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder token = new StringBuilder("DEV");
        for (int group = 0; group < 3; group++) {
            token.append('-');
            for (int i = 0; i < 4; i++) {
                int index = (int) (Math.random() * alphabet.length());
                token.append(alphabet.charAt(index));
            }
        }
        return token.toString();
    }

    @PostMapping("/enroll")
    public ResponseEntity<Device> enroll(@Valid @RequestBody EnrollRequest request) {
        Device device = enrollmentService.enrollDevice(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(device);
    }

    @GetMapping("/devices")
    public ResponseEntity<List<Device>> getAllDevices() {
        return ResponseEntity.ok(enrollmentService.getAllDevices());
    }

    @GetMapping("/devices/{deviceId}")
    public ResponseEntity<Device> getDevice(@PathVariable String deviceId) {
        return enrollmentService.getDevice(deviceId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/enrollment/generate")
    public ResponseEntity<Map<String, Object>> generateToken(@Valid @RequestBody EnrollmentRequest request) {
        String token = generateDevToken();
        // TODO: Persist generated token with employee and expiry in DB.
        return ResponseEntity.ok(Map.of(
                "token", token,
                "employeeId", request.getEmployeeId(),
                "expiresAt", LocalDateTime.now().plusHours(24)
        ));
    }
}