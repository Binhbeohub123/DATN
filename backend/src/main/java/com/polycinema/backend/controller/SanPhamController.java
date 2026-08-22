package com.polycinema.backend.controller;

import com.polycinema.backend.entity.SanPham;
import com.polycinema.backend.service.SanPhamService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/san-pham")
@RequiredArgsConstructor
public class SanPhamController {

    private final SanPhamService sanPhamService;

    /** GET /api/san-pham — public */
    @GetMapping
    public ResponseEntity<List<SanPham>> getAllSanPham() {
        return ResponseEntity.ok(sanPhamService.getAllActive());
    }

    /** GET /api/san-pham/combo — public */
    @GetMapping("/combo")
    public ResponseEntity<List<SanPham>> getCombo() {
        return ResponseEntity.ok(sanPhamService.getByLoai("combo"));
    }

    /** GET /api/san-pham/food — public */
    @GetMapping("/food")
    public ResponseEntity<List<SanPham>> getFood() {
        return ResponseEntity.ok(sanPhamService.getByLoai("food"));
    }

    /** GET /api/san-pham/drink — public */
    @GetMapping("/drink")
    public ResponseEntity<List<SanPham>> getDrink() {
        return ResponseEntity.ok(sanPhamService.getByLoai("drink"));
    }
}
