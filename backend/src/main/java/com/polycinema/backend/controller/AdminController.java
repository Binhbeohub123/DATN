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

        // Format-compatibility check: room's dinhDang must be in the movie's dinhDangs list
        java.util.Optional<String> formatErr = validateDinhDangCompatibility(phim, phongChieu);
        if (formatErr.isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("message", formatErr.get()));
        }

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
     * POST /api/admin/lich-chieu/import-preview
     * Accepts a multipart Excel file (.xlsx) with columns:
     *   Tên phim | Tên phòng chiếu | Ngày chiếu (dd/MM/yyyy) | Giờ bắt đầu (HH:mm) | Giá cơ bản
     *
     * Parses each data row, validates (movie lookup, room lookup, format compatibility,
     * room+time conflict — both against DB and against earlier rows in the same file),
     * and returns a preview array without saving anything.
     *
     * Response: [{ row, tenPhim, tenPhong, ngayChieu, gioChieu, giaCoBan, valid, reason }]
     */
    @PostMapping("/lich-chieu/import-preview")
    public ResponseEntity<?> importPreviewLichChieu(
            @org.springframework.web.bind.annotation.RequestParam("file")
            org.springframework.web.multipart.MultipartFile file) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "File Excel không được để trống"));
        }

        java.util.List<Map<String, Object>> preview = new java.util.ArrayList<>();

        // In-memory list of validated slots from this file — used for intra-file conflict checks
        java.util.List<java.time.LocalDateTime[]> pendingSlots = new java.util.ArrayList<>(); // [start, end, phongId]
        java.util.List<Long> pendingPhongIds = new java.util.ArrayList<>();

        try (org.apache.poi.xssf.usermodel.XSSFWorkbook wb =
                     new org.apache.poi.xssf.usermodel.XSSFWorkbook(file.getInputStream())) {

            org.apache.poi.ss.usermodel.Sheet sheet = wb.getSheetAt(0);
            if (sheet == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "File Excel không có sheet nào"));
            }

            java.time.format.DateTimeFormatter dateFmt =
                    java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");

            int rowNum = 0;
            for (org.apache.poi.ss.usermodel.Row row : sheet) {
                rowNum++;
                if (rowNum == 1) continue; // skip header

                // Helper: read cell as trimmed string regardless of cell type
                java.util.function.Function<Integer, String> cell = (col) -> {
                    org.apache.poi.ss.usermodel.Cell c = row.getCell(col,
                            org.apache.poi.ss.usermodel.Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                    if (c == null) return "";
                    switch (c.getCellType()) {
                        case STRING:  return c.getStringCellValue().trim();
                        case NUMERIC:
                            if (org.apache.poi.ss.usermodel.DateUtil.isCellDateFormatted(c)) {
                                // Date cell — format as dd/MM/yyyy
                                java.util.Date d = c.getDateCellValue();
                                java.time.LocalDate ld = d.toInstant()
                                        .atZone(java.time.ZoneId.systemDefault()).toLocalDate();
                                return String.format("%02d/%02d/%04d",
                                        ld.getDayOfMonth(), ld.getMonthValue(), ld.getYear());
                            }
                            // Numeric — convert to plain integer string if whole number
                            double dv = c.getNumericCellValue();
                            return dv == Math.floor(dv) ? String.valueOf((long) dv) : String.valueOf(dv);
                        case BOOLEAN: return String.valueOf(c.getBooleanCellValue());
                        case FORMULA:
                            try { return String.valueOf(c.getStringCellValue()).trim(); }
                            catch (Exception ignored) {
                                return String.valueOf(c.getNumericCellValue());
                            }
                        default: return "";
                    }
                };

                String tenRap    = cell.apply(0);
                String tenPhim   = cell.apply(1);
                String tenPhong  = cell.apply(2);
                String ngayChieu = cell.apply(3);
                String gioChieu  = cell.apply(4);
                String giaRaw    = cell.apply(5);

                Map<String, Object> entry = new java.util.LinkedHashMap<>();
                entry.put("row",       rowNum);
                entry.put("tenRap",    tenRap);
                entry.put("tenPhim",   tenPhim);
                entry.put("tenPhong",  tenPhong);
                entry.put("ngayChieu", ngayChieu);
                entry.put("gioChieu",  gioChieu);
                entry.put("giaCoBan",  giaRaw);

                // Skip completely empty rows
                if (tenRap.isEmpty() && tenPhim.isEmpty() && tenPhong.isEmpty() && ngayChieu.isEmpty() && gioChieu.isEmpty()) {
                    continue;
                }

                // Validate required fields
                if (tenRap.isEmpty() || tenPhim.isEmpty() || tenPhong.isEmpty() || ngayChieu.isEmpty() || gioChieu.isEmpty()) {
                    entry.put("valid",  false);
                    entry.put("reason", "Dong " + rowNum + ": Thieu thong tin bat buoc (ten rap, ten phim, ten phong, ngay, gio)");
                    preview.add(entry);
                    continue;
                }

                // Parse date
                java.time.LocalDate date;
                try {
                    date = java.time.LocalDate.parse(ngayChieu.trim(), dateFmt);
                } catch (Exception e) {
                    entry.put("valid",  false);
                    entry.put("reason", "Dong " + rowNum + ": Ngay chieu sai dinh dang, can dd/MM/yyyy");
                    preview.add(entry);
                    continue;
                }

                // Parse time
                java.time.LocalTime time;
                try {
                    time = java.time.LocalTime.parse(gioChieu.trim());
                } catch (Exception e) {
                    entry.put("valid",  false);
                    entry.put("reason", "Dong " + rowNum + ": Gio chieu sai dinh dang, can HH:mm");
                    preview.add(entry);
                    continue;
                }

                // Parse price
                java.math.BigDecimal giaCoBan;
                try {
                    giaCoBan = new java.math.BigDecimal(giaRaw.replaceAll("[^0-9.]", ""));
                } catch (Exception e) {
                    giaCoBan = java.math.BigDecimal.ZERO;
                }
                entry.put("giaCoBan", giaCoBan);

                // Lookup movie by exact name (case-insensitive)
                final String tenPhimFinal = tenPhim;
                java.util.Optional<Phim> phimOpt = phimRepository.findByIsDeletedFalse().stream()
                        .filter(p -> p.getTenPhim() != null && p.getTenPhim().trim().equalsIgnoreCase(tenPhimFinal.trim()))
                        .findFirst();
                if (phimOpt.isEmpty()) {
                    entry.put("valid",  false);
                    entry.put("reason", "Dong " + rowNum + ": Khong tim thay phim [" + tenPhim + "]");
                    preview.add(entry);
                    continue;
                }
                Phim phim = phimOpt.get();

                if (phim.getThoiLuong() == null || phim.getThoiLuong() <= 0) {
                    entry.put("valid",  false);
                    entry.put("reason", "Dong " + rowNum + ": Phim [" + tenPhim + "] chua co thoi luong");
                    preview.add(entry);
                    continue;
                }

                // Lookup room by cinema name + room name (both case-insensitive)
                final String tenRapFinal   = tenRap;
                final String tenPhongFinal = tenPhong;

                // Step 1: find the cinema by name
                java.util.Optional<PhongChieu> anyInRap = phongChieuRepository.findAll().stream()
                        .filter(p -> p.getRapChieu() != null
                                && p.getRapChieu().getTenRap() != null
                                && p.getRapChieu().getTenRap().trim().equalsIgnoreCase(tenRapFinal.trim()))
                        .findFirst();
                if (anyInRap.isEmpty()) {
                    entry.put("valid",  false);
                    entry.put("reason", "Dong " + rowNum + ": Khong tim thay rap chieu [" + tenRap + "]");
                    preview.add(entry);
                    continue;
                }

                // Step 2: find the room within that cinema
                java.util.Optional<PhongChieu> phongOpt = phongChieuRepository.findAll().stream()
                        .filter(p -> Boolean.TRUE.equals(p.getTrangThai()))
                        .filter(p -> p.getRapChieu() != null
                                && p.getRapChieu().getTenRap() != null
                                && p.getRapChieu().getTenRap().trim().equalsIgnoreCase(tenRapFinal.trim()))
                        .filter(p -> p.getTenPhong() != null && p.getTenPhong().trim().equalsIgnoreCase(tenPhongFinal.trim()))
                        .findFirst();
                if (phongOpt.isEmpty()) {
                    entry.put("valid",  false);
                    entry.put("reason", "Dong " + rowNum + ": Khong tim thay phong chieu [" + tenPhong + "] trong rap [" + tenRap + "]");
                    preview.add(entry);
                    continue;
                }
                PhongChieu phong = phongOpt.get();

                // Compute start/end times
                java.time.LocalDateTime start = java.time.LocalDateTime.of(date, time);
                java.time.LocalDateTime end   = start.plusMinutes(phim.getThoiLuong());

                // Format-compatibility check
                java.util.Optional<String> fmtErr = validateDinhDangCompatibility(phim, phong);
                if (fmtErr.isPresent()) {
                    entry.put("valid",  false);
                    entry.put("reason", "Dong " + rowNum + ": " + fmtErr.get());
                    preview.add(entry);
                    continue;
                }

                // Room+time conflict check against DB
                Long phongId = phong.getId();
                java.util.Optional<LichChieu> dbConflict = lichChieuRepository.findAll().stream()
                        .filter(lc -> !Boolean.TRUE.equals(lc.getIsDeleted()))
                        .filter(lc -> lc.getPhongChieu() != null && phongId.equals(lc.getPhongChieu().getId()))
                        .filter(lc -> lc.getThoiGianBatDau() != null && lc.getThoiGianKetThuc() != null
                                && start.isBefore(lc.getThoiGianKetThuc())
                                && end.isAfter(lc.getThoiGianBatDau()))
                        .findFirst();
                if (dbConflict.isPresent()) {
                    LichChieu cx = dbConflict.get();
                    String cxS = cx.getThoiGianBatDau().toLocalTime().toString().substring(0, 5);
                    String cxE = cx.getThoiGianKetThuc().toLocalTime().toString().substring(0, 5);
                    entry.put("valid",  false);
                    entry.put("reason", "Dong " + rowNum + ": " + phong.getTenPhong()
                            + " da co suat chieu tu " + cxS + " den " + cxE);
                    preview.add(entry);
                    continue;
                }

                // Intra-file conflict check (against already-valid rows in this same import)
                boolean intraConflict = false;
                for (int pi = 0; pi < pendingSlots.size(); pi++) {
                    if (!pendingPhongIds.get(pi).equals(phongId)) continue;
                    java.time.LocalDateTime ps = pendingSlots.get(pi)[0];
                    java.time.LocalDateTime pe = pendingSlots.get(pi)[1];
                    if (start.isBefore(pe) && end.isAfter(ps)) {
                        intraConflict = true;
                        break;
                    }
                }
                if (intraConflict) {
                    entry.put("valid",  false);
                    entry.put("reason", "Dong " + rowNum + ": Trung gio voi mot dong khac trong file cung phong nay");
                    preview.add(entry);
                    continue;
                }

                // All checks passed — mark valid
                entry.put("valid",          true);
                entry.put("reason",         "");
                entry.put("phimId",         phim.getId());
                entry.put("phongChieuId",   phong.getId());
                entry.put("thoiGianBatDau", start.toString());
                entry.put("thoiGianKetThuc",end.toString());
                pendingSlots.add(new java.time.LocalDateTime[]{start, end});
                pendingPhongIds.add(phongId);
                preview.add(entry);
            }

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Loi doc file Excel: " + e.getMessage()));
        }

        if (preview.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "File Excel khong co du lieu (co the chi co dong tieu de)"));
        }
        return ResponseEntity.ok(preview);
    }

    /**
     * POST /api/admin/lich-chieu/import-confirm
     * Accepts the valid rows from import-preview, re-validates each from scratch,
     * saves only those that still pass, and returns succeeded/failed in the same
     * shape as batchCreateLichChieu.
     *
     * Body: array of row objects as returned by import-preview (fields: phimId, phongChieuId,
     *       thoiGianBatDau, thoiGianKetThuc, giaCoBan, tenPhim, tenPhong, ngayChieu, gioChieu, row)
     */
    @PostMapping("/lich-chieu/import-confirm")
    @SuppressWarnings("unchecked")
    public ResponseEntity<?> importConfirmLichChieu(@RequestBody java.util.List<Map<String, Object>> rows) {
        if (rows == null || rows.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Khong co du lieu de luu"));
        }

        java.util.List<Map<String, Object>> succeeded = new java.util.ArrayList<>();
        java.util.List<Map<String, Object>> failed    = new java.util.ArrayList<>();

        // Track rows saved in this batch for intra-batch conflict checking
        java.util.List<java.time.LocalDateTime[]> savedSlots  = new java.util.ArrayList<>();
        java.util.List<Long>                      savedPhongIds = new java.util.ArrayList<>();

        for (Map<String, Object> row : rows) {
            int rowNum = row.get("row") instanceof Number ? ((Number) row.get("row")).intValue() : 0;

            try {
                // Re-resolve from DB — never trust client-supplied IDs directly
                Object phimIdRaw   = row.get("phimId");
                Object phongIdRaw  = row.get("phongChieuId");
                Object startRaw    = row.get("thoiGianBatDau");
                Object endRaw      = row.get("thoiGianKetThuc");
                Object giaRaw      = row.get("giaCoBan");

                if (phimIdRaw == null || phongIdRaw == null || startRaw == null || endRaw == null) {
                    Map<String, Object> f = new java.util.LinkedHashMap<>(row);
                    f.put("reason", "Thieu thong tin bat buoc (phimId, phongChieuId, thoiGianBatDau, thoiGianKetThuc)");
                    failed.add(f); continue;
                }

                Long phimId  = ((Number) phimIdRaw).longValue();
                Long phongId = ((Number) phongIdRaw).longValue();
                java.time.LocalDateTime start = java.time.LocalDateTime.parse(startRaw.toString());
                java.time.LocalDateTime end   = java.time.LocalDateTime.parse(endRaw.toString());

                Phim phim = phimRepository.findById(phimId).orElse(null);
                if (phim == null) {
                    Map<String, Object> f = new java.util.LinkedHashMap<>(row);
                    f.put("reason", "Khong tim thay phim id=" + phimId);
                    failed.add(f); continue;
                }

                PhongChieu phong = phongChieuRepository.findById(phongId).orElse(null);
                if (phong == null) {
                    Map<String, Object> f = new java.util.LinkedHashMap<>(row);
                    f.put("reason", "Khong tim thay phong chieu id=" + phongId);
                    failed.add(f); continue;
                }

                // Format-compatibility re-check
                java.util.Optional<String> fmtErr = validateDinhDangCompatibility(phim, phong);
                if (fmtErr.isPresent()) {
                    Map<String, Object> f = new java.util.LinkedHashMap<>(row);
                    f.put("reason", fmtErr.get());
                    failed.add(f); continue;
                }

                // DB conflict re-check
                java.util.Optional<LichChieu> dbConflict = lichChieuRepository.findAll().stream()
                        .filter(lc -> !Boolean.TRUE.equals(lc.getIsDeleted()))
                        .filter(lc -> lc.getPhongChieu() != null && phongId.equals(lc.getPhongChieu().getId()))
                        .filter(lc -> lc.getThoiGianBatDau() != null && lc.getThoiGianKetThuc() != null
                                && start.isBefore(lc.getThoiGianKetThuc())
                                && end.isAfter(lc.getThoiGianBatDau()))
                        .findFirst();
                if (dbConflict.isPresent()) {
                    LichChieu cx = dbConflict.get();
                    String cxS = cx.getThoiGianBatDau().toLocalTime().toString().substring(0, 5);
                    String cxE = cx.getThoiGianKetThuc().toLocalTime().toString().substring(0, 5);
                    Map<String, Object> f = new java.util.LinkedHashMap<>(row);
                    f.put("reason", phong.getTenPhong() + " da co suat chieu tu " + cxS + " den " + cxE);
                    failed.add(f); continue;
                }

                // Intra-batch conflict check
                boolean intraConflict = false;
                for (int pi = 0; pi < savedSlots.size(); pi++) {
                    if (!savedPhongIds.get(pi).equals(phongId)) continue;
                    java.time.LocalDateTime ps = savedSlots.get(pi)[0];
                    java.time.LocalDateTime pe = savedSlots.get(pi)[1];
                    if (start.isBefore(pe) && end.isAfter(ps)) { intraConflict = true; break; }
                }
                if (intraConflict) {
                    Map<String, Object> f = new java.util.LinkedHashMap<>(row);
                    f.put("reason", "Trung gio voi mot suat khac trong cung lan nhap nay");
                    failed.add(f); continue;
                }

                // Save
                java.math.BigDecimal gia = giaRaw != null
                        ? new java.math.BigDecimal(giaRaw.toString()) : java.math.BigDecimal.ZERO;
                LichChieu lc = new LichChieu();
                lc.setPhim(phim);
                lc.setPhongChieu(phong);
                lc.setThoiGianBatDau(start);
                lc.setThoiGianKetThuc(end);
                lc.setGiaCoBan(gia);
                lc.setTrangThai("active");
                lc.setIsDeleted(false);
                LichChieu saved = lichChieuRepository.save(lc);

                savedSlots.add(new java.time.LocalDateTime[]{start, end});
                savedPhongIds.add(phongId);

                Map<String, Object> s = new java.util.LinkedHashMap<>(row);
                s.put("id",  saved.getId());
                s.put("row", rowNum);
                succeeded.add(s);

            } catch (Exception e) {
                Map<String, Object> f = new java.util.LinkedHashMap<>(row);
                f.put("reason", "Loi xu ly dong " + rowNum + ": " + e.getMessage());
                failed.add(f);
            }
        }

        Map<String, Object> result = new java.util.LinkedHashMap<>();
        result.put("succeeded",      succeeded);
        result.put("failed",         failed);
        result.put("totalRequested", rows.size());
        result.put("totalSucceeded", succeeded.size());
        result.put("totalFailed",    failed.size());
        return ResponseEntity.ok(result);
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

        // Format-compatibility check: room's dinhDang must be in the movie's dinhDangs list
        // lc.getPhim() reflects the updated phim (if body changed it) or the existing one.
        if (lc.getPhim() != null && phong != null) {
            java.util.Optional<String> formatErr = validateDinhDangCompatibility(lc.getPhim(), phong);
            if (formatErr.isPresent()) {
                return ResponseEntity.badRequest().body(Map.of("message", formatErr.get()));
            }
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

    /**
     * Validates that the room's format (PhongChieu.dinhDang) is compatible with
     * the movie's allowed formats (Phim.dinhDangs).
     *
     * Rules:
     *  - If phong.getDinhDang() is null  → skip, return empty (not enough data to compare)
     *  - If phim.getDinhDangs() contains the room's dinhDang by id → compatible, return empty
     *  - If phim.getDinhDangs() is empty → skip (movie has no format restrictions set yet)
     *  - Otherwise → return an error message in Vietnamese
     *
     * @return Optional.empty() if compatible or not enough data; Optional.of(message) if incompatible
     */
    private java.util.Optional<String> validateDinhDangCompatibility(Phim phim, PhongChieu phong) {
        DinhDang roomFormat = phong.getDinhDang();
        // Rule 1: no format on the room → nothing to validate
        if (roomFormat == null) return java.util.Optional.empty();

        java.util.List<DinhDang> movieFormats = phim.getDinhDangs();
        // Rule 2: movie has no formats defined yet → skip validation
        if (movieFormats == null || movieFormats.isEmpty()) return java.util.Optional.empty();

        // Rule 3: check if any of the movie's formats matches the room's format by id
        boolean compatible = movieFormats.stream()
                .anyMatch(df -> roomFormat.getId() != null && roomFormat.getId().equals(df.getId()));

        if (!compatible) {
            String formatName = roomFormat.getTenDinhDang() != null ? roomFormat.getTenDinhDang() : "dinh dang nay";
            String movieName  = phim.getTenPhim()           != null ? phim.getTenPhim()           : "phim nay";
            return java.util.Optional.of(
                    "Phong chieu su dung dinh dang [" + formatName + "] khong duoc ho tro boi phim [" + movieName + "]");
        }
        return java.util.Optional.empty();
    }

    // ─────────────────────────────────────────────────────────────
    // CINEMA ADMIN CRUD
    // ─────────────────────────────────────────────────────────────

    /** GET /api/admin/rap-chieu — returns ALL cinemas regardless of trangThai */
    @GetMapping("/rap-chieu")
    public ResponseEntity<java.util.List<RapChieu>> getAllRap() {
        return ResponseEntity.ok(rapChieuRepository.findAll());
    }

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
        // banDoUrl: always overwrite (including clearing to null when blank string sent)
        rap.setBanDoUrl(body.getBanDoUrl() != null && !body.getBanDoUrl().isBlank()
                ? body.getBanDoUrl().trim() : null);
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

    /** GET /api/admin/san-pham — returns ALL products regardless of dangHoatDong */
    @GetMapping("/san-pham")
    public ResponseEntity<java.util.List<SanPham>> getAllSanPham() {
        return ResponseEntity.ok(sanPhamRepository.findAll());
    }

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

    /** GET /api/admin/banner — returns ALL banners regardless of status or date range */
    @GetMapping("/banner")
    public ResponseEntity<java.util.List<Banner>> getAllBanners() {
        return ResponseEntity.ok(bannerRepository.findAllByOrderByThuTuAsc());
    }

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
        if (body.getNgayBatDau() != null) banner.setNgayBatDau(body.getNgayBatDau());
        if (body.getNgayKetThuc() != null) banner.setNgayKetThuc(body.getNgayKetThuc());
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
