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

    @PrePersist
    protected void onCreate() {
        if (trangThai == null) trangThai = true;
    }
}
