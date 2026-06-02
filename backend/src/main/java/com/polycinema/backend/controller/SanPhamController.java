package com.polycinema.backend.controller;

import com.polycinema.backend.entity.SanPham;
import com.polycinema.backend.repository.SanPhamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/san-pham")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class SanPhamController {

    private final SanPhamRepository sanPhamRepository;

    /**
     * GET /api/san-pham — public
     * Trả về toàn bộ sản phẩm đang hoạt động (combo, food, drink).
     */
    @GetMapping
    public ResponseEntity<List<SanPham>> getAllSanPham() {
        List<SanPham> sanPhams = sanPhamRepository.findByDangHoatDongTrue();
        return ResponseEntity.ok(sanPhams);
    }

    /**
     * GET /api/san-pham/combo — public
     * Trả về riêng sản phẩm loại "combo".
     */
    @GetMapping("/combo")
    public ResponseEntity<List<SanPham>> getCombo() {
        List<SanPham> combos = sanPhamRepository.findByLoaiSanPhamAndDangHoatDongTrue("combo");
        return ResponseEntity.ok(combos);
    }

    /**
     * GET /api/san-pham/food — public
     */
    @GetMapping("/food")
    public ResponseEntity<List<SanPham>> getFood() {
        List<SanPham> foods = sanPhamRepository.findByLoaiSanPhamAndDangHoatDongTrue("food");
        return ResponseEntity.ok(foods);
    }

    /**
     * GET /api/san-pham/drink — public
     */
    @GetMapping("/drink")
    public ResponseEntity<List<SanPham>> getDrink() {
        List<SanPham> drinks = sanPhamRepository.findByLoaiSanPhamAndDangHoatDongTrue("drink");
        return ResponseEntity.ok(drinks);
    }
}
