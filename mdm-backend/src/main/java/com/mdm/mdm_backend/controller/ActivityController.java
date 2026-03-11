package com.mdm.mdm_backend.controller;

import com.mdm.mdm_backend.model.entity.DeviceActivity;
import com.mdm.mdm_backend.repository.DeviceActivityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ActivityController {

    private final DeviceActivityRepository activityRepository;

    @GetMapping("/activities")
    public ResponseEntity<List<DeviceActivity>> getActivities(
            @RequestParam(defaultValue = "10") int limit) {
        List<DeviceActivity> all = activityRepository.findAllByOrderByCreatedAtDesc();
        List<DeviceActivity> result = all.size() > limit ? all.subList(0, limit) : all;
        return ResponseEntity.ok(result);
    }

    @GetMapping("/devices/{deviceId}/activities")
    public ResponseEntity<List<DeviceActivity>> getDeviceActivities(@PathVariable String deviceId) {
        return ResponseEntity.ok(activityRepository.findByDeviceIdOrderByCreatedAtDesc(deviceId));
    }
}
