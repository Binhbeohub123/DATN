package com.polycinema.backend.repository;

import com.polycinema.backend.entity.GheNgoi;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GheNgoiRepository
        extends JpaRepository<GheNgoi, Long> {

    List<GheNgoi> findByPhongChieuId(Long phongChieuId);

    List<GheNgoi> findByPhongChieuIdAndLoaiGhe(Long phongChieuId, String loaiGhe);

    long countByPhongChieuId(Long phongChieuId);
}
