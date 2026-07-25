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
public class BannerController {

    private final BannerRepository bannerRepository;

    // GET /api/banner — public, trả về tất cả banner đang hoạt động trong khoảng ngày hiện tại
    @GetMapping
    public ResponseEntity<List<Banner>> getActiveBanners() {
        List<Banner> banners = bannerRepository.findActiveBannersForDate(java.time.LocalDate.now());
        return ResponseEntity.ok(banners);
    }
}
