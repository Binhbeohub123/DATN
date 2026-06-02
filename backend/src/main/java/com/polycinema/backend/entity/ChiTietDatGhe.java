package com.polycinema.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Entity
@Table(name = "ChiTietDatGhe")
@IdClass(ChiTietDatGheId.class)
@Data
public class ChiTietDatGhe {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DatVeId")
    @JsonIgnoreProperties({"chiTietDatGhe", "nguoiDung", "lichChieu", "khuyenMai", "hibernateLazyInitializer", "handler"})
    private DatVe datVe;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "GheNgoiId")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private GheNgoi gheNgoi;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "LichChieuId")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private LichChieu lichChieu;

    @Column(name = "GiaTien", nullable = false, precision = 12, scale = 2)
    private BigDecimal giaTien;

    @Column(name = "HeSoGiaLucDat", nullable = false, precision = 3, scale = 2)
    private BigDecimal heSoGiaLucDat;
}
