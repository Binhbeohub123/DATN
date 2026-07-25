package com.polycinema.backend.repository;

import com.polycinema.backend.entity.DanhGiaPhim;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DanhGiaPhimRepository
        extends JpaRepository<DanhGiaPhim, Long> {

    List<DanhGiaPhim> findByPhimId(Long phimId);

    Optional<DanhGiaPhim> findByPhimIdAndNguoiDungId(Long phimId, Long nguoiDungId);
}
