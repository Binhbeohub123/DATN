package com.polycinema.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Generic key-value config store.
 * Used by the seat-lock system to persist SEAT_LOCK_MINUTES without a code deploy.
 */
@Entity
@Table(name = "SystemConfig")
@Data
public class SystemConfig {

    @Id
    @Column(name = "ConfigKey", length = 100)
    private String configKey;

    @Column(name = "ConfigValue", length = 500, nullable = false)
    private String configValue;

    @Column(name = "UpdatedAt")
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    protected void onPersist() {
        updatedAt = LocalDateTime.now();
    }
}
