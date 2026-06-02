package com.polycinema.backend.repository;

import com.polycinema.backend.entity.SanPham;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SanPhamRepository
        extends JpaRepository<SanPham, Long> {

    List<SanPham> findByDangHoatDongTrue();

    List<SanPham> findByLoaiSanPhamAndDangHoatDongTrue(String loaiSanPham);
}
