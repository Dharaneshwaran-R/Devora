package com.mdm.mdm_backend.service;

import com.mdm.mdm_backend.model.dto.DeviceResponse;
import com.mdm.mdm_backend.model.dto.EnrollRequest;
import com.mdm.mdm_backend.model.entity.Device;
import com.mdm.mdm_backend.model.entity.DeviceInfo;
import com.mdm.mdm_backend.model.entity.Employee;
import com.mdm.mdm_backend.model.entity.EnrollmentToken;
import com.mdm.mdm_backend.repository.AppInventoryRepository;
import com.mdm.mdm_backend.repository.DeviceInfoRepository;
import com.mdm.mdm_backend.repository.DeviceRepository;
import com.mdm.mdm_backend.repository.EmployeeRepository;
import com.mdm.mdm_backend.repository.EnrollmentTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final DeviceInfoRepository deviceInfoRepository;
    private final AppInventoryRepository appInventoryRepository;
    private final EmployeeRepository employeeRepository;

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
        upsertEmployee(employeeId, employeeName, null, null);
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
            upsertEmployee(et.getEmployeeId(), et.getEmployeeName(), deviceId, null);
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

        if ((employeeName == null || employeeName.isBlank()) || (employeeId == null || employeeId.isBlank())) {
            Optional<Employee> employee = employeeRepository.findByDeviceId(request.getDeviceId());
            if (employee.isPresent()) {
                Employee existingEmployee = employee.get();
                employeeName = existingEmployee.getEmployeeName();
                employeeId = existingEmployee.getEmployeeId();
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
        String employeeId = device.getEmployeeId();
        String employeeName = device.getEmployeeName();
        String model = device.getDeviceModel();
        String manufacturer = device.getManufacturer();
        String osVersion = null;
        String sdkVersion = null;
        String serialNumber = null;

        // Enrich from latest device_info if fields are missing
        List<DeviceInfo> infoList = deviceInfoRepository.findByDeviceIdOrderByCollectedAtDesc(device.getDeviceId());
        if (!infoList.isEmpty()) {
            DeviceInfo latest = infoList.get(0);
            if (model == null || model.isBlank()) model = latest.getModel();
            if (manufacturer == null || manufacturer.isBlank()) manufacturer = latest.getManufacturer();
            osVersion = latest.getOsVersion();
            sdkVersion = latest.getSdkVersion();
            serialNumber = latest.getSerialNumber();
        }

        if ((employeeName == null || employeeName.isBlank()) || (employeeId == null || employeeId.isBlank())) {
            Optional<Employee> employee = employeeRepository.findByDeviceId(device.getDeviceId());
            if (employee.isPresent()) {
                Employee mappedEmployee = employee.get();
                employeeId = mappedEmployee.getEmployeeId();
                employeeName = mappedEmployee.getEmployeeName();
            }
        }

        return DeviceResponse.builder()
                .id(device.getId())
                .deviceId(device.getDeviceId())
                .employeeId(employeeId)
                .employeeName(employeeName)
                .enrollmentMethod(device.getEnrollmentMethod())
                .deviceModel(model)
                .manufacturer(manufacturer)
                .osVersion(osVersion)
                .sdkVersion(sdkVersion)
                .serialNumber(serialNumber)
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

    // ════════════════════════════════════════
    // DEVICE DELETION
    // ════════════════════════════════════════

    @Transactional
    public boolean deleteDeviceWithAllData(String deviceId) {
        Optional<Device> device = deviceRepository.findByDeviceId(deviceId);
        if (device.isEmpty()) {
            log.warn("Device not found for deletion: {}", deviceId);
            return false;
        }

        // 1) DELETE FROM app_inventory WHERE device_id = ?
        appInventoryRepository.deleteByDeviceId(deviceId);

        // 2) DELETE FROM device_info WHERE device_id = ?
        deviceInfoRepository.deleteByDeviceId(deviceId);

        // 3) UPDATE enrollment_tokens SET status = 'REVOKED' WHERE device_id = ?
        List<EnrollmentToken> tokens = enrollmentTokenRepository.findByDeviceId(deviceId);
        for (EnrollmentToken token : tokens) {
            token.setStatus("REVOKED");
        }
        if (!tokens.isEmpty()) {
            enrollmentTokenRepository.saveAll(tokens);
        }

        // 4) DELETE FROM devices WHERE device_id = ?
        deviceRepository.delete(device.get());

        employeeRepository.findByDeviceId(deviceId).ifPresent(employee -> {
            employee.setDeviceId(null);
            employee.setDeviceName(null);
            employee.setUpdatedAt(LocalDateTime.now());
            employeeRepository.save(employee);
        });

        log.info("Deleted device and related data for deviceId={}", deviceId);
        return true;
    }

    public boolean checkDeviceExists(String deviceId) {
        return deviceRepository.existsByDeviceIdAndStatus(deviceId, "ACTIVE");
    }

    public List<EnrollmentToken> getActiveEnrollmentTokens() {
        return enrollmentTokenRepository.findByStatusAndExpiresAtAfterOrderByCreatedAtDesc(
                "PENDING",
                LocalDateTime.now()
        );
    }

    private void upsertEmployee(String employeeId, String employeeName, String deviceId, String deviceName) {
        if (employeeId == null || employeeId.isBlank() || employeeName == null || employeeName.isBlank()) {
            return;
        }

        Employee employee = employeeRepository.findByEmployeeId(employeeId)
                .orElse(Employee.builder().employeeId(employeeId).build());

        employee.setEmployeeName(employeeName);
        if (deviceId != null) {
            employee.setDeviceId(deviceId);
        }
        if (deviceName != null && !deviceName.isBlank()) {
            employee.setDeviceName(deviceName);
        }
        employee.setUpdatedAt(LocalDateTime.now());
        employeeRepository.save(employee);
    }
}