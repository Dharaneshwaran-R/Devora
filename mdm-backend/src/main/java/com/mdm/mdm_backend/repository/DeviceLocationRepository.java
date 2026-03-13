package com.mdm.mdm_backend.repository;

import com.mdm.mdm_backend.model.entity.DeviceLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

public interface DeviceLocationRepository extends JpaRepository<DeviceLocation, Long> {

    Optional<DeviceLocation> findTopByDeviceIdOrderByRecordedAtDesc(String deviceId);

    List<DeviceLocation> findTop10ByDeviceIdOrderByRecordedAtDesc(String deviceId);

    @Modifying
    @Transactional
    @Query(
        value = "DELETE FROM device_locations WHERE device_id = :deviceId " +
                "AND id NOT IN (SELECT id FROM (SELECT id FROM device_locations WHERE device_id = :deviceId " +
                "ORDER BY recorded_at DESC LIMIT 50) t)",
        nativeQuery = true
    )
    void deleteOldLocations(@Param("deviceId") String deviceId);

    @Transactional
    void deleteByDeviceId(String deviceId);
}
