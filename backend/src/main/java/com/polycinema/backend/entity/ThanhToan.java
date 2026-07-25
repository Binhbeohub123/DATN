package com.polycinema.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "ThanhToan")
@Data
public class ThanhToan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DatVeId")
    @JsonIgnoreProperties({"chiTietDatGhe", "chiTietDatSanPham", "nguoiDung", "lichChieu", "khuyenMai", "hibernateLazyInitializer", "handler"})
    private DatVe datVe;

    @Column(name = "SoTien", nullable = false, precision = 15, scale = 2)
    private BigDecimal soTien;

    @Column(name = "PhuongThucThanhToan", length = 30)
    private String phuongThucThanhToan;

    @Column(name = "MaGiaoDich", length = 100)
    private String maGiaoDich;

    @Column(name = "TrangThai", length = 20)
    private String trangThai;

    @Column(name = "ThoiGianThanhToan")
    private LocalDateTime thoiGianThanhToan;

    @Column(name = "LyDoHoan", columnDefinition = "NVARCHAR(MAX)")
    private String lyDoHoan;

    @Column(name = "NgayHoan")
    private LocalDateTime ngayHoan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "NguoiXuLyId")
    @JsonIgnoreProperties({"matKhauHash", "hibernateLazyInitializer", "handler"})
    private NguoiDung nguoiXuLy;

    @PrePersist
    protected void onCreate() {
        if (trangThai == null) trangThai = "pending";
    }
}
