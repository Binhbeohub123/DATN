package com.polycinema.backend.dto;

import com.polycinema.backend.entity.LichChieu;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class LichChieuResponse {
    private Long id;
    private Long phimId;
    private Long phongChieuId;
    private String tenPhong;
    private String loaiPhong;
    private LocalDateTime thoiGianBatDau;
    private LocalDateTime thoiGianKetThuc;
    private BigDecimal giaCoBan;
    private String trangThai;
    private Boolean isDeleted;

    /**
     * Convert LichChieu entity to DTO
     */
    public static LichChieuResponse from(LichChieu lc) {
        if (lc == null) return null;

        return new LichChieuResponse(
                lc.getId(),
                lc.getPhim() != null ? lc.getPhim().getId() : null,
                lc.getPhongChieu() != null ? lc.getPhongChieu().getId() : null,
                lc.getPhongChieu() != null ? lc.getPhongChieu().getTenPhong() : null,
                lc.getPhongChieu() != null ? lc.getPhongChieu().getLoaiPhong() : null,
                lc.getThoiGianBatDau(),
                lc.getThoiGianKetThuc(),
                lc.getGiaCoBan(),
                lc.getTrangThai(),
                lc.getIsDeleted()
        );
    }
}
