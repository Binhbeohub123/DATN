package com.polycinema.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.math.BigDecimal;

@Entity
@Table(name = "ChiTietDatSanPham")
@IdClass(ChiTietDatSanPhamId.class)
@Data
public class ChiTietDatSanPham {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DatVeId")
    @JsonIgnoreProperties({"chiTietDatGhe", "chiTietDatSanPham", "nguoiDung", "lichChieu", "khuyenMai", "hibernateLazyInitializer", "handler"})
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private DatVe datVe;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SanPhamId")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private SanPham sanPham;

    @Column(name = "SoLuong", nullable = false)
    private Integer soLuong;

    @Column(name = "GiaLucMua", nullable = false, precision = 12, scale = 2)
    private BigDecimal giaLucMua;
}
