package com.polycinema.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "LichChieu",
       uniqueConstraints = @UniqueConstraint(columnNames = {"PhongChieuId", "ThoiGianBatDau"}))
@Data
public class LichChieu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PhimId")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Phim phim;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PhongChieuId")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private PhongChieu phongChieu;

    @Column(name = "ThoiGianBatDau", nullable = false)
    private LocalDateTime thoiGianBatDau;

    @Column(name = "ThoiGianKetThuc", nullable = false)
    private LocalDateTime thoiGianKetThuc;

    @Column(name = "GiaCoBan", nullable = false, precision = 12, scale = 2)
    private BigDecimal giaCoBan;

    @Column(name = "TrangThai", length = 20)
    private String trangThai;

    @Column(name = "IsDeleted", nullable = false)
    private Boolean isDeleted;

    @Column(name = "NgayTao")
    private LocalDateTime ngayTao;

    @PrePersist
    protected void onCreate() {
        if (trangThai == null) trangThai = "active";
        if (isDeleted == null) isDeleted = false;
        if (ngayTao == null) ngayTao = LocalDateTime.now();
    }
}
