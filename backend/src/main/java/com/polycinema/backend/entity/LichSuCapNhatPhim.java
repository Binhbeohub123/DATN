package com.polycinema.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "LichSuCapNhatPhim")
@Data
public class LichSuCapNhatPhim {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PhimId")
    private Phim phim;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "NguoiSuaId")
    private NguoiDung nguoiSua;

    @Column(name = "TruongSua", nullable = false, length = 100)
    private String truongSua;

    @Column(name = "GiaTriCu", columnDefinition = "NVARCHAR(MAX)")
    private String giaTriCu;

    @Column(name = "GiaTriMoi", columnDefinition = "NVARCHAR(MAX)")
    private String giaTriMoi;

    @Column(name = "ThoiGian")
    private LocalDateTime thoiGian;

    @PrePersist
    protected void onCreate() {
        if (thoiGian == null) thoiGian = LocalDateTime.now();
    }
}
