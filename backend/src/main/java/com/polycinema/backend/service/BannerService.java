package com.polycinema.backend.service;

import com.polycinema.backend.entity.Banner;
import com.polycinema.backend.repository.BannerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BannerService {

    private final BannerRepository bannerRepository;

    public List<Banner> findAllByOrderByThuTuAsc() {
        return bannerRepository.findAllByOrderByThuTuAsc();
    }

    public Optional<Banner> findById(Long id) {
        return bannerRepository.findById(id);
    }

    public Banner save(Banner banner) {
        return bannerRepository.save(banner);
    }

    public void deleteById(Long id) {
        bannerRepository.deleteById(id);
    }
}
