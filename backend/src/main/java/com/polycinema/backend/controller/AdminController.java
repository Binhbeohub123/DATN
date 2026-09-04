package com.polycinema.backend.controller;

import com.polycinema.backend.entity.Banner;
import com.polycinema.backend.entity.ChiTietDatGhe;
import com.polycinema.backend.entity.ChiTietDatSanPham;
import com.polycinema.backend.entity.DatVe;
import com.polycinema.backend.entity.DinhDang;
import com.polycinema.backend.entity.GheNgoi;
import com.polycinema.backend.entity.LichChieu;
import com.polycinema.backend.entity.NguoiDung;
import com.polycinema.backend.entity.Phim;
import com.polycinema.backend.entity.PhongChieu;
import com.polycinema.backend.entity.RapChieu;
import com.polycinema.backend.entity.SanPham;
import com.polycinema.backend.entity.SeatLock;
import com.polycinema.backend.entity.SystemConfig;
import com.polycinema.backend.entity.TheLoai;
import com.polycinema.backend.entity.ThanhToan;
import com.polycinema.backend.service.*;
import com.polycinema.backend.util.SeatDisplayUtil;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddressList;
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

    private final DatVeService datVeService;
    private final AuthService authService;
    private final EmailService emailService;
    private final PhimService phimService;
    private final NguoiDungService nguoiDungService;
    private final LichChieuService lichChieuService;
    private final RapChieuService rapChieuService;
    private final PhongChieuService phongChieuService;
    private final GheNgoiService gheNgoiService;
    private final SanPhamService sanPhamService;
    private final BannerService bannerService;
    private final TheLoaiService theLoaiService;
    private final DinhDangService dinhDangService;
    private final SeatLockService seatLockService;
    private final SystemConfigService systemConfigService;

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

        List<DatVe> allBookings = datVeService.findAll();
        List<DatVe> todayBookings = allBookings.stream()
                .filter(dv -> dv.getNgayTao() != null
                        && dv.getNgayTao().isAfter(todayStart)
                        && dv.getNgayTao().isBefore(todayEnd))
                .collect(Collectors.toList());

        long totalUsers = nguoiDungService.count();
        long totalMovies = phimService.findAllActive().size();

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

        List<DatVe> paid = datVeService.findAll().stream()
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
        Page<NguoiDung> result = nguoiDungService.searchAdmin(q, pr);

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
     * Safety: an admin account can only be locked while at least one other
     * admin remains active — so the system can never be left without an admin
     * to unlock accounts (covers locking your own account too).
     */
    @PutMapping("/users/{id}/lock")
    public ResponseEntity<?> lockUser(@PathVariable Long id, @RequestBody Map<String, String> body) {
        try {
            NguoiDung user = nguoiDungService.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng"));

            String reason = body.get("reason");
            if (reason == null || reason.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Vui lòng nhập lý do khóa tài khoản"));
            }

            // Last-admin guard: never lock the last ACTIVE admin (self or another)
            if ("admin".equalsIgnoreCase(user.getVaiTro())) {
                long activeAdmins = nguoiDungService.countByVaiTroAndTrangThai("admin", Boolean.TRUE);
                if (activeAdmins <= 1) {
                    return ResponseEntity.badRequest()
                            .body(Map.of("message", "Không thể khóa tài khoản admin cuối cùng. Hệ thống cần ít nhất 1 admin hoạt động."));
                }
            }

            user.setTrangThai(false);
            user.setLyDoKhoa(reason);
            nguoiDungService.save(user);

            org.slf4j.LoggerFactory.getLogger(AdminController.class).info(
                "[ACCOUNT_LOCK] admin={} targetUserId={} email={} reason={}",
                getCallerEmail(), id, user.getEmail(), reason);

            return ResponseEntity.ok(Map.of("message", "Đã khóa tài khoản"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Lỗi khóa tài khoản: " + e.getMessage()));
        }
    }

    /**
     * PUT /api/admin/users/{id}/unlock
     */
    @PutMapping("/users/{id}/unlock")
    public ResponseEntity<?> unlockUser(@PathVariable Long id) {
        NguoiDung user = nguoiDungService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng"));
        user.setTrangThai(true);
        user.setLyDoKhoa(null);
        nguoiDungService.save(user);
        return ResponseEntity.ok(Map.of("message", "Đã mở khóa tài khoản"));
    }

    /**
     * POST /api/admin/users
     * Admin creates a new staff or admin account.
     * Body: { "email", "password", "hoTen", "soDienThoai" (optional), "vaiTro": "staff"|"admin" }
     * The new account is immediately active (no email verification needed).
     * createdBy is set to the calling admin's ID.
     */
    @PostMapping("/users")
    public ResponseEntity<?> createUser(@RequestBody Map<String, String> body) {
        try {
            // Resolve the calling admin's ID
            String callerEmail = getCallerEmail();
            if (callerEmail == null) {
                return ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "Vui lòng đăng nhập"));
            }
            Long callerAdminId = nguoiDungService.findByEmail(callerEmail)
                    .map(NguoiDung::getId).orElse(null);

            String email      = body.get("email");
            String password   = body.get("password");
            String hoTen      = body.get("hoTen");
            String soDienThoai = body.get("soDienThoai");
            String vaiTro     = body.get("vaiTro");

            NguoiDung created = authService.createStaffOrAdmin(
                    email, password, hoTen, soDienThoai, vaiTro, callerAdminId);
            return ResponseEntity.status(org.springframework.http.HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Lỗi tạo tài khoản: " + e.getMessage()));
        }
    }

    /**
     * PUT /api/admin/users/{id}/points
     * Body: { "delta": 100, "lyDo": "Bù điểm khiếu nại" }
     * Adjusts a user's diemTichLuy by delta (positive = add, negative = subtract).
     * Balance is clamped to 0 (cannot go negative).
     * Adjustment is logged to application log for audit purposes.
     */
    @PutMapping("/users/{id}/points")
    public ResponseEntity<?> adjustPoints(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        try {
            NguoiDung user = nguoiDungService.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng"));

            Object deltaRaw = body.get("delta");
            if (deltaRaw == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "Thiếu trường delta"));
            }
            int delta = ((Number) deltaRaw).intValue();
            String lyDo = body.get("lyDo") != null ? String.valueOf(body.get("lyDo")) : "";

            int before = user.getDiemTichLuy() == null ? 0 : user.getDiemTichLuy();
            int after  = Math.max(0, before + delta);
            user.setDiemTichLuy(after);
            nguoiDungService.save(user);

            // Audit log (application-level; no new table needed)
            String adminEmail = getCallerEmail();
            org.slf4j.LoggerFactory.getLogger(AdminController.class).info(
                "[POINTS_ADJUST] admin={} targetUserId={} before={} delta={} after={} lyDo={}",
                adminEmail, id, before, delta, after, lyDo);

            Map<String, Object> result = new java.util.LinkedHashMap<>();
            result.put("userId",       id);
            result.put("diemTichLuy",  after);
            result.put("delta",        delta);
            result.put("lyDo",         lyDo);
            result.put("message",      "Đã điều chỉnh điểm tích lũy");
            return ResponseEntity.ok(result);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Lỗi điều chỉnh điểm: " + e.getMessage()));
        }
    }

    /**
     * PUT /api/admin/users/{id}/role
     * Body: { "vaiTro": "customer" | "staff" | "admin" }
     * Safety checks:
     *  - Rejects if target user == calling admin (self-lockout prevention)
     *  - Rejects if demoting the last remaining admin account
     */
    @PutMapping("/users/{id}/role")
    public ResponseEntity<?> changeUserRole(@PathVariable Long id, @RequestBody Map<String, String> body) {
        try {
            String newRole = body.get("vaiTro");
            if (newRole == null || (!newRole.equals("customer") && !newRole.equals("staff") && !newRole.equals("admin"))) {
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "Vai trò không hợp lệ. Chỉ chấp nhận: customer, staff, admin"));
            }

            NguoiDung target = nguoiDungService.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng"));

            // Self-change guard
            String callerEmail = getCallerEmail();
            if (callerEmail != null) {
                NguoiDung caller = nguoiDungService.findByEmail(callerEmail).orElse(null);
                if (caller != null && caller.getId().equals(id)) {
                    return ResponseEntity.badRequest()
                            .body(Map.of("message", "Không thể thay đổi vai trò của chính mình"));
                }
            }

            // Last-admin guard: prevent demoting the last admin
            if ("admin".equals(target.getVaiTro()) && !"admin".equals(newRole)) {
                long adminCount = nguoiDungService.countByVaiTro("admin");
                if (adminCount <= 1) {
                    return ResponseEntity.badRequest()
                            .body(Map.of("message", "Không thể hạ cấp tài khoản admin cuối cùng"));
                }
            }

            String oldRole = target.getVaiTro();
            target.setVaiTro(newRole);
            nguoiDungService.save(target);

            org.slf4j.LoggerFactory.getLogger(AdminController.class).info(
                "[ROLE_CHANGE] admin={} targetUserId={} oldRole={} newRole={}",
                callerEmail, id, oldRole, newRole);

            return ResponseEntity.ok(Map.of(
                    "userId",  id,
                    "vaiTro",  newRole,
                    "message", "Đã cập nhật vai trò"));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Lỗi thay đổi vai trò: " + e.getMessage()));
        }
    }

    /**
     * PUT /api/admin/users/{id}/profile
     * Body: { "hoTen": "...", "email": "..." }  (either or both fields)
     * Validates:
     *  - hoTen must not be blank if provided
     *  - email must be valid format if provided
     *  - email must be unique (not taken by a different account)
     * When email changes: notifications are sent to both old and new addresses,
     * and emailChangedAt is stamped so JwtAuthenticationFilter rejects any token
     * issued before this moment (forcing the user to log in again with the new email).
     * Password-reset OTPs always look up by the CURRENT email column value —
     * the existing in-memory OTP store in AuthService already does this correctly
     * (sendForgotOtp calls findByEmail with whatever email the user provides at
     * request time, which is the DB value after any admin change).
     */
    @PutMapping("/users/{id}/profile")
    public ResponseEntity<?> updateUserProfile(@PathVariable Long id, @RequestBody Map<String, String> body) {
        try {
            NguoiDung user = nguoiDungService.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng"));

            boolean changed = false;
            String oldEmail = user.getEmail();   // capture before any mutation
            boolean emailChanged = false;

            // ── hoTen ─────────────────────────────────────────────
            if (body.containsKey("hoTen")) {
                String hoTen = body.get("hoTen");
                if (hoTen == null || hoTen.isBlank()) {
                    return ResponseEntity.badRequest()
                            .body(Map.of("message", "Họ tên không được để trống"));
                }
                user.setHoTen(hoTen.trim());
                changed = true;
            }

            // ── email ─────────────────────────────────────────────
            if (body.containsKey("email")) {
                String email = body.get("email");
                if (email == null || email.isBlank()) {
                    return ResponseEntity.badRequest()
                            .body(Map.of("message", "Email không được để trống"));
                }
                email = email.trim().toLowerCase();

                // Basic email format validation
                if (!email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
                    return ResponseEntity.badRequest()
                            .body(Map.of("message", "Email không hợp lệ"));
                }

                // Uniqueness check — reject if taken by a different account
                java.util.Optional<NguoiDung> existing = nguoiDungService.findByEmail(email);
                if (existing.isPresent() && !existing.get().getId().equals(id)) {
                    return ResponseEntity.badRequest()
                            .body(Map.of("message", "Email " + email + " đã được sử dụng bởi tài khoản khác"));
                }

                if (!email.equals(oldEmail)) {
                    user.setEmail(email);
                    // Stamp the change time — JwtAuthenticationFilter uses this to reject
                    // tokens issued before this moment (old-email session invalidation).
                    user.setEmailChangedAt(LocalDateTime.now());
                    emailChanged = true;
                }
                changed = true;
            }

            if (!changed) {
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "Không có trường nào được cập nhật"));
            }

            nguoiDungService.save(user);

            // ── Email-change notifications (fire-and-forget, non-blocking) ──
            if (emailChanged) {
                final String newEmail = user.getEmail();
                final String displayName = user.getHoTen();
                final String capturedOld = oldEmail;
                new Thread(() ->
                    emailService.sendEmailChangedNotification(capturedOld, newEmail, displayName)
                ).start();
            }

            String adminEmail = getCallerEmail();
            org.slf4j.LoggerFactory.getLogger(AdminController.class).info(
                "[PROFILE_UPDATE] admin={} targetUserId={} fields={} emailChanged={}",
                adminEmail, id, body.keySet(), emailChanged);

            Map<String, Object> result = new java.util.LinkedHashMap<>();
            result.put("id",           user.getId());
            result.put("hoTen",        user.getHoTen());
            result.put("email",        user.getEmail());
            result.put("emailChanged", emailChanged);
            result.put("message",      "Đã cập nhật thông tin người dùng"
                    + (emailChanged ? ". Email thông báo đã được gửi tới cả hai địa chỉ." : ""));
            return ResponseEntity.ok(result);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Lỗi cập nhật thông tin: " + e.getMessage()));
        }
    }


    private String getCallerEmail() {
        try {
            org.springframework.security.core.Authentication auth =
                    org.springframework.security.core.context.SecurityContextHolder
                            .getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) return null;
            Object p = auth.getPrincipal();
            if (!(p instanceof String)) return null;
            String email = (String) p;
            return "anonymousUser".equals(email) ? null : email;
        } catch (Exception e) { return null; }
    }

    // ─────────────────────────────────────────────────────────────
    // MOVIE ADMIN CRUD
    // ─────────────────────────────────────────────────────────────

    /**
     * GET /api/admin/phim — all movies including deleted.
     * trangThai is computed from showtimes, not the stored value.
     */
    @GetMapping("/phim")
    public ResponseEntity<?> getAllPhim() {
        List<Phim> movies = phimService.findAllIncludingDeleted();
        phimService.applyComputedStatus(movies);
        return ResponseEntity.ok(movies);
    }

    /**
     * POST /api/admin/phim
     * trangThai in the body is IGNORED — computed status is applied at read time.
     * New movies always start as "chua_chieu" (no showtimes yet).
     */
    @PostMapping("/phim")
    public ResponseEntity<?> createPhim(@RequestBody Map<String, Object> body) {
        Phim phim = new Phim();
        phim.setIsDeleted(false);
        phim.setTrangThai("sap_chieu");  // auto-computed; stored default must satisfy DB CHECK (ngung_chieu/dang_chieu/sap_chieu)
        applyPhimFields(phim, body);
        // Never use caller-supplied trangThai — keep a DB-valid default
        phim.setTrangThai("sap_chieu");
        Phim saved = phimService.save(phim);
        phimService.applyComputedStatus(saved);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    /**
     * PUT /api/admin/phim/{id}
     * trangThai in the body is IGNORED — computed status is applied at read time.
     */
    @PutMapping("/phim/{id}")
    public ResponseEntity<?> updatePhim(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Phim phim = phimService.findByIdOptional(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phim"));
        String storedStatus = phim.getTrangThai();  // preserve stored value
        applyPhimFields(phim, body);
        phim.setTrangThai(storedStatus);  // restore: never override with incoming value
        Phim saved = phimService.save(phim);
        phimService.applyComputedStatus(saved);
        return ResponseEntity.ok(saved);
    }

    /** Helper — applies fields from request body to a Phim entity, including theLoaiIds. */
    @SuppressWarnings("unchecked")
    private void applyPhimFields(Phim phim, Map<String, Object> body) {
        if (body.containsKey("tenPhim"))         phim.setTenPhim((String) body.get("tenPhim"));
        if (body.containsKey("tenPhimTiengAnh")) phim.setTenPhimTiengAnh((String) body.get("tenPhimTiengAnh"));
        if (body.containsKey("daoDien"))         phim.setDaoDien((String) body.get("daoDien"));
        if (body.containsKey("dienVienChinh"))   phim.setDienVienChinh((String) body.get("dienVienChinh"));
        if (body.containsKey("thoiLuong") && body.get("thoiLuong") != null) phim.setThoiLuong(((Number) body.get("thoiLuong")).intValue());
        if (body.containsKey("ngonNgu"))         phim.setNgonNgu((String) body.get("ngonNgu"));
        if (body.containsKey("phanLoaiDoTuoi"))  phim.setPhanLoaiDoTuoi((String) body.get("phanLoaiDoTuoi"));
        if (body.containsKey("posterUrl"))       phim.setPosterUrl((String) body.get("posterUrl"));
        if (body.containsKey("trailerUrl"))      phim.setTrailerUrl((String) body.get("trailerUrl"));
        if (body.containsKey("moTa"))            phim.setMoTa((String) body.get("moTa"));
        if (body.containsKey("trangThai"))       phim.setTrangThai((String) body.get("trangThai"));
        if (body.containsKey("ngayCongChieu") && body.get("ngayCongChieu") != null) {
            String rawDate = ((String) body.get("ngayCongChieu")).trim();
            if (!rawDate.isEmpty()) {
                phim.setNgayCongChieu(java.time.LocalDate.parse(rawDate));
            }
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
                            return theLoaiService.findById(gid)
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
                            return dinhDangService.findById(fid)
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
        Phim phim = phimService.findByIdOptional(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phim"));
        phim.setIsDeleted(true);
        phimService.save(phim);
        return ResponseEntity.ok(Map.of("message", "Đã xóa phim"));
    }

    // ─────────────────────────────────────────────────────────────
    // SCHEDULE ADMIN CRUD
    // ─────────────────────────────────────────────────────────────

    private String computeTrangThai(LichChieu lc) {
        if (lc.getThoiGianBatDau() == null || lc.getThoiGianKetThuc() == null) return "chua_chieu";
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        if (now.isBefore(lc.getThoiGianBatDau())) return "chua_chieu";
        if (now.isAfter(lc.getThoiGianKetThuc())) return "da_chieu";
        return "dang_chieu";
    }

    /**
     * GET /api/admin/lich-chieu/validate-date
     * Checks whether a given room/time/date combination has conflicts.
     * Used by the frontend date-chip selector to show green/red indicators.
     * Params: phongChieuId, ngay (yyyy-MM-dd), thoiGianBatDau (HH:mm), thoiGianNghi (minutes), phimId
     */
    @GetMapping("/lich-chieu/validate-date")
    public ResponseEntity<?> validateDate(
            @RequestParam Long phongChieuId,
            @RequestParam String ngay,
            @RequestParam String thoiGianBatDau,
            @RequestParam(defaultValue = "15") int thoiGianNghi,
            @RequestParam Long phimId,
            @RequestParam(required = false) Long excludeId) {

        Phim phim = phimService.findByIdOptional(phimId).orElse(null);
        if (phim == null || phim.getThoiLuong() == null) {
            return ResponseEntity.ok(Map.of("date", ngay, "valid", false, "reason", "Không tìm thấy phim"));
        }

        String[] hm = thoiGianBatDau.split(":");
        java.time.LocalDateTime start = java.time.LocalDate.parse(ngay)
                .atTime(Integer.parseInt(hm[0]), Integer.parseInt(hm[1]));
        java.time.LocalDateTime end = start.plusMinutes(phim.getThoiLuong() + thoiGianNghi);

        java.util.Optional<LichChieu> conflict = lichChieuService.findAll().stream()
                .filter(lc -> !Boolean.TRUE.equals(lc.getIsDeleted()))
                .filter(lc -> lc.getPhongChieu() != null && phongChieuId.equals(lc.getPhongChieu().getId()))
                .filter(lc -> excludeId == null || !excludeId.equals(lc.getId()))
                .filter(lc -> lc.getThoiGianBatDau() != null && lc.getThoiGianKetThuc() != null
                        && start.isBefore(lc.getThoiGianKetThuc())
                        && end.isAfter(lc.getThoiGianBatDau()))
                .findFirst();

        if (conflict.isPresent()) {
            LichChieu cx = conflict.get();
            String cxS = cx.getThoiGianBatDau().toLocalTime().toString().substring(0, 5);
            String cxE = cx.getThoiGianKetThuc().toLocalTime().toString().substring(0, 5);
            return ResponseEntity.ok(Map.of(
                    "date", ngay, "valid", false,
                    "reason", "Trùng với suất chiếu " + cxS + "–" + cxE));
        }
        return ResponseEntity.ok(Map.of("date", ngay, "valid", true, "reason", ""));
    }

    /**
     * GET /api/admin/lich-chieu?page=0&size=20&dateFrom=yyyy-MM-dd&dateTo=yyyy-MM-dd&rapChieuId=X&phongChieuId=Y
     * Uses DB-level pagination and optional filters — no findAll().
     * When rapChieuId is provided, size is raised to 500 automatically so all
     * rooms of the cinema load in one request for client-side grouping.
     */
    @GetMapping("/lich-chieu")
    public ResponseEntity<?> getAllLichChieu(
            @RequestParam(defaultValue = "0")   int page,
            @RequestParam(defaultValue = "20")  int size,
            @RequestParam(required = false) String dateFrom,
            @RequestParam(required = false) String dateTo,
            @RequestParam(required = false) Long rapChieuId,
            @RequestParam(required = false) Long phongChieuId) {

        LocalDateTime from = (dateFrom != null && !dateFrom.isBlank())
                ? LocalDate.parse(dateFrom).atStartOfDay() : null;
        LocalDateTime to = (dateTo != null && !dateTo.isBlank())
                ? LocalDate.parse(dateTo).plusDays(1).atStartOfDay() : null;

        // Raise page size when cinema filter is active so grouped view loads all rooms
        int effectiveSize = (rapChieuId != null) ? Math.max(size, 500) : size;

        PageRequest pr = PageRequest.of(page, effectiveSize);
        Page<LichChieu> result = lichChieuService.findAdminPage(from, to, rapChieuId, phongChieuId, pr);

        result.getContent().forEach(lc -> lc.setTrangThai(computeTrangThai(lc)));

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
        Phim phim = phimService.findByIdOptional(phimId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phim"));
        PhongChieu phongChieu = phongChieuService.findById(phongId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phòng chiếu"));
        lichChieu.setPhim(phim);
        lichChieu.setPhongChieu(phongChieu);

        // Format-compatibility check: room's dinhDang must be in the movie's dinhDangs list
        java.util.Optional<String> formatErr = validateDinhDangCompatibility(phim, phongChieu);
        if (formatErr.isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("message", formatErr.get()));
        }

        // Conflict detection: same room, overlapping time (include break time from existing schedules)
        if (lichChieu.getPhongChieu() != null && lichChieu.getThoiGianBatDau() != null && lichChieu.getThoiGianKetThuc() != null) {
            LocalDateTime start = lichChieu.getThoiGianBatDau();
            LocalDateTime end = lichChieu.getThoiGianKetThuc();

            java.util.Optional<LichChieu> conflicting = lichChieuService.findAll().stream()
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

        return ResponseEntity.status(HttpStatus.CREATED).body(lichChieuService.save(lichChieu));
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
        Long phimId  = body.get("phimId")       != null ? ((Number) body.get("phimId")).longValue()       : null;
        Long phongId = body.get("phongChieuId") != null ? ((Number) body.get("phongChieuId")).longValue() : null;
        java.util.List<String> dates = body.get("dates") instanceof java.util.List
                ? (java.util.List<String>) body.get("dates") : java.util.List.of();

        if (phimId == null || phongId == null || dates.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Thiếu phimId, phongChieuId hoặc dates"));
        }

        Phim phim = phimService.findByIdOptional(phimId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phim id=" + phimId));
        PhongChieu phong = phongChieuService.findById(phongId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phòng chiếu id=" + phongId));

        // ── Build the list of shows to create ────────────────────────────────
        // Multi-show mode: body.shows = [{ startTime, endTime, thoiGianNghi, giaCoBan }, ...]
        // Single-show mode (backwards-compatible): body.startTime / endTime / thoiGianNghi / giaCoBan
        java.util.List<Map<String, Object>> showDefs;
        if (body.get("shows") instanceof java.util.List) {
            showDefs = (java.util.List<Map<String, Object>>) body.get("shows");
            if (showDefs.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Danh sách suất chiếu trống"));
            }
        } else {
            // Legacy single-show path
            String startTime = (String) body.get("startTime");
            String endTime   = (String) body.get("endTime");
            if (startTime == null || endTime == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "Thiếu startTime, endTime hoặc shows"));
            }
            int legacyNghi = body.get("thoiGianNghi") != null ? ((Number) body.get("thoiGianNghi")).intValue() : 15;
            java.math.BigDecimal legacyGia = body.get("giaCoBan") != null
                    ? new java.math.BigDecimal(body.get("giaCoBan").toString()) : java.math.BigDecimal.ZERO;
            Map<String, Object> single = new java.util.LinkedHashMap<>();
            single.put("startTime", startTime);
            single.put("endTime", endTime);
            single.put("thoiGianNghi", legacyNghi);
            single.put("giaCoBan", legacyGia);
            showDefs = java.util.List.of(single);
        }

        // Pre-load all active schedules in this room once — reused to avoid N+1
        java.util.List<LichChieu> roomSchedules = lichChieuService.findAll().stream()
                .filter(lc -> !Boolean.TRUE.equals(lc.getIsDeleted()))
                .filter(lc -> lc.getPhongChieu() != null && phongId.equals(lc.getPhongChieu().getId()))
                .collect(java.util.stream.Collectors.toList());

        java.util.List<Map<String, Object>> succeeded = new java.util.ArrayList<>();
        java.util.List<Map<String, Object>> failed    = new java.util.ArrayList<>();
        int totalRequested = dates.size() * showDefs.size();

        for (String dateStr : dates) {
            java.time.LocalDate date = java.time.LocalDate.parse(dateStr);

            for (Map<String, Object> show : showDefs) {
                String stRaw = (String) show.get("startTime");
                String etRaw = (String) show.get("endTime");
                int nghi = show.get("thoiGianNghi") != null ? ((Number) show.get("thoiGianNghi")).intValue() : 15;
                java.math.BigDecimal gia = show.get("giaCoBan") != null
                        ? new java.math.BigDecimal(show.get("giaCoBan").toString()) : java.math.BigDecimal.ZERO;

                try {
                    String[] sParts = stRaw.split(":");
                    String[] eParts = etRaw.split(":");
                    int sH = Integer.parseInt(sParts[0]), sM = Integer.parseInt(sParts[1]);
                    int eH = Integer.parseInt(eParts[0]), eM = Integer.parseInt(eParts[1]);

                    java.time.LocalDateTime start = date.atTime(sH, sM);
                    java.time.LocalDateTime end;
                    if (eH < sH || (eH == sH && eM <= sM)) {
                        end = date.plusDays(1).atTime(eH, eM);
                    } else {
                        end = date.atTime(eH, eM);
                    }

                    // Overlap check against pre-loaded + already-saved room schedules
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
                        f.put("startTime", stRaw);
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
                    lc.setGiaCoBan(gia);
                    lc.setThoiGianNghi(nghi);
                    lc.setTrangThai("active");
                    lc.setIsDeleted(false);
                    LichChieu saved = lichChieuService.save(lc);

                    // Track so intra-batch shows on the same date also see it
                    roomSchedules.add(saved);

                    Map<String, Object> s = new java.util.LinkedHashMap<>();
                    s.put("date", dateStr);
                    s.put("startTime", stRaw);
                    s.put("id",   saved.getId());
                    s.put("thoiGianBatDau",  saved.getThoiGianBatDau().toString());
                    s.put("thoiGianKetThuc", saved.getThoiGianKetThuc().toString());
                    succeeded.add(s);

                } catch (Exception e) {
                    Map<String, Object> f = new java.util.LinkedHashMap<>();
                    f.put("date", dateStr);
                    f.put("startTime", stRaw != null ? stRaw : "?");
                    f.put("reason", e.getMessage());
                    failed.add(f);
                }
            }
        }

        Map<String, Object> result = new java.util.LinkedHashMap<>();
        result.put("succeeded",      succeeded);
        result.put("failed",         failed);
        result.put("totalRequested", totalRequested);
        result.put("totalSucceeded", succeeded.size());
        result.put("totalFailed",    failed.size());
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

                // Date/time cells are read with dedicated readers (Excel stores both
                // dates AND times as date-formatted numerics; a single generic reader
                // would corrupt the time column). Ngày → dd/MM/yyyy, Giờ → HH:mm.
                String tenRap    = com.polycinema.backend.util.ExcelCellReader.readStringCell(
                        row.getCell(0, org.apache.poi.ss.usermodel.Row.MissingCellPolicy.RETURN_BLANK_AS_NULL));
                String tenPhim   = com.polycinema.backend.util.ExcelCellReader.readStringCell(
                        row.getCell(1, org.apache.poi.ss.usermodel.Row.MissingCellPolicy.RETURN_BLANK_AS_NULL));
                String tenPhong  = com.polycinema.backend.util.ExcelCellReader.readStringCell(
                        row.getCell(2, org.apache.poi.ss.usermodel.Row.MissingCellPolicy.RETURN_BLANK_AS_NULL));
                String ngayChieu = com.polycinema.backend.util.ExcelCellReader.readDateCell(
                        row.getCell(3, org.apache.poi.ss.usermodel.Row.MissingCellPolicy.RETURN_BLANK_AS_NULL));
                String gioChieu  = com.polycinema.backend.util.ExcelCellReader.readTimeCell(
                        row.getCell(4, org.apache.poi.ss.usermodel.Row.MissingCellPolicy.RETURN_BLANK_AS_NULL));
                String giaRaw    = com.polycinema.backend.util.ExcelCellReader.readStringCell(
                        row.getCell(5, org.apache.poi.ss.usermodel.Row.MissingCellPolicy.RETURN_BLANK_AS_NULL));

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
                java.util.Optional<Phim> phimOpt = phimService.findAllActive().stream()
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

                // Lookup room by cinema name + room name (both case-insensitive).
                // BUG 1: the template dropdown writes Ten Phong with a trailing
                // " (Ten Rap)" suffix (to disambiguate same-named rooms across cinemas).
                // Strip that suffix before matching against the DB's bare tenPhong.
                // The display value in the preview keeps the original (with suffix).
                final String tenRapFinal   = tenRap;
                final String tenPhongFinal = tenPhong;
                final String tenPhongClean = tenPhong.replaceAll("\\s*\\([^)]*\\)\\s*$", "").trim();

                // Step 1: find the cinema by name
                java.util.Optional<PhongChieu> anyInRap = phongChieuService.findAll().stream()
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
                java.util.Optional<PhongChieu> phongOpt = phongChieuService.findAll().stream()
                        .filter(p -> Boolean.TRUE.equals(p.getTrangThai()))
                        .filter(p -> p.getRapChieu() != null
                                && p.getRapChieu().getTenRap() != null
                                && p.getRapChieu().getTenRap().trim().equalsIgnoreCase(tenRapFinal.trim()))
                        .filter(p -> p.getTenPhong() != null && p.getTenPhong().trim().equalsIgnoreCase(tenPhongClean.trim()))
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
                java.util.Optional<LichChieu> dbConflict = lichChieuService.findAll().stream()
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
     * GET /api/admin/lich-chieu/import-template
     * Returns a downloadable .xlsx template file with:
     *   - Header row (bold, colored)
     *   - Data validation dropdowns for Ten Rap (A), Ten Phim (B), Ten Phong (C)
     *   - 30 empty rows ready to fill
     *   - Hidden "Data" sheet as source for dropdown lists
     */
    @GetMapping("/lich-chieu/import-template")
    public ResponseEntity<byte[]> downloadImportTemplate() {
        try (org.apache.poi.xssf.usermodel.XSSFWorkbook wb = new org.apache.poi.xssf.usermodel.XSSFWorkbook()) {

            org.apache.poi.ss.usermodel.Sheet sheet = wb.createSheet("Lich Chieu");
            org.apache.poi.ss.usermodel.Sheet dataSheet = wb.createSheet("Data");
            wb.setSheetHidden(wb.getSheetIndex(dataSheet), true);

            // ── Header row (row 0) ──
            org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(0);
            String[] headers = {"Ten Rap", "Ten Phim", "Ten Phong", "Ngay Chieu", "Gio Chieu", "Gia Co Ban"};
            org.apache.poi.ss.usermodel.CellStyle headerStyle = wb.createCellStyle();
            org.apache.poi.ss.usermodel.Font boldFont = wb.createFont();
            boldFont.setBold(true);
            headerStyle.setFont(boldFont);
            org.apache.poi.ss.usermodel.FillPatternType solidFill = org.apache.poi.ss.usermodel.FillPatternType.SOLID_FOREGROUND;
            headerStyle.setFillPattern(solidFill);
            headerStyle.setFillForegroundColor(org.apache.poi.ss.usermodel.IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setAlignment(org.apache.poi.ss.usermodel.HorizontalAlignment.CENTER);
            for (int i = 0; i < headers.length; i++) {
                org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // ── Populate hidden Data sheet ──
            java.util.List<RapChieu> raps = rapChieuService.findAll().stream()
                    .filter(r -> Boolean.TRUE.equals(r.getTrangThai()))
                    .collect(java.util.stream.Collectors.toList());
            java.util.List<Phim> phims = phimService.findAllActive();
            java.util.List<PhongChieu> phongs = phongChieuService.findAll().stream()
                    .filter(p -> Boolean.TRUE.equals(p.getTrangThai()))
                    .collect(java.util.stream.Collectors.toList());

            // Column A = Ten Rap
            for (int i = 0; i < raps.size(); i++) {
                org.apache.poi.ss.usermodel.Row dr = dataSheet.createRow(i);
                dr.createCell(0).setCellValue(raps.get(i).getTenRap());
            }
            // Column B = Ten Phim
            for (int i = 0; i < phims.size(); i++) {
                org.apache.poi.ss.usermodel.Row dr = dataSheet.getRow(i);
                if (dr == null) dr = dataSheet.createRow(i);
                dr.createCell(1).setCellValue(phims.get(i).getTenPhim());
            }
            // Column C = Ten Phong (Ten Rap)
            int maxPhongRows = Math.max(phongs.size(), Math.max(raps.size(), phims.size()));
            for (int i = 0; i < phongs.size(); i++) {
                org.apache.poi.ss.usermodel.Row dr = dataSheet.getRow(i);
                if (dr == null) dr = dataSheet.createRow(i);
                PhongChieu p = phongs.get(i);
                String rapName = (p.getRapChieu() != null) ? p.getRapChieu().getTenRap() : "";
                dr.createCell(2).setCellValue(p.getTenPhong() + " (" + rapName + ")");
            }

            // ── Data validation (dropdown) for 30 rows using hidden Data sheet ──
            int dataRows = 30;
            org.apache.poi.ss.usermodel.DataValidationHelper dvHelper = sheet.getDataValidationHelper();

            // Column A: Ten Rap — reference Data!A1:A{count}
            if (!raps.isEmpty()) {
                String formula = "Data!$A$1:$A$" + raps.size();
                org.apache.poi.ss.usermodel.DataValidationConstraint rapConstraint = dvHelper.createFormulaListConstraint(formula);
                CellRangeAddressList rapRange = new CellRangeAddressList(1, dataRows, 0, 0);
                org.apache.poi.ss.usermodel.DataValidation rapDv = dvHelper.createValidation(rapConstraint, rapRange);
                rapDv.setShowErrorBox(true);

                rapDv.createErrorBox("Gia tri khong hop le", "Vui long chon tu danh sach rap.");
                sheet.addValidationData(rapDv);
            }

            // Column B: Ten Phim — reference Data!B1:B{count}
            if (!phims.isEmpty()) {
                String formula = "Data!$B$1:$B$" + phims.size();
                org.apache.poi.ss.usermodel.DataValidationConstraint phimConstraint = dvHelper.createFormulaListConstraint(formula);
                CellRangeAddressList phimRange = new CellRangeAddressList(1, dataRows, 1, 1);
                org.apache.poi.ss.usermodel.DataValidation phimDv = dvHelper.createValidation(phimConstraint, phimRange);
                phimDv.setShowErrorBox(true);

                phimDv.createErrorBox("Gia tri khong hop le", "Vui long chon tu danh sach phim.");
                sheet.addValidationData(phimDv);
            }

            // Column C: Ten Phong (Ten Rap) — reference Data!C1:C{count}
            if (!phongs.isEmpty()) {
                String formula = "Data!$C$1:$C$" + phongs.size();
                org.apache.poi.ss.usermodel.DataValidationConstraint phongConstraint = dvHelper.createFormulaListConstraint(formula);
                CellRangeAddressList phongRange = new CellRangeAddressList(1, dataRows, 2, 2);
                org.apache.poi.ss.usermodel.DataValidation phongDv = dvHelper.createValidation(phongConstraint, phongRange);
                phongDv.setShowErrorBox(true);

                phongDv.createErrorBox("Gia tri khong hop le", "Vui long chon tu danh sach phong chieu.");
                sheet.addValidationData(phongDv);
            }

            // ── BUG 3: Giờ Chiếu (column E) as a REAL Excel TIME cell ──
            // Format "h:mm:ss AM/PM" so Excel renders e.g. "1:30:00 AM" and provides
            // its native time picker (type "1:30 PM" and Excel understands 13:30).
            org.apache.poi.ss.usermodel.CellStyle timeStyle = wb.createCellStyle();
            timeStyle.setDataFormat(wb.createDataFormat().getFormat("h:mm:ss AM/PM"));
            // Example row (row 1): demonstrates the format with a genuine time value.
            org.apache.poi.ss.usermodel.Row example = sheet.createRow(1);
            example.createCell(0).setCellValue("");
            example.createCell(1).setCellValue("");
            example.createCell(2).setCellValue("");
            example.createCell(3).setCellValue("");
            org.apache.poi.ss.usermodel.Cell exTime = example.createCell(4);
            // Excel TIME = fractional part of a day. 1:30 AM = 1.5h/24 = 0.0625.
            // Written as a numeric fraction so POI re-reads a valid time serial
            // (a raw 1899-12-31 Date gets a bogus serial and would show 00:00).
            exTime.setCellValue((1.5 / 24.0));
            exTime.setCellStyle(timeStyle);
            example.createCell(5).setCellValue(0);
            // Apply the time style to the whole input region of column E (rows 1..dataRows)
            // so every Gio Chieu cell is a TIME, not TEXT.
            for (int r = 1; r <= dataRows; r++) {
                org.apache.poi.ss.usermodel.Row row = sheet.getRow(r);
                if (row == null) row = sheet.createRow(r);
                org.apache.poi.ss.usermodel.Cell e = row.getCell(4);
                if (e == null) e = row.createCell(4);
                if (e == exTime) continue; // already styled
                e.setCellStyle(timeStyle);
            }

            // ── Auto-size columns ──
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // ── Write to byte array ──
            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            wb.write(baos);
            byte[] bytes = baos.toByteArray();

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"Mau_Import_Lich_Chieu.xlsx\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .contentLength(bytes.length)
                    .body(bytes);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("message", "Loi tao file mau: " + e.getMessage()).toString().getBytes());
        }
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

                Phim phim = phimService.findByIdOptional(phimId).orElse(null);
                if (phim == null) {
                    Map<String, Object> f = new java.util.LinkedHashMap<>(row);
                    f.put("reason", "Khong tim thay phim id=" + phimId);
                    failed.add(f); continue;
                }

                PhongChieu phong = phongChieuService.findById(phongId).orElse(null);
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
                java.util.Optional<LichChieu> dbConflict = lichChieuService.findAll().stream()
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
                Object nghi = row.get("thoiGianNghi");
                int nghibMin = nghi instanceof Number ? ((Number) nghi).intValue() : 15;
                LichChieu lc = new LichChieu();
                lc.setPhim(phim);
                lc.setPhongChieu(phong);
                lc.setThoiGianBatDau(start);
                lc.setThoiGianKetThuc(end);
                lc.setGiaCoBan(gia);
                lc.setThoiGianNghi(nghibMin);
                lc.setTrangThai("active");
                lc.setIsDeleted(false);
                LichChieu saved = lichChieuService.save(lc);

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
     * Requires: no active (non-cancelled) tickets for this showtime.
     */
    @PutMapping("/lich-chieu/{id}")
    public ResponseEntity<?> updateLichChieu(@PathVariable Long id, @RequestBody LichChieu body) {
        LichChieu lc = lichChieuService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy lịch chiếu"));

        // Ticket check: block if any non-cancelled booking exists
        boolean hasTickets = datVeService.findByLichChieuId(id).stream()
                .anyMatch(dv -> !"cancelled".equalsIgnoreCase(dv.getTrangThai()));
        if (hasTickets) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Không thể chỉnh sửa lịch chiếu đã phát sinh vé"));
        }

        if ("da_chieu".equals(computeTrangThai(lc))) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Không thể chỉnh sửa lịch chiếu đã chiếu"));
        }

        LocalDateTime start = body.getThoiGianBatDau() != null ? body.getThoiGianBatDau() : lc.getThoiGianBatDau();
        LocalDateTime end = body.getThoiGianKetThuc() != null ? body.getThoiGianKetThuc() : lc.getThoiGianKetThuc();
        PhongChieu phong = lc.getPhongChieu();

        if (body.getPhongChieu() != null && body.getPhongChieu().getId() != null) {
            phong = phongChieuService.findById(body.getPhongChieu().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phòng chiếu"));
        }
        if (body.getPhim() != null && body.getPhim().getId() != null) {
            Phim phim = phimService.findByIdOptional(body.getPhim().getId())
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
            java.util.Optional<LichChieu> conflicting = lichChieuService.findAll().stream()
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
        if (body.getThoiGianNghi() != null) lc.setThoiGianNghi(body.getThoiGianNghi());
        if (body.getGiaCoBan() != null) lc.setGiaCoBan(body.getGiaCoBan());
        if (body.getTrangThai() != null) lc.setTrangThai(body.getTrangThai());
        if (body.getPhongChieu() != null && body.getPhongChieu().getId() != null) lc.setPhongChieu(phong);

        return ResponseEntity.ok(lichChieuService.save(lc));
    }

    /**
     * DELETE /api/admin/lich-chieu/{id}
     * Requires: no active (non-cancelled) tickets for this showtime.
     */
    @DeleteMapping("/lich-chieu/{id}")
    public ResponseEntity<?> deleteLichChieu(@PathVariable Long id) {
        LichChieu lc = lichChieuService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy lịch chiếu"));

        boolean hasTickets = datVeService.findByLichChieuId(id).stream()
                .anyMatch(dv -> !"cancelled".equalsIgnoreCase(dv.getTrangThai()));
        if (hasTickets) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Không thể xóa lịch chiếu đã phát sinh vé"));
        }

        if ("da_chieu".equals(computeTrangThai(lc))) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Không thể xóa lịch chiếu đã chiếu"));
        }

        lc.setIsDeleted(true);
        lichChieuService.save(lc);
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
        return ResponseEntity.ok(rapChieuService.findAll());
    }

    @PostMapping("/rap-chieu")
    public ResponseEntity<?> createRap(@RequestBody RapChieu rap) {
        rap.setId(null);
        if (rap.getTrangThai() == null) rap.setTrangThai(true);
        return ResponseEntity.status(HttpStatus.CREATED).body(rapChieuService.save(rap));
    }

    @PutMapping("/rap-chieu/{id}")
    public ResponseEntity<?> updateRap(@PathVariable Long id, @RequestBody RapChieu body) {
        RapChieu rap = rapChieuService.getById(id)
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
        return ResponseEntity.ok(rapChieuService.save(rap));
    }

    @DeleteMapping("/rap-chieu/{id}")
    public ResponseEntity<?> deleteRap(@PathVariable Long id) {
        RapChieu rap = rapChieuService.getById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy rạp chiếu"));
        rap.setTrangThai(false);
        rapChieuService.save(rap);
        return ResponseEntity.ok(Map.of("message", "Đã vô hiệu hóa rạp chiếu"));
    }

    // ─────────────────────────────────────────────────────────────
    // SEAT (GHE NGOI) — admin seat map display + seat/row management
    // ─────────────────────────────────────────────────────────────

    /**
     * GET /api/admin/ghe-ngoi?phongChieuId={id}
     * Returns all seats for the given room.
     * Used by the admin "Ghế ngồi" tab seat-map display.
     */
    @GetMapping("/ghe-ngoi")
    public ResponseEntity<?> getGheByPhong(@RequestParam Long phongChieuId) {
        List<GheNgoi> gheList = gheNgoiService.findByPhongChieuId(phongChieuId);
        SeatDisplayUtil.applyLabels(gheList, SeatDisplayUtil.buildRoomLabels(gheList));
        SeatDisplayUtil.warnMissing(gheList, "GET /api/admin/ghe-ngoi");
        return ResponseEntity.ok(gheList);
    }

    /** Valid seat types shared by POST /ghe-ngoi/row and PUT /ghe-ngoi/{id}. */
    private static final java.util.Set<String> VALID_LOAI_GHE =
            java.util.Set.of("thường", "vip", "cặp đôi", "trống");

    /**
     * Standard price factor per seat type (kept in sync with createGheRow's switch).
     */
    private static BigDecimal heSoGiaFor(String loaiGhe) {
        switch (loaiGhe) {
            case "vip":     return new BigDecimal("1.50");
            case "cặp đôi": return new BigDecimal("2.00");
            case "trống":   return BigDecimal.ZERO;
            default:        return BigDecimal.ONE;
        }
    }

    /**
     * Bookable seat count for a room — excludes 'trống' cells (aisles/gaps),
     * so PhongChieu.sucChua never counts them.
     */
    private int countBookableSeats(Long phongChieuId) {
        return (int) gheNgoiService.findByPhongChieuId(phongChieuId)
                .stream()
                .filter(g -> !"trống".equals(g.getLoaiGhe()))
                .count();
    }

    /**
     * POST /api/admin/ghe-ngoi/row
     * Creates an entire seat row (e.g. F1..F10) in one call.
     * Existing seats in the row (same HangGhe + SoGhe) are skipped, so the
     * endpoint is safe to re-run. After inserting, the room's sucChua is
     * resynced to the actual seat count so "Sức chứa" always matches reality.
     * Body: { "phongChieuId": 1, "hangGhe": "F", "soGheTu": 1, "soGheDen": 10, "loaiGhe": "thường" }
     */
    @PostMapping("/ghe-ngoi/row")
    @Transactional
    public ResponseEntity<?> createGheRow(@RequestBody Map<String, Object> body) {
        try {
            Long phongChieuId = ((Number) body.get("phongChieuId")).longValue();
            String hangGhe    = String.valueOf(body.get("hangGhe")).trim().toUpperCase();
            int soGheTu       = ((Number) body.get("soGheTu")).intValue();
            int soGheDen      = ((Number) body.get("soGheDen")).intValue();
            String loaiGhe    = body.get("loaiGhe") != null ? String.valueOf(body.get("loaiGhe")) : "thường";

            if (hangGhe.isBlank() || soGheTu <= 0 || soGheDen < soGheTu) {
                return ResponseEntity.badRequest().body(Map.of("message", "Dữ liệu dãy ghế không hợp lệ"));
            }
            if (hangGhe.length() > 2) {
                return ResponseEntity.badRequest().body(Map.of("message", "Tên dãy ghế tối đa 2 ký tự"));
            }
            if (!VALID_LOAI_GHE.contains(loaiGhe)) {
                return ResponseEntity.badRequest().body(Map.of("message", "Loại ghế không hợp lệ"));
            }

            PhongChieu phong = phongChieuService.findById(phongChieuId)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phòng chiếu"));

            // Build existing keys once, trimmed (CHAR(2) pads HangGhe with spaces)
            java.util.Set<String> existingKeys = gheNgoiService.findByPhongChieuId(phongChieuId)
                    .stream()
                    .map(g -> g.getHangGhe().trim() + "-" + g.getSoGhe())
                    .collect(java.util.stream.Collectors.toSet());

            BigDecimal heSoGia = heSoGiaFor(loaiGhe);

            int created = 0;
            for (int n = soGheTu; n <= soGheDen; n++) {
                String key = hangGhe + "-" + n;
                if (existingKeys.contains(key)) continue;
                GheNgoi ghe = new GheNgoi();
                ghe.setPhongChieu(phong);
                ghe.setHangGhe(hangGhe);
                ghe.setSoGhe(n);
                ghe.setLoaiGhe(loaiGhe);
                ghe.setHeSoGia(heSoGia);
                gheNgoiService.save(ghe);
                created++;
            }

            // Link "Sức chứa" to the real seat count
            int soGheThucTe = countBookableSeats(phongChieuId);
            phong.setSucChua(soGheThucTe);
            phongChieuService.save(phong);

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("message",    "Đã thêm " + created + " ghế (dãy " + hangGhe + ")");
            result.put("created",    created);
            result.put("soGheThucTe", soGheThucTe);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Lỗi thêm dãy ghế: " + e.getMessage()));
        }
    }

    /**
     * DELETE /api/admin/ghe-ngoi/row?phongChieuId={id}&hangGhe={F}
     * Deletes every seat of a row, then resyncs the room's sucChua.
     */
    @DeleteMapping("/ghe-ngoi/row")
    public ResponseEntity<?> deleteGheRow(@RequestParam Long phongChieuId, @RequestParam String hangGhe) {
        try {
            PhongChieu phong = phongChieuService.findById(phongChieuId)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phòng chiếu"));
            String target = hangGhe.trim().toUpperCase();
            List<GheNgoi> seats = gheNgoiService.findByPhongChieuId(phongChieuId)
                    .stream()
                    .filter(g -> target.equals(g.getHangGhe().trim()))
                    .collect(Collectors.toList());
            gheNgoiService.deleteAll(seats);

            int soGheThucTe = countBookableSeats(phongChieuId);
            phong.setSucChua(soGheThucTe);
            phongChieuService.save(phong);

            return ResponseEntity.ok(Map.of(
                    "message",    "Đã xóa dãy " + target + " (" + seats.size() + " ghế)",
                    "deleted",    seats.size(),
                    "soGheThucTe", soGheThucTe));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Lỗi xóa dãy ghế: " + e.getMessage()));
        }
    }

    /**
     * DELETE /api/admin/ghe-ngoi/{id}
     * Deletes a single seat, then resyncs the room's sucChua.
     */
    @DeleteMapping("/ghe-ngoi/{id}")
    public ResponseEntity<?> deleteGhe(@PathVariable Long id) {
        try {
            GheNgoi ghe = gheNgoiService.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy ghế"));
            Long phongChieuId = ghe.getPhongChieu() != null ? ghe.getPhongChieu().getId() : null;
            gheNgoiService.delete(ghe);

            if (phongChieuId != null) {
                PhongChieu phong = phongChieuService.findById(phongChieuId).orElse(null);
                if (phong != null) {
                    int soGheThucTe = countBookableSeats(phongChieuId);
                    phong.setSucChua(soGheThucTe);
                    phongChieuService.save(phong);
                }
            }
            return ResponseEntity.ok(Map.of("message", "Đã xóa ghế"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Lỗi xóa ghế: " + e.getMessage()));
        }
    }

    /**
     * PUT /api/admin/ghe-ngoi/{id}
     * Updates loaiGhe and/or heSoGia on a single seat.
     * Body: { "loaiGhe": "vip" | "thường" | "cặp đôi" | "trống", "heSoGia": 1.50 }
     * When loaiGhe changes without an explicit heSoGia, the standard factor for
     * the new type is applied automatically ('trống' → 0).
     */
    @PutMapping("/ghe-ngoi/{id}")
    public ResponseEntity<?> updateGhe(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        GheNgoi ghe = gheNgoiService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy ghế"));
        boolean loaiChanged = false;
        if (body.containsKey("loaiGhe")) {
            String loai = body.get("loaiGhe") == null ? null : String.valueOf(body.get("loaiGhe")).trim();
            if (!VALID_LOAI_GHE.contains(loai)) {
                return ResponseEntity.badRequest().body(Map.of(
                        "message", "Loại ghế không hợp lệ (chấp nhận: thường, vip, cặp đôi, trống)"));
            }
            ghe.setLoaiGhe(loai);
            loaiChanged = true;
        }
        if (body.containsKey("heSoGia")) {
            ghe.setHeSoGia(new java.math.BigDecimal(body.get("heSoGia").toString()));
        } else if (loaiChanged) {
            ghe.setHeSoGia(heSoGiaFor(ghe.getLoaiGhe()));
        }
        GheNgoi saved = gheNgoiService.save(ghe);
        if (loaiChanged) {
            // Keep "Sức chứa" in sync when a seat type changes (e.g. → 'trống'),
            // same mechanism as POST /ghe-ngoi/row and the DELETE endpoints.
            PhongChieu phong = phongChieuService.findById(saved.getPhongChieu().getId())
                    .orElse(null);
            if (phong != null) {
                phong.setSucChua(countBookableSeats(phong.getId()));
                phongChieuService.save(phong);
            }
        }
        return ResponseEntity.ok(saved);
    }

    // ─────────────────────────────────────────────────────────────
    // ROOM ADMIN CRUD
    // ─────────────────────────────────────────────────────────────

    @PostMapping("/phong-chieu")
    public ResponseEntity<?> createPhong(@RequestBody PhongChieu phong) {
        phong.setId(null);
        if (phong.getTrangThai() == null) phong.setTrangThai(true);
        if (phong.getSucChua() == null) phong.setSucChua(0);
        bindPhongReferences(phong);
        return ResponseEntity.status(HttpStatus.CREATED).body(phongChieuService.save(phong));
    }

    /**
     * GET /api/admin/phong-chieu?rapChieuId={id}
     * Lists ALL rooms of a cinema (regardless of trangThai) enriched with the
     * real seat count (soGheThucTe), so the admin "Phòng chiếu" tab shows a
     * "Sức chứa" value that always matches the seats actually configured.
     */
    @GetMapping("/phong-chieu")
    public ResponseEntity<?> getPhongByRap(@RequestParam Long rapChieuId) {
        List<PhongChieu> phongs = phongChieuService.findByRapChieuId(rapChieuId);
        List<Map<String, Object>> result = phongs.stream().map(p -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id",          p.getId());
            m.put("tenPhong",    p.getTenPhong());
            m.put("loaiPhong",   p.getLoaiPhong());
            m.put("trangThai",   p.getTrangThai());
            m.put("sucChua",     p.getSucChua());
            m.put("soGheThucTe", countBookableSeats(p.getId()));
            m.put("dinhDangId",  p.getDinhDang() != null ? p.getDinhDang().getId() : null);
            m.put("dinhDang",    p.getDinhDang() != null ? p.getDinhDang().getTenDinhDang() : null);
            return m;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @PutMapping("/phong-chieu/{id}")
    public ResponseEntity<?> updatePhong(@PathVariable Long id, @RequestBody PhongChieu body) {
        PhongChieu phong = phongChieuService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phòng chiếu"));
        if (body.getTenPhong() != null) phong.setTenPhong(body.getTenPhong());
        if (body.getLoaiPhong() != null) phong.setLoaiPhong(body.getLoaiPhong());
        if (body.getSucChua() != null) phong.setSucChua(body.getSucChua());
        if (body.getSoDoGhe() != null) phong.setSoDoGhe(body.getSoDoGhe());
        if (body.getTrangThai() != null) phong.setTrangThai(body.getTrangThai());
        // DinhDangId is now required — bound to a managed reference
        if (body.getDinhDang() != null && body.getDinhDang().getId() != null) {
            phong.setDinhDang(dinhDangService.findById(body.getDinhDang().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy định dạng id=" + body.getDinhDang().getId())));
        } else {
            throw new IllegalArgumentException("Vui lòng chọn định dạng cho phòng chiếu");
        }
        return ResponseEntity.ok(phongChieuService.save(phong));
    }

    /**
     * DELETE /api/admin/phong-chieu/{id} — soft-deactivate (RQ49)
     * Sets trangThai = false so the room is hidden from active scheduling.
     */
    @DeleteMapping("/phong-chieu/{id}")
    public ResponseEntity<?> deactivatePhong(@PathVariable Long id) {
        PhongChieu phong = phongChieuService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phòng chiếu"));
        phong.setTrangThai(false);
        phongChieuService.save(phong);
        return ResponseEntity.ok(Map.of("message", "Đã vô hiệu hóa phòng chiếu"));
    }

    /**
     * Binds RapChieu + DinhDang references on a PhongChieu to managed entities
     * fetched by id. DinhDangId is required — throws if missing (prevents the
     * NULL-dinhDang bug that hid rooms from the schedule dropdown).
     */
    private void bindPhongReferences(PhongChieu phong) {
        if (phong.getRapChieu() == null || phong.getRapChieu().getId() == null) {
            throw new IllegalArgumentException("Vui lòng chọn rạp chiếu cho phòng");
        }
        RapChieu rap = rapChieuService.getById(phong.getRapChieu().getId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy rạp chiếu id=" + phong.getRapChieu().getId()));
        phong.setRapChieu(rap);

        if (phong.getDinhDang() == null || phong.getDinhDang().getId() == null) {
            throw new IllegalArgumentException("Vui lòng chọn định dạng cho phòng chiếu");
        }
        DinhDang dd = dinhDangService.findById(phong.getDinhDang().getId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy định dạng id=" + phong.getDinhDang().getId()));
        phong.setDinhDang(dd);
    }

    // ─────────────────────────────────────────────────────────────
    // PRODUCT ADMIN CRUD
    // ─────────────────────────────────────────────────────────────

    /** GET /api/admin/san-pham — returns ALL products regardless of dangHoatDong */
    @GetMapping("/san-pham")
    public ResponseEntity<java.util.List<SanPham>> getAllSanPham() {
        return ResponseEntity.ok(sanPhamService.findAll());
    }

    @PostMapping("/san-pham")
    public ResponseEntity<?> createSanPham(@RequestBody SanPham sp) {
        sp.setId(null);
        if (sp.getDangHoatDong() == null) sp.setDangHoatDong(true);
        return ResponseEntity.status(HttpStatus.CREATED).body(sanPhamService.save(sp));
    }

    @PutMapping("/san-pham/{id}")
    public ResponseEntity<?> updateSanPham(@PathVariable Long id, @RequestBody SanPham body) {
        SanPham sp = sanPhamService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm"));
        if (body.getTenSanPham() != null) sp.setTenSanPham(body.getTenSanPham());
        if (body.getMoTa() != null) sp.setMoTa(body.getMoTa());
        if (body.getGia() != null) sp.setGia(body.getGia());
        if (body.getLoaiSanPham() != null) sp.setLoaiSanPham(body.getLoaiSanPham());
        if (body.getAnhUrl() != null) sp.setAnhUrl(body.getAnhUrl());
        if (body.getTonKho() != null) sp.setTonKho(body.getTonKho());
        if (body.getDangHoatDong() != null) sp.setDangHoatDong(body.getDangHoatDong());
        return ResponseEntity.ok(sanPhamService.save(sp));
    }

    @DeleteMapping("/san-pham/{id}")
    public ResponseEntity<?> deleteSanPham(@PathVariable Long id) {
        SanPham sp = sanPhamService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm"));
        sp.setDangHoatDong(false);
        sanPhamService.save(sp);
        return ResponseEntity.ok(Map.of("message", "Đã vô hiệu hóa sản phẩm"));
    }

    // ─────────────────────────────────────────────────────────────
    // BANNER ADMIN CRUD
    // ─────────────────────────────────────────────────────────────

    /** GET /api/admin/banner — returns ALL banners regardless of status or date range */
    @GetMapping("/banner")
    public ResponseEntity<java.util.List<Banner>> getAllBanners() {
        return ResponseEntity.ok(bannerService.findAllByOrderByThuTuAsc());
    }

    @PostMapping("/banner")
    public ResponseEntity<?> createBanner(@RequestBody Banner banner) {
        banner.setId(null);
        if (banner.getDangHoatDong() == null) banner.setDangHoatDong(true);
        // Exactly one FK must be set and must match loaiBanner
        ResponseEntity<?> validErr = validateBannerTarget(banner);
        if (validErr != null) return validErr;
        return ResponseEntity.status(HttpStatus.CREATED).body(bannerService.save(banner));
    }

    @PutMapping("/banner/{id}")
    public ResponseEntity<?> updateBanner(@PathVariable Long id, @RequestBody Banner body) {
        Banner banner = bannerService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy banner"));
        if (body.getTieuDe() != null)       banner.setTieuDe(body.getTieuDe());
        if (body.getHinhAnh() != null)       banner.setHinhAnh(body.getHinhAnh());
        if (body.getThuTu() != null)         banner.setThuTu(body.getThuTu());
        if (body.getDangHoatDong() != null)  banner.setDangHoatDong(body.getDangHoatDong());
        if (body.getNgayBatDau() != null)    banner.setNgayBatDau(body.getNgayBatDau());
        if (body.getNgayKetThuc() != null)   banner.setNgayKetThuc(body.getNgayKetThuc());
        if (body.getMoTa() != null)          banner.setMoTa(body.getMoTa());
        // When the typed-target fields are explicitly present in the request, update them
        if (body.getLoaiBanner() != null) {
            banner.setLoaiBanner(body.getLoaiBanner());
            banner.setPhimId(body.getPhimId());
            // Validate after applying new target
            ResponseEntity<?> validErr = validateBannerTarget(banner);
            if (validErr != null) return validErr;
        }
        return ResponseEntity.ok(bannerService.save(banner));
    }

    /** Enforces the loaiBanner/FK rule. "Phim" requires a movie; "Khac" has no link. */
    private ResponseEntity<?> validateBannerTarget(Banner b) {
        String type = b.getLoaiBanner();
        if (type == null || type.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Vui lòng chọn loại banner (Phim / Khac)"));
        }
        if ("Phim".equals(type) && b.getPhimId() == null)
            return ResponseEntity.badRequest().body(Map.of("message", "Loại 'Phim' yêu cầu chọn phim"));
        if ("Khac".equals(type))
            b.setPhimId(null);
        return null;
    }

    @DeleteMapping("/banner/{id}")
    public ResponseEntity<?> deleteBanner(@PathVariable Long id) {
        bannerService.deleteById(id);
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
        Page<DatVe> result = datVeService.searchAdmin(query, status, pr);
        datVeService.ganNhanHienThiChoVes(result.getContent(), "GET /api/admin/dat-ve");

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
        try {
            datVeService.refundBooking(id);
            DatVe datVe = datVeService.findById(id).orElse(null);
            return ResponseEntity.ok(Map.of(
                    "message", "Đã hoàn tiền thành công",
                    "maDatVe", datVe != null ? datVe.getMaDatVe() : "",
                    "trangThai", "cancelled"
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Lỗi hoàn tiền: " + e.getMessage()));
        }
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

        List<DatVe> paid = datVeService.findAll().stream()
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
        List<SeatLock> locks = seatLockService.findActiveByLichChieu(lichChieuId);
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
            gheNgoiService.findById(lock.getGheNgoiId()).ifPresent(ghe -> {
                m.put("hangGhe",  (ghe.getHangGhe() != null ? ghe.getHangGhe().trim() : ""));
                m.put("soGhe",    ghe.getSoGhe());
                m.put("loaiGhe",  ghe.getLoaiGhe());
                m.put("seatLabel", (ghe.getHangGhe() != null ? ghe.getHangGhe().trim() : "") + ghe.getSoGhe());
            });
            // Attach user email/name if available
            if (lock.getNguoiDungId() != null) {
                nguoiDungService.findById(lock.getNguoiDungId()).ifPresent(u -> {
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
        if (!seatLockService.existsById(lockId)) {
            return ResponseEntity.notFound().build();
        }
        seatLockService.deleteById(lockId);
        return ResponseEntity.ok(Map.of("message", "Đã giải phóng ghế"));
    }

    /**
     * GET /api/admin/config/seat-lock-minutes
     * Returns current lock duration in minutes.
     */
    @GetMapping("/config/seat-lock-minutes")
    public ResponseEntity<?> getSeatLockMinutes() {
        int minutes = systemConfigService.findById("SEAT_LOCK_MINUTES")
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
        SystemConfig config = systemConfigService.findById("SEAT_LOCK_MINUTES")
                .orElseGet(() -> { SystemConfig c = new SystemConfig(); c.setConfigKey("SEAT_LOCK_MINUTES"); return c; });
        config.setConfigValue(String.valueOf(minutes));
        systemConfigService.save(config);
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
        LichChieu lc = lichChieuService.findById(lichChieuId).orElse(null);
        if (lc == null) return ResponseEntity.notFound().build();
        Long phongChieuId = lc.getPhongChieu().getId();

        // 2. All seats in the room
        List<GheNgoi> allSeats = gheNgoiService.findByPhongChieuId(phongChieuId);

        // 3. Active seat locks for this showtime
        LocalDateTime now = LocalDateTime.now();
        List<SeatLock> activeLocks = seatLockService.findActiveByLichChieu(lichChieuId);
        // Map: gheNgoiId → SeatLock (for O(1) lookup)
        Map<Long, SeatLock> lockMap = activeLocks.stream()
                .collect(Collectors.toMap(SeatLock::getGheNgoiId, s -> s, (a, b) -> a));

        // 4. Booked seats from ChiTietDatGhe linked to confirmed/paid bookings
        //    (only non-cancelled bookings count as holding the seat)
        Set<Long> bookedSeatIds = lichChieuService.findChiTietDatGheByLichChieuId(lichChieuId)
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
        Map<Long, Integer> displayLabels = SeatDisplayUtil.buildRoomLabels(allSeats);
        SeatDisplayUtil.applyLabels(allSeats, displayLabels);
        SeatDisplayUtil.warnMissing(allSeats, "GET /api/admin/seat-map");
        List<Map<String, Object>> result = allSeats.stream().map(seat -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("gheNgoiId", seat.getId());
            m.put("hangGhe",   (seat.getHangGhe() != null ? seat.getHangGhe().trim() : ""));
            m.put("soGhe",     seat.getSoGhe());
            m.put("soGheHienThi", displayLabels.get(seat.getId()));
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
