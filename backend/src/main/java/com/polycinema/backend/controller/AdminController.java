package com.polycinema.backend.controller;

import com.polycinema.backend.entity.*;
import com.polycinema.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@CrossOrigin(origins = "http://localhost:5173")
public class AdminController {

    private final DatVeRepository datVeRepository;
    private final NguoiDungRepository nguoiDungRepository;
    private final PhimRepository phimRepository;
    private final LichChieuRepository lichChieuRepository;
    private final RapChieuRepository rapChieuRepository;
    private final PhongChieuRepository phongChieuRepository;
    private final GheNgoiRepository gheNgoiRepository;
    private final SanPhamRepository sanPhamRepository;
    private final BannerRepository bannerRepository;
    private final ThanhToanRepository thanhToanRepository;

    // ─────────────────────────────────────────────────────────────
    // STATS
    // ─────────────────────────────────────────────────────────────

    /**
     * GET /api/admin/stats
     * Returns today's KPIs.
     */
    @GetMapping("/stats")
    public ResponseEntity<?> getStats() {
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = todayStart.plusDays(1);

        List<DatVe> allBookings = datVeRepository.findAll();
        List<DatVe> todayBookings = allBookings.stream()
                .filter(dv -> dv.getNgayTao() != null
                        && dv.getNgayTao().isAfter(todayStart)
                        && dv.getNgayTao().isBefore(todayEnd))
                .collect(Collectors.toList());

        long totalUsers = nguoiDungRepository.count();
        long totalMovies = phimRepository.findByIsDeletedFalse().size();

        BigDecimal todayRevenue = todayBookings.stream()
                .filter(dv -> "paid".equals(dv.getTrangThaiThanhToan()))
                .map(DatVe::getTongTienThanhToan)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalRevenue = allBookings.stream()
                .filter(dv -> "paid".equals(dv.getTrangThaiThanhToan()))
                .map(DatVe::getTongTienThanhToan)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long pendingBookings = allBookings.stream()
                .filter(dv -> "pending".equals(dv.getTrangThai()))
                .count();

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("todayTickets", todayBookings.size());
        stats.put("todayRevenue", todayRevenue);
        stats.put("totalRevenue", totalRevenue);
        stats.put("totalUsers", totalUsers);
        stats.put("totalMovies", totalMovies);
        stats.put("pendingBookings", pendingBookings);

        return ResponseEntity.ok(stats);
    }

