package com.polycinema.backend.repository;

import com.polycinema.backend.entity.DatVe;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DatVeRepository
        extends JpaRepository<DatVe, Long> {

    Optional<DatVe> findByMaDatVe(String maDatVe);

    List<DatVe> findByNguoiDungId(Long nguoiDungId);

    List<DatVe> findByNguoiDungIdAndTrangThai(Long nguoiDungId, String trangThai);

    List<DatVe> findByLichChieuId(Long lichChieuId);
}
