package com.polycinema.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "NguoiDung")
@Data
public class NguoiDung {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Long id;

    @Column(name = "Email", nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "SoDienThoai", unique = true, length = 20)
    private String soDienThoai;

    @JsonIgnore
    @Column(name = "MatKhauHash", columnDefinition = "NVARCHAR(MAX)")
    private String matKhauHash;

    @Column(name = "HoTen", length = 100)
    private String hoTen;

    @Column(name = "NgaySinh")
    private LocalDate ngaySinh;

    @Column(name = "AnhDaiDien", columnDefinition = "NVARCHAR(MAX)")
    private String anhDaiDien;

    @Column(name = "GoogleId", unique = true, length = 100)
    private String googleId;

    @Column(name = "FacebookId", unique = true, length = 100)
    private String facebookId;

    @Column(name = "VaiTro", length = 20)
    private String vaiTro;

    @Column(name = "TrangThai")
    private Boolean trangThai;

    @Column(name = "IsEmailVerified")
    private Boolean isEmailVerified;

    @Column(name = "TongTienDaChi", precision = 15, scale = 2)
    private BigDecimal tongTienDaChi;

    @Column(name = "CapDoThanhVien", length = 20)
    private String capDoThanhVien;

    @Column(name = "DiemTichLuy")
    private Integer diemTichLuy;

    @Column(name = "LyDoKhoa", length = 200)
    private String lyDoKhoa;

    @Column(name = "NgayTao")
    private LocalDateTime ngayTao;

    @Column(name = "NgayCapNhat")
    private LocalDateTime ngayCapNhat;

    /** ID of the admin who created this account (null = self-registered or seed admin). */
    @Column(name = "CreatedBy")
    private Long createdBy;

    /**
     * Stamped whenever an admin changes this user's email address.
     * JwtAuthenticationFilter checks JWT iat against this value — any token
     * issued BEFORE this timestamp is rejected, effectively invalidating
     * sessions that were active at the time of the email change.
     */
    @Column(name = "EmailChangedAt")
    private LocalDateTime emailChangedAt;

    @PrePersist
    protected void onCreate() {
        if (trangThai == null) trangThai = true;
        if (isEmailVerified == null) isEmailVerified = false;
        if (vaiTro == null) vaiTro = "customer";
        if (tongTienDaChi == null) tongTienDaChi = BigDecimal.ZERO;
        if (capDoThanhVien == null) capDoThanhVien = "Thường";
        if (diemTichLuy == null) diemTichLuy = 0;
        if (ngayTao == null) ngayTao = LocalDateTime.now();
        if (ngayCapNhat == null) ngayCapNhat = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        ngayCapNhat = LocalDateTime.now();
    }
}
