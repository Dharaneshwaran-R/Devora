package com.mdm.mdm_backend.controller;

import com.mdm.mdm_backend.model.dto.AdminLoginRequest;
import com.mdm.mdm_backend.model.dto.AdminLoginResponse;
import com.mdm.mdm_backend.model.dto.AdminRegisterRequest;
import com.mdm.mdm_backend.model.entity.Admin;
import com.mdm.mdm_backend.repository.AdminRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminRepository adminRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody AdminRegisterRequest request) {
        if (adminRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("success", false, "message", "Email already registered"));
        }

        Admin admin = Admin.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .createdAt(LocalDateTime.now())
                .build();

        adminRepository.save(admin);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("success", true, "message", "Admin registered successfully"));
    }

    @PostMapping("/login")
    public ResponseEntity<AdminLoginResponse> login(@Valid @RequestBody AdminLoginRequest request) {
        return adminRepository.findByEmail(request.getEmail())
                .filter(admin -> passwordEncoder.matches(request.getPassword(), admin.getPassword()))
                .map(admin -> ResponseEntity.ok(
                        new AdminLoginResponse(true, admin.getName(), "Login successful")))
                .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new AdminLoginResponse(false, null, "Invalid email or password")));
    }
}
