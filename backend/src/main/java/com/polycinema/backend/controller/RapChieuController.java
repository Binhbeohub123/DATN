package com.polycinema.backend.controller;

import com.polycinema.backend.entity.PhongChieu;
import com.polycinema.backend.entity.RapChieu;
import com.polycinema.backend.repository.PhongChieuRepository;
import com.polycinema.backend.repository.RapChieuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/rap-chieu")
@RequiredArgsConstructor
public class RapChieuController {

    private final RapChieuRepository rapChieuRepository;
    private final PhongChieuRepository phongChieuRepository;

    /**
     * GET /api/rap-chieu — public
     * Trả về danh sách rạp đang hoạt động.
     */
    @GetMapping
    public ResponseEntity<List<RapChieu>> getAllRap() {
        List<RapChieu> raps = rapChieuRepository.findByTrangThaiTrue();
        return ResponseEntity.ok(raps);
    }

    /**
     * GET /api/rap-chieu/cities — public
     * Distinct non-null city names from active cinemas, sorted A-Z.
     * Used to populate the city-selector dropdown on the movie detail page.
     * Returns only cities where at least one active cinema is present.
     */
    @GetMapping("/cities")
    public ResponseEntity<List<String>> getCities() {
        return ResponseEntity.ok(rapChieuRepository.findDistinctCities());
    }

    /**
     * GET /api/rap-chieu/{id} — public
     * Trả về chi tiết một rạp chiếu theo id.
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getRapById(@PathVariable Long id) {
        return rapChieuRepository.findById(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * GET /api/rap-chieu/{id}/phong — public
     * Trả về danh sách phòng chiếu đang hoạt động của một rạp.
     */
    @GetMapping("/{id}/phong")
    public ResponseEntity<?> getPhongByRap(@PathVariable Long id) {
        Optional<RapChieu> rapOpt = rapChieuRepository.findById(id);
        if (rapOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        List<PhongChieu> phongs = phongChieuRepository.findByRapChieuIdAndTrangThaiTrue(id);
        return ResponseEntity.ok(phongs);
    }
}
