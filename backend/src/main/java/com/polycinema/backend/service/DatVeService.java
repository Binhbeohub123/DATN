package com.polycinema.backend.service;

import com.polycinema.backend.entity.*;
import com.polycinema.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
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
            Integer diemSuDung) {
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
                Object sanPhamRaw = combo.get("sanPhamId") != null ? combo.get("sanPhamId") : combo.get("id");
                Object soLuongRaw = combo.get("soLuong");
                if (sanPhamRaw == null) {
                    throw new IllegalArgumentException("Thiếu trường sanPhamId trong comboData");
                }
                if (soLuongRaw == null) {
                    throw new IllegalArgumentException("Thiếu trường soLuong trong comboData");
                }

                Long sanPhamId = ((Number) sanPhamRaw).longValue();
                Integer soLuong = ((Number) soLuongRaw).intValue();
                SanPham sanPham = sanPhamRepository.findById(sanPhamId)
                        .orElseThrow(() -> new IllegalArgumentException("Sản phẩm ID " + sanPhamId + " không tồn tại"));
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
        if (diemSuDung != null && diemSuDung > 0) {
            tienGiamTuDiem = BigDecimal.valueOf(diemSuDung * 1000L).min(tongTienGoc);
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
        datVe.setDiemSuDung(diemSuDung != null ? diemSuDung : 0);
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

        return savedDatVe;
    }

    /**
     * Tạo đơn đặt vé tại quầy (POS)
     */
    @Transactional
    public DatVe createCounterSale(
            Long nhanVienId,
            Long khachHangId,
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
        if (khachHangId != null) {
            khachHang = nguoiDungRepository.findById(khachHangId).orElse(null);
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
                Object sanPhamRaw = combo.get("sanPhamId") != null ? combo.get("sanPhamId") : combo.get("id");
                Object soLuongRaw = combo.get("soLuong");
                if (sanPhamRaw == null) {
                    throw new IllegalArgumentException("Thiếu trường sanPhamId trong comboData");
                }
                if (soLuongRaw == null) {
                    throw new IllegalArgumentException("Thiếu trường soLuong trong comboData");
                }

                Long sanPhamId = ((Number) sanPhamRaw).longValue();
                Integer soLuong = ((Number) soLuongRaw).intValue();
                SanPham sanPham = sanPhamRepository.findById(sanPhamId)
                        .orElseThrow(() -> new IllegalArgumentException("Sản phẩm ID " + sanPhamId + " không tồn tại"));
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
            if ("percent".equalsIgnoreCase(khuyenMai.getLoaiGiamGia())) {
                tienGiamKhuyenMai = tongTienGoc.multiply(khuyenMai.getGiaTriGiam()).divide(BigDecimal.valueOf(100));
                if (khuyenMai.getGiaTriGiamToiDa() != null) {
                    tienGiamKhuyenMai = tienGiamKhuyenMai.min(khuyenMai.getGiaTriGiamToiDa());
                }
            } else {
                tienGiamKhuyenMai = khuyenMai.getGiaTriGiam().min(tongTienGoc);
            }
        }

        BigDecimal tienGiamTuDiem = BigDecimal.ZERO;
        if (diemSuDung != null && diemSuDung > 0 && khachHang != null) {
            tienGiamTuDiem = BigDecimal.valueOf(diemSuDung * 1000L).min(tongTienGoc);
        }

        BigDecimal tongTienThanhToan = tongTienGoc.subtract(tienGiamKhuyenMai).subtract(tienGiamTuDiem)
                .max(BigDecimal.ZERO);

        DatVe datVe = new DatVe();
        datVe.setMaDatVe(generateBookingCode());
        datVe.setNguoiDung(khachHang);
        datVe.setNhanVien(nhanVien);
        datVe.setLichChieu(lichChieu);
        datVe.setKhuyenMai(khuyenMai);
        datVe.setTongTienGoc(tongTienGoc);
        datVe.setTienGiamKhuyenMai(tienGiamKhuyenMai);
        datVe.setDiemSuDung(diemSuDung != null ? diemSuDung : 0);
        datVe.setTienGiamTuDiem(tienGiamTuDiem);
        datVe.setTongTienThanhToan(tongTienThanhToan);
        datVe.setTrangThai("confirmed");
        datVe.setTrangThaiThanhToan("paid");
        datVe.setTrangThaiCheckIn("chưa sử dụng");
        String posMaQR = datVe.getMaDatVe() + "-" + System.currentTimeMillis();
        datVe.setMaQR(posMaQR); // POS QR code

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

        // Tạo ThanhToan tiền mặt
        ThanhToan tt = new ThanhToan();
        tt.setDatVe(savedDatVe);
        tt.setSoTien(tongTienThanhToan);
        tt.setPhuongThucThanhToan("Tiền mặt");
        tt.setMaGiaoDich("POS-" + savedDatVe.getMaDatVe());
        tt.setTrangThai("success");
        tt.setThoiGianThanhToan(LocalDateTime.now());
        tt.setNguoiXuLy(nhanVien);
        thanhToanRepository.save(tt);

        // Tích điểm nếu có khách hàng
        if (khachHang != null) {
            long pointsEarned = tongTienThanhToan.divide(BigDecimal.valueOf(1000)).longValue();
            khachHang.setDiemTichLuy(
                    (khachHang.getDiemTichLuy() == null ? 0 : khachHang.getDiemTichLuy()) + (int) pointsEarned);
            BigDecimal currentSpent = khachHang.getTongTienDaChi() == null ? BigDecimal.ZERO
                    : khachHang.getTongTienDaChi();
            BigDecimal newSpent = currentSpent.add(tongTienThanhToan);
            khachHang.setTongTienDaChi(newSpent);

            double amount = newSpent.doubleValue();
            if (amount >= 30_000_000)
                khachHang.setCapDoThanhVien("Kim Cương");
            else if (amount >= 15_000_000)
                khachHang.setCapDoThanhVien("Vàng");
            else if (amount >= 5_000_000)
                khachHang.setCapDoThanhVien("Bạc");
            else
                khachHang.setCapDoThanhVien("Thường");

            if (diemSuDung != null && diemSuDung > 0) {
                int remaining = khachHang.getDiemTichLuy() - diemSuDung;
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
     * TrangThai CHECK: 'pending' | 'confirmed' | 'cancelled'
     * TrangThaiThanhToan CHECK: 'unpaid' | 'paid'
     *
     * We ONLY set TrangThai = 'cancelled'.
     * We NEVER touch TrangThaiThanhToan — it stays 'unpaid' (payment never
     * happened).
     * Only markPaid() in ThanhToanService sets TrangThaiThanhToan = 'paid'.
     *
     * Also deletes ChiTietDatGhe rows so the seats show as available in the seat
     * map.
     * GheNgoi has no status field — availability is determined solely by whether a
     * ChiTietDatGhe record exists for that seat + showtime.
     */
    @Transactional
    public void performCancel(DatVe datVe) {
        // ── 1. Mark booking cancelled ─────────────────────────────
        datVe.setTrangThai("cancelled");
        // DO NOT call setTrangThaiThanhToan() — CHECK constraint only allows
        // 'unpaid'|'paid'
        datVeRepository.save(datVe);

        // ── 2. Release seat records and all seat locks ────────────
        // releaseSeatsByBooking handles:
        // - ChiTietDatGhe deletion (frees seats on seat map)
        // - SeatLock deletion by maDatVe
        // - SeatLock deletion by gheNgoiId + lichChieuId (orphaned locks)
        releaseSeatsByBooking(datVe);

        // ── 3. Decrement promo usage counter ──────────────────────
        if (datVe.getKhuyenMai() != null) {
            KhuyenMai km = datVe.getKhuyenMai();
            if (km.getDaSuDung() != null && km.getDaSuDung() > 0) {
                km.setDaSuDung(km.getDaSuDung() - 1);
                khuyenMaiRepository.save(km);
            }
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
     * 1. Read ChiTietDatGhe list first (needed for step 3)
     * 2. Delete ChiTietDatGhe rows (frees seats on the seat map)
     * 3. Delete SeatLock by maDatVe (named locks)
     * 4. Delete SeatLock by gheNgoiId + lichChieuId (orphaned / unnamed locks)
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
    // Helpers
    // ─────────────────────────────────────────────────────────────

    private String generateBookingCode() {
        long timestamp = System.currentTimeMillis();
        int random = new Random().nextInt(1000);
        return String.format("BK%d%03d", timestamp % 1000000, random);
    }
}
