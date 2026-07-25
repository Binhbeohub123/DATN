package com.polycinema.backend.dto;

import com.polycinema.backend.entity.LichChieu;
import com.polycinema.backend.entity.PhongChieu;
import com.polycinema.backend.entity.RapChieu;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Flat DTO for a showtime — used by:
 *   GET /api/phim/{id}/lich-chieu   (PhimController)
 *   GET /api/lich-chieu/search      (LichChieuController)
 *
 * Phase 3 additions: rapChieuId, tenRap, thanhPho, tenDinhDang
 * (needed for the city-selector and cinema-grouped display in Phase 4).
 */
@Data
@AllArgsConstructor
public class LichChieuResponse {

    // ── LichChieu ──
    private Long   id;
    private Long   phimId;

    // ── PhongChieu ──
    private Long   phongChieuId;
    private String tenPhong;
    private String loaiPhong;       // legacy free-text; kept for backward-compat

    // ── RapChieu (via PhongChieu.rapChieu) ──
    private Long   rapChieuId;
    private String tenRap;
    private String thanhPho;

    // ── DinhDang (via PhongChieu.dinhDang) ──
    private Long   dinhDangId;
    private String tenDinhDang;

    // ── Schedule times & price ──
    private LocalDateTime thoiGianBatDau;
    private LocalDateTime thoiGianKetThuc;
    private BigDecimal    giaCoBan;
    private String        trangThai;
    private Boolean       isDeleted;

    /**
     * Map a LichChieu entity to this DTO.
     * All nested relations are read defensively — null-safe at every level.
     * Both LAZY-loaded (phimId path) and JOIN FETCH (search path) graphs work.
     */
    public static LichChieuResponse from(LichChieu lc) {
        if (lc == null) return null;

        PhongChieu pc  = lc.getPhongChieu();
        RapChieu   rap = (pc != null) ? pc.getRapChieu() : null;

        return new LichChieuResponse(
                lc.getId(),
                lc.getPhim()  != null ? lc.getPhim().getId() : null,

                pc  != null ? pc.getId()       : null,
                pc  != null ? pc.getTenPhong() : null,
                pc  != null ? pc.getLoaiPhong(): null,

                rap != null ? rap.getId()      : null,
                rap != null ? rap.getTenRap()  : null,
                rap != null ? rap.getThanhPho(): null,

                (pc != null && pc.getDinhDang() != null) ? pc.getDinhDang().getId()          : null,
                (pc != null && pc.getDinhDang() != null) ? pc.getDinhDang().getTenDinhDang() : null,

                lc.getThoiGianBatDau(),
                lc.getThoiGianKetThuc(),
                lc.getGiaCoBan(),
                lc.getTrangThai(),
                lc.getIsDeleted()
        );
    }
}
