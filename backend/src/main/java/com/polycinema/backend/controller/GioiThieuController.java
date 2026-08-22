package com.polycinema.backend.controller;

import com.polycinema.backend.entity.GioiThieu;
import com.polycinema.backend.service.GioiThieuService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST controller for the singleton "Giới Thiệu" (About) content block.
 *
 * Public:
 *   GET  /api/gioi-thieu   — returns content (or in-memory defaults if row absent)
 *
 * ADMIN:
 *   PUT  /api/gioi-thieu   — upsert: update existing row or insert with id=1
 */
@RestController
@RequestMapping("/api/gioi-thieu")
@RequiredArgsConstructor
public class GioiThieuController {

    private final GioiThieuService gioiThieuService;

    /** GET /api/gioi-thieu — public */
    @GetMapping
    public ResponseEntity<GioiThieu> get() {
        return ResponseEntity.ok(gioiThieuService.get());
    }

    /** PUT /api/gioi-thieu — ADMIN */
    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GioiThieu> update(@RequestBody Map<String, String> body) {
        return ResponseEntity.ok(gioiThieuService.update(body));
    }
}
