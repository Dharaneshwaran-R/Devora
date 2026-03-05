package com.mdm.mdm_backend.service;

import com.mdm.mdm_backend.model.dto.EnrollRequest;
import com.mdm.mdm_backend.model.entity.Device;
import com.mdm.mdm_backend.repository.DeviceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class EnrollmentService {

    private final DeviceRepository deviceRepository;

    public Device enrollDevice(EnrollRequest request) {
        if (deviceRepository.existsByDeviceId(request.getDeviceId())) {
            log.info("Device {} already enrolled, updating...", request.getDeviceId());
            Device existing = deviceRepository.findByDeviceId(request.getDeviceId()).get();
            existing.setStatus("ACTIVE");
            return deviceRepository.save(existing);
        }

        Device device = Device.builder()
                .deviceId(request.getDeviceId())
                .enrollmentToken(request.getEnrollmentToken())
                .enrollmentMethod(request.getEnrollmentMethod())
                .enrolledAt(LocalDateTime.now())
                .status("ACTIVE")
                .build();

        log.info("Enrolling new device: {}", request.getDeviceId());
        return deviceRepository.save(device);
    }

    public List<Device> getAllDevices() {
        return deviceRepository.findAll();
    }

    public Optional<Device> getDevice(String deviceId) {
        return deviceRepository.findByDeviceId(deviceId);
    }

    public long countDevices() {
        return deviceRepository.count();
    }

    public long countByStatus(String status) {
        return deviceRepository.countByStatus(status);
    }
}