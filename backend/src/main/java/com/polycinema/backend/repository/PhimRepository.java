package com.polycinema.backend.repository;

import com.polycinema.backend.entity.Phim;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PhimRepository extends JpaRepository<Phim, Long> {

    List<Phim> findByTrangThaiAndIsDeletedFalse(String trangThai);

    List<Phim> findByIsDeletedFalse();

    List<Phim> findByTenPhimContainingIgnoreCaseAndIsDeletedFalse(String tenPhim);

    /** Filter movies by genre — used by GET /api/phim?theLoaiId=X */
    @Query("SELECT DISTINCT p FROM Phim p JOIN p.theLoais t " +
           "WHERE t.id = :theLoaiId AND p.isDeleted = false")
    List<Phim> findByTheLoaiId(@Param("theLoaiId") Long theLoaiId);

    /** Top 10 now-showing movies ordered by rating DESC — GET /api/phim/noi-bat */
    @Query("SELECT p FROM Phim p WHERE p.trangThai = 'dang_chieu' AND p.isDeleted = false " +
           "ORDER BY p.diemDanhGia DESC")
    org.springframework.data.domain.Page<Phim> findTop10NowShowing(
            org.springframework.data.domain.Pageable pageable);

    /** Used by DinhDangController to guard DELETE — counts movies that have this format. */
    @Query("SELECT COUNT(p) FROM Phim p JOIN p.dinhDangs d WHERE d.id = :dinhDangId AND p.isDeleted = false")
    long countByDinhDangId(@Param("dinhDangId") Long dinhDangId);
}
