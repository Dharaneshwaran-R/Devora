package com.mdm.mdm_backend.model.entity;

import jakarta.persistence.*;
import lombok.*;
import tools.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

@Entity
@Table(name = "admin_notifications")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class AdminNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String deviceId;

    @Column(nullable = false)
    private String type;  // APP_INSTALLED, APP_UPDATED, APP_REMOVED

    @Column(nullable = false)
    private String title;

    private String message;

    private boolean read;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
}
