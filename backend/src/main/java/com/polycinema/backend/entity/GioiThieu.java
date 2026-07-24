package com.polycinema.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Singleton content block for the public "Giới Thiệu" (About) page.
 * Always a single row with id = 1. NO @GeneratedValue — the controller
 * always sets id = 1 explicitly, matching the DB CHECK (Id = 1) constraint.
 */
@Entity
@Table(name = "GioiThieu")
@Data
public class GioiThieu {

    /** Fixed singleton id — always 1. Set explicitly by the controller. */
    @Id
    @Column(name = "Id")
    private Long id;

    @Column(name = "TieuDe", length = 300)
    private String tieuDe;

    @Column(name = "NoiDung", columnDefinition = "NVARCHAR(MAX)")
    private String noiDung;

    @Column(name = "HinhAnhUrl", columnDefinition = "NVARCHAR(MAX)")
    private String hinhAnhUrl;

    @Column(name = "NgayCapNhat")
    private LocalDateTime ngayCapNhat;

    @PreUpdate
    @PrePersist
    protected void onUpdate() {
        ngayCapNhat = LocalDateTime.now();
    }
}
