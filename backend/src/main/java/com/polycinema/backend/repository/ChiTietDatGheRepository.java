package com.polycinema.backend.repository;

import com.polycinema.backend.entity.ChiTietDatGhe;
import com.polycinema.backend.entity.ChiTietDatGheId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChiTietDatGheRepository
        extends JpaRepository<ChiTietDatGhe, ChiTietDatGheId> {

    List<ChiTietDatGhe> findByDatVeId(Long datVeId);

    List<ChiTietDatGhe> findByLichChieuIdAndGheNgoiId(Long lichChieuId, Long gheNgoiId);

    List<ChiTietDatGhe> findByLichChieuId(Long lichChieuId);
}
