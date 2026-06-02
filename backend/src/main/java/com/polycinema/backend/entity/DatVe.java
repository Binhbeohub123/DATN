package com.polycinema.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "DatVe")
@Data
public class DatVe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Long id;

    @Column(name = "MaDatVe", nullable = false, unique = true, length = 20)
    private String maDatVe;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "NguoiDungId")
    @JsonIgnoreProperties({"matKhauHash", "hibernateLazyInitializer", "handler"})
    private NguoiDung nguoiDung;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "LichChieuId")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private LichChieu lichChieu;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "KhuyenMaiId")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private KhuyenMai khuyenMai;

    // One-to-many to ChiTietDatGhe for seat details in response
    @OneToMany(mappedBy = "datVe", fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"datVe", "hibernateLazyInitializer", "handler"})
    private List<ChiTietDatGhe> chiTietDatGhe;

    @Column(name = "TongTienGoc", nullable = false, precision = 15, scale = 2)
    private BigDecimal tongTienGoc;

    @Column(name = "TienGiamKhuyenMai", precision = 12, scale = 2)
    private BigDecimal tienGiamKhuyenMai;

    @Column(name = "DiemSuDung")
    private Integer diemSuDung;

    @Column(name = "TienGiamTuDiem", precision = 12, scale = 2)
    private BigDecimal tienGiamTuDiem;

    @Column(name = "TongTienThanhToan", nullable = false, precision = 15, scale = 2)
    private BigDecimal tongTienThanhToan;

    @Column(name = "TrangThai", length = 20)
    private String trangThai;

    @Column(name = "TrangThaiThanhToan", length = 20)
    private String trangThaiThanhToan;

    @Column(name = "MaQR", columnDefinition = "NVARCHAR(MAX)")
    private String maQR;

    @Column(name = "NgayTao")
    private LocalDateTime ngayTao;

    @Column(name = "HetHanGiuGhe")
    private LocalDateTime hetHanGiuGhe;

    @PrePersist
    protected void onCreate() {
        if (tongTienGoc == null) tongTienGoc = BigDecimal.ZERO;
        if (tienGiamKhuyenMai == null) tienGiamKhuyenMai = BigDecimal.ZERO;
        if (diemSuDung == null) diemSuDung = 0;
        if (tienGiamTuDiem == null) tienGiamTuDiem = BigDecimal.ZERO;
        if (trangThai == null) trangThai = "pending";
        if (trangThaiThanhToan == null) trangThaiThanhToan = "unpaid";
        if (ngayTao == null) ngayTao = LocalDateTime.now();
    }
}
