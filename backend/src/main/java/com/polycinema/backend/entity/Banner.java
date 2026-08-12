package com.polycinema.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "Banner")
@Data
public class Banner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Long id;

    @Column(name = "TieuDe", length = 200)
    private String tieuDe;

    @Column(name = "HinhAnh", nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String hinhAnh;

    @Column(name = "ThuTu")
    private Integer thuTu;

    /** Typed banner target: "Phim" | "Khac" */
    @Column(name = "LoaiBanner", length = 20)
    private String loaiBanner;

    /** FK to Phim.Id — set when loaiBanner = "Phim" */
    @Column(name = "PhimId")
    private Long phimId;

    @Column(name = "NgayBatDau")
    private LocalDate ngayBatDau;

    @Column(name = "NgayKetThuc")
    private LocalDate ngayKetThuc;

    @Column(name = "DangHoatDong")
    private Boolean dangHoatDong;

    @Column(name = "MoTa", columnDefinition = "NVARCHAR(500)")
    private String moTa;

    @Column(name = "NgayTao")
    private LocalDateTime ngayTao;

    @PrePersist
    protected void onCreate() {
        if (thuTu == null) thuTu = 0;
        if (dangHoatDong == null) dangHoatDong = true;
        if (ngayTao == null) ngayTao = LocalDateTime.now();
    }
}
