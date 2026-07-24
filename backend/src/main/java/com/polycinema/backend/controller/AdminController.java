package com.polycinema.backend.controller;

import com.polycinema.backend.entity.*;
import com.polycinema.backend.repository.*;
import com.polycinema.backend.service.DatVeService;
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
    private final SeatLockRepository seatLockRepository;
    private final SystemConfigRepository systemConfigRepository;
    private final DatVeService datVeService;
    private final TheLoaiRepository theLoaiRepository;
    private final ChiTietDatGheRepository chiTietDatGheRepository;
    private final DinhDangRepository dinhDangRepository;

    // ─────────────────────────────────────────────────────────────
    // STATS
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
     * Uses DB-level search and pagination — no findAll().
     */
    @GetMapping("/users")
    public ResponseEntity<?> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search) {

        String q = (search != null && !search.isBlank()) ? search.trim() : null;
        PageRequest pr = PageRequest.of(page, size);
        Page<NguoiDung> result = nguoiDungRepository.searchAdmin(q, pr);

        // Mask password hashes before sending to client
        result.getContent().forEach(u -> u.setMatKhauHash(null));

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("content",       result.getContent());
        body.put("totalElements", result.getTotalElements());
        body.put("totalPages",    result.getTotalPages());
        body.put("page",          result.getNumber());
        body.put("size",          result.getSize());

        return ResponseEntity.ok(body);
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
     * Body: standard Phim fields + optional theLoaiIds: [1, 2, 3]
     */
    @PostMapping("/phim")
    public ResponseEntity<?> createPhim(@RequestBody Map<String, Object> body) {
        Phim phim = new Phim();
        phim.setIsDeleted(false);
        applyPhimFields(phim, body);
        return ResponseEntity.status(HttpStatus.CREATED).body(phimRepository.save(phim));
    }

    /**
     * PUT /api/admin/phim/{id}
     * Body: any subset of Phim fields + optional theLoaiIds: [1, 2, 3]
     * If theLoaiIds is present it replaces the entire genre list.
     */
    @PutMapping("/phim/{id}")
    public ResponseEntity<?> updatePhim(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Phim phim = phimRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phim"));
        applyPhimFields(phim, body);
        return ResponseEntity.ok(phimRepository.save(phim));
    }

    /** Helper — applies fields from request body to a Phim entity, including theLoaiIds. */
    @SuppressWarnings("unchecked")
    private void applyPhimFields(Phim phim, Map<String, Object> body) {
        if (body.containsKey("tenPhim"))         phim.setTenPhim((String) body.get("tenPhim"));
        if (body.containsKey("tenPhimTiengAnh")) phim.setTenPhimTiengAnh((String) body.get("tenPhimTiengAnh"));
        if (body.containsKey("daoDien"))         phim.setDaoDien((String) body.get("daoDien"));
        if (body.containsKey("dienVienChinh"))   phim.setDienVienChinh((String) body.get("dienVienChinh"));
        if (body.containsKey("thoiLuong"))       phim.setThoiLuong(((Number) body.get("thoiLuong")).intValue());
        if (body.containsKey("ngonNgu"))         phim.setNgonNgu((String) body.get("ngonNgu"));
        if (body.containsKey("phanLoaiDoTuoi"))  phim.setPhanLoaiDoTuoi((String) body.get("phanLoaiDoTuoi"));
        if (body.containsKey("posterUrl"))       phim.setPosterUrl((String) body.get("posterUrl"));
        if (body.containsKey("trailerUrl"))      phim.setTrailerUrl((String) body.get("trailerUrl"));
        if (body.containsKey("moTa"))            phim.setMoTa((String) body.get("moTa"));
        if (body.containsKey("trangThai"))       phim.setTrangThai((String) body.get("trangThai"));
        if (body.containsKey("ngayCongChieu") && body.get("ngayCongChieu") != null) {
            phim.setNgayCongChieu(java.time.LocalDate.parse((String) body.get("ngayCongChieu")));
        }
        // Replace entire genre list when theLoaiIds is supplied
        if (body.containsKey("theLoaiIds")) {
            List<?> rawIds = (List<?>) body.get("theLoaiIds");
            List<TheLoai> genres;
            if (rawIds == null || rawIds.isEmpty()) {
                genres = new java.util.ArrayList<>();
            } else {
                genres = rawIds.stream()
                        .map(raw -> {
                            Long gid = ((Number) raw).longValue();
                            return theLoaiRepository.findById(gid)
                                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thể loại id=" + gid));
                        })
                        .collect(Collectors.toList());
            }
            // Clear first to avoid duplicates with EAGER-loaded collection
            phim.getTheLoais().clear();
            phim.getTheLoais().addAll(genres);
        }
        // Replace entire format list when dinhDangIds is supplied
        if (body.containsKey("dinhDangIds")) {
            List<?> rawIds = (List<?>) body.get("dinhDangIds");
            List<DinhDang> formats;
            if (rawIds == null || rawIds.isEmpty()) {
                formats = new java.util.ArrayList<>();
            } else {
                formats = rawIds.stream()
                        .map(raw -> {
                            Long fid = ((Number) raw).longValue();
                            return dinhDangRepository.findById(fid)
                                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy định dạng id=" + fid));
                        })
                        .collect(Collectors.toList());
            }
            phim.getDinhDangs().clear();
            phim.getDinhDangs().addAll(formats);
        }
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
     * GET /api/admin/lich-chieu?page=0&size=20&dateFrom=yyyy-MM-dd&dateTo=yyyy-MM-dd
     * Uses DB-level pagination and date-range filter — no findAll().
     */
    @GetMapping("/lich-chieu")
    public ResponseEntity<?> getAllLichChieu(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String dateFrom,
            @RequestParam(required = false) String dateTo) {

        LocalDateTime from = (dateFrom != null && !dateFrom.isBlank())
                ? LocalDate.parse(dateFrom).atStartOfDay() : null;
        LocalDateTime to = (dateTo != null && !dateTo.isBlank())
                ? LocalDate.parse(dateTo).plusDays(1).atStartOfDay() : null;

        PageRequest pr = PageRequest.of(page, size);
        Page<LichChieu> result = lichChieuRepository.findAdminPage(from, to, pr);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("content",       result.getContent());
        body.put("totalElements", result.getTotalElements());
        body.put("totalPages",    result.getTotalPages());
        body.put("page",          result.getNumber());
        body.put("size",          result.getSize());

        return ResponseEntity.ok(body);
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

            java.util.Optional<LichChieu> conflicting = lichChieuRepository.findAll().stream()
                    .filter(lc -> !Boolean.TRUE.equals(lc.getIsDeleted()))
                    .filter(lc -> lc.getPhongChieu() != null && phongId.equals(lc.getPhongChieu().getId()))
                    .filter(lc -> lc.getThoiGianBatDau() != null && lc.getThoiGianKetThuc() != null
                            && start.isBefore(lc.getThoiGianKetThuc())
                            && end.isAfter(lc.getThoiGianBatDau()))
                    .findFirst();

            if (conflicting.isPresent()) {
                LichChieu cx = conflicting.get();
                String roomName = phongChieu.getTenPhong() != null ? phongChieu.getTenPhong() : "phòng này";
                String cxStart  = cx.getThoiGianBatDau().toLocalTime().toString().substring(0, 5);
                String cxEnd    = cx.getThoiGianKetThuc().toLocalTime().toString().substring(0, 5);
                return ResponseEntity.badRequest()
                        .body(Map.of("message", roomName + " đã có suất chiếu từ " + cxStart + "–" + cxEnd
                                + " vào ngày này, không thể tạo suất chiếu trùng giờ"));
            }
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(lichChieuRepository.save(lichChieu));
    }

    /**
     * POST /api/admin/lich-chieu/batch
     * Creates the same showtime (film/room/time-of-day/price) across multiple dates in one call.
     * Each date is checked independently for room conflicts.
     * Returns a summary: which dates succeeded and which failed (with reason).
     * Body:
     * {
     *   "phimId": 1,
     *   "phongChieuId": 1,
     *   "startTime": "09:00",   // HH:mm
     *   "endTime":   "11:15",   // HH:mm — if end < start, end is on the NEXT calendar day
     *   "giaCoBan": 80000,
     *   "dates": ["2026-07-23","2026-07-24","2026-07-25"]
     * }
     * Response: { succeeded: [...], failed: [{date, reason}] }
     */
    @PostMapping("/lich-chieu/batch")
    @SuppressWarnings("unchecked")
    public ResponseEntity<?> batchCreateLichChieu(@RequestBody Map<String, Object> body) {
        Long phimId    = body.get("phimId")    != null ? ((Number) body.get("phimId")).longValue()    : null;
        Long phongId   = body.get("phongChieuId") != null ? ((Number) body.get("phongChieuId")).longValue() : null;
        String startTime = (String) body.get("startTime");  // "HH:mm"
        String endTime   = (String) body.get("endTime");    // "HH:mm"
        java.math.BigDecimal giaCoBan = body.get("giaCoBan") != null
                ? new java.math.BigDecimal(body.get("giaCoBan").toString()) : java.math.BigDecimal.ZERO;
        java.util.List<String> dates = body.get("dates") instanceof java.util.List
                ? (java.util.List<String>) body.get("dates") : java.util.List.of();

        if (phimId == null || phongId == null || startTime == null || endTime == null || dates.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Thiếu phimId, phongChieuId, startTime, endTime hoặc dates"));
        }

        Phim phim = phimRepository.findById(phimId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phim id=" + phimId));
        PhongChieu phong = phongChieuRepository.findById(phongId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phòng chiếu id=" + phongId));

        // Parse HH:mm into hours+minutes
        String[] sParts = startTime.split(":");
        String[] eParts = endTime.split(":");
        int startH = Integer.parseInt(sParts[0]), startM = Integer.parseInt(sParts[1]);
        int endH   = Integer.parseInt(eParts[0]),  endM   = Integer.parseInt(eParts[1]);

        // Pre-load all active schedules in this room once — reused per date to avoid N+1
        java.util.List<LichChieu> roomSchedules = lichChieuRepository.findAll().stream()
                .filter(lc -> !Boolean.TRUE.equals(lc.getIsDeleted()))
                .filter(lc -> lc.getPhongChieu() != null && phongId.equals(lc.getPhongChieu().getId()))
                .collect(java.util.stream.Collectors.toList());

        java.util.List<Map<String, Object>> succeeded = new java.util.ArrayList<>();
        java.util.List<Map<String, Object>> failed    = new java.util.ArrayList<>();

        for (String dateStr : dates) {
            try {
                java.time.LocalDate date = java.time.LocalDate.parse(dateStr);
                java.time.LocalDateTime start = date.atTime(startH, startM);

                // If end time <= start time, the showtime ends the NEXT calendar day
                java.time.LocalDateTime end;
                if (endH < startH || (endH == startH && endM <= startM)) {
                    end = date.plusDays(1).atTime(endH, endM);
                } else {
                    end = date.atTime(endH, endM);
                }

                // Overlap check against pre-loaded room schedules
                java.util.Optional<LichChieu> conflict = roomSchedules.stream()
                        .filter(lc -> lc.getThoiGianBatDau() != null && lc.getThoiGianKetThuc() != null
                                && start.isBefore(lc.getThoiGianKetThuc())
                                && end.isAfter(lc.getThoiGianBatDau()))
                        .findFirst();

                if (conflict.isPresent()) {
                    LichChieu cx = conflict.get();
                    String cxS = cx.getThoiGianBatDau().toLocalTime().toString().substring(0, 5);
                    String cxE = cx.getThoiGianKetThuc().toLocalTime().toString().substring(0, 5);
                    Map<String, Object> f = new java.util.LinkedHashMap<>();
                    f.put("date", dateStr);
                    f.put("reason", phong.getTenPhong() + " đã có suất chiếu từ " + cxS + "–" + cxE + " vào ngày này");
                    failed.add(f);
                    continue;
                }

                // Save
                LichChieu lc = new LichChieu();
                lc.setPhim(phim);
                lc.setPhongChieu(phong);
                lc.setThoiGianBatDau(start);
                lc.setThoiGianKetThuc(end);
                lc.setGiaCoBan(giaCoBan);
                lc.setTrangThai("active");
                lc.setIsDeleted(false);
                LichChieu saved = lichChieuRepository.save(lc);

                // Add to in-memory list so subsequent dates in this batch also see it
                roomSchedules.add(saved);

                Map<String, Object> s = new java.util.LinkedHashMap<>();
                s.put("date", dateStr);
                s.put("id",   saved.getId());
                s.put("thoiGianBatDau",  saved.getThoiGianBatDau().toString());
                s.put("thoiGianKetThuc", saved.getThoiGianKetThuc().toString());
                succeeded.add(s);

            } catch (Exception e) {
                Map<String, Object> f = new java.util.LinkedHashMap<>();
                f.put("date", dateStr);
                f.put("reason", e.getMessage());
                failed.add(f);
            }
        }

        Map<String, Object> result = new java.util.LinkedHashMap<>();
        result.put("succeeded", succeeded);
        result.put("failed",    failed);
        result.put("totalRequested",  dates.size());
        result.put("totalSucceeded",  succeeded.size());
        result.put("totalFailed",     failed.size());
        return ResponseEntity.ok(result);
    }

    /**
     * POST /api/admin/lich-chieu/auto-generate
     * Auto-computes N non-overlapping showtimes for a single day across one or more rooms.
     * ALL-OR-NOTHING: if N doesn't fit, nothing is saved; returns how many could fit.
     *
     * Body:
     * {
     *   "phimId":        1,
     *   "phongChieuIds": [1, 2],      // rooms to fill (in order, round-robin)
     *   "date":          "2026-08-01",
     *   "soSuat":        6,            // desired showtime count
     *   "openTime":      "09:00",      // window start HH:mm
     *   "closeTime":     "23:00",      // window end HH:mm
     *   "bufferMinutes": 15,           // buffer between end of one and start of next (default 15)
     *   "giaCoBan":      80000
     * }
     * Success response: { succeeded: [...], totalCreated: N }
     * Shortfall response (HTTP 422):
     *   { message: "Chỉ có thể xếp được X/N suất trong khung giờ này", canFit: X, requested: N, preview: [...] }
     */
    @PostMapping("/lich-chieu/auto-generate")
    @SuppressWarnings("unchecked")
    public ResponseEntity<?> autoGenerateLichChieu(@RequestBody Map<String, Object> body) {
        Long phimId       = body.get("phimId")  != null ? ((Number) body.get("phimId")).longValue()  : null;
        String dateStr    = (String) body.get("date");
        String openTime   = (String) body.get("openTime");
        String closeTime  = (String) body.get("closeTime");
        int soSuat        = body.get("soSuat")  != null ? ((Number) body.get("soSuat")).intValue()    : 0;
        int bufferMin     = body.get("bufferMinutes") != null ? ((Number) body.get("bufferMinutes")).intValue() : 15;
        java.math.BigDecimal giaCoBan = body.get("giaCoBan") != null
                ? new java.math.BigDecimal(body.get("giaCoBan").toString()) : java.math.BigDecimal.valueOf(80000);
        java.util.List<?> rawRoomIds  = body.get("phongChieuIds") instanceof java.util.List
                ? (java.util.List<?>) body.get("phongChieuIds") : java.util.List.of();

        if (phimId == null || dateStr == null || openTime == null || closeTime == null
                || soSuat <= 0 || rawRoomIds.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Thiếu phimId, phongChieuIds, date, openTime, closeTime hoặc soSuat"));
        }

        Phim phim = phimRepository.findById(phimId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phim id=" + phimId));
        if (phim.getThoiLuong() == null || phim.getThoiLuong() <= 0) {
            return ResponseEntity.badRequest().body(Map.of("message", "Phim chưa có thời lượng (thoiLuong)"));
        }

        // Resolve rooms in order
        java.util.List<PhongChieu> rooms = new java.util.ArrayList<>();
        for (Object raw : rawRoomIds) {
            Long rid = ((Number) raw).longValue();
            rooms.add(phongChieuRepository.findById(rid)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phòng chiếu id=" + rid)));
        }

        // Parse window
        java.time.LocalDate date = java.time.LocalDate.parse(dateStr);
        String[] op = openTime.split(":");
        String[] cp = closeTime.split(":");
        int openH = Integer.parseInt(op[0]), openM = Integer.parseInt(op[1]);
        int closeH = Integer.parseInt(cp[0]), closeM = Integer.parseInt(cp[1]);
        java.time.LocalDateTime windowStart = date.atTime(openH, openM);
        // If closeTime is at or before openTime in minutes-of-day, the window crosses midnight
        // (e.g. openTime=16:00, closeTime=00:00 → close is next-day midnight)
        java.time.LocalDateTime windowEnd;
        if (closeH * 60 + closeM <= openH * 60 + openM) {
            windowEnd = date.plusDays(1).atTime(closeH, closeM);
        } else {
            windowEnd = date.atTime(closeH, closeM);
        }
        int showDurationMin = phim.getThoiLuong();
        int slotMin = showDurationMin + bufferMin; // total slot per showing

        // Pre-load existing schedules for all rooms on this date
        java.util.Map<Long, java.util.List<LichChieu>> existingByRoom = new java.util.LinkedHashMap<>();
        for (PhongChieu room : rooms) {
            existingByRoom.put(room.getId(), lichChieuRepository.findAll().stream()
                    .filter(lc -> !Boolean.TRUE.equals(lc.getIsDeleted()))
                    .filter(lc -> lc.getPhongChieu() != null && room.getId().equals(lc.getPhongChieu().getId()))
                    .collect(java.util.stream.Collectors.toList()));
        }

        // ── Greedy placement algorithm ────────────────────────────────────────
        // Iterate rooms round-robin, advance cursor within a room past any existing
        // showtime that would conflict, and place a slot when free.
        java.util.List<Map<String, Object>> preview   = new java.util.ArrayList<>();
        java.util.List<Map<String, Object>> succeeded = new java.util.ArrayList<>();

        // Pointer: next-available start time per room
        java.util.Map<Long, java.time.LocalDateTime> cursors = new java.util.LinkedHashMap<>();
        for (PhongChieu room : rooms) cursors.put(room.getId(), windowStart);

        int placed = 0;
        int maxIterations = soSuat * rooms.size() * 5; // safety cap
        int iter = 0;

        while (placed < soSuat && iter++ < maxIterations) {
            // Try each room in round-robin order for the next slot
            boolean anyRoomAdvanced = false;
            for (PhongChieu room : rooms) {
                if (placed >= soSuat) break;
                java.time.LocalDateTime cursor = cursors.get(room.getId());
                java.time.LocalDateTime slotEnd = cursor.plusMinutes(showDurationMin);

                // Must fit within window
                if (!slotEnd.isBefore(windowEnd) && !slotEnd.equals(windowEnd)) break;

                // Check conflict with existing schedules in this room
                java.time.LocalDateTime fCursor = cursor;
                java.util.Optional<LichChieu> conflict = existingByRoom.get(room.getId()).stream()
                        .filter(lc -> lc.getThoiGianBatDau() != null && lc.getThoiGianKetThuc() != null
                                && fCursor.isBefore(lc.getThoiGianKetThuc())
                                && slotEnd.isAfter(lc.getThoiGianBatDau()))
                        .findFirst();

                if (conflict.isPresent()) {
                    // Advance cursor past the conflicting slot + buffer
                    java.time.LocalDateTime newCursor = conflict.get().getThoiGianKetThuc().plusMinutes(bufferMin);
                    cursors.put(room.getId(), newCursor);
                    anyRoomAdvanced = true;
                    continue; // retry this room on next iteration
                }

                // Slot fits — record it
                Map<String, Object> slot = new java.util.LinkedHashMap<>();
                slot.put("roomId",           room.getId());
                slot.put("tenPhong",         room.getTenPhong());
                slot.put("thoiGianBatDau",   cursor.toString());
                slot.put("thoiGianKetThuc",  slotEnd.toString());
                preview.add(slot);

                // Advance cursor for this room
                cursors.put(room.getId(), cursor.plusMinutes(slotMin));
                placed++;
                anyRoomAdvanced = true;
            }
            if (!anyRoomAdvanced) break; // all rooms exhausted
        }

        // Shortfall check — fail entirely if N doesn't fit
        if (placed < soSuat) {
            return ResponseEntity.status(422)
                    .body(Map.of(
                            "message",   "Chỉ có thể xếp được " + placed + "/" + soSuat + " suất trong khung giờ này",
                            "canFit",    placed,
                            "requested", soSuat,
                            "preview",   preview));
        }

        // All N fit — save atomically (inside the same request thread, same Hibernate session)
        for (int i = 0; i < preview.size(); i++) {
            Map<String, Object> slot = preview.get(i);
            Long roomId = ((Number) slot.get("roomId")).longValue();
            PhongChieu room = rooms.stream().filter(r -> r.getId().equals(roomId)).findFirst().orElseThrow();

            LichChieu lc = new LichChieu();
            lc.setPhim(phim);
            lc.setPhongChieu(room);
            lc.setThoiGianBatDau(java.time.LocalDateTime.parse((String) slot.get("thoiGianBatDau")));
            lc.setThoiGianKetThuc(java.time.LocalDateTime.parse((String) slot.get("thoiGianKetThuc")));
            lc.setGiaCoBan(giaCoBan);
            lc.setTrangThai("active");
            lc.setIsDeleted(false);
            LichChieu saved = lichChieuRepository.save(lc);

            Map<String, Object> s = new java.util.LinkedHashMap<>(slot);
            s.put("id", saved.getId());
            succeeded.add(s);
        }

        return ResponseEntity.ok(Map.of(
                "succeeded",    succeeded,
                "totalCreated", succeeded.size(),
                "date",         dateStr,
                "phim",         phim.getTenPhim()));
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
            final PhongChieu finalPhong = phong;
            java.util.Optional<LichChieu> conflicting = lichChieuRepository.findAll().stream()
                    .filter(other -> !Boolean.TRUE.equals(other.getIsDeleted()))
                    .filter(other -> !other.getId().equals(id))
                    .filter(other -> other.getPhongChieu() != null && phongId.equals(other.getPhongChieu().getId()))
                    .filter(other -> other.getThoiGianBatDau() != null && other.getThoiGianKetThuc() != null
                            && start.isBefore(other.getThoiGianKetThuc())
                            && end.isAfter(other.getThoiGianBatDau()))
                    .findFirst();

            if (conflicting.isPresent()) {
                LichChieu cx = conflicting.get();
                String roomName = finalPhong.getTenPhong() != null ? finalPhong.getTenPhong() : "phòng này";
                String cxStart  = cx.getThoiGianBatDau().toLocalTime().toString().substring(0, 5);
                String cxEnd    = cx.getThoiGianKetThuc().toLocalTime().toString().substring(0, 5);
                return ResponseEntity.badRequest()
                        .body(Map.of("message", roomName + " đã có suất chiếu từ " + cxStart + "–" + cxEnd
                                + " vào ngày này, không thể tạo suất chiếu trùng giờ"));
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
        if (body.getTenRap()   != null) rap.setTenRap(body.getTenRap());
        if (body.getDiaChi()   != null) rap.setDiaChi(body.getDiaChi());
        if (body.getTrangThai()!= null) rap.setTrangThai(body.getTrangThai());
        if (body.getThanhPho() != null) rap.setThanhPho(body.getThanhPho());
        if (body.getLatitude() != null) rap.setLatitude(body.getLatitude());
        if (body.getLongitude()!= null) rap.setLongitude(body.getLongitude());
        if (body.getHinhAnh()  != null) rap.setHinhAnh(body.getHinhAnh());
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
    // SEAT (GHE NGOI) — read-only list for admin seat map display
    // ─────────────────────────────────────────────────────────────

    /**
     * GET /api/admin/ghe-ngoi?phongChieuId={id}
     * Returns all seats for the given room.
     * Used by the admin "Ghế ngồi" tab seat-map display.
     */
    @GetMapping("/ghe-ngoi")
    public ResponseEntity<?> getGheByPhong(@RequestParam Long phongChieuId) {
        return ResponseEntity.ok(gheNgoiRepository.findByPhongChieuId(phongChieuId));
    }

    /**
     * PUT /api/admin/ghe-ngoi/{id}
     * Updates loaiGhe and/or heSoGia on a single seat.
     * Body: { "loaiGhe": "vip" | "thuong" | "cap_doi", "heSoGia": 1.50 }
     */
    @PutMapping("/ghe-ngoi/{id}")
    public ResponseEntity<?> updateGhe(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        GheNgoi ghe = gheNgoiRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy ghế"));
        if (body.containsKey("loaiGhe")) ghe.setLoaiGhe((String) body.get("loaiGhe"));
        if (body.containsKey("heSoGia")) ghe.setHeSoGia(new java.math.BigDecimal(body.get("heSoGia").toString()));
        return ResponseEntity.ok(gheNgoiRepository.save(ghe));
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
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String trangThai) {

        // Normalise: empty string → null so JPQL IS NULL check matches
        String query    = (q         != null && !q.isBlank())         ? q.trim()         : null;
        String status   = (trangThai != null && !trangThai.isBlank()) ? trangThai.trim() : null;

        PageRequest pr = PageRequest.of(page, size);
        Page<DatVe> result = datVeRepository.searchAdmin(query, status, pr);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("content",       result.getContent());
        body.put("totalElements", result.getTotalElements());
        body.put("totalPages",    Math.max(1, result.getTotalPages()));
        body.put("page",          result.getNumber());
        body.put("size",          result.getSize());

        return ResponseEntity.ok(body);
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

    // ─────────────────────────────────────────────────────────────
    // ADMIN BOOKING CANCEL
    // ─────────────────────────────────────────────────────────────

    /**
     * PUT /api/admin/dat-ve/{maDatVe}/cancel
     * Admin force-cancels any booking regardless of owner.
     * Releases seat locks and decrements promo usage counter.
     */
    @PutMapping("/dat-ve/{maDatVe}/cancel")
    public ResponseEntity<?> adminCancelBooking(@PathVariable String maDatVe) {
        try {
            datVeService.cancelBookingByAdmin(maDatVe);
            return ResponseEntity.ok(Map.of("message", "Đã hủy đơn đặt vé " + maDatVe));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Lỗi hủy vé: " + e.getMessage()));
        }
    }

    // ─────────────────────────────────────────────────────────────
    // SEAT LOCK ADMIN MANAGEMENT
    // ─────────────────────────────────────────────────────────────

    /**
     * GET /api/admin/seat-locks?lichChieuId=X
     * Returns all active seat locks for a showtime with user info.
     */
    @GetMapping("/seat-locks")
    public ResponseEntity<?> getSeatLocks(@RequestParam Long lichChieuId) {
        LocalDateTime now = LocalDateTime.now();
        List<SeatLock> locks = seatLockRepository.findActiveByLichChieu(lichChieuId, now);
        // Enrich with seat label and user info
        List<Map<String, Object>> result = locks.stream().map(lock -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id",          lock.getId());
            m.put("gheNgoiId",   lock.getGheNgoiId());
            m.put("lichChieuId", lock.getLichChieuId());
            m.put("nguoiDungId", lock.getNguoiDungId());
            m.put("lockedAt",    lock.getLockedAt());
            m.put("expiresAt",   lock.getExpiresAt());
            m.put("maDatVe",     lock.getMaDatVe());
            // Attach seat label (e.g. "B6") from GheNgoi
            gheNgoiRepository.findById(lock.getGheNgoiId()).ifPresent(ghe -> {
                m.put("hangGhe",  (ghe.getHangGhe() != null ? ghe.getHangGhe().trim() : ""));
                m.put("soGhe",    ghe.getSoGhe());
                m.put("loaiGhe",  ghe.getLoaiGhe());
                m.put("seatLabel", (ghe.getHangGhe() != null ? ghe.getHangGhe().trim() : "") + ghe.getSoGhe());
            });
            // Attach user email/name if available
            if (lock.getNguoiDungId() != null) {
                nguoiDungRepository.findById(lock.getNguoiDungId()).ifPresent(u -> {
                    m.put("email", u.getEmail());
                    m.put("hoTen", u.getHoTen());
                });
            }
            return m;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    /**
     * DELETE /api/admin/seat-locks/{lockId}
     * Force-releases a specific seat lock.
     */
    @DeleteMapping("/seat-locks/{lockId}")
    public ResponseEntity<?> releaseSeatLock(@PathVariable Long lockId) {
        if (!seatLockRepository.existsById(lockId)) {
            return ResponseEntity.notFound().build();
        }
        seatLockRepository.deleteById(lockId);
        return ResponseEntity.ok(Map.of("message", "Đã giải phóng ghế"));
    }

    /**
     * GET /api/admin/config/seat-lock-minutes
     * Returns current lock duration in minutes.
     */
    @GetMapping("/config/seat-lock-minutes")
    public ResponseEntity<?> getSeatLockMinutes() {
        int minutes = systemConfigRepository.findById("SEAT_LOCK_MINUTES")
                .map(c -> {
                    try { return Integer.parseInt(c.getConfigValue()); }
                    catch (NumberFormatException e) { return 10; }
                })
                .orElse(10);
        return ResponseEntity.ok(Map.of("minutes", minutes));
    }

    /**
     * PUT /api/admin/config/seat-lock-minutes
     * Body: { "minutes": 15 }
     * Updates the SEAT_LOCK_MINUTES system config.
     */
    @PutMapping("/config/seat-lock-minutes")
    public ResponseEntity<?> setSeatLockMinutes(@RequestBody Map<String, Object> body) {
        Object raw = body.get("minutes");
        if (raw == null) return ResponseEntity.badRequest().body("Thiếu trường 'minutes'");
        int minutes;
        try { minutes = ((Number) raw).intValue(); }
        catch (ClassCastException e) { return ResponseEntity.badRequest().body("minutes phải là số nguyên"); }
        if (minutes < 1 || minutes > 60) {
            return ResponseEntity.badRequest().body("minutes phải trong khoảng 1–60");
        }
        SystemConfig config = systemConfigRepository.findById("SEAT_LOCK_MINUTES")
                .orElseGet(() -> { SystemConfig c = new SystemConfig(); c.setConfigKey("SEAT_LOCK_MINUTES"); return c; });
        config.setConfigValue(String.valueOf(minutes));
        systemConfigRepository.save(config);
        return ResponseEntity.ok(Map.of("minutes", minutes));
    }

    // ─────────────────────────────────────────────────────────────
    // SEAT MAP — combined seat status for a showtime (admin view)
    // ─────────────────────────────────────────────────────────────

    /**
     * GET /api/admin/seat-map?lichChieuId={id}
     * Returns all seats for the showtime with their current status.
     * Status priority: locked > booked > available
     * Each locked seat includes lockId so the admin can force-release it.
     */
    @GetMapping("/seat-map")
    public ResponseEntity<?> getSeatMap(@RequestParam Long lichChieuId) {
        // 1. Resolve showtime → room
        LichChieu lc = lichChieuRepository.findById(lichChieuId).orElse(null);
        if (lc == null) return ResponseEntity.notFound().build();
        Long phongChieuId = lc.getPhongChieu().getId();

        // 2. All seats in the room
        List<GheNgoi> allSeats = gheNgoiRepository.findByPhongChieuId(phongChieuId);

        // 3. Active seat locks for this showtime
        LocalDateTime now = LocalDateTime.now();
        List<SeatLock> activeLocks = seatLockRepository.findActiveByLichChieu(lichChieuId, now);
        // Map: gheNgoiId → SeatLock (for O(1) lookup)
        Map<Long, SeatLock> lockMap = activeLocks.stream()
                .collect(Collectors.toMap(SeatLock::getGheNgoiId, s -> s, (a, b) -> a));

        // 4. Booked seats from ChiTietDatGhe linked to confirmed/paid bookings
        //    (only non-cancelled bookings count as holding the seat)
        Set<Long> bookedSeatIds = chiTietDatGheRepository.findByLichChieuId(lichChieuId)
                .stream()
                .filter(ct -> {
                    DatVe dv = ct.getDatVe();
                    return dv != null
                        && !"cancelled".equals(dv.getTrangThai())
                        && ("confirmed".equals(dv.getTrangThai())
                            || "pending".equals(dv.getTrangThai())
                            || "paid".equals(dv.getTrangThaiThanhToan()));
                })
                .map(ct -> ct.getGheNgoi().getId())
                .collect(Collectors.toSet());

        // 5. Build response
        List<Map<String, Object>> result = allSeats.stream().map(seat -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("gheNgoiId", seat.getId());
            m.put("hangGhe",   (seat.getHangGhe() != null ? seat.getHangGhe().trim() : ""));
            m.put("soGhe",     seat.getSoGhe());
            m.put("loaiGhe",   seat.getLoaiGhe() != null ? seat.getLoaiGhe() : "thường");

            if (lockMap.containsKey(seat.getId())) {
                SeatLock lock = lockMap.get(seat.getId());
                m.put("status",         "locked");
                m.put("lockId",         lock.getId());
                m.put("expiresAt",      lock.getExpiresAt().toString());
                m.put("lockedByUserId", lock.getNguoiDungId());
                m.put("maDatVe",        lock.getMaDatVe());
            } else if (bookedSeatIds.contains(seat.getId())) {
                m.put("status", "booked");
            } else {
                m.put("status", "available");
            }
            return m;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }
}
