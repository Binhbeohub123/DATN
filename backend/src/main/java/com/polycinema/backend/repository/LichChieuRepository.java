package com.polycinema.backend.repository;

import com.polycinema.backend.entity.LichChieu;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface LichChieuRepository
        extends JpaRepository<LichChieu, Long> {

    List<LichChieu> findByPhimIdAndTrangThaiAndIsDeletedFalse(Long phimId, String trangThai);

    List<LichChieu> findByPhongChieuIdAndIsDeletedFalse(Long phongChieuId);

    @Query("SELECT DISTINCT lc FROM LichChieu lc " +
           "LEFT JOIN FETCH lc.phongChieu pc " +
           "LEFT JOIN FETCH pc.rapChieu rc " +
           "WHERE lc.phim.id = :phimId AND lc.isDeleted = false " +
           "AND pc.trangThai = true AND rc.trangThai = true " +
           "AND lc.thoiGianBatDau > :after " +
           "ORDER BY lc.thoiGianBatDau ASC")
    List<LichChieu> findByPhimIdAndIsDeletedFalseAndThoiGianBatDauAfter(
            @Param("phimId") Long phimId,
            @Param("after") LocalDateTime after);

    // ── Admin paginated ───────────────────────────────────────────
    /**
     * Returns active (non-deleted) schedules, optionally filtered by date range.
     * dateFrom / dateTo = null → no date filter on that bound.
     */
    @Query("""
           SELECT lc FROM LichChieu lc
           WHERE lc.isDeleted = false
             AND (:dateFrom IS NULL OR lc.thoiGianBatDau >= :dateFrom)
             AND (:dateTo   IS NULL OR lc.thoiGianBatDau <  :dateTo)
           ORDER BY lc.thoiGianBatDau DESC
           """)
    Page<LichChieu> findAdminPage(
            @Param("dateFrom") LocalDateTime dateFrom,
            @Param("dateTo")   LocalDateTime dateTo,
            Pageable pageable);

    // ── Public filtered search ────────────────────────────────────
    /**
     * GET /api/lich-chieu/search
     * Filters by phimId, city (thanhPho on RapChieu), dinhDangId (on PhongChieu),
     * and a 30-day window from now.
     * All params are optional — null means "no filter on this dimension".
     */
    @Query("""
           SELECT DISTINCT lc FROM LichChieu lc
           LEFT JOIN FETCH lc.phongChieu pc
           LEFT JOIN FETCH pc.rapChieu rc
           WHERE lc.isDeleted = false
             AND pc.trangThai = true
             AND rc.trangThai = true
             AND lc.thoiGianBatDau >= :from
             AND lc.thoiGianBatDau <= :to
             AND (:phimId     IS NULL OR lc.phim.id            = :phimId)
             AND (:thanhPho   IS NULL OR LOWER(rc.thanhPho)    = LOWER(:thanhPho))
             AND (:dinhDangId IS NULL OR pc.dinhDang.id        = :dinhDangId)
           ORDER BY lc.thoiGianBatDau ASC
           """)
    List<LichChieu> searchPublic(
            @Param("phimId")     Long phimId,
            @Param("thanhPho")   String thanhPho,
            @Param("dinhDangId") Long dinhDangId,
            @Param("from")       LocalDateTime from,
            @Param("to")         LocalDateTime to);
}
