package com.polycinema.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "Phim")
@Data
public class Phim {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Long id;

    @Column(name = "TenPhim", nullable = false, length = 255)
    private String tenPhim;

    @Column(name = "TenPhimTiengAnh", length = 255)
    private String tenPhimTiengAnh;

    @Column(name = "TheLoai", length = 500)
    private String theLoai;

    @Column(name = "DaoDien", length = 200)
    private String daoDien;

    @Column(name = "DienVienChinh", length = 500)
    private String dienVienChinh;

    @Column(name = "ThoiLuong")
    private Integer thoiLuong;

    @Column(name = "NgonNgu", length = 50)
    private String ngonNgu;

    @Column(name = "PhanLoaiDoTuoi", length = 10)
    private String phanLoaiDoTuoi;

    @Column(name = "PosterUrl", columnDefinition = "NVARCHAR(MAX)")
    private String posterUrl;

    @Column(name = "TrailerUrl", columnDefinition = "NVARCHAR(MAX)")
    private String trailerUrl;

    @Column(name = "MoTa", columnDefinition = "NVARCHAR(MAX)")
    private String moTa;

    @Column(name = "DiemDanhGia", precision = 3, scale = 2)
    private BigDecimal diemDanhGia;

    @Column(name = "SoLuongDanhGia")
    private Integer soLuongDanhGia;

    @Column(name = "TrangThai", length = 20)
    private String trangThai;

    @Column(name = "IsDeleted")
    private Boolean isDeleted;

    @Column(name = "NgayCongChieu")
    private LocalDate ngayCongChieu;

    @Column(name = "NgayTao")
    private LocalDateTime ngayTao;

    @PrePersist
    protected void onCreate() {
        if (diemDanhGia == null) diemDanhGia = BigDecimal.ZERO;
        if (soLuongDanhGia == null) soLuongDanhGia = 0;
        if (trangThai == null) trangThai = "sap_chieu";
        if (isDeleted == null) isDeleted = false;
        if (ngayTao == null) ngayTao = LocalDateTime.now();
    }
}
