package com.polycinema.backend.repository;

import com.polycinema.backend.entity.DatVe;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface DatVeRepository
        extends JpaRepository<DatVe, Long> {

    Optional<DatVe> findByMaDatVe(String maDatVe);

    Optional<DatVe> findByMaQR(String maQR);

    /**
     * History list for a user.
     * chiTietDatGhe and chiTietDatSanPham are EAGER on DatVe — loaded automatically.
     */
    List<DatVe> findByNguoiDungIdOrderByIdDesc(Long nguoiDungId);

    /**
     * Single booking detail — same as findById, EAGER fetch handles collections.
     */
    default Optional<DatVe> findByIdWithDetails(Long id) {
        return findById(id);
    }

    List<DatVe> findByNguoiDungIdAndTrangThai(Long nguoiDungId, String trangThai);

    List<DatVe> findByLichChieuId(Long lichChieuId);

    // ── Admin paginated search ────────────────────────────────────
    /**
     * Search across maDatVe, user email/name, and movie title.
     * trangThai = null or blank → match all statuses.
     */
    @Query("""
           SELECT dv FROM DatVe dv
           LEFT JOIN dv.nguoiDung u
           LEFT JOIN dv.lichChieu lc
           LEFT JOIN lc.phim p
           WHERE (:trangThai IS NULL OR :trangThai = '' OR dv.trangThai = :trangThai)
             AND (
               :q IS NULL OR :q = ''
               OR LOWER(dv.maDatVe)        LIKE LOWER(CONCAT('%', :q, '%'))
               OR LOWER(u.email)           LIKE LOWER(CONCAT('%', :q, '%'))
               OR LOWER(u.hoTen)           LIKE LOWER(CONCAT('%', :q, '%'))
               OR LOWER(p.tenPhim)         LIKE LOWER(CONCAT('%', :q, '%'))
             )
           ORDER BY dv.ngayTao DESC NULLS LAST
           """)
    Page<DatVe> searchAdmin(
            @Param("q") String q,
            @Param("trangThai") String trangThai,
            Pageable pageable);

    // ── Auto-expiry query ─────────────────────────────────────────
    /**
     * Finds bookings that were created before `cutoff` and are still unpaid.
     * Used by BookingExpiryService to auto-cancel stale bookings.
     */
    @Query("SELECT dv FROM DatVe dv " +
           "WHERE dv.trangThaiThanhToan = 'unpaid' " +
           "  AND dv.trangThai NOT IN ('cancelled', 'confirmed') " +
           "  AND dv.ngayTao < :cutoff")
    List<DatVe> findExpiredUnpaidBookings(@Param("cutoff") LocalDateTime cutoff);
}
