package com.polycinema.backend.dto;

import com.polycinema.backend.entity.DinhDang;
import com.polycinema.backend.entity.LichChieu;
import com.polycinema.backend.entity.Phim;
import com.polycinema.backend.entity.PhongChieu;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A showtime enriched with its movie's display fields.
 * Used by GET /api/lich-chieu/rap/{rapChieuId} to render the
 * "Lịch chiếu theo rạp" section on the cinema detail page.
 */
@Data
@AllArgsConstructor
public class ShowtimeMovieResponse {

    // ── Showtime (LichChieu) ──
    private Long          lichChieuId;
    private LocalDateTime thoiGianBatDau;
    private LocalDateTime thoiGianKetThuc;
    private Integer       thoiGianNghi;
    private BigDecimal    giaCoBan;

    // ── Room (PhongChieu) ──
    private Long   phongChieuId;
    private String tenPhong;
    private String loaiPhong;
    private Long   dinhDangId;
    private String tenDinhDang;

    // ── Movie (Phim) ──
    private Long   phimId;
    private String tenPhim;
    private String posterUrl;
    private Integer thoiLuong;
    private List<String> theLoai;
    private String phanLoaiDoTuoi;
    private String ngonNgu;
    private BigDecimal diemDanhGia;
    private LocalDate ngayCongChieu;
    private String trailerUrl;
    private String trangThai;

    /** Map a LichChieu (with eager-fetched phim + phongChieu graph) to this DTO. */
    public static ShowtimeMovieResponse from(LichChieu lc) {
        PhongChieu pc = lc.getPhongChieu();
        Phim       p  = lc.getPhim();
        DinhDang   dd = (pc != null) ? pc.getDinhDang() : null;

        List<String> theLoai = (p != null && p.getTheLoais() != null)
                ? p.getTheLoais().stream().map(t -> t.getTenTheLoai()).collect(Collectors.toList())
                : new ArrayList<>();

        return new ShowtimeMovieResponse(
                lc.getId(),
                lc.getThoiGianBatDau(),
                lc.getThoiGianKetThuc(),
                lc.getThoiGianNghi() != null ? lc.getThoiGianNghi() : 15,
                lc.getGiaCoBan(),

                pc != null ? pc.getId()       : null,
                pc != null ? pc.getTenPhong() : null,
                pc != null ? pc.getLoaiPhong(): null,
                dd != null ? dd.getId()          : null,
                dd != null ? dd.getTenDinhDang() : null,

                p != null ? p.getId()          : null,
                p != null ? p.getTenPhim()     : null,
                p != null ? p.getPosterUrl()   : null,
                p != null ? p.getThoiLuong()   : null,
                theLoai,
                p != null ? p.getPhanLoaiDoTuoi() : null,
                p != null ? p.getNgonNgu()        : null,
                p != null ? p.getDiemDanhGia()    : null,
                p != null ? p.getNgayCongChieu()  : null,
                p != null ? p.getTrailerUrl()     : null,
                p != null ? p.getTrangThai()      : null
        );
    }
}
