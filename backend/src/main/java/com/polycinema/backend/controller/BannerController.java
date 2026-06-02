package com.polycinema.backend.controller;

import com.polycinema.backend.entity.Banner;
import com.polycinema.backend.repository.BannerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/banner")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class BannerController {

    private final BannerRepository bannerRepository;

    // GET /api/banner — public, trả về tất cả banner đang hoạt động
    @GetMapping
    public ResponseEntity<List<Banner>> getActiveBanners() {
        List<Banner> banners = bannerRepository.findByDangHoatDongTrueOrderByThuTuAsc();
        return ResponseEntity.ok(banners);
    }
}
