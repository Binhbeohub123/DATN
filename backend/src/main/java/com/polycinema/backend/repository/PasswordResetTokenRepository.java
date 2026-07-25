package com.polycinema.backend.repository;

import com.polycinema.backend.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordResetTokenRepository
        extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByTokenAndDaSuDungFalse(String token);

    void deleteByNguoiDungId(Long nguoiDungId);
}
