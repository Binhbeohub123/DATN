package com.polycinema.backend.repository;

import com.polycinema.backend.entity.SeatLock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SeatLockRepository extends JpaRepository<SeatLock, Long> {

    /** Check if a specific seat is currently locked for a showtime. */
    Optional<SeatLock> findByGheNgoiIdAndLichChieuIdAndExpiresAtAfter(
            Long gheNgoiId, Long lichChieuId, LocalDateTime now);

    /** All active locks for a showtime — used by seat-map endpoint. */
    List<SeatLock> findAllByLichChieuIdAndExpiresAtAfter(
            Long lichChieuId, LocalDateTime now);

    /** All locks that have already expired — used by cleanup scheduler. */
    List<SeatLock> findAllByExpiresAtBefore(LocalDateTime now);

    /** Release a single seat lock. */
    @Modifying
    @Query("DELETE FROM SeatLock s WHERE s.gheNgoiId = :gheId AND s.lichChieuId = :lichId")
    void deleteByGheNgoiIdAndLichChieuId(
            @Param("gheId") Long gheNgoiId,
            @Param("lichId") Long lichChieuId);

    /** Release all seat locks belonging to a booking (on cancel or confirm). */
    @Modifying
    @Query("DELETE FROM SeatLock s WHERE s.maDatVe = :maDatVe")
    void deleteByMaDatVe(@Param("maDatVe") String maDatVe);

    /** All active locks for a showtime — with user info for admin view. */
    @Query("SELECT s FROM SeatLock s WHERE s.lichChieuId = :lichId AND s.expiresAt > :now")
    List<SeatLock> findActiveByLichChieu(
            @Param("lichId") Long lichChieuId,
            @Param("now") LocalDateTime now);

    /**
     * Active locked gheNgoiIds for a showtime, EXCLUDING a specific user's own locks.
     * Used by the locked-seats endpoint so the lock owner sees their seats as
     * "selected" (cyan) rather than "locked by other" (amber).
     */
    @Query("SELECT s.gheNgoiId FROM SeatLock s " +
           "WHERE s.lichChieuId = :lichChieuId " +
           "  AND s.expiresAt > :now " +
           "  AND s.nguoiDungId != :excludeUserId")
    List<Long> findGheNgoiIdsByLichChieuIdExcludingUser(
            @Param("lichChieuId")   Long lichChieuId,
            @Param("now")           LocalDateTime now,
            @Param("excludeUserId") Long excludeUserId);
}
