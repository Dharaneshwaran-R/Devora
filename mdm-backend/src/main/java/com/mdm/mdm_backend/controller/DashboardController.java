package com.mdm.mdm_backend.controller;

import com.mdm.mdm_backend.model.dto.DashboardStats;
import com.mdm.mdm_backend.repository.DeviceInfoRepository;
import com.mdm.mdm_backend.service.AppInventoryService;
import com.mdm.mdm_backend.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class DashboardController {

    private final EnrollmentService enrollmentService;
    private final AppInventoryService appInventoryService;
    private final DeviceInfoRepository deviceInfoRepository;

    // DeviceInfoSyncWorker runs every 15 minutes; a 20-minute window avoids false offline flips.
    private static final long ACTIVE_HEARTBEAT_WINDOW_MINUTES = 20L;

    @GetMapping("/dashboard/stats")
    public ResponseEntity<DashboardStats> getStats() {
        long totalDevices = enrollmentService.countDevices();
        long activeDevices = deviceInfoRepository.countRecentlySyncedActiveDevices(
            LocalDateTime.now().minusMinutes(ACTIVE_HEARTBEAT_WINDOW_MINUTES)
        );
        long inactiveDevices = Math.max(0L, totalDevices - activeDevices);

        DashboardStats stats = DashboardStats.builder()
            .totalDevices(totalDevices)
            .activeDevices(activeDevices)
            .inactiveDevices(inactiveDevices)
                .totalApps(appInventoryService.countApps())
                .build();
        return ResponseEntity.ok(stats);
    }
}
