package com.polycinema.backend.repository;

import com.polycinema.backend.entity.NguoiDung;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface NguoiDungRepository
        extends JpaRepository<NguoiDung, Long> {

    Optional<NguoiDung> findByEmail(String email);

    Optional<NguoiDung> findByGoogleId(String googleId);

    Optional<NguoiDung> findBySoDienThoai(String soDienThoai);

    // ── Admin paginated search ────────────────────────────────────
    /**
     * Search by email, name, or phone number. q = null/blank → all users.
     */
    @Query("""
           SELECT u FROM NguoiDung u
           WHERE :q IS NULL OR :q = ''
              OR LOWER(u.email)        LIKE LOWER(CONCAT('%', :q, '%'))
              OR LOWER(u.hoTen)        LIKE LOWER(CONCAT('%', :q, '%'))
              OR u.soDienThoai         LIKE CONCAT('%', :q, '%')
           ORDER BY u.id DESC
           """)
    Page<NguoiDung> searchAdmin(@Param("q") String q, Pageable pageable);

    /** Count users by exact vaiTro — used to guard last-admin demotion. */
    long countByVaiTro(String vaiTro);

    /** Count ACTIVE users by vaiTro — used to guard locking the last remaining admin. */
    long countByVaiTroAndTrangThai(String vaiTro, Boolean trangThai);
}
