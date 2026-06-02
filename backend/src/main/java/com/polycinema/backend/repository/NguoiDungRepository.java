package com.polycinema.backend.repository;

import com.polycinema.backend.entity.NguoiDung;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NguoiDungRepository
        extends JpaRepository<NguoiDung, Long> {

    Optional<NguoiDung> findByEmail(String email);

    Optional<NguoiDung> findByGoogleId(String googleId);

    Optional<NguoiDung> findBySoDienThoai(String soDienThoai);
}