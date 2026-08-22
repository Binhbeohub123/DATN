package com.polycinema.backend.controller;

import com.polycinema.backend.entity.PhongChieu;
import com.polycinema.backend.entity.RapChieu;
import com.polycinema.backend.service.RapChieuService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rap-chieu")
@RequiredArgsConstructor
public class RapChieuController {

    private final RapChieuService rapChieuService;

    /** GET /api/rap-chieu — public */
    @GetMapping
    public ResponseEntity<List<RapChieu>> getAllRap() {
        return ResponseEntity.ok(rapChieuService.getAllActive());
    }

    /** GET /api/rap-chieu/cities — public */
    @GetMapping("/cities")
    public ResponseEntity<List<String>> getCities() {
        return ResponseEntity.ok(rapChieuService.getDistinctCities());
    }

    /** GET /api/rap-chieu/{id} — public */
    @GetMapping("/{id}")
    public ResponseEntity<?> getRapById(@PathVariable Long id) {
        return rapChieuService.getById(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /** GET /api/rap-chieu/{id}/phong — public */
    @GetMapping("/{id}/phong")
    public ResponseEntity<?> getPhongByRap(@PathVariable Long id) {
        if (rapChieuService.getById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        List<PhongChieu> phongs = rapChieuService.getPhongActiveByRapId(id);
        return ResponseEntity.ok(phongs);
    }
}
