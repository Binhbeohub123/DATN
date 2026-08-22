package com.polycinema.backend.service;

import com.polycinema.backend.entity.*;
import com.polycinema.backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DatVeService {

    private final DatVeRepository datVeRepository;
    private final ChiTietDatGheRepository chiTietDatGheRepository;
    private final ChiTietDatSanPhamRepository chiTietDatSanPhamRepository;
    private final LichChieuRepository lichChieuRepository;
    private final GheNgoiRepository gheNgoiRepository;
    private final SanPhamRepository sanPhamRepository;
    private final KhuyenMaiRepository khuyenMaiRepository;
    private final NguoiDungRepository nguoiDungRepository;
    private final SeatLockRepository seatLockRepository;
    private final ThanhToanRepository thanhToanRepository;

    // ─────────────────────────────────────────────────────────────
    // Loyalty points policy [RQ]
    //   Earn  : 1 điểm / 1.000đ thực trả, chỉ khi đơn (tiền thực trả) >= 100.000đ
    //   Redeem: 1 điểm = 100đ; yêu cầu đơn (gốc) >= 100.000đ và tối thiểu 100 điểm;
    //           giảm tối đa 30% giá trị đơn → chỉ trừ ĐÚNG số điểm đã dùng thực tế.
    // ─────────────────────────────────────────────────────────────
    private static final BigDecimal MIN_ORDER_FOR_POINTS = new BigDecimal("100000");
    private static final BigDecimal MIN_POINTS_TO_REDEEM = BigDecimal.valueOf(100);
    private static final BigDecimal POINT_VALUE_VND       = BigDecimal.valueOf(100);
    private static final BigDecimal REDEEM_CAP_RATE       = new BigDecimal("0.30");

    /**
     * Applies the redeem policy for a points request.
     * @return the actual discount (VND) granted — always a whole multiple of
     *         POINT_VALUE_VND, so the points really consumed are
     *         discount / POINT_VALUE_VND (no more wasted points).
     */
    private BigDecimal applyPointsPolicy(NguoiDung user, int diemSuDung, BigDecimal tongTienGoc) {
        if (tongTienGoc.compareTo(MIN_ORDER_FOR_POINTS) < 0) {
            throw new IllegalArgumentException("Đơn từ 100.000đ trở lên mới được dùng điểm");
        }
        if (diemSuDung < MIN_POINTS_TO_REDEEM.intValue()) {
            throw new IllegalArgumentException("Cần tối thiểu 100 điểm để sử dụng");
        }
        int balance = user.getDiemTichLuy() == null ? 0 : user.getDiemTichLuy();
        if (diemSuDung > balance) {
            throw new IllegalArgumentException("Số điểm không đủ (bạn đang có " + balance + " điểm)");
        }
        BigDecimal giam = BigDecimal.valueOf(diemSuDung).multiply(POINT_VALUE_VND)
                .min(tongTienGoc.multiply(REDEEM_CAP_RATE));
        // Round down to a whole number of points so the deduction matches the money
        long points = giam.divideToIntegralValue(POINT_VALUE_VND).longValue();
        return BigDecimal.valueOf(points).multiply(POINT_VALUE_VND);
    }

    /** Points actually redeemed by a discount amount (whole points only). */
    private int actualPoints(BigDecimal discount) {
        return discount.divideToIntegralValue(POINT_VALUE_VND).intValue();
    }

    // ─────────────────────────────────────────────────────────────
    // Promo code policy
    //   - Giới hạn số lượt dùng tối đa trên toàn hệ thống (GioiHanSuDung)
    //   - Mỗi người dùng chỉ được dùng 1 mã 1 lần (đếm đơn còn hiệu lực)
    // ─────────────────────────────────────────────────────────────
    private void validatePromo(KhuyenMai km, NguoiDung user) {
        if (!Boolean.TRUE.equals(km.getDangHoatDong())) {
            throw new IllegalArgumentException("Mã khuyến mãi đã hết hiệu lực");
        }
        java.time.LocalDate today = java.time.LocalDate.now();
        if (km.getNgayBatDau() != null && today.isBefore(km.getNgayBatDau())) {
            throw new IllegalArgumentException("Mã khuyến mãi chưa có hiệu lực");
        }
        if (km.getNgayKetThuc() != null && today.isAfter(km.getNgayKetThuc())) {
            throw new IllegalArgumentException("Mã khuyến mãi đã hết hạn");
        }

        // Giới hạn lượt dùng toàn hệ thống — đếm trực tiếp trên DB (đơn chưa hủy)
        long used = datVeRepository.countByKhuyenMaiIdAndTrangThaiNot(km.getId(), "cancelled");
        if (km.getGioiHanSuDung() != null && used >= km.getGioiHanSuDung()) {
            throw new IllegalArgumentException("Mã khuyến mãi đã hết lượt sử dụng");
        }

        // Mỗi người chỉ dùng 1 mã 1 lần (chỉ áp dụng khi có tài khoản)
        if (user != null) {
            long userUsed = datVeRepository.countByKhuyenMaiIdAndNguoiDungIdAndTrangThaiNot(
                    km.getId(), user.getId(), "cancelled");
            if (userUsed > 0) {
                throw new IllegalArgumentException("Bạn đã sử dụng mã khuyến mãi này rồi");
            }
        }
    }

    /**
     * Tạo đơn đặt vé
     */
    @Transactional
    public DatVe createBooking(
            Long userId,
            Long lichChieuId,
            List<Long> gheIds,
            List<Map<String, Object>> comboData,
            String maKhuyenMai,
            Integer diemSuDung
    ) {
        if (gheIds == null || gheIds.isEmpty()) {
            throw new IllegalArgumentException("Phải chọn ít nhất 1 ghế");
        }
        if (gheIds.size() > 8) {
            throw new IllegalArgumentException("Tối đa 8 ghế/lần đặt");
        }

        LichChieu lichChieu = lichChieuRepository.findById(lichChieuId)
                .orElseThrow(() -> new IllegalArgumentException("Lịch chiếu không tồn tại"));

        NguoiDung nguoiDung = nguoiDungRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Người dùng không tồn tại"));

        List<GheNgoi> gheList = new ArrayList<>();
        for (Long gheId : gheIds) {
            GheNgoi ghe = gheNgoiRepository.findById(gheId)
                    .orElseThrow(() -> new IllegalArgumentException("Ghế ID " + gheId + " không tồn tại"));
            if (!ghe.getPhongChieu().getId().equals(lichChieu.getPhongChieu().getId())) {
                throw new IllegalArgumentException("Ghế ID " + gheId + " không thuộc phòng chiếu này");
            }
            gheList.add(ghe);
        }

        // Check if seats are already taken in this showtime
        Set<Long> gheDaDat = chiTietDatGheRepository.findByLichChieuId(lichChieuId)
                .stream()
                .map(ct -> ct.getGheNgoi().getId())
                .collect(Collectors.toSet());
        for (Long gheId : gheIds) {
            if (gheDaDat.contains(gheId)) {
                throw new IllegalArgumentException("Ghế ID " + gheId + " đã được đặt");
            }
        }

        BigDecimal tongTienGhe = BigDecimal.ZERO;
        for (GheNgoi ghe : gheList) {
            tongTienGhe = tongTienGhe.add(lichChieu.getGiaCoBan().multiply(ghe.getHeSoGia()));
        }

        BigDecimal tongTienCombo = BigDecimal.ZERO;
        List<ChiTietDatSanPham> chiTietComboList = new ArrayList<>();
        if (comboData != null && !comboData.isEmpty()) {
            for (Map<String, Object> combo : comboData) {
                Long sanPhamId = ((Number) combo.get("id")).longValue();
                Integer soLuong = ((Number) combo.get("soLuong")).intValue();
                SanPham sanPham = sanPhamRepository.findById(sanPhamId)
                        .orElseThrow(() -> new IllegalArgumentException("Sản phẩm ID " + sanPhamId + " không tồn tại"));
                Integer tonKhoHienTai = sanPham.getTonKho() == null ? 0 : sanPham.getTonKho();
                if (tonKhoHienTai < soLuong) {
                    throw new IllegalArgumentException("Sản phẩm '" + sanPham.getTenSanPham()
                            + "' không đủ tồn kho (còn " + tonKhoHienTai + ")");
                }
                BigDecimal giaSanPham = sanPham.getGia();
                tongTienCombo = tongTienCombo.add(giaSanPham.multiply(BigDecimal.valueOf(soLuong)));
                ChiTietDatSanPham chiTiet = new ChiTietDatSanPham();
                chiTiet.setSanPham(sanPham);
                chiTiet.setSoLuong(soLuong);
                chiTiet.setGiaLucMua(giaSanPham);
                chiTietComboList.add(chiTiet);
            }
        }

        BigDecimal tongTienGoc = tongTienGhe.add(tongTienCombo);

        KhuyenMai khuyenMai = null;
        BigDecimal tienGiamKhuyenMai = BigDecimal.ZERO;
        if (maKhuyenMai != null && !maKhuyenMai.isEmpty()) {
            khuyenMai = khuyenMaiRepository.findByMaKhuyenMai(maKhuyenMai)
                    .orElseThrow(() -> new IllegalArgumentException("Mã khuyến mãi không hợp lệ"));
            validatePromo(khuyenMai, nguoiDung);
            if (khuyenMai.getDonHangToiThieu() != null
                    && tongTienGoc.compareTo(khuyenMai.getDonHangToiThieu()) < 0) {
                throw new IllegalArgumentException("Đơn hàng tối thiểu "
                        + khuyenMai.getDonHangToiThieu().toPlainString() + " VND để dùng mã này");
            }
            if ("percent".equalsIgnoreCase(khuyenMai.getLoaiGiamGia())) {
                tienGiamKhuyenMai = tongTienGoc.multiply(khuyenMai.getGiaTriGiam())
                        .divide(BigDecimal.valueOf(100));
                if (khuyenMai.getGiaTriGiamToiDa() != null) {
                    tienGiamKhuyenMai = tienGiamKhuyenMai.min(khuyenMai.getGiaTriGiamToiDa());
                }
            } else {
                tienGiamKhuyenMai = khuyenMai.getGiaTriGiam().min(tongTienGoc);
            }
        }

        BigDecimal tienGiamTuDiem = BigDecimal.ZERO;
        int diemThucDung = 0;
        if (diemSuDung != null && diemSuDung > 0) {
            tienGiamTuDiem = applyPointsPolicy(nguoiDung, diemSuDung, tongTienGoc);
            diemThucDung = actualPoints(tienGiamTuDiem);
        }

        BigDecimal tongTienThanhToan = tongTienGoc.subtract(tienGiamKhuyenMai).subtract(tienGiamTuDiem);
        if (tongTienThanhToan.compareTo(BigDecimal.ZERO) < 0) {
            tongTienThanhToan = BigDecimal.ZERO;
        }

        DatVe datVe = new DatVe();
        datVe.setMaDatVe(generateBookingCode());
        datVe.setNguoiDung(nguoiDung);
        datVe.setLichChieu(lichChieu);
        datVe.setKhuyenMai(khuyenMai);
        datVe.setTongTienGoc(tongTienGoc);
        datVe.setTienGiamKhuyenMai(tienGiamKhuyenMai);
        datVe.setDiemSuDung(diemThucDung);
        datVe.setTienGiamTuDiem(tienGiamTuDiem);
        datVe.setTongTienThanhToan(tongTienThanhToan);
        datVe.setTrangThai("pending");
        datVe.setTrangThaiThanhToan("unpaid");
        datVe.setHetHanGiuGhe(LocalDateTime.now().plusMinutes(15));

        DatVe savedDatVe = datVeRepository.save(datVe);

        for (GheNgoi ghe : gheList) {
            ChiTietDatGhe chiTiet = new ChiTietDatGhe();
            chiTiet.setDatVe(savedDatVe);
            chiTiet.setGheNgoi(ghe);
            chiTiet.setLichChieu(lichChieu);
            chiTiet.setGiaTien(lichChieu.getGiaCoBan().multiply(ghe.getHeSoGia()));
            chiTiet.setHeSoGiaLucDat(ghe.getHeSoGia());
            chiTietDatGheRepository.save(chiTiet);
        }

        for (ChiTietDatSanPham chiTiet : chiTietComboList) {
            chiTiet.setDatVe(savedDatVe);
            chiTietDatSanPhamRepository.save(chiTiet);
        }

        // Đếm lượt dùng mã khuyến mãi (sẽ trừ lại khi đơn bị hủy)
        if (khuyenMai != null && khuyenMai.getDaSuDung() != null) {
            khuyenMai.setDaSuDung(khuyenMai.getDaSuDung() + 1);
            khuyenMaiRepository.save(khuyenMai);
        }

        return savedDatVe;
    }

    // ─────────────────────────────────────────────────────────────
    // Query methods
    // ─────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<DatVe> getBookingHistory(Long userId) {
        return datVeRepository.findByNguoiDungIdOrderByIdDesc(userId);
    }

    @Transactional(readOnly = true)
    public DatVe getBookingDetail(Long bookingId) {
        return datVeRepository.findByIdWithDetails(bookingId).orElse(null);
    }

    public DatVe findByMaDatVe(String maDatVe) {
        return datVeRepository.findByMaDatVe(maDatVe).orElse(null);
    }

    @Transactional
    public DatVe checkIn(String maDatVe, NguoiDung nhanVien) {
        DatVe datVe = datVeRepository.findByMaDatVe(maDatVe)
                .orElseThrow(() -> new IllegalArgumentException("Mã vé không hợp lệ"));
        if ("đã sử dụng".equals(datVe.getTrangThaiCheckIn())) {
            throw new IllegalArgumentException("Vé đã được sử dụng lúc " +
                    (datVe.getThoiGianCheckIn() != null ? datVe.getThoiGianCheckIn().toString() : "không rõ"));
        }
        if (datVe.getLichChieu() != null && datVe.getLichChieu().getThoiGianBatDau() != null) {
            java.time.LocalDateTime now = java.time.LocalDateTime.now();
            java.time.LocalDateTime start = datVe.getLichChieu().getThoiGianBatDau();
            if (now.isBefore(start.minusMinutes(20))) {
                throw new IllegalArgumentException("Chưa tới giờ check-in (chỉ hỗ trợ check-in trước 20 phút)");
            }
            if (now.isAfter(start.plusMinutes(120))) {
                throw new IllegalArgumentException("Suất chiếu đã kết thúc hoặc quá giờ check-in");
            }
        }
        datVe.setTrangThaiCheckIn("đã sử dụng");
        datVe.setThoiGianCheckIn(java.time.LocalDateTime.now());
        datVe.setNhanVienCheckIn(nhanVien);
        return datVeRepository.save(datVe);
    }

    public Map<String, Object> getShiftReport(Long nhanVienId, java.time.LocalDateTime from, java.time.LocalDateTime to) {
        java.time.LocalDateTime start = from != null ? from : java.time.LocalDate.now().atStartOfDay();
        java.time.LocalDateTime end   = to   != null ? to   : java.time.LocalDateTime.now();

        List<DatVe> allDatVe = datVeRepository.findAll();

        List<DatVe> shiftSales = allDatVe.stream()
                .filter(dv -> dv.getNhanVien() != null && dv.getNhanVien().getId().equals(nhanVienId))
                .filter(dv -> dv.getNgayTao() != null && !dv.getNgayTao().isBefore(start) && !dv.getNgayTao().isAfter(end))
                .filter(dv -> "paid".equals(dv.getTrangThaiThanhToan()))
                .collect(java.util.stream.Collectors.toList());

        long tongVeBan = shiftSales.size();
        java.math.BigDecimal tongDoanhThu = shiftSales.stream()
                .map(dv -> dv.getTongTienThanhToan() != null ? dv.getTongTienThanhToan() : java.math.BigDecimal.ZERO)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

        long soVeCheckIn = allDatVe.stream()
                .filter(dv -> dv.getNhanVienCheckIn() != null && dv.getNhanVienCheckIn().getId().equals(nhanVienId))
                .filter(dv -> dv.getThoiGianCheckIn() != null && !dv.getThoiGianCheckIn().isBefore(start) && !dv.getThoiGianCheckIn().isAfter(end))
                .count();

        Map<String, Object> stats = new java.util.LinkedHashMap<>();
        stats.put("from", start.toString());
        stats.put("to", end.toString());
        stats.put("tongVeBan", tongVeBan);
        stats.put("tongDoanhThu", tongDoanhThu);
        stats.put("soVeCheckIn", soVeCheckIn);
        return stats;
    }

    // ─────────────────────────────────────────────────────────────
    // Cancel — customer-facing (owner check enforced)
    // ─────────────────────────────────────────────────────────────

    @Transactional
    public void cancelBooking(Long bookingId, Long userId) {
        DatVe datVe = datVeRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Đơn đặt vé không tồn tại"));

        if (!datVe.getNguoiDung().getId().equals(userId)) {
            throw new IllegalArgumentException("Bạn không có quyền hủy đơn này");
        }
        if ("paid".equalsIgnoreCase(datVe.getTrangThaiThanhToan())) {
            throw new IllegalArgumentException("Không thể hủy đơn đã thanh toán");
        }

        performCancel(datVe);
    }

    // ─────────────────────────────────────────────────────────────
    // Cancel — admin (no owner check)
    // ─────────────────────────────────────────────────────────────

    @Transactional
    public void cancelBookingByAdmin(String maDatVe) {
        DatVe datVe = datVeRepository.findByMaDatVe(maDatVe)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn đặt vé: " + maDatVe));
        if ("cancelled".equals(datVe.getTrangThai())) {
            throw new IllegalArgumentException("Đơn đặt vé đã bị hủy trước đó");
        }
        performCancel(datVe);
    }

    // ─────────────────────────────────────────────────────────────
    // Core cancel — used by customer, admin, and expiry scheduler
    // ─────────────────────────────────────────────────────────────

    /**
     * Cancels a booking.
     *
     * IMPORTANT — DB constraints:
     *   TrangThai         CHECK: 'pending' | 'confirmed' | 'cancelled'
     *   TrangThaiThanhToan CHECK: 'unpaid'  | 'paid'
     *
     * We ONLY set TrangThai = 'cancelled'.
     * We NEVER touch TrangThaiThanhToan — it stays 'unpaid' (payment never happened).
     * Only markPaid() in ThanhToanService sets TrangThaiThanhToan = 'paid'.
     *
     * Also deletes ChiTietDatGhe rows so the seats show as available in the seat map.
     * GheNgoi has no status field — availability is determined solely by whether a
     * ChiTietDatGhe record exists for that seat + showtime.
     */
    @Transactional
    public void performCancel(DatVe datVe) {
        // Idempotency guard — a cancelled booking must not be processed twice
        // (webhook + cancel redirect + auto-expiry can all call this).
        if ("cancelled".equals(datVe.getTrangThai())) {
            log.debug("performCancel: booking {} already cancelled — skipping", datVe.getMaDatVe());
            return;
        }

        // ── 1. Mark booking cancelled ─────────────────────────────
        datVe.setTrangThai("cancelled");
        // DO NOT call setTrangThaiThanhToan() — CHECK constraint only allows 'unpaid'|'paid'
        datVeRepository.save(datVe);

        // ── 2. Release seat records and all seat locks ────────────
        // releaseSeatsByBooking handles:
        //   - ChiTietDatGhe deletion (frees seats on seat map)
        //   - SeatLock deletion by maDatVe
        //   - SeatLock deletion by gheNgoiId + lichChieuId (orphaned locks)
        releaseSeatsByBooking(datVe);

        // ── 3. Decrement promo usage counter ──────────────────────
        if (datVe.getKhuyenMai() != null) {
            KhuyenMai km = datVe.getKhuyenMai();
            if (km.getDaSuDung() != null && km.getDaSuDung() > 0) {
                km.setDaSuDung(km.getDaSuDung() - 1);
                khuyenMaiRepository.save(km);
            }
        }

        // ── 4. Restore product inventory if the booking was paid ──
        // Only paid bookings had their stock deducted (deductStock is called
        // on payment). Unpaid/cancelled bookings never deducted anything.
        if ("paid".equalsIgnoreCase(datVe.getTrangThaiThanhToan())) {
            restoreStock(datVe);
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Seat release — shared by performCancel() and BookingExpiryService
    // ─────────────────────────────────────────────────────────────

    /**
     * Deletes all ChiTietDatGhe records for this booking so the seats
     * become available again in the seat map, and releases any orphaned
     * SeatLock records that are tied to those seats by (gheNgoiId, lichChieuId)
     * — these are locks created when the user clicked a seat before the booking
     * was confirmed, where maDatVe may be NULL.
     *
     * Call order:
     *  1. Read ChiTietDatGhe list first (needed for step 3)
     *  2. Delete ChiTietDatGhe rows (frees seats on the seat map)
     *  3. Delete SeatLock by maDatVe (named locks)
     *  4. Delete SeatLock by gheNgoiId + lichChieuId (orphaned / unnamed locks)
     */
    @Transactional
    public void releaseSeatsByBooking(DatVe datVe) {
        // Step 1 — read BEFORE deleting (needed for step 4)
        List<ChiTietDatGhe> seats = chiTietDatGheRepository.findByDatVeId(datVe.getId());

        // Step 2 — delete ChiTietDatGhe rows → seats become available in seat map
        if (!seats.isEmpty()) {
            chiTietDatGheRepository.deleteAll(seats);
        }

        // Step 3 — delete SeatLock records tied to this booking by maDatVe
        seatLockRepository.deleteByMaDatVe(datVe.getMaDatVe());

        // Step 4 — delete orphaned SeatLock records by (gheNgoiId, lichChieuId)
        // These exist when a user locked a seat before the booking was confirmed
        // and the lock's maDatVe was never set (NULL).
        if (datVe.getLichChieu() != null) {
            Long lichChieuId = datVe.getLichChieu().getId();
            for (ChiTietDatGhe ct : seats) {
                seatLockRepository.deleteByGheNgoiIdAndLichChieuId(
                        ct.getGheNgoi().getId(), lichChieuId);
            }
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Counter sale — staff POS (walk-in, immediate payment)
    // ─────────────────────────────────────────────────────────────

    @Transactional
    public DatVe createCounterSale(
            Long nhanVienId,
            String khachHangEmail,
            Long lichChieuId,
            List<Long> gheIds,
            List<Map<String, Object>> comboData,
            String maKhuyenMai,
            Integer diemSuDung) {

        if (gheIds == null || gheIds.isEmpty()) {
            throw new IllegalArgumentException("Phải chọn ít nhất 1 ghế");
        }

        LichChieu lichChieu = lichChieuRepository.findById(lichChieuId)
                .orElseThrow(() -> new IllegalArgumentException("Lịch chiếu không tồn tại"));

        NguoiDung nhanVien = nguoiDungRepository.findById(nhanVienId)
                .orElseThrow(() -> new IllegalArgumentException("Nhân viên không tồn tại"));

        NguoiDung khachHang = null;
        if (khachHangEmail != null && !khachHangEmail.isBlank()) {
            khachHang = nguoiDungRepository.findByEmail(khachHangEmail.trim())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Không tìm thấy tài khoản với email " + khachHangEmail.trim()));
        }

        List<GheNgoi> gheList = new ArrayList<>();
        for (Long gheId : gheIds) {
            GheNgoi ghe = gheNgoiRepository.findById(gheId)
                    .orElseThrow(() -> new IllegalArgumentException("Ghế ID " + gheId + " không tồn tại"));
            if (!ghe.getPhongChieu().getId().equals(lichChieu.getPhongChieu().getId())) {
                throw new IllegalArgumentException("Ghế ID " + gheId + " không thuộc phòng chiếu này");
            }
            gheList.add(ghe);
        }

        Set<Long> gheDaDat = chiTietDatGheRepository.findByLichChieuId(lichChieuId)
                .stream().map(ct -> ct.getGheNgoi().getId()).collect(Collectors.toSet());
        for (Long gheId : gheIds) {
            if (gheDaDat.contains(gheId)) {
                throw new IllegalArgumentException("Ghế ID " + gheId + " đã được đặt");
            }
        }

        BigDecimal tongTienGhe = BigDecimal.ZERO;
        for (GheNgoi ghe : gheList) {
            tongTienGhe = tongTienGhe.add(lichChieu.getGiaCoBan().multiply(ghe.getHeSoGia()));
        }

        BigDecimal tongTienCombo = BigDecimal.ZERO;
        List<ChiTietDatSanPham> chiTietComboList = new ArrayList<>();
        if (comboData != null && !comboData.isEmpty()) {
            for (Map<String, Object> combo : comboData) {
                Object sanPhamRaw = combo.get("sanPhamId") != null ? combo.get("sanPhamId") : combo.get("id");
                Object soLuongRaw = combo.get("soLuong");
                if (sanPhamRaw == null) throw new IllegalArgumentException("Thiếu trường sanPhamId trong comboData");
                if (soLuongRaw == null) throw new IllegalArgumentException("Thiếu trường soLuong trong comboData");
                Long sanPhamId = ((Number) sanPhamRaw).longValue();
                Integer soLuong = ((Number) soLuongRaw).intValue();
                SanPham sanPham = sanPhamRepository.findById(sanPhamId)
                        .orElseThrow(() -> new IllegalArgumentException("Sản phẩm ID " + sanPhamId + " không tồn tại"));
                Integer tonKhoHienTai = sanPham.getTonKho() == null ? 0 : sanPham.getTonKho();
                if (tonKhoHienTai < soLuong) {
                    throw new IllegalArgumentException("Sản phẩm '" + sanPham.getTenSanPham()
                            + "' không đủ tồn kho (còn " + tonKhoHienTai + ")");
                }
                tongTienCombo = tongTienCombo.add(sanPham.getGia().multiply(BigDecimal.valueOf(soLuong)));
                ChiTietDatSanPham chiTiet = new ChiTietDatSanPham();
                chiTiet.setSanPham(sanPham);
                chiTiet.setSoLuong(soLuong);
                chiTiet.setGiaLucMua(sanPham.getGia());
                chiTietComboList.add(chiTiet);
            }
        }

        BigDecimal tongTienGoc = tongTienGhe.add(tongTienCombo);

        KhuyenMai khuyenMai = null;
        BigDecimal tienGiamKhuyenMai = BigDecimal.ZERO;
        if (maKhuyenMai != null && !maKhuyenMai.isEmpty()) {
            khuyenMai = khuyenMaiRepository.findByMaKhuyenMai(maKhuyenMai)
                    .orElseThrow(() -> new IllegalArgumentException("Mã khuyến mãi không hợp lệ"));
            validatePromo(khuyenMai, khachHang);
            if (khuyenMai.getDonHangToiThieu() != null
                    && tongTienGoc.compareTo(khuyenMai.getDonHangToiThieu()) < 0) {
                throw new IllegalArgumentException("Đơn hàng tối thiểu "
                        + khuyenMai.getDonHangToiThieu().toPlainString() + " VND để dùng mã này");
            }
            if ("percent".equalsIgnoreCase(khuyenMai.getLoaiGiamGia())) {
                tienGiamKhuyenMai = tongTienGoc.multiply(khuyenMai.getGiaTriGiam())
                        .divide(BigDecimal.valueOf(100));
                if (khuyenMai.getGiaTriGiamToiDa() != null) {
                    tienGiamKhuyenMai = tienGiamKhuyenMai.min(khuyenMai.getGiaTriGiamToiDa());
                }
            } else {
                tienGiamKhuyenMai = khuyenMai.getGiaTriGiam().min(tongTienGoc);
            }
        }

        BigDecimal tienGiamTuDiem = BigDecimal.ZERO;
        int diemThucDung = 0;
        if (diemSuDung != null && diemSuDung > 0) {
            if (khachHang == null) {
                throw new IllegalArgumentException("Chưa chọn khách hàng để dùng điểm");
            }
            tienGiamTuDiem = applyPointsPolicy(khachHang, diemSuDung, tongTienGoc);
            diemThucDung = actualPoints(tienGiamTuDiem);
        }

        BigDecimal tongTienThanhToan = tongTienGoc.subtract(tienGiamKhuyenMai)
                .subtract(tienGiamTuDiem).max(BigDecimal.ZERO);

        DatVe datVe = new DatVe();
        datVe.setMaDatVe(generateBookingCode());
        datVe.setNguoiDung(khachHang);
        datVe.setNhanVien(nhanVien);
        datVe.setLichChieu(lichChieu);
        datVe.setKhuyenMai(khuyenMai);
        datVe.setTongTienGoc(tongTienGoc);
        datVe.setTienGiamKhuyenMai(tienGiamKhuyenMai);
        datVe.setDiemSuDung(diemThucDung);
        datVe.setTienGiamTuDiem(tienGiamTuDiem);
        datVe.setTongTienThanhToan(tongTienThanhToan);
        datVe.setTrangThai("confirmed");
        datVe.setTrangThaiThanhToan("paid");
        datVe.setTrangThaiCheckIn("chưa sử dụng");
        datVe.setMaQR(datVe.getMaDatVe() + "-" + System.currentTimeMillis());

        DatVe savedDatVe = datVeRepository.save(datVe);

        for (GheNgoi ghe : gheList) {
            ChiTietDatGhe chiTiet = new ChiTietDatGhe();
            chiTiet.setDatVe(savedDatVe);
            chiTiet.setGheNgoi(ghe);
            chiTiet.setLichChieu(lichChieu);
            chiTiet.setGiaTien(lichChieu.getGiaCoBan().multiply(ghe.getHeSoGia()));
            chiTiet.setHeSoGiaLucDat(ghe.getHeSoGia());
            chiTietDatGheRepository.save(chiTiet);
        }

        for (ChiTietDatSanPham chiTiet : chiTietComboList) {
            chiTiet.setDatVe(savedDatVe);
            chiTietDatSanPhamRepository.save(chiTiet);
        }

        // POS sale is paid immediately — deduct product inventory now
        deductStock(savedDatVe);

        ThanhToan tt = new ThanhToan();
        tt.setDatVe(savedDatVe);
        tt.setSoTien(tongTienThanhToan);
        tt.setPhuongThucThanhToan("Cash-POS");
        tt.setMaGiaoDich("POS-" + savedDatVe.getMaDatVe());
        tt.setTrangThai("success");
        tt.setThoiGianThanhToan(LocalDateTime.now());
        tt.setNguoiXuLy(nhanVien);
        thanhToanRepository.save(tt);

        if (khachHang != null) {
            long pointsEarned = 0;
            if (tongTienThanhToan.compareTo(MIN_ORDER_FOR_POINTS) >= 0) {
                pointsEarned = tongTienThanhToan.divideToIntegralValue(BigDecimal.valueOf(1000)).longValue();
            }
            khachHang.setDiemTichLuy((khachHang.getDiemTichLuy() == null ? 0 : khachHang.getDiemTichLuy()) + (int) pointsEarned);
            BigDecimal currentSpent = khachHang.getTongTienDaChi() == null ? BigDecimal.ZERO : khachHang.getTongTienDaChi();
            BigDecimal newSpent = currentSpent.add(tongTienThanhToan);
            khachHang.setTongTienDaChi(newSpent);
            double amount = newSpent.doubleValue();
            if (amount >= 30_000_000) khachHang.setCapDoThanhVien("Kim Cương");
            else if (amount >= 15_000_000) khachHang.setCapDoThanhVien("Vàng");
            else if (amount >= 5_000_000) khachHang.setCapDoThanhVien("Bạc");
            else khachHang.setCapDoThanhVien("Thường");
            if (diemThucDung > 0) {
                int remaining = khachHang.getDiemTichLuy() - diemThucDung;
                khachHang.setDiemTichLuy(Math.max(0, remaining));
            }
            nguoiDungRepository.save(khachHang);
        }

        if (khuyenMai != null && khuyenMai.getDaSuDung() != null) {
            khuyenMai.setDaSuDung(khuyenMai.getDaSuDung() + 1);
            khuyenMaiRepository.save(khuyenMai);
        }

        return savedDatVe;
    }

    // ─────────────────────────────────────────────────────────────
    // Inventory — deduct/restore product stock for paid bookings
    // ─────────────────────────────────────────────────────────────

    /**
     * Deducts SanPham.tonKho for every ChiTietDatSanPham of this booking.
     * Called when a booking becomes PAID (online payment confirmation via
     * ThanhToanService.markPaid, or immediately for POS counter sales).
     *
     * Throws IllegalArgumentException if any product lacks enough stock
     * (rejects the payment/sale rather than going negative).
     */
    @Transactional
    public void deductStock(DatVe datVe) {
        List<ChiTietDatSanPham> items = chiTietDatSanPhamRepository.findByDatVeId(datVe.getId());
        for (ChiTietDatSanPham ct : items) {
            SanPham sp = ct.getSanPham();
            int tonKho = sp.getTonKho() == null ? 0 : sp.getTonKho();
            int soLuong = ct.getSoLuong() == null ? 0 : ct.getSoLuong();
            if (tonKho < soLuong) {
                throw new IllegalArgumentException("Sản phẩm '" + sp.getTenSanPham()
                        + "' không đủ tồn kho (còn " + tonKho + ")");
            }
            sp.setTonKho(tonKho - soLuong);
            sanPhamRepository.save(sp);
        }
    }

    /**
     * Restores SanPham.tonKho for every ChiTietDatSanPham of this booking.
     * Called when a paid booking is cancelled or refunded.
     */
    @Transactional
    public void restoreStock(DatVe datVe) {
        List<ChiTietDatSanPham> items = chiTietDatSanPhamRepository.findByDatVeId(datVe.getId());
        for (ChiTietDatSanPham ct : items) {
            SanPham sp = ct.getSanPham();
            int tonKho = sp.getTonKho() == null ? 0 : sp.getTonKho();
            int soLuong = ct.getSoLuong() == null ? 0 : ct.getSoLuong();
            sp.setTonKho(tonKho + soLuong);
            sanPhamRepository.save(sp);
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Admin methods
    // ─────────────────────────────────────────────────────────────

    public java.util.List<DatVe> findAll() {
        return datVeRepository.findAll();
    }

    public java.util.Optional<DatVe> findById(Long id) {
        return datVeRepository.findById(id);
    }

    public org.springframework.data.domain.Page<DatVe> searchAdmin(String q, String trangThai, org.springframework.data.domain.Pageable pageable) {
        return datVeRepository.searchAdmin(q, trangThai, pageable);
    }

    public java.util.List<DatVe> findByLichChieuId(Long lichChieuId) {
        return datVeRepository.findByLichChieuId(lichChieuId);
    }

    public DatVe save(DatVe datVe) {
        return datVeRepository.save(datVe);
    }

    @Transactional
    public void refundBooking(Long id) {
        DatVe datVe = datVeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn đặt vé"));
        if (!"paid".equals(datVe.getTrangThaiThanhToan())) {
            throw new IllegalArgumentException("Chỉ có thể hoàn tiền cho đơn đã thanh toán (trạng thái 'paid')");
        }
        datVe.setTrangThai("cancelled");
        datVeRepository.save(datVe);

        List<ThanhToan> payments = thanhToanRepository.findByDatVeId(id);
        for (ThanhToan tt : payments) {
            tt.setTrangThai("refunded");
            tt.setNgayHoan(LocalDateTime.now());
            tt.setLyDoHoan("Admin hoàn tiền");
        }
        if (!payments.isEmpty()) thanhToanRepository.saveAll(payments);

        restoreStock(datVe);
    }

    // ─────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────

    private String generateBookingCode() {
        long timestamp = System.currentTimeMillis();
        int random = new Random().nextInt(1000);
        return String.format("BK%d%03d", timestamp % 1000000, random);
    }
}
