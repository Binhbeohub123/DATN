package com.polycinema.backend.service;

import com.polycinema.backend.entity.SystemConfig;
import com.polycinema.backend.repository.SystemConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SystemConfigService {

    private final SystemConfigRepository systemConfigRepository;

    public Optional<SystemConfig> findById(String configKey) {
        return systemConfigRepository.findById(configKey);
    }

    public SystemConfig save(SystemConfig config) {
        return systemConfigRepository.save(config);
    }
}
