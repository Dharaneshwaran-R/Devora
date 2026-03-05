package com.mdm.mdm_backend.service;

import com.mdm.mdm_backend.model.dto.DeviceInfoRequest;
import com.mdm.mdm_backend.model.entity.DeviceInfo;
import com.mdm.mdm_backend.repository.DeviceInfoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeviceInfoService {

    private final DeviceInfoRepository deviceInfoRepository;

    public DeviceInfo saveDeviceInfo(DeviceInfoRequest request) {
        DeviceInfo info = DeviceInfo.builder()
                .deviceId(request.getDeviceId())
                .model(request.getModel())
                .manufacturer(request.getManufacturer())
                .osVersion(request.getOsVersion())
                .sdkVersion(request.getSdkVersion())
                .serialNumber(request.getSerialNumber())
                .imei(request.getImei())
                .deviceType(request.getDeviceType())
                .collectedAt(LocalDateTime.now())
                .build();

        log.info("Saving device info for: {}", request.getDeviceId());
        return deviceInfoRepository.save(info);
    }

    public List<DeviceInfo> getDeviceInfo(String deviceId) {
        return deviceInfoRepository.findByDeviceIdOrderByCollectedAtDesc(deviceId);
    }
}