package com.polycinema.backend.repository;

import com.polycinema.backend.entity.LichChieu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface LichChieuRepository
        extends JpaRepository<LichChieu, Long> {

    List<LichChieu> findByPhimIdAndTrangThaiAndIsDeletedFalse(Long phimId, String trangThai);

    List<LichChieu> findByPhongChieuIdAndIsDeletedFalse(Long phongChieuId);

    @Query("SELECT DISTINCT lc FROM LichChieu lc " +
           "LEFT JOIN FETCH lc.phongChieu " +
           "WHERE lc.phim.id = :phimId AND lc.isDeleted = false " +
           "AND lc.thoiGianBatDau > :after " +
           "ORDER BY lc.thoiGianBatDau ASC")
    List<LichChieu> findByPhimIdAndIsDeletedFalseAndThoiGianBatDauAfter(
            @Param("phimId") Long phimId,
            @Param("after") LocalDateTime after);
}
