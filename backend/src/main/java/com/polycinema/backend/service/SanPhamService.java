package com.polycinema.backend.service;

import com.polycinema.backend.entity.SanPham;
import com.polycinema.backend.repository.SanPhamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SanPhamService {

    private final SanPhamRepository sanPhamRepository;

    public List<SanPham> getAllActive() {
        return sanPhamRepository.findByDangHoatDongTrue();
    }

    public List<SanPham> getByLoai(String loaiSanPham) {
        return sanPhamRepository.findByLoaiSanPhamAndDangHoatDongTrue(loaiSanPham);
    }

    // ── Admin methods ─────────────────────────────────────────

    public List<SanPham> findAll() {
        return sanPhamRepository.findAll();
    }

    public java.util.Optional<SanPham> findById(Long id) {
        return sanPhamRepository.findById(id);
    }

    public SanPham save(SanPham sp) {
        return sanPhamRepository.save(sp);
    }
}
