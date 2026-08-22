package com.polycinema.backend.service;

import com.polycinema.backend.entity.PhongChieu;
import com.polycinema.backend.entity.RapChieu;
import com.polycinema.backend.repository.PhongChieuRepository;
import com.polycinema.backend.repository.RapChieuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RapChieuService {

    private final RapChieuRepository rapChieuRepository;
    private final PhongChieuRepository phongChieuRepository;

    public List<RapChieu> getAllActive() {
        return rapChieuRepository.findByTrangThaiTrue();
    }

    public List<String> getDistinctCities() {
        return rapChieuRepository.findDistinctCities();
    }

    public Optional<RapChieu> getById(Long id) {
        return rapChieuRepository.findById(id);
    }

    public List<PhongChieu> getPhongActiveByRapId(Long rapId) {
        return phongChieuRepository.findByRapChieuIdAndTrangThaiTrue(rapId);
    }

    // ── Admin methods ─────────────────────────────────────────

    public List<RapChieu> findAll() {
        return rapChieuRepository.findAll();
    }

    public RapChieu save(RapChieu rap) {
        return rapChieuRepository.save(rap);
    }
}
