package com.mdm.mdm_backend.controller;

import com.mdm.mdm_backend.model.dto.DashboardStats;
import com.mdm.mdm_backend.service.AppInventoryService;
import com.mdm.mdm_backend.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class DashboardController {

    private final EnrollmentService enrollmentService;
    private final AppInventoryService appInventoryService;

    @GetMapping("/dashboard/stats")
    public ResponseEntity<DashboardStats> getStats() {
        DashboardStats stats = DashboardStats.builder()
                .totalDevices(enrollmentService.countDevices())
                .activeDevices(enrollmentService.countByStatus("ACTIVE"))
                .inactiveDevices(enrollmentService.countByStatus("INACTIVE"))
                .totalApps(appInventoryService.countApps())
                .build();
        return ResponseEntity.ok(stats);
    }
}
