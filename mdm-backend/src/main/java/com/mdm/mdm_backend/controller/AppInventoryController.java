package com.mdm.mdm_backend.controller;

import com.mdm.mdm_backend.model.dto.AppInventoryRequest;
import com.mdm.mdm_backend.model.entity.AppInventory;
import com.mdm.mdm_backend.service.AppInventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AppInventoryController {

    private final AppInventoryService appInventoryService;

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
}
