package com.mdm.mdm_backend.repository;

import com.mdm.mdm_backend.model.entity.DeviceLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

public interface DeviceLocationRepository extends JpaRepository<DeviceLocation, Long> {
    Optional<DeviceLocation> findTopByDeviceIdOrderByRecordedAtDesc(String deviceId);
    @Transactional
    void deleteByDeviceId(String deviceId);
}
