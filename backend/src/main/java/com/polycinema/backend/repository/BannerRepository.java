package com.polycinema.backend.repository;

import com.polycinema.backend.entity.Banner;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BannerRepository
        extends JpaRepository<Banner, Long> {

    List<Banner> findByDangHoatDongTrueOrderByThuTuAsc();
}
