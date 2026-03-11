package com.mdm.mdm_backend.service;

import com.mdm.mdm_backend.model.entity.Device;
import com.mdm.mdm_backend.repository.DeviceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Scheduled job that detects devices where the MDM app has been uninstalled
 * or the device has gone offline.
 *
 * Logic:
 *   - The enrolled device sends a heartbeat every ~15 minutes via SyncWorker.
 *   - If no heartbeat has been received for more than INACTIVE_THRESHOLD_MINUTES,
 *     the device is presumed offline/uninstalled and marked INACTIVE.
 *   - Runs every 5 minutes to keep the status reasonably up-to-date.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DeviceStatusScheduler {

    // How long with no heartbeat before a device is considered inactive
    private static final int INACTIVE_THRESHOLD_SECONDS = 60;

    private final DeviceRepository deviceRepository;

    @Scheduled(fixedDelay = 30_000) // every 30 seconds
    @Transactional
    public void markStaleDevicesInactive() {
        LocalDateTime threshold = LocalDateTime.now().minusSeconds(INACTIVE_THRESHOLD_SECONDS);
        List<Device> staleDevices = deviceRepository.findStaleActiveDevices(threshold);

        if (staleDevices.isEmpty()) {
            return;
        }

        log.info("Marking {} stale device(s) as INACTIVE (no heartbeat for {}+ sec)",
                staleDevices.size(), INACTIVE_THRESHOLD_SECONDS);

        for (Device device : staleDevices) {
            device.setStatus("INACTIVE");
            log.info("Device {} ({}) marked INACTIVE — last heartbeat: {}",
                    device.getDeviceId(),
                    device.getEmployeeName() != null ? device.getEmployeeName() : "unknown",
                    device.getLastHeartbeat() != null ? device.getLastHeartbeat() : "never");
        }

        deviceRepository.saveAll(staleDevices);
    }
}
