package com.polycinema.backend.repository;

import com.polycinema.backend.entity.ThanhToan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ThanhToanRepository
        extends JpaRepository<ThanhToan, Long> {

    List<ThanhToan> findByDatVeId(Long datVeId);

    List<ThanhToan> findByTrangThai(String trangThai);
}
