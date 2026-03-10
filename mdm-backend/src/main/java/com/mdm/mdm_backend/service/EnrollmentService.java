package com.mdm.mdm_backend.service;

import com.mdm.mdm_backend.model.dto.DeviceResponse;
import com.mdm.mdm_backend.model.dto.EnrollRequest;
import com.mdm.mdm_backend.model.entity.Device;
import com.mdm.mdm_backend.model.entity.EnrollmentToken;
import com.mdm.mdm_backend.repository.DeviceRepository;
import com.mdm.mdm_backend.repository.EnrollmentTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EnrollmentService {

    private final DeviceRepository deviceRepository;
    private final EnrollmentTokenRepository enrollmentTokenRepository;

    // ════════════════════════════════════════
    // ENROLLMENT TOKEN MANAGEMENT
    // ════════════════════════════════════════

    public EnrollmentToken generateEnrollmentToken(String token, String employeeId, String employeeName) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusHours(24);

        EnrollmentToken enrollmentToken = EnrollmentToken.builder()
                .token(token)
                .employeeId(employeeId)
                .employeeName(employeeName)
                .createdAt(now)
                .expiresAt(expiresAt)
                .status("PENDING")
                .build();

        log.info("Generated enrollment token for employee: {} ({})", employeeName, employeeId);
        return enrollmentTokenRepository.save(enrollmentToken);
    }

    public Optional<EnrollmentToken> getEnrollmentToken(String token) {
        return enrollmentTokenRepository.findByTokenAndStatus(token, "PENDING");
    }

    public void markTokenAsUsed(String token, String deviceId) {
        Optional<EnrollmentToken> enrollmentToken = enrollmentTokenRepository.findByToken(token);
        if (enrollmentToken.isPresent()) {
            EnrollmentToken et = enrollmentToken.get();
            et.setStatus("USED");
            et.setDeviceId(deviceId);
            et.setUsedAt(LocalDateTime.now());
            enrollmentTokenRepository.save(et);
            log.info("Marked token {} as USED for device: {}", token, deviceId);
        }
    }

    // ════════════════════════════════════════
    // DEVICE ENROLLMENT
    // ════════════════════════════════════════

    public Device enrollDevice(EnrollRequest request) {
        // If enrollment token is provided, get employee info from token
        String employeeName = null;
        String employeeId = null;

        if (request.getEnrollmentToken() != null && !request.getEnrollmentToken().isBlank()) {
            Optional<EnrollmentToken> tokenData = enrollmentTokenRepository.findByToken(request.getEnrollmentToken());
            if (tokenData.isPresent()) {
                EnrollmentToken et = tokenData.get();
                employeeName = et.getEmployeeName();
                employeeId = et.getEmployeeId();
                // Mark token as used
                markTokenAsUsed(request.getEnrollmentToken(), request.getDeviceId());
            }
        }

        if (deviceRepository.existsByDeviceId(request.getDeviceId())) {
            log.info("Device {} already enrolled, updating...", request.getDeviceId());
            Device existing = deviceRepository.findByDeviceId(request.getDeviceId()).get();
            existing.setStatus("ACTIVE");
            if (employeeName != null) {
                existing.setEmployeeName(employeeName);
                existing.setEmployeeId(employeeId);
            }
            return deviceRepository.save(existing);
        }

        Device device = Device.builder()
                .deviceId(request.getDeviceId())
                .enrollmentToken(request.getEnrollmentToken())
                .enrollmentMethod(request.getEnrollmentMethod())
                .employeeName(employeeName)
                .employeeId(employeeId)
                .enrolledAt(LocalDateTime.now())
                .status("ACTIVE")
                .build();

        log.info("Enrolling new device: {} for employee: {}", request.getDeviceId(), employeeName);
        return deviceRepository.save(device);
    }

    public List<Device> getAllDevices() {
        return deviceRepository.findAll();
    }

    public List<DeviceResponse> getAllDevicesAsResponse() {
        return deviceRepository.findAll().stream()
                .map(this::convertToDeviceResponse)
                .collect(Collectors.toList());
    }

    public Optional<Device> getDevice(String deviceId) {
        return deviceRepository.findByDeviceId(deviceId);
    }

    public Optional<DeviceResponse> getDeviceAsResponse(String deviceId) {
        return deviceRepository.findByDeviceId(deviceId)
                .map(this::convertToDeviceResponse);
    }

    private DeviceResponse convertToDeviceResponse(Device device) {
        return DeviceResponse.builder()
                .id(device.getId())
                .deviceId(device.getDeviceId())
                .employeeId(device.getEmployeeId())
                .employeeName(device.getEmployeeName())
                .enrollmentMethod(device.getEnrollmentMethod())
                .deviceModel(device.getDeviceModel())
                .manufacturer(device.getManufacturer())
                .enrolledAt(device.getEnrolledAt())
                .status(device.getStatus())
                .build();
    }

    public long countDevices() {
        return deviceRepository.count();
    }

    public long countByStatus(String status) {
        return deviceRepository.countByStatus(status);
    }
}