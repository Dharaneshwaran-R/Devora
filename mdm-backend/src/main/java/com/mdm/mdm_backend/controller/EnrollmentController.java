package com.mdm.mdm_backend.controller;

import com.mdm.mdm_backend.model.dto.DeviceResponse;
import com.mdm.mdm_backend.model.dto.EnrollRequest;
import com.mdm.mdm_backend.model.dto.EnrollmentRequest;
import com.mdm.mdm_backend.model.entity.Device;
import com.mdm.mdm_backend.model.entity.EnrollmentToken;
import com.mdm.mdm_backend.service.EnrollmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

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

    /**
     * Enroll a device with its details.
     * If enrollmentToken is provided, employee details are fetched from the token.
     */
    @PostMapping("/enroll")
    public ResponseEntity<DeviceResponse> enroll(@Valid @RequestBody EnrollRequest request) {
        Device device = enrollmentService.enrollDevice(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                DeviceResponse.builder()
                        .id(device.getId())
                        .deviceId(device.getDeviceId())
                        .employeeId(device.getEmployeeId())
                        .employeeName(device.getEmployeeName())
                        .enrollmentMethod(device.getEnrollmentMethod())
                        .deviceModel(device.getDeviceModel())
                        .manufacturer(device.getManufacturer())
                        .enrolledAt(device.getEnrolledAt())
                        .status(device.getStatus())
                        .build()
        );
    }

    /**
     * Get all devices with employee information
     */
    @GetMapping("/devices")
    public ResponseEntity<List<DeviceResponse>> getAllDevices() {
        return ResponseEntity.ok(enrollmentService.getAllDevicesAsResponse());
    }

    /**
     * Get a specific device by deviceId
     */
    @GetMapping("/devices/{deviceId}")
    public ResponseEntity<DeviceResponse> getDevice(@PathVariable String deviceId) {
        return enrollmentService.getDeviceAsResponse(deviceId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Generate enrollment token for an employee.
     * Token is stored in DB with employee details and expiry time.
     * Token can be used by device to enroll with employee information.
     */
    @PostMapping("/enrollment/generate")
    public ResponseEntity<Map<String, Object>> generateToken(@Valid @RequestBody EnrollmentRequest request) {
        String token = generateDevToken();
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(24);

        // Persist token with employee details and expiry
        EnrollmentToken enrollmentToken = enrollmentService.generateEnrollmentToken(
                token,
                request.getEmployeeId(),
                request.getEmployeeName()
        );

        return ResponseEntity.ok(Map.of(
                "token", token,
                "employeeId", request.getEmployeeId(),
                "employeeName", request.getEmployeeName(),
                "expiresAt", expiresAt,
                "status", "PENDING"
        ));
    }
}