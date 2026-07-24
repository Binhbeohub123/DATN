package com.polycinema.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Phim")
@Data
public class Phim {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Long id;

    @Column(name = "TenPhim", nullable = false, length = 255)
    private String tenPhim;

    @Column(name = "TenPhimTiengAnh", length = 255)
    private String tenPhimTiengAnh;

    // ── N-N relationship to TheLoai via Phim_TheLoai join table ──
    // The old TheLoai VARCHAR column has been dropped from the DB.
    // EAGER fetch ensures the genre list is always populated in API responses,
    // including the admin movie list (loaded outside a transaction).
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "Phim_TheLoai",
            joinColumns        = @JoinColumn(name = "PhimId"),
            inverseJoinColumns = @JoinColumn(name = "TheLoaiId")
    )
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private List<TheLoai> theLoais = new ArrayList<>();

    // ── N-N relationship to DinhDang via Phim_DinhDang join table ──
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "Phim_DinhDang",
            joinColumns        = @JoinColumn(name = "PhimId"),
            inverseJoinColumns = @JoinColumn(name = "DinhDangId")
    )
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private List<DinhDang> dinhDangs = new ArrayList<>();

    @Column(name = "DaoDien", length = 200)
    private String daoDien;

    @Column(name = "DienVienChinh", length = 500)
    private String dienVienChinh;

    @Column(name = "ThoiLuong")
    private Integer thoiLuong;

    @Column(name = "NgonNgu", length = 50)
    private String ngonNgu;

    @Column(name = "PhanLoaiDoTuoi", length = 10)
    private String phanLoaiDoTuoi;

    @Column(name = "PosterUrl", columnDefinition = "NVARCHAR(MAX)")
    private String posterUrl;

    @Column(name = "TrailerUrl", columnDefinition = "NVARCHAR(MAX)")
    private String trailerUrl;

    @Column(name = "MoTa", columnDefinition = "NVARCHAR(MAX)")
    private String moTa;

    @Column(name = "DiemDanhGia", precision = 3, scale = 2)
    private BigDecimal diemDanhGia;

    @Column(name = "SoLuongDanhGia")
    private Integer soLuongDanhGia;

    @Column(name = "TrangThai", length = 20)
    private String trangThai;

    @Column(name = "IsDeleted")
    private Boolean isDeleted;

    @Column(name = "NgayCongChieu")
    private LocalDate ngayCongChieu;

    @Column(name = "NgayTao")
    private LocalDateTime ngayTao;

    @PrePersist
    protected void onCreate() {
        if (diemDanhGia == null)   diemDanhGia   = BigDecimal.ZERO;
        if (soLuongDanhGia == null) soLuongDanhGia = 0;
        if (trangThai == null)     trangThai      = "sap_chieu";
        if (isDeleted == null)     isDeleted      = false;
        if (ngayTao == null)       ngayTao        = LocalDateTime.now();
        if (theLoais == null)      theLoais        = new ArrayList<>();
        if (dinhDangs == null)     dinhDangs       = new ArrayList<>();
    }
}
