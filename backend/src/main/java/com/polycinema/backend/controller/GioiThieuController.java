package com.polycinema.backend.controller;

import com.polycinema.backend.entity.GioiThieu;
import com.polycinema.backend.repository.GioiThieuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST controller for the singleton "Giới Thiệu" (About) content block.
 *
 * The row always has id = 1 (enforced by DB CHECK constraint and set
 * explicitly here — entity has NO @GeneratedValue to avoid IDENTITY mismatch).
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

    private static final Long SINGLETON_ID = 1L;

    private static final String DEFAULT_TIEU_DE  = "HỆ THỐNG CỤM RẠP POLYCINEMA";
    private static final String DEFAULT_NOI_DUNG =
        "PolyCinema — hệ thống rạp chiếu phim hiện đại với chất lượng âm thanh và hình ảnh đỉnh cao.\n\n" +
        "Chúng tôi mang đến trải nghiệm điện ảnh đích thực tại nhiều tỉnh thành trên toàn quốc, " +
        "với các công nghệ chiếu phim tiên tiến như 2D, 3D, IMAX và 4DX.\n\n" +
        "Đặt vé nhanh chóng, tiện lợi — thưởng thức những bộ phim bom tấn ngay hôm nay cùng PolyCinema.";

    private final GioiThieuRepository gioiThieuRepository;

    /** GET /api/gioi-thieu — public */
    @GetMapping
    public ResponseEntity<GioiThieu> get() {
        return ResponseEntity.ok(
            gioiThieuRepository.findById(SINGLETON_ID).orElseGet(() -> {
                // Row not yet created — return an in-memory object with defaults
                // (not saved; first real save happens via PUT below)
                GioiThieu g = new GioiThieu();
                g.setId(SINGLETON_ID);
                g.setTieuDe(DEFAULT_TIEU_DE);
                g.setNoiDung(DEFAULT_NOI_DUNG);
                g.setHinhAnhUrl(null);
                return g;
            })
        );
    }

    /** PUT /api/gioi-thieu — ADMIN
     *
     * Always uses SINGLETON_ID (1). Works for both:
     * - First save (no row yet):  save() issues INSERT with id=1
     * - Subsequent saves (row exists): save() issues UPDATE
     */
    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GioiThieu> update(@RequestBody Map<String, String> body) {
        // Fetch existing or construct a fresh object — id MUST be 1 in both cases
        GioiThieu g = gioiThieuRepository.findById(SINGLETON_ID).orElseGet(() -> {
            GioiThieu n = new GioiThieu();
            n.setId(SINGLETON_ID);   // explicit — no IDENTITY, row must be id=1
            return n;
        });
        if (body.containsKey("tieuDe"))     g.setTieuDe(body.get("tieuDe"));
        if (body.containsKey("noiDung"))    g.setNoiDung(body.get("noiDung"));
        if (body.containsKey("hinhAnhUrl")) g.setHinhAnhUrl(body.get("hinhAnhUrl"));
        return ResponseEntity.ok(gioiThieuRepository.save(g));
    }
}
