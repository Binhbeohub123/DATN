package com.polycinema.backend.repository;

import com.polycinema.backend.entity.RapChieu;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RapChieuRepository
        extends JpaRepository<RapChieu, Long> {

    List<RapChieu> findByTrangThaiTrue();
}
