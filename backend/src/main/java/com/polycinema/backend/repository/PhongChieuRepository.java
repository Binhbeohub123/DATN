package com.polycinema.backend.repository;

import com.polycinema.backend.entity.PhongChieu;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PhongChieuRepository
        extends JpaRepository<PhongChieu, Long> {

    List<PhongChieu> findByRapChieuIdAndTrangThaiTrue(Long rapChieuId);

    List<PhongChieu> findByRapChieuId(Long rapChieuId);

    /** Used by DinhDangController to guard DELETE — checks if any room references this format. */
    long countByDinhDangId(Long dinhDangId);
}
