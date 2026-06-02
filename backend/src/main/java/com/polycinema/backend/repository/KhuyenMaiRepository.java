package com.polycinema.backend.repository;

import com.polycinema.backend.entity.KhuyenMai;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface KhuyenMaiRepository
        extends JpaRepository<KhuyenMai, Long> {

    Optional<KhuyenMai> findByMaKhuyenMai(String maKhuyenMai);

    List<KhuyenMai> findByDangHoatDongTrue();
}
