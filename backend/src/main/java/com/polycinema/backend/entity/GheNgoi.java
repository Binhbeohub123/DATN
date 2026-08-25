package com.polycinema.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Entity
@Table(name = "GheNgoi",
       uniqueConstraints = @UniqueConstraint(columnNames = {"PhongChieuId", "HangGhe", "SoGhe"}))
@Data
public class GheNgoi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PhongChieuId")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private PhongChieu phongChieu;

    @Column(name = "HangGhe", nullable = false, length = 2, columnDefinition = "CHAR(2)")
    private String hangGhe;

    @Column(name = "SoGhe", nullable = false)
    private Integer soGhe;

    @Column(name = "LoaiGhe", length = 20)
    private String loaiGhe;

    @Column(name = "HeSoGia", precision = 3, scale = 2)
    private BigDecimal heSoGia;

    /**
     * Nhãn số ghế hiển thị: đếm tuần tự các ghế KHÔNG phải 'trống' trong cùng
     * hàng, theo soGhe vật lý tăng dần. Ô 'trống' (lối đi) để null.
     * Không persist — được tính lúc đọc bởi SeatDisplayUtil.
     * soGhe vật lý vẫn là định danh/layout duy nhất (grid-column, API...).
     */
    @Transient
    private Integer soGheHienThi;

    @PrePersist
    protected void onCreate() {
        if (loaiGhe == null) loaiGhe = "thường";
        if (heSoGia == null) heSoGia = BigDecimal.ONE;
    }
}
