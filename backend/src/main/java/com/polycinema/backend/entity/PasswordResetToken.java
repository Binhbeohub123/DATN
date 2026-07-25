package com.polycinema.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "PasswordResetToken")
@Data
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "NguoiDungId")
    private NguoiDung nguoiDung;

    @Column(name = "Token", nullable = false, unique = true, length = 255)
    private String token;

    @Column(name = "NgayTao")
    private LocalDateTime ngayTao;

    @Column(name = "NgayHetHan", nullable = false)
    private LocalDateTime ngayHetHan;

    @Column(name = "DaSuDung")
    private Boolean daSuDung;

    @PrePersist
    protected void onCreate() {
        if (ngayTao == null) ngayTao = LocalDateTime.now();
        if (daSuDung == null) daSuDung = false;
    }
}
