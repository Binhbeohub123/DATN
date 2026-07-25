package com.polycinema.backend.repository;

import com.polycinema.backend.entity.KhuyenMai;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;
import java.util.List;

public interface KhuyenMaiRepository
        extends JpaRepository<KhuyenMai, Long> {

    Optional<KhuyenMai> findByMaKhuyenMai(String maKhuyenMai);

    List<KhuyenMai> findByDangHoatDongTrue();

    /**
     * Active promotions whose date range includes today.
     * Used by GET /api/khuyen-mai/active — enriched with phims list.
     */
    @Query("""
           SELECT DISTINCT km FROM KhuyenMai km
           LEFT JOIN FETCH km.phims
           WHERE km.dangHoatDong = true
             AND (km.ngayBatDau  IS NULL OR km.ngayBatDau  <= :today)
             AND (km.ngayKetThuc IS NULL OR km.ngayKetThuc >= :today)
           ORDER BY km.ngayKetThuc ASC
           """)
    List<KhuyenMai> findActiveWithPhims(@Param("today") LocalDate today);

    /**
     * ALL promotions (any status) with phims eagerly fetched in a single join.
     * Used by GET /api/khuyen-mai/all (admin list) so the phims relation is
     * always populated — avoids the EAGER N+1 / closed-session empty-list bug
     * that occurs when using plain findAll() in a stateless REST context.
     */
    @Query("SELECT DISTINCT km FROM KhuyenMai km LEFT JOIN FETCH km.phims ORDER BY km.id DESC")
    List<KhuyenMai> findAllWithPhims();
}
