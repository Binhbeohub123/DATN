package com.polycinema.backend.controller;

import com.polycinema.backend.dto.TheLoaiDTO;
import com.polycinema.backend.service.TheLoaiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * GET  /api/the-loai         — public, returns all genres sorted alphabetically
 * POST /api/the-loai         — ADMIN only, create genre
 * PUT  /api/the-loai/{id}    — ADMIN only, rename genre
 * DELETE /api/the-loai/{id}  — ADMIN only, delete genre
 */
@RestController
@RequestMapping("/api/the-loai")
@RequiredArgsConstructor
public class TheLoaiController {

    private final TheLoaiService theLoaiService;

    /** Public — returns all genres sorted alphabetically. */
    @GetMapping
    public ResponseEntity<List<TheLoaiDTO>> getAll() {
        return ResponseEntity.ok(theLoaiService.getAll());
    }

    /** ADMIN — create a new genre. */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> create(@RequestBody Map<String, String> body) {
        try {
            TheLoaiDTO dto = theLoaiService.create(body.get("tenTheLoai"));
            return ResponseEntity.status(HttpStatus.CREATED).body(dto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    /** ADMIN — rename an existing genre. */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Map<String, String> body) {
        try {
            return ResponseEntity.ok(theLoaiService.update(id, body.get("tenTheLoai")));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    /** ADMIN — delete a genre (cascade handled by DB ON DELETE CASCADE on Phim_TheLoai). */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            theLoaiService.delete(id);
            return ResponseEntity.ok(Map.of("message", "Đã xóa thể loại"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
