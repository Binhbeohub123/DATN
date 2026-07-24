package com.polycinema.backend.repository;

import com.polycinema.backend.entity.RapChieu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RapChieuRepository
        extends JpaRepository<RapChieu, Long> {

    List<RapChieu> findByTrangThaiTrue();

    /**
     * Distinct non-null, non-empty city names from active cinemas.
     * Used by GET /api/rap-chieu/cities to populate the city dropdown.
     * Returns sorted alphabetically.
     */
    @Query("SELECT DISTINCT r.thanhPho FROM RapChieu r " +
           "WHERE r.trangThai = true AND r.thanhPho IS NOT NULL AND r.thanhPho <> '' " +
           "ORDER BY r.thanhPho ASC")
    List<String> findDistinctCities();
}
