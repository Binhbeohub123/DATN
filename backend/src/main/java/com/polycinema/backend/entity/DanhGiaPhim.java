package com.polycinema.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "DanhGiaPhim",
       uniqueConstraints = @UniqueConstraint(columnNames = {"PhimId", "NguoiDungId"}))
@Data
public class DanhGiaPhim {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PhimId")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Phim phim;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "NguoiDungId")
    @JsonIgnoreProperties({"matKhauHash", "hibernateLazyInitializer", "handler"})
    private NguoiDung nguoiDung;

    @Column(name = "Diem")
    private Integer diem;

    @Column(name = "BinhLuan", columnDefinition = "NVARCHAR(MAX)")
    private String binhLuan;

    @Column(name = "NgayTao")
    private LocalDateTime ngayTao;

    @PrePersist
    protected void onCreate() {
        if (ngayTao == null) ngayTao = LocalDateTime.now();
    }
}
