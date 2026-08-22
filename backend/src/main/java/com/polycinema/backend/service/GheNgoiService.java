package com.polycinema.backend.service;

import com.polycinema.backend.entity.GheNgoi;
import com.polycinema.backend.repository.GheNgoiRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GheNgoiService {

    private final GheNgoiRepository gheNgoiRepository;

    public List<GheNgoi> findByPhongChieuId(Long phongChieuId) {
        return gheNgoiRepository.findByPhongChieuId(phongChieuId);
    }

    public Optional<GheNgoi> findById(Long id) {
        return gheNgoiRepository.findById(id);
    }

    public long countByPhongChieuId(Long phongChieuId) {
        return gheNgoiRepository.countByPhongChieuId(phongChieuId);
    }

    public GheNgoi save(GheNgoi ghe) {
        return gheNgoiRepository.save(ghe);
    }

    public void delete(GheNgoi ghe) {
        gheNgoiRepository.delete(ghe);
    }

    public void deleteAll(Iterable<GheNgoi> ghes) {
        gheNgoiRepository.deleteAll(ghes);
    }
}
