package com.polycinema.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Entity
@Table(name = "SanPham")
@Data
public class SanPham {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Long id;

    @Column(name = "TenSanPham", nullable = false, length = 100)
    private String tenSanPham;

    @Column(name = "LoaiSanPham", length = 20)
    private String loaiSanPham;

    @Column(name = "MoTa", columnDefinition = "NVARCHAR(MAX)")
    private String moTa;

    @Column(name = "Gia", nullable = false, precision = 12, scale = 2)
    private BigDecimal gia;

    @Column(name = "TonKho")
    private Integer tonKho;

    @Column(name = "AnhUrl", columnDefinition = "NVARCHAR(MAX)")
    private String anhUrl;

    @Column(name = "DangHoatDong")
    private Boolean dangHoatDong;

    @PrePersist
    protected void onCreate() {
        if (loaiSanPham == null) loaiSanPham = "food";
        if (tonKho == null) tonKho = 0;
        if (dangHoatDong == null) dangHoatDong = true;
    }
}
