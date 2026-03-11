package com.mdm.mdm_backend.model.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "device_locations")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class DeviceLocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String deviceId;

    private Double latitude;
    private Double longitude;
    private Float accuracy;
    private String address;

    @Column(nullable = false)
    private LocalDateTime recordedAt;
}
