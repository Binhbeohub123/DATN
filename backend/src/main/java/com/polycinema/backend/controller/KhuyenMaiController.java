package com.polycinema.backend.controller;

import com.polycinema.backend.entity.KhuyenMai;
import com.polycinema.backend.service.KhuyenMaiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/khuyen-mai")
@RequiredArgsConstructor
public class KhuyenMaiController {

    private final KhuyenMaiService khuyenMaiService;

    /** POST /api/khuyen-mai/validate — authenticated */
    @PostMapping("/validate")
    public ResponseEntity<?> validate(@RequestBody Map<String, Object> req) {
        String maKhuyenMai = (String) req.get("maKhuyenMai");
        BigDecimal tongTien = BigDecimal.ZERO;
        Object tongTienRaw = req.get("tongTien");
        if (tongTienRaw != null) {
            try { tongTien = new BigDecimal(tongTienRaw.toString()); }
            catch (NumberFormatException ignored) {}
        }
        Long userId = getUserIdFromToken();
        return ResponseEntity.ok(khuyenMaiService.validate(maKhuyenMai, tongTien, userId));
    }

    /** GET /api/khuyen-mai — public */
    @GetMapping
    public ResponseEntity<List<KhuyenMai>> getAll() {
        return ResponseEntity.ok(khuyenMaiService.getAllActive());
    }

    /** GET /api/khuyen-mai/all — ADMIN */
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<KhuyenMai>> getAllAdmin() {
        return ResponseEntity.ok(khuyenMaiService.getAllWithPhims());
    }

    /** GET /api/khuyen-mai/active — public */
    @GetMapping("/active")
    public ResponseEntity<List<KhuyenMai>> getActive() {
        return ResponseEntity.ok(khuyenMaiService.getActiveWithPhims());
    }

    /** POST /api/khuyen-mai — ADMIN */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> create(@RequestBody Map<String, Object> body) {
        return ResponseEntity.status(HttpStatus.CREATED).body(khuyenMaiService.create(body));
    }

    /** PUT /api/khuyen-mai/{id} — ADMIN */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(khuyenMaiService.update(id, body));
    }

    /** DELETE /api/khuyen-mai/{id} — ADMIN */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        khuyenMaiService.deactivate(id);
        return ResponseEntity.ok(Map.of("message", "Đã vô hiệu hóa khuyến mãi"));
    }

    private Long getUserIdFromToken() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated()) {
                Object principal = auth.getPrincipal();
                String email = principal instanceof String ? (String) principal : null;
                if (email != null && !email.equals("anonymousUser")) {
                    return khuyenMaiService.getUserIdByEmail(email);
                }
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }
}
