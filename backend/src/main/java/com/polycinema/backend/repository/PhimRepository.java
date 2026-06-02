package com.polycinema.backend.repository;

import com.polycinema.backend.entity.Phim;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PhimRepository
        extends JpaRepository<Phim, Long> {

    List<Phim> findByTrangThaiAndIsDeletedFalse(String trangThai);

    List<Phim> findByIsDeletedFalse();

    List<Phim> findByTenPhimContainingIgnoreCaseAndIsDeletedFalse(String tenPhim);
}
