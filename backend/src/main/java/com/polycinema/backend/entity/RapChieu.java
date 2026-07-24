package com.polycinema.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "RapChieu")
@Data
public class RapChieu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Long id;

    @Column(name = "TenRap", nullable = false, length = 100)
    private String tenRap;

    @Column(name = "DiaChi", nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String diaChi;

    @Column(name = "SoDienThoai", length = 20)
    private String soDienThoai;

    @Column(name = "BanDoUrl", columnDefinition = "NVARCHAR(MAX)")
    private String banDoUrl;

    @Column(name = "TrangThai")
    private Boolean trangThai;

    /** City name — added in Phase 1 migration. */
    @Column(name = "ThanhPho", length = 100)
    private String thanhPho;

    /** GPS latitude — DECIMAL(10,7). */
    @Column(name = "Latitude", precision = 10, scale = 7)
    private java.math.BigDecimal latitude;

    /** GPS longitude — DECIMAL(10,7). */
    @Column(name = "Longitude", precision = 10, scale = 7)
    private java.math.BigDecimal longitude;

    /** Cinema image URL — added in Phase 1 migration. */
    @Column(name = "HinhAnh", columnDefinition = "NVARCHAR(MAX)")
    private String hinhAnh;

    @PrePersist
    protected void onCreate() {
        if (trangThai == null) trangThai = true;
    }
}
