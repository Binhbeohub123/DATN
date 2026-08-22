package com.polycinema.backend.controller;

import com.polycinema.backend.entity.DinhDang;
import com.polycinema.backend.service.DinhDangService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for movie/room format (DinhDang) lookup.
 *
 * Public:
 *   GET  /api/dinh-dang          — all formats, sorted A-Z
 *
 * ADMIN:
 *   POST   /api/dinh-dang        — create new format
 *   PUT    /api/dinh-dang/{id}   — rename existing format
 *   DELETE /api/dinh-dang/{id}   — hard-delete (blocked if still referenced)
 */
@RestController
@RequestMapping("/api/dinh-dang")
@RequiredArgsConstructor
public class DinhDangController {

    private final DinhDangService dinhDangService;

    /** GET /api/dinh-dang — public */
    @GetMapping
    public ResponseEntity<List<DinhDang>> getAll() {
        return ResponseEntity.ok(dinhDangService.getAll());
    }

    /** POST /api/dinh-dang — ADMIN */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> create(@RequestBody Map<String, String> body) {
        try {
            DinhDang dd = dinhDangService.create(body.get("tenDinhDang"));
            return ResponseEntity.status(HttpStatus.CREATED).body(dd);
        } catch (IllegalArgumentException e) {
            String msg = e.getMessage();
            if (msg.contains("đã tồn tại")) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Map.of("message", msg));
            }
            return ResponseEntity.badRequest().body(Map.of("message", msg));
        }
    }

    /** PUT /api/dinh-dang/{id} — ADMIN */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Map<String, String> body) {
        try {
            return ResponseEntity.ok(dinhDangService.update(id, body.get("tenDinhDang")));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    /** DELETE /api/dinh-dang/{id} — ADMIN */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            dinhDangService.delete(id);
            return ResponseEntity.ok(Map.of("message", "Đã xóa định dạng"));
        } catch (IllegalArgumentException e) {
            String msg = e.getMessage();
            if (msg.contains("Không tìm thấy")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", msg));
        }
    }
}
