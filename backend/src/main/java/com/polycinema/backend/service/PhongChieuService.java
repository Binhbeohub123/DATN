package com.polycinema.backend.service;

import com.polycinema.backend.entity.PhongChieu;
import com.polycinema.backend.repository.PhongChieuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PhongChieuService {

    private final PhongChieuRepository phongChieuRepository;

    public List<PhongChieu> findByRapChieuId(Long rapChieuId) {
        return phongChieuRepository.findByRapChieuId(rapChieuId);
    }

    public Optional<PhongChieu> findById(Long id) {
        return phongChieuRepository.findById(id);
    }

    public PhongChieu save(PhongChieu phong) {
        return phongChieuRepository.save(phong);
    }

    public List<PhongChieu> findAll() {
        return phongChieuRepository.findAll();
    }
}
