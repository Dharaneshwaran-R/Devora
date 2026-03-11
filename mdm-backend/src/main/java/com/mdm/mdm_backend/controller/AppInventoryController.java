package com.mdm.mdm_backend.controller;

import com.mdm.mdm_backend.model.dto.AppInventoryRequest;
import com.mdm.mdm_backend.model.dto.NewAppNotificationRequest;
import com.mdm.mdm_backend.model.entity.AdminNotification;
import com.mdm.mdm_backend.model.entity.AppInventory;
import com.mdm.mdm_backend.model.entity.RestrictedApp;
import com.mdm.mdm_backend.repository.AdminNotificationRepository;
import com.mdm.mdm_backend.repository.DeviceRepository;
import com.mdm.mdm_backend.repository.AppInventoryRepository;
import com.mdm.mdm_backend.repository.RestrictedAppRepository;
import com.mdm.mdm_backend.service.AppInventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class AppInventoryController {

    private final AppInventoryService appInventoryService;
    private final AdminNotificationRepository notificationRepository;
    private final DeviceRepository deviceRepository;
    private final AppInventoryRepository appInventoryRepository;
    private final RestrictedAppRepository restrictedAppRepository;

    @PostMapping("/app-inventory")
    public ResponseEntity<?> saveInventory(@Valid @RequestBody AppInventoryRequest request) {
        List<AppInventory> saved = appInventoryService.saveInventory(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of(
                        "message", "App inventory saved successfully",
                        "appsCount", saved.size()
                ));
    }

    @GetMapping("/app-inventory/{deviceId}")
    public ResponseEntity<List<AppInventory>> getInventory(@PathVariable String deviceId) {
        List<AppInventory> apps = appInventoryService.getInventory(deviceId);
        if (apps.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(apps);
    }

    @PostMapping("/app-inventory/notify")
    public ResponseEntity<?> notifyNewApp(@Valid @RequestBody NewAppNotificationRequest request) {
        // Look up employee name from device
        String employeeName = deviceRepository.findByDeviceId(request.getDeviceId())
                .map(d -> d.getEmployeeName() != null ? d.getEmployeeName() : request.getDeviceId().substring(0, 8))
                .orElse(request.getDeviceId().substring(0, 8));

        String title = request.getAction().equals("INSTALLED")
                ? "New app installed"
                : "App updated";

        String message = String.format("%s %s \"%s\" (%s)",
                employeeName,
                request.getAction().equals("INSTALLED") ? "installed" : "updated",
                request.getAppName(),
                request.getPackageName());

        AdminNotification notification = AdminNotification.builder()
                .deviceId(request.getDeviceId())
                .type("APP_" + request.getAction())
                .title(title)
                .message(message)
                .read(false)
                .createdAt(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .build();
        notificationRepository.save(notification);

        // Also add/update the app in inventory
        AppInventory app = AppInventory.builder()
                .deviceId(request.getDeviceId())
                .appName(request.getAppName())
                .packageName(request.getPackageName())
                .versionName(request.getVersionName())
                .versionCode(request.getVersionCode())
                .isSystemApp(request.getIsSystemApp() != null ? request.getIsSystemApp() : false)
                .collectedAt(LocalDateTime.now())
                .build();
        appInventoryRepository.save(app);

        log.info("App {} notification: {} on device {}", request.getAction(), request.getAppName(), request.getDeviceId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Notification recorded"));
    }

    @GetMapping("/notifications")
    public ResponseEntity<List<AdminNotification>> getNotifications() {
        return ResponseEntity.ok(notificationRepository.findAllByOrderByCreatedAtDesc());
    }

    @GetMapping("/notifications/unread")
    public ResponseEntity<?> getUnreadCount() {
        long count = notificationRepository.countByReadFalse();
        return ResponseEntity.ok(Map.of("count", count));
    }

    @PutMapping("/notifications/{id}/read")
    public ResponseEntity<?> markAsRead(@PathVariable Long id) {
        return notificationRepository.findById(id)
                .map(n -> {
                    n.setRead(true);
                    notificationRepository.save(n);
                    return ResponseEntity.ok(Map.of("message", "Marked as read"));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // ═════════════════════════════════════════
    // APP RESTRICTION
    // ═════════════════════════════════════════

    @PostMapping("/devices/{deviceId}/restrict-app")
    public ResponseEntity<?> restrictApp(
            @PathVariable String deviceId,
            @RequestBody Map<String, String> body) {
        String packageName = body.get("packageName");
        String appName = body.getOrDefault("appName", packageName);

        if (packageName == null || packageName.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "packageName is required"));
        }

        if (restrictedAppRepository.existsByDeviceIdAndPackageName(deviceId, packageName)) {
            return ResponseEntity.ok(Map.of("message", "App already restricted"));
        }

        RestrictedApp restricted = RestrictedApp.builder()
                .deviceId(deviceId)
                .packageName(packageName)
                .appName(appName)
                .restrictedAt(LocalDateTime.now())
                .build();
        restrictedAppRepository.save(restricted);

        log.info("Restricted app {} on device {}", packageName, deviceId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "App restricted successfully"));
    }

    @DeleteMapping("/devices/{deviceId}/restrict-app/{packageName}")
    public ResponseEntity<?> unrestrictApp(
            @PathVariable String deviceId,
            @PathVariable String packageName) {
        if (!restrictedAppRepository.existsByDeviceIdAndPackageName(deviceId, packageName)) {
            return ResponseEntity.notFound().build();
        }
        restrictedAppRepository.deleteByDeviceIdAndPackageName(deviceId, packageName);
        log.info("Unrestricted app {} on device {}", packageName, deviceId);
        return ResponseEntity.ok(Map.of("message", "App unrestricted successfully"));
    }

    @GetMapping("/devices/{deviceId}/restricted-apps")
    public ResponseEntity<List<RestrictedApp>> getRestrictedApps(@PathVariable String deviceId) {
        return ResponseEntity.ok(restrictedAppRepository.findByDeviceId(deviceId));
    }
}
