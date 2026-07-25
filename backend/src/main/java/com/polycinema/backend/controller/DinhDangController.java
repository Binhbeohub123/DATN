package com.polycinema.backend.controller;

import com.polycinema.backend.entity.DinhDang;
import com.polycinema.backend.repository.DinhDangRepository;
import com.polycinema.backend.repository.PhimRepository;
import com.polycinema.backend.repository.PhongChieuRepository;
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

    private final DinhDangRepository dinhDangRepository;
    private final PhimRepository phimRepository;
    private final PhongChieuRepository phongChieuRepository;

    /** GET /api/dinh-dang — public */
    @GetMapping
    public ResponseEntity<List<DinhDang>> getAll() {
        return ResponseEntity.ok(dinhDangRepository.findAllByOrderByTenDinhDangAsc());
    }

    /** POST /api/dinh-dang — ADMIN */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> create(@RequestBody Map<String, String> body) {
        String ten = body.get("tenDinhDang");
        if (ten == null || ten.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "tenDinhDang không được để trống"));
        }
        if (dinhDangRepository.findByTenDinhDang(ten.trim()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "Định dạng '" + ten.trim() + "' đã tồn tại"));
        }
        DinhDang dd = new DinhDang();
        dd.setTenDinhDang(ten.trim());
        return ResponseEntity.status(HttpStatus.CREATED).body(dinhDangRepository.save(dd));
    }

    /** PUT /api/dinh-dang/{id} — ADMIN */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Map<String, String> body) {
        DinhDang dd = dinhDangRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy định dạng id=" + id));
        String ten = body.get("tenDinhDang");
        if (ten == null || ten.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "tenDinhDang không được để trống"));
        }
        dd.setTenDinhDang(ten.trim());
        return ResponseEntity.ok(dinhDangRepository.save(dd));
    }

    /**
     * DELETE /api/dinh-dang/{id} — ADMIN
     *
     * Blocks deletion with 409 Conflict if this format is still referenced by:
     *   - any Phim row (via Phim_DinhDang — which has ON DELETE CASCADE, so deletion
     *     would silently strip the format from those movies with no warning)
     *   - any PhongChieu row (via PhongChieu.DinhDangId — plain FK with no cascade,
     *     so deletion would throw a raw DB constraint error → 500)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        if (!dinhDangRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        long phimCount  = phimRepository.countByDinhDangId(id);
        long phongCount = phongChieuRepository.countByDinhDangId(id);

        if (phimCount > 0 || phongCount > 0) {
            String detail = buildReferenceDetail(phimCount, phongCount);
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message",
                            "Không thể xóa: định dạng này đang được sử dụng bởi " + detail));
        }

        dinhDangRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Đã xóa định dạng"));
    }

    private String buildReferenceDetail(long phimCount, long phongCount) {
        if (phimCount > 0 && phongCount > 0) {
            return phimCount + " phim và " + phongCount + " phòng chiếu";
        }
        if (phimCount > 0) {
            return phimCount + " phim";
        }
        return phongCount + " phòng chiếu";
    }
}