    /**
     * GET /api/admin/doanh-thu?from=yyyy-MM-dd&to=yyyy-MM-dd
     * Revenue grouped by day.
     */
    @GetMapping("/doanh-thu")
    public ResponseEntity<?> getRevenue(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {

        List<DatVe> paid = datVeRepository.findAll().stream()
                .filter(dv -> "paid".equals(dv.getTrangThaiThanhToan()))
                .collect(Collectors.toList());

        // Group by date using ngayTao (actual booking creation date)
        Map<String, BigDecimal> byDay = new TreeMap<>();
        for (DatVe dv : paid) {
            if (dv.getNgayTao() == null) continue;
            String day = dv.getNgayTao().toLocalDate().toString();
            byDay.merge(day, dv.getTongTienThanhToan() == null ? BigDecimal.ZERO : dv.getTongTienThanhToan(), BigDecimal::add);
        }

        List<Map<String, Object>> result = byDay.entrySet().stream()
                .map(e -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("ngay", e.getKey());
                    m.put("doanhThu", e.getValue());
                    return m;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    // ─────────────────────────────────────────────────────────────
    // USER MANAGEMENT
    // ─────────────────────────────────────────────────────────────

    /**
     * GET /api/admin/users?page=0&size=20&search=
     */
    @GetMapping("/users")
    public ResponseEntity<?> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search) {

        List<NguoiDung> all = nguoiDungRepository.findAll();

        if (search != null && !search.isBlank()) {
            String q = search.toLowerCase();
            all = all.stream()
                    .filter(u -> (u.getEmail() != null && u.getEmail().toLowerCase().contains(q))
                            || (u.getHoTen() != null && u.getHoTen().toLowerCase().contains(q))
                            || (u.getSoDienThoai() != null && u.getSoDienThoai().contains(q)))
                    .collect(Collectors.toList());
        }

        // Mask password
        all.forEach(u -> u.setMatKhauHash(null));

        int total = all.size();
        int start = page * size;
        int end = Math.min(start + size, total);
        List<NguoiDung> paged = start < total ? all.subList(start, end) : Collections.emptyList();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("content", paged);
        result.put("totalElements", total);
        result.put("totalPages", (int) Math.ceil((double) total / size));
        result.put("page", page);
        result.put("size", size);

        return ResponseEntity.ok(result);
    }

    /**
     * PUT /api/admin/users/{id}/lock
     * Body: { "reason": "..." }
     */
    @PutMapping("/users/{id}/lock")
    public ResponseEntity<?> lockUser(@PathVariable Long id, @RequestBody Map<String, String> body) {
        NguoiDung user = nguoiDungRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng"));
        user.setTrangThai(false);
        user.setLyDoKhoa(body.get("reason"));
        nguoiDungRepository.save(user);
        return ResponseEntity.ok(Map.of("message", "Đã khóa tài khoản"));
    }

    /**
     * PUT /api/admin/users/{id}/unlock
     */
    @PutMapping("/users/{id}/unlock")
    public ResponseEntity<?> unlockUser(@PathVariable Long id) {
        NguoiDung user = nguoiDungRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng"));
        user.setTrangThai(true);
        user.setLyDoKhoa(null);
        nguoiDungRepository.save(user);
        return ResponseEntity.ok(Map.of("message", "Đã mở khóa tài khoản"));
    }

    // ─────────────────────────────────────────────────────────────
    // MOVIE ADMIN CRUD
    // ─────────────────────────────────────────────────────────────

    /**
     * GET /api/admin/phim — all movies including deleted
     */
    @GetMapping("/phim")
    public ResponseEntity<?> getAllPhim() {
        return ResponseEntity.ok(phimRepository.findAll());
    }

    /**
     * POST /api/admin/phim
     */
    @PostMapping("/phim")
    public ResponseEntity<?> createPhim(@RequestBody Phim phim) {
        phim.setId(null);
        phim.setIsDeleted(false);
        return ResponseEntity.status(HttpStatus.CREATED).body(phimRepository.save(phim));
    }

    /**
     * PUT /api/admin/phim/{id}
     */
    @PutMapping("/phim/{id}")
    public ResponseEntity<?> updatePhim(@PathVariable Long id, @RequestBody Phim body) {
        Phim phim = phimRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phim"));
        if (body.getTenPhim() != null) phim.setTenPhim(body.getTenPhim());
        if (body.getTenPhimTiengAnh() != null) phim.setTenPhimTiengAnh(body.getTenPhimTiengAnh());
        if (body.getTheLoai() != null) phim.setTheLoai(body.getTheLoai());
        if (body.getDaoDien() != null) phim.setDaoDien(body.getDaoDien());
        if (body.getDienVienChinh() != null) phim.setDienVienChinh(body.getDienVienChinh());
        if (body.getThoiLuong() != null) phim.setThoiLuong(body.getThoiLuong());
        if (body.getNgonNgu() != null) phim.setNgonNgu(body.getNgonNgu());
        if (body.getPhanLoaiDoTuoi() != null) phim.setPhanLoaiDoTuoi(body.getPhanLoaiDoTuoi());
        if (body.getPosterUrl() != null) phim.setPosterUrl(body.getPosterUrl());
        if (body.getTrailerUrl() != null) phim.setTrailerUrl(body.getTrailerUrl());
        if (body.getMoTa() != null) phim.setMoTa(body.getMoTa());
        if (body.getTrangThai() != null) phim.setTrangThai(body.getTrangThai());
        if (body.getNgayCongChieu() != null) phim.setNgayCongChieu(body.getNgayCongChieu());
        return ResponseEntity.ok(phimRepository.save(phim));
    }

    /**
     * DELETE /api/admin/phim/{id} — soft delete
     */
    @DeleteMapping("/phim/{id}")
    public ResponseEntity<?> deletePhim(@PathVariable Long id) {
        Phim phim = phimRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phim"));
        phim.setIsDeleted(true);
        phimRepository.save(phim);
        return ResponseEntity.ok(Map.of("message", "Đã xóa phim"));
    }

    // ─────────────────────────────────────────────────────────────
    // SCHEDULE ADMIN CRUD
    // ─────────────────────────────────────────────────────────────

    /**
     * GET /api/admin/lich-chieu
     */
    @GetMapping("/lich-chieu")
    public ResponseEntity<?> getAllLichChieu() {
        return ResponseEntity.ok(lichChieuRepository.findAll().stream()
                .filter(lc -> !Boolean.TRUE.equals(lc.getIsDeleted()))
                .collect(Collectors.toList()));
    }

    /**
     * POST /api/admin/lich-chieu
     */
    @PostMapping("/lich-chieu")
    public ResponseEntity<?> createLichChieu(@RequestBody LichChieu lichChieu) {
        lichChieu.setId(null);
        lichChieu.setIsDeleted(false);

        if (lichChieu.getPhim() == null || lichChieu.getPhim().getId() == null) {
            throw new IllegalArgumentException("Thiếu thông tin phim");
        }
        if (lichChieu.getPhongChieu() == null || lichChieu.getPhongChieu().getId() == null) {
            throw new IllegalArgumentException("Thiếu thông tin phòng chiếu");
        }
        if (lichChieu.getThoiGianBatDau() == null || lichChieu.getThoiGianKetThuc() == null) {
            throw new IllegalArgumentException("Thiếu thời gian bắt đầu/kết thúc");
        }
        if (!lichChieu.getThoiGianKetThuc().isAfter(lichChieu.getThoiGianBatDau())) {
            throw new IllegalArgumentException("Thời gian kết thúc phải sau thời gian bắt đầu");
        }

        // Always attach managed entities to avoid detached/transient entity issues.
        Long phimId = lichChieu.getPhim().getId();
        Long phongId = lichChieu.getPhongChieu().getId();
        Phim phim = phimRepository.findById(phimId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phim"));
        PhongChieu phongChieu = phongChieuRepository.findById(phongId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phòng chiếu"));
        lichChieu.setPhim(phim);
        lichChieu.setPhongChieu(phongChieu);

        // Conflict detection: same room, overlapping time
        if (lichChieu.getPhongChieu() != null && lichChieu.getThoiGianBatDau() != null && lichChieu.getThoiGianKetThuc() != null) {
            LocalDateTime start = lichChieu.getThoiGianBatDau();
            LocalDateTime end = lichChieu.getThoiGianKetThuc();

            boolean conflict = lichChieuRepository.findAll().stream()
                    .filter(lc -> !Boolean.TRUE.equals(lc.getIsDeleted()))
                    .filter(lc -> lc.getPhongChieu() != null && phongId.equals(lc.getPhongChieu().getId()))
                    .anyMatch(lc -> lc.getThoiGianBatDau() != null && lc.getThoiGianKetThuc() != null
                            && start.isBefore(lc.getThoiGianKetThuc())
                            && end.isAfter(lc.getThoiGianBatDau()));

            if (conflict) {
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "Phòng chiếu đã có lịch chiếu trong khung giờ này"));
            }
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(lichChieuRepository.save(lichChieu));
    }

    /**
     * PUT /api/admin/lich-chieu/{id}
     */
    @PutMapping("/lich-chieu/{id}")
    public ResponseEntity<?> updateLichChieu(@PathVariable Long id, @RequestBody LichChieu body) {
        LichChieu lc = lichChieuRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy lịch chiếu"));

        LocalDateTime start = body.getThoiGianBatDau() != null ? body.getThoiGianBatDau() : lc.getThoiGianBatDau();
        LocalDateTime end = body.getThoiGianKetThuc() != null ? body.getThoiGianKetThuc() : lc.getThoiGianKetThuc();
        PhongChieu phong = lc.getPhongChieu();

        if (body.getPhongChieu() != null && body.getPhongChieu().getId() != null) {
            phong = phongChieuRepository.findById(body.getPhongChieu().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phòng chiếu"));
        }
        if (body.getPhim() != null && body.getPhim().getId() != null) {
            Phim phim = phimRepository.findById(body.getPhim().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phim"));
            lc.setPhim(phim);
        }

        if (start != null && end != null && !end.isAfter(start)) {
            throw new IllegalArgumentException("Thời gian kết thúc phải sau thời gian bắt đầu");
        }

        if (phong != null && start != null && end != null) {
            Long phongId = phong.getId();
            boolean conflict = lichChieuRepository.findAll().stream()
                    .filter(other -> !Boolean.TRUE.equals(other.getIsDeleted()))
                    .filter(other -> !other.getId().equals(id))
                    .filter(other -> other.getPhongChieu() != null && phongId.equals(other.getPhongChieu().getId()))
                    .anyMatch(other -> other.getThoiGianBatDau() != null && other.getThoiGianKetThuc() != null
                            && start.isBefore(other.getThoiGianKetThuc())
                            && end.isAfter(other.getThoiGianBatDau()));

            if (conflict) {
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "Phòng chiếu đã có lịch chiếu khác trong khung giờ này"));
            }
        }

        if (body.getThoiGianBatDau() != null) lc.setThoiGianBatDau(body.getThoiGianBatDau());
        if (body.getThoiGianKetThuc() != null) lc.setThoiGianKetThuc(body.getThoiGianKetThuc());
        if (body.getGiaCoBan() != null) lc.setGiaCoBan(body.getGiaCoBan());
        if (body.getTrangThai() != null) lc.setTrangThai(body.getTrangThai());
        if (body.getPhongChieu() != null && body.getPhongChieu().getId() != null) lc.setPhongChieu(phong);

        return ResponseEntity.ok(lichChieuRepository.save(lc));
    }

    /**
     * DELETE /api/admin/lich-chieu/{id}
     */
    @DeleteMapping("/lich-chieu/{id}")
    public ResponseEntity<?> deleteLichChieu(@PathVariable Long id) {
        LichChieu lc = lichChieuRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy lịch chiếu"));
        lc.setIsDeleted(true);
        lichChieuRepository.save(lc);
        return ResponseEntity.ok(Map.of("message", "Đã xóa lịch chiếu"));
    }

    // ─────────────────────────────────────────────────────────────
    // CINEMA ADMIN CRUD
    // ─────────────────────────────────────────────────────────────

    @PostMapping("/rap-chieu")
    public ResponseEntity<?> createRap(@RequestBody RapChieu rap) {
        rap.setId(null);
        if (rap.getTrangThai() == null) rap.setTrangThai(true);
        return ResponseEntity.status(HttpStatus.CREATED).body(rapChieuRepository.save(rap));
    }

    @PutMapping("/rap-chieu/{id}")
    public ResponseEntity<?> updateRap(@PathVariable Long id, @RequestBody RapChieu body) {
        RapChieu rap = rapChieuRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy rạp chiếu"));
        if (body.getTenRap() != null) rap.setTenRap(body.getTenRap());
        if (body.getDiaChi() != null) rap.setDiaChi(body.getDiaChi());
        if (body.getTrangThai() != null) rap.setTrangThai(body.getTrangThai());
        return ResponseEntity.ok(rapChieuRepository.save(rap));
    }

    @DeleteMapping("/rap-chieu/{id}")
    public ResponseEntity<?> deleteRap(@PathVariable Long id) {
        RapChieu rap = rapChieuRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy rạp chiếu"));
        rap.setTrangThai(false);
        rapChieuRepository.save(rap);
        return ResponseEntity.ok(Map.of("message", "Đã vô hiệu hóa rạp chiếu"));
    }

    // ─────────────────────────────────────────────────────────────
    // ROOM ADMIN CRUD
    // ─────────────────────────────────────────────────────────────

    @PostMapping("/phong-chieu")
    public ResponseEntity<?> createPhong(@RequestBody PhongChieu phong) {
        phong.setId(null);
        if (phong.getTrangThai() == null) phong.setTrangThai(true);
        return ResponseEntity.status(HttpStatus.CREATED).body(phongChieuRepository.save(phong));
    }

    @PutMapping("/phong-chieu/{id}")
    public ResponseEntity<?> updatePhong(@PathVariable Long id, @RequestBody PhongChieu body) {
        PhongChieu phong = phongChieuRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phòng chiếu"));
        if (body.getTenPhong() != null) phong.setTenPhong(body.getTenPhong());
        if (body.getLoaiPhong() != null) phong.setLoaiPhong(body.getLoaiPhong());
        if (body.getSucChua() != null) phong.setSucChua(body.getSucChua());
        if (body.getSoDoGhe() != null) phong.setSoDoGhe(body.getSoDoGhe());
        if (body.getTrangThai() != null) phong.setTrangThai(body.getTrangThai());
        return ResponseEntity.ok(phongChieuRepository.save(phong));
    }

    /**
     * DELETE /api/admin/phong-chieu/{id} — soft-deactivate (RQ49)
     * Sets trangThai = false so the room is hidden from active scheduling.
     */
    @DeleteMapping("/phong-chieu/{id}")
    public ResponseEntity<?> deactivatePhong(@PathVariable Long id) {
        PhongChieu phong = phongChieuRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phòng chiếu"));
        phong.setTrangThai(false);
        phongChieuRepository.save(phong);
        return ResponseEntity.ok(Map.of("message", "Đã vô hiệu hóa phòng chiếu"));
    }

    // ─────────────────────────────────────────────────────────────
    // PRODUCT ADMIN CRUD
    // ─────────────────────────────────────────────────────────────

    @PostMapping("/san-pham")
    public ResponseEntity<?> createSanPham(@RequestBody SanPham sp) {
        sp.setId(null);
        if (sp.getDangHoatDong() == null) sp.setDangHoatDong(true);
        return ResponseEntity.status(HttpStatus.CREATED).body(sanPhamRepository.save(sp));
    }

    @PutMapping("/san-pham/{id}")
    public ResponseEntity<?> updateSanPham(@PathVariable Long id, @RequestBody SanPham body) {
        SanPham sp = sanPhamRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm"));
        if (body.getTenSanPham() != null) sp.setTenSanPham(body.getTenSanPham());
        if (body.getMoTa() != null) sp.setMoTa(body.getMoTa());
        if (body.getGia() != null) sp.setGia(body.getGia());
        if (body.getLoaiSanPham() != null) sp.setLoaiSanPham(body.getLoaiSanPham());
        if (body.getAnhUrl() != null) sp.setAnhUrl(body.getAnhUrl());
        if (body.getTonKho() != null) sp.setTonKho(body.getTonKho());
        if (body.getDangHoatDong() != null) sp.setDangHoatDong(body.getDangHoatDong());
        return ResponseEntity.ok(sanPhamRepository.save(sp));
    }

    @DeleteMapping("/san-pham/{id}")
    public ResponseEntity<?> deleteSanPham(@PathVariable Long id) {
        SanPham sp = sanPhamRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm"));
        sp.setDangHoatDong(false);
        sanPhamRepository.save(sp);
        return ResponseEntity.ok(Map.of("message", "Đã vô hiệu hóa sản phẩm"));
    }

    // ─────────────────────────────────────────────────────────────
    // BANNER ADMIN CRUD
    // ─────────────────────────────────────────────────────────────

    @PostMapping("/banner")
    public ResponseEntity<?> createBanner(@RequestBody Banner banner) {
        banner.setId(null);
        if (banner.getDangHoatDong() == null) banner.setDangHoatDong(true);
        return ResponseEntity.status(HttpStatus.CREATED).body(bannerRepository.save(banner));
    }

    @PutMapping("/banner/{id}")
    public ResponseEntity<?> updateBanner(@PathVariable Long id, @RequestBody Banner body) {
        Banner banner = bannerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy banner"));
        if (body.getTieuDe() != null) banner.setTieuDe(body.getTieuDe());
        if (body.getHinhAnh() != null) banner.setHinhAnh(body.getHinhAnh());
        if (body.getLinkUrl() != null) banner.setLinkUrl(body.getLinkUrl());
        if (body.getThuTu() != null) banner.setThuTu(body.getThuTu());
        if (body.getDangHoatDong() != null) banner.setDangHoatDong(body.getDangHoatDong());
        return ResponseEntity.ok(bannerRepository.save(banner));
    }

    @DeleteMapping("/banner/{id}")
    public ResponseEntity<?> deleteBanner(@PathVariable Long id) {
        bannerRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Đã xóa banner"));
    }

    // ─────────────────────────────────────────────────────────────
    // BOOKINGS OVERVIEW
    // ─────────────────────────────────────────────────────────────

    @GetMapping("/dat-ve")
    @Transactional(readOnly = true)
    public ResponseEntity<?> getAllBookings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String trangThai) {
        String query = q != null ? q.trim().toLowerCase() : "";
        List<DatVe> all = datVeRepository.findAll().stream()
                .filter(dv -> trangThai == null || trangThai.isBlank()
                        || trangThai.equalsIgnoreCase(dv.getTrangThai()))
                .filter(dv -> {
                    if (query.isEmpty()) return true;
                    if (dv.getMaDatVe() != null && dv.getMaDatVe().toLowerCase().contains(query)) return true;
                    if (dv.getNguoiDung() != null) {
                        if (dv.getNguoiDung().getEmail() != null
                                && dv.getNguoiDung().getEmail().toLowerCase().contains(query)) return true;
                        if (dv.getNguoiDung().getHoTen() != null
                                && dv.getNguoiDung().getHoTen().toLowerCase().contains(query)) return true;
                    }
                    if (dv.getLichChieu() != null && dv.getLichChieu().getPhim() != null
                            && dv.getLichChieu().getPhim().getTenPhim() != null
                            && dv.getLichChieu().getPhim().getTenPhim().toLowerCase().contains(query)) return true;
                    return false;
                })
                .sorted(Comparator.comparing(DatVe::getNgayTao, Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());

        int total = all.size();
        int start = page * size;
        int end = Math.min(start + size, total);
        List<DatVe> paged = start < total ? all.subList(start, end) : Collections.emptyList();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("content", paged);
        result.put("totalElements", total);
        result.put("totalPages", Math.max(1, (int) Math.ceil((double) total / size)));
        return ResponseEntity.ok(result);
    }

    /**
     * POST /api/admin/dat-ve/{id}/refund  (RQ73)
     * Marks a paid booking as refunded:
     *   - DatVe.trangThai          → "refunded"
     *   - DatVe.trangThaiThanhToan → "refunded"
     *   - ThanhToan.trangThai      → "refunded"  (all payment records for this booking)
     * Returns 400 if the booking is not currently in "paid" state.
     */
    @PostMapping("/dat-ve/{id}/refund")
    public ResponseEntity<?> refundBooking(@PathVariable Long id) {
        DatVe datVe = datVeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn đặt vé"));

        if (!"paid".equals(datVe.getTrangThaiThanhToan())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Chỉ có thể hoàn tiền cho đơn đã thanh toán (trạng thái 'paid')"));
        }

        // Update DatVe statuses — DB constraint allows: pending/confirmed/cancelled for trangThai
        // and paid/unpaid for trangThaiThanhToan.
        // We set trangThai → "cancelled" (booking is voided) and leave trangThaiThanhToan as "paid"
        // so the payment record remains intact; the refund is tracked on ThanhToan.trangThai.
        datVe.setTrangThai("cancelled");
        datVeRepository.save(datVe);

        // Update all associated ThanhToan records
        List<ThanhToan> payments = thanhToanRepository.findByDatVeId(id);
        for (ThanhToan tt : payments) {
            tt.setTrangThai("refunded");
            tt.setNgayHoan(LocalDateTime.now());
            tt.setLyDoHoan("Admin hoàn tiền");
        }
        if (!payments.isEmpty()) thanhToanRepository.saveAll(payments);

        return ResponseEntity.ok(Map.of(
                "message", "Đã hoàn tiền thành công",
                "maDatVe", datVe.getMaDatVe(),
                "trangThai", datVe.getTrangThai()
        ));
    }

    // ─────────────────────────────────────────────────────────────
    // REVENUE EXPORT (Excel via Apache POI)
    // ─────────────────────────────────────────────────────────────

    /**
     * GET /api/admin/report/export?from=yyyy-MM-dd&to=yyyy-MM-dd
     * Returns an Excel (.xlsx) with revenue summary and booking list.
     */
    @GetMapping("/report/export")
    public ResponseEntity<byte[]> exportReport(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {

        LocalDate dateFrom = (from != null && !from.isBlank())
                ? LocalDate.parse(from) : LocalDate.now().minusDays(30);
        LocalDate dateTo   = (to   != null && !to.isBlank())
                ? LocalDate.parse(to)   : LocalDate.now();

        List<DatVe> paid = datVeRepository.findAll().stream()
                .filter(dv -> "paid".equals(dv.getTrangThaiThanhToan()))
                .filter(dv -> dv.getNgayTao() != null)
                .filter(dv -> {
                    LocalDate day = dv.getNgayTao().toLocalDate();
                    return !day.isBefore(dateFrom) && !day.isAfter(dateTo);
                })
                .sorted(Comparator.comparing(DatVe::getNgayTao))
                .collect(Collectors.toList());

        Map<String, BigDecimal> revenueByDay = new TreeMap<>();
        Map<String, Integer>    ticketsByDay  = new TreeMap<>();
        for (DatVe dv : paid) {
            String day = dv.getNgayTao().toLocalDate().toString();
            revenueByDay.merge(day, dv.getTongTienThanhToan() != null ? dv.getTongTienThanhToan() : BigDecimal.ZERO, BigDecimal::add);
            ticketsByDay.merge(day, dv.getChiTietDatGhe() != null ? dv.getChiTietDatGhe().size() : 0, Integer::sum);
        }

        try (org.apache.poi.xssf.usermodel.XSSFWorkbook wb = new org.apache.poi.xssf.usermodel.XSSFWorkbook()) {

            org.apache.poi.ss.usermodel.CellStyle boldStyle = wb.createCellStyle();
            org.apache.poi.ss.usermodel.Font boldFont = wb.createFont();
            boldFont.setBold(true);
            boldStyle.setFont(boldFont);
            boldStyle.setFillForegroundColor(org.apache.poi.ss.usermodel.IndexedColors.ORANGE.getIndex());
            boldStyle.setFillPattern(org.apache.poi.ss.usermodel.FillPatternType.SOLID_FOREGROUND);

            // Sheet 1 — daily summary
            org.apache.poi.ss.usermodel.Sheet s1 = wb.createSheet("Doanh Thu Theo Ngay");
            org.apache.poi.ss.usermodel.Row h1 = s1.createRow(0);
            for (int i = 0; i < 3; i++) {
                org.apache.poi.ss.usermodel.Cell c = h1.createCell(i);
                c.setCellStyle(boldStyle);
                c.setCellValue(new String[]{"Ngay", "Doanh Thu (VND)", "So Ve"}[i]);
            }
            int rowN = 1;
            BigDecimal grandRev = BigDecimal.ZERO;
            int grandTix = 0;
            for (String day : revenueByDay.keySet()) {
                org.apache.poi.ss.usermodel.Row r = s1.createRow(rowN++);
                r.createCell(0).setCellValue(day);
                BigDecimal rev = revenueByDay.getOrDefault(day, BigDecimal.ZERO);
                r.createCell(1).setCellValue(rev.doubleValue());
                int tix = ticketsByDay.getOrDefault(day, 0);
                r.createCell(2).setCellValue(tix);
                grandRev = grandRev.add(rev);
                grandTix += tix;
            }
            org.apache.poi.ss.usermodel.Row tot = s1.createRow(rowN);
            org.apache.poi.ss.usermodel.Cell t0 = tot.createCell(0); t0.setCellValue("TONG"); t0.setCellStyle(boldStyle);
            org.apache.poi.ss.usermodel.Cell t1 = tot.createCell(1); t1.setCellValue(grandRev.doubleValue()); t1.setCellStyle(boldStyle);
            org.apache.poi.ss.usermodel.Cell t2 = tot.createCell(2); t2.setCellValue(grandTix); t2.setCellStyle(boldStyle);
            for (int i = 0; i < 3; i++) s1.autoSizeColumn(i);

            // Sheet 2 — booking list
            org.apache.poi.ss.usermodel.Sheet s2 = wb.createSheet("Danh Sach Dat Ve");
            org.apache.poi.ss.usermodel.Row h2 = s2.createRow(0);
            String[] cols2 = {"Ma Dat Ve","Ngay Tao","Phim","Tong Tien (VND)","Trang Thai"};
            for (int i = 0; i < cols2.length; i++) {
                org.apache.poi.ss.usermodel.Cell c = h2.createCell(i);
                c.setCellValue(cols2[i]); c.setCellStyle(boldStyle);
            }
            int rn2 = 1;
            for (DatVe dv : paid) {
                org.apache.poi.ss.usermodel.Row r = s2.createRow(rn2++);
                r.createCell(0).setCellValue(dv.getMaDatVe());
                r.createCell(1).setCellValue(dv.getNgayTao().toString());
                String tenPhim = (dv.getLichChieu() != null && dv.getLichChieu().getPhim() != null)
                        ? dv.getLichChieu().getPhim().getTenPhim() : "—";
                r.createCell(2).setCellValue(tenPhim);
                r.createCell(3).setCellValue(dv.getTongTienThanhToan() != null ? dv.getTongTienThanhToan().doubleValue() : 0);
                r.createCell(4).setCellValue(dv.getTrangThai());
            }
            for (int i = 0; i < cols2.length; i++) s2.autoSizeColumn(i);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            wb.write(baos);
            byte[] bytes = baos.toByteArray();

            String filename = "bao-cao-" + dateFrom + "_" + dateTo + ".xlsx";
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.parseMediaType(
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headers.setContentDisposition(
                    org.springframework.http.ContentDisposition.attachment()
                            .filename(filename, java.nio.charset.StandardCharsets.UTF_8)
                            .build());
            headers.setContentLength(bytes.length);
            return new ResponseEntity<>(bytes, headers, HttpStatus.OK);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
