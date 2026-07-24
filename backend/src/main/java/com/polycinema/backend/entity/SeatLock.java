package com.polycinema.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Represents a temporary seat reservation while a user is selecting seats.
 * Locks are auto-expired by SeatLockCleanupService every 60 seconds.
 */
@Entity
@Table(name = "SeatLock",
       uniqueConstraints = @UniqueConstraint(columnNames = {"GheNgoiId", "LichChieuId"}))
@Data
public class SeatLock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Long id;

    @Column(name = "GheNgoiId", nullable = false)
    private Long gheNgoiId;

    @Column(name = "LichChieuId", nullable = false)
    private Long lichChieuId;

    /** The user who holds the lock. */
    @Column(name = "NguoiDungId")
    private Long nguoiDungId;

    @Column(name = "LockedAt", nullable = false)
    private LocalDateTime lockedAt;

    @Column(name = "ExpiresAt", nullable = false)
    private LocalDateTime expiresAt;

    /** Set when the booking is created — used for bulk release on cancel/confirm. */
    @Column(name = "MaDatVe", length = 20)
    private String maDatVe;

    @PrePersist
    protected void onCreate() {
        if (lockedAt == null) lockedAt = LocalDateTime.now();
    }
}
