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
@CrossOrigin(origins = "http://localhost:5173")
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
