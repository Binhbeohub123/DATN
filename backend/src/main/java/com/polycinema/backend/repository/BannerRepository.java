package com.polycinema.backend.repository;

import com.polycinema.backend.entity.Banner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface BannerRepository
        extends JpaRepository<Banner, Long> {

    List<Banner> findByDangHoatDongTrueOrderByThuTuAsc();

    List<Banner> findAllByOrderByThuTuAsc();

    /**
     * Returns banners that are active AND within their date window.
     * Null start/end dates mean "no restriction on that side".
     */
    @Query("SELECT b FROM Banner b WHERE b.dangHoatDong = true " +
           "AND (b.ngayBatDau IS NULL OR b.ngayBatDau <= :today) " +
           "AND (b.ngayKetThuc IS NULL OR b.ngayKetThuc >= :today) " +
           "ORDER BY b.thuTu ASC")
    List<Banner> findActiveBannersForDate(@Param("today") LocalDate today);
}
