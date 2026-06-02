package com.polycinema.backend.repository;

import com.polycinema.backend.entity.LichSuCapNhatPhim;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LichSuCapNhatPhimRepository
        extends JpaRepository<LichSuCapNhatPhim, Long> {

    List<LichSuCapNhatPhim> findByPhimIdOrderByThoiGianDesc(Long phimId);
}
