package com.polycinema.backend.repository;

import com.polycinema.backend.entity.ChiTietDatSanPham;
import com.polycinema.backend.entity.ChiTietDatSanPhamId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChiTietDatSanPhamRepository
        extends JpaRepository<ChiTietDatSanPham, ChiTietDatSanPhamId> {

    List<ChiTietDatSanPham> findByDatVeId(Long datVeId);
}
