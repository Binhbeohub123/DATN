package com.polycinema.backend.controller;

import com.polycinema.backend.dto.TheLoaiDTO;
import com.polycinema.backend.entity.TheLoai;
import com.polycinema.backend.repository.TheLoaiRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    private final TheLoaiRepository theLoaiRepository;

    /** Public — returns all genres sorted alphabetically. */
    @GetMapping
    public ResponseEntity<List<TheLoaiDTO>> getAll() {
        List<TheLoaiDTO> result = theLoaiRepository.findAllByOrderByTenTheLoaiAsc()
                .stream()
                .map(TheLoaiDTO::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    /** ADMIN — create a new genre. */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> create(@RequestBody Map<String, String> body) {
        String name = body.get("tenTheLoai");
        if (name == null || name.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "tenTheLoai không được để trống"));
        }
        if (theLoaiRepository.findByTenTheLoai(name.trim()).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Thể loại đã tồn tại"));
        }
        TheLoai saved = theLoaiRepository.save(new TheLoai(null, name.trim()));
        return ResponseEntity.status(HttpStatus.CREATED).body(TheLoaiDTO.from(saved));
    }

    /** ADMIN — rename an existing genre. */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Map<String, String> body) {
        TheLoai tl = theLoaiRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thể loại id=" + id));
        String name = body.get("tenTheLoai");
        if (name == null || name.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "tenTheLoai không được để trống"));
        }
        tl.setTenTheLoai(name.trim());
        return ResponseEntity.ok(TheLoaiDTO.from(theLoaiRepository.save(tl)));
    }

    /** ADMIN — delete a genre (cascade handled by DB ON DELETE CASCADE on Phim_TheLoai). */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        if (!theLoaiRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        theLoaiRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Đã xóa thể loại"));
    }
}
