package com.polycinema.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "PhongChieu")
@Data
public class PhongChieu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "RapChieuId")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private RapChieu rapChieu;

    @Column(name = "TenPhong", nullable = false, length = 50)
    private String tenPhong;

    @Column(name = "LoaiPhong", nullable = false, length = 30)
    private String loaiPhong;

    @Column(name = "SucChua", nullable = false)
    private Integer sucChua;

    @Column(name = "SoDoGhe", columnDefinition = "NVARCHAR(MAX)")
    private String soDoGhe;

    @Column(name = "TrangThai")
    private Boolean trangThai;

    @PrePersist
    protected void onCreate() {
        if (trangThai == null) trangThai = true;
    }
}
