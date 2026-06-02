package com.polycinema.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "KhuyenMai")
@Data
public class KhuyenMai {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Long id;

    @Column(name = "MaKhuyenMai", nullable = false, unique = true, length = 50)
    private String maKhuyenMai;

    @Column(name = "TenKhuyenMai", length = 200)
    private String tenKhuyenMai;

    @Column(name = "MoTa", columnDefinition = "NVARCHAR(MAX)")
    private String moTa;

    @Column(name = "LoaiGiamGia", length = 20)
    private String loaiGiamGia;

    @Column(name = "GiaTriGiam", precision = 12, scale = 2)
    private BigDecimal giaTriGiam;

    @Column(name = "GiaTriGiamToiDa", precision = 12, scale = 2)
    private BigDecimal giaTriGiamToiDa;

    @Column(name = "DonHangToiThieu", precision = 12, scale = 2)
    private BigDecimal donHangToiThieu;

    @Column(name = "CapDoApDung", length = 20)
    private String capDoApDung;

    @Column(name = "NgayBatDau")
    private LocalDate ngayBatDau;

    @Column(name = "NgayKetThuc")
    private LocalDate ngayKetThuc;

    @Column(name = "GioiHanSuDung")
    private Integer gioiHanSuDung;

    @Column(name = "DaSuDung")
    private Integer daSuDung;

    @Column(name = "DangHoatDong")
    private Boolean dangHoatDong;

    @PrePersist
    protected void onCreate() {
        if (daSuDung == null) daSuDung = 0;
        if (dangHoatDong == null) dangHoatDong = true;
    }
}
