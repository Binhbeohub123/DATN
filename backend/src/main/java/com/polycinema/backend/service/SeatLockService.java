package com.polycinema.backend.service;

import com.polycinema.backend.entity.SeatLock;
import com.polycinema.backend.entity.GheNgoi;
import com.polycinema.backend.repository.GheNgoiRepository;
import com.polycinema.backend.repository.SeatLockRepository;
import com.polycinema.backend.repository.SystemConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SeatLockService {

    private static final String CONFIG_KEY   = "SEAT_LOCK_MINUTES";
    private static final int    DEFAULT_MINS = 10;

    private final SeatLockRepository seatLockRepository;
    private final GheNgoiRepository gheNgoiRepository;
    private final SystemConfigRepository systemConfigRepository;
    private final SeatNotificationService seatNotificationService;

    // ─────────────────────────────────────────────────────────────
    // Read lock duration from SystemConfig
    // ─────────────────────────────────────────────────────────────
    public int getSeatLockDuration() {
        return systemConfigRepository.findById(CONFIG_KEY)
                .map(c -> {
                    try { return Integer.parseInt(c.getConfigValue()); }
                    catch (NumberFormatException e) { return DEFAULT_MINS; }
                })
                .orElse(DEFAULT_MINS);
    }

    // ─────────────────────────────────────────────────────────────
    // Lock a seat for a user
    // ─────────────────────────────────────────────────────────────
    /**
     * Attempts to lock a seat for the given showtime.
     *
     * @throws IllegalArgumentException if the seat is already locked by someone else.
     */
    @Transactional
    public SeatLock lockSeat(Long gheNgoiId, Long lichChieuId, Long nguoiDungId) {
        // 'trống' cells are aisles/gaps — never lockable
        GheNgoi ghe = gheNgoiRepository.findById(gheNgoiId)
                .orElseThrow(() -> new IllegalArgumentException("Ghế không tồn tại"));
        if ("trống".equals(ghe.getLoaiGhe())) {
            throw new IllegalArgumentException("Ghế ID " + gheNgoiId
                    + " là ô trống/lối đi trong sơ đồ, không thể chọn");
        }

        LocalDateTime now = LocalDateTime.now();

        // Check for an existing active lock
        seatLockRepository
                .findByGheNgoiIdAndLichChieuIdAndExpiresAtAfter(gheNgoiId, lichChieuId, now)
                .ifPresent(existing -> {
                    if (!existing.getNguoiDungId().equals(nguoiDungId)) {
                        throw new IllegalArgumentException("Ghế này đang được giữ bởi người dùng khác");
                    }
                    // Same user re-locking — delete old lock so we create a fresh one below
                    seatLockRepository.deleteByGheNgoiIdAndLichChieuId(gheNgoiId, lichChieuId);
                });

        int lockMinutes = getSeatLockDuration();

        SeatLock lock = new SeatLock();
        lock.setGheNgoiId(gheNgoiId);
        lock.setLichChieuId(lichChieuId);
        lock.setNguoiDungId(nguoiDungId);
        lock.setLockedAt(now);
        lock.setExpiresAt(now.plusMinutes(lockMinutes));

        SeatLock saved = seatLockRepository.save(lock);
        if (saved.getExpiresAt() != null) {
            long epochMs = saved.getExpiresAt().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();
            seatNotificationService.broadcastSeatLocked(lichChieuId, gheNgoiId, epochMs);
        }
        return saved;
    }

    // ─────────────────────────────────────────────────────────────
    // Release a single seat lock
    // ─────────────────────────────────────────────────────────────
    @Transactional
    public void releaseSeat(Long gheNgoiId, Long lichChieuId) {
        seatLockRepository.deleteByGheNgoiIdAndLichChieuId(gheNgoiId, lichChieuId);
        seatNotificationService.broadcastSeatUnlocked(lichChieuId, gheNgoiId);
    }

    // ─────────────────────────────────────────────────────────────
    // Release all locks for a booking
    // ─────────────────────────────────────────────────────────────
    @Transactional
    public void releaseSeatsByBooking(String maDatVe) {
        if (maDatVe != null && !maDatVe.isBlank()) {
            seatLockRepository.deleteByMaDatVe(maDatVe);
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Tag all seat locks with a booking code (called after booking created)
    // ─────────────────────────────────────────────────────────────
    @Transactional
    public void confirmSeatLocks(List<Long> gheNgoiIds, Long lichChieuId, String maDatVe) {
        LocalDateTime now = LocalDateTime.now();
        for (Long gheId : gheNgoiIds) {
            seatLockRepository
                    .findByGheNgoiIdAndLichChieuIdAndExpiresAtAfter(gheId, lichChieuId, now)
                    .ifPresent(lock -> {
                        lock.setMaDatVe(maDatVe);
                        seatLockRepository.save(lock);
                    });
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Get locked seat IDs for a showtime (public seat-map use)
    // ─────────────────────────────────────────────────────────────
    /**
     * Returns all active locked gheNgoiIds for a showtime.
     * Used when no user context is available (public/unauthenticated).
     */
    public List<Long> getLockedSeatIds(Long lichChieuId) {
        return seatLockRepository
                .findAllByLichChieuIdAndExpiresAtAfter(lichChieuId, LocalDateTime.now())
                .stream()
                .map(SeatLock::getGheNgoiId)
                .collect(Collectors.toList());
    }

    /**
     * Returns active locked gheNgoiIds for a showtime, EXCLUDING the current user's
     * own locks so their seats show as "selected" (cyan) rather than "locked by other"
     * (amber) when they navigate back to the seat map.
     *
     * @param excludeUserId if non-null, that user's locks are excluded from the result
     */
    public List<Long> getLockedSeatIds(Long lichChieuId, Long excludeUserId) {
        if (excludeUserId != null) {
            return seatLockRepository.findGheNgoiIdsByLichChieuIdExcludingUser(
                    lichChieuId, LocalDateTime.now(), excludeUserId);
        }
        return getLockedSeatIds(lichChieuId);
    }

    /**
     * Active locks currently held BY the given user on a showtime, so the frontend
     * can restore them as "selected" (cyan) with their remaining countdown after
     * a page reload. Returns entries of { gheNgoiId, expiresAt }.
     */
    public List<Map<String, Object>> getMyActiveLocks(Long lichChieuId, Long nguoiDungId) {
        if (nguoiDungId == null) return List.of();
        return seatLockRepository
                .findByLichChieuIdAndNguoiDungIdAndExpiresAtAfter(lichChieuId, nguoiDungId, LocalDateTime.now())
                .stream()
                .map(l -> Map.<String, Object>of(
                        "gheNgoiId", l.getGheNgoiId(),
                        "expiresAt", l.getExpiresAt().truncatedTo(java.time.temporal.ChronoUnit.MILLIS).toString()))
                .collect(Collectors.toList());
    }

    // ── Admin methods ─────────────────────────────────────────

    public List<SeatLock> findActiveByLichChieu(Long lichChieuId) {
        return seatLockRepository.findActiveByLichChieu(lichChieuId, LocalDateTime.now());
    }

    public boolean existsById(Long id) {
        return seatLockRepository.existsById(id);
    }

    @Transactional
    public void deleteById(Long id) {
        seatLockRepository.findById(id).ifPresent(lock -> {
            seatNotificationService.broadcastSeatUnlocked(lock.getLichChieuId(), lock.getGheNgoiId());
            seatLockRepository.deleteById(id);
        });
    }
}
