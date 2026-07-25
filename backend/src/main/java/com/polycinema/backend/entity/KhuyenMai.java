package com.polycinema.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "KhuyenMai")
@Data
public class KhuyenMai {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Long id;

    @Column(name = "MaKhuyenMai", nullable = false, unique = true, length = 50)
    private String maKhuyenMai;

    @Column(name = "TenKhuyenMai", length = 200)
    private String tenKhuyenMai;

    @Column(name = "MoTa", columnDefinition = "NVARCHAR(MAX)")
    private String moTa;

    @Column(name = "LoaiGiamGia", length = 20)
    private String loaiGiamGia;

    @Column(name = "GiaTriGiam", precision = 12, scale = 2)
    private BigDecimal giaTriGiam;

    @Column(name = "GiaTriGiamToiDa", precision = 12, scale = 2)
    private BigDecimal giaTriGiamToiDa;

    @Column(name = "DonHangToiThieu", precision = 12, scale = 2)
    private BigDecimal donHangToiThieu;

    @Column(name = "CapDoApDung", length = 20)
    private String capDoApDung;

    @Column(name = "NgayBatDau")
    private LocalDate ngayBatDau;

    @Column(name = "NgayKetThuc")
    private LocalDate ngayKetThuc;

    @Column(name = "GioiHanSuDung")
    private Integer gioiHanSuDung;

    @Column(name = "DaSuDung")
    private Integer daSuDung;

    @Column(name = "DangHoatDong")
    private Boolean dangHoatDong;

    // ── N-N relationship to Phim via KhuyenMai_Phim join table ──
    // A promotion can apply to multiple movies; EAGER so the list is
    // always present in API responses.
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "KhuyenMai_Phim",
            joinColumns        = @JoinColumn(name = "KhuyenMaiId"),
            inverseJoinColumns = @JoinColumn(name = "PhimId")
    )
    @JsonIgnoreProperties({"theLoais", "dinhDangs", "hibernateLazyInitializer", "handler"})
    private List<Phim> phims = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (daSuDung == null) daSuDung = 0;
        if (dangHoatDong == null) dangHoatDong = true;
        if (phims == null) phims = new ArrayList<>();
    }
}
