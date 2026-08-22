package com.polycinema.backend.controller;

import com.polycinema.backend.entity.DatVe;
import com.polycinema.backend.entity.SeatLock;
import com.polycinema.backend.service.AuthService;
import com.polycinema.backend.service.DatVeService;
import com.polycinema.backend.service.SeatLockService;
import com.polycinema.backend.service.ThanhToanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dat-ve")
@RequiredArgsConstructor
public class DatVeController {

    private final DatVeService datVeService;
    private final AuthService authService;
    private final SeatLockService seatLockService;
    private final ThanhToanService thanhToanService;

    @PostMapping
    public ResponseEntity<?> createBooking(@RequestBody Map<String, Object> req) {
        try {
            Long userId = authService.getUserIdFromToken();
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Vui lòng đăng nhập");
            }

            Object lichChieuRaw = req.get("lichChieuId");
            if (lichChieuRaw == null) {
                return ResponseEntity.badRequest().body("Thiếu trường lichChieuId");
            }
            Long lichChieuId = ((Number) lichChieuRaw).longValue();

            @SuppressWarnings("unchecked")
            List<Long> gheIds = req.get("gheIds") == null ? java.util.Collections.emptyList()
                    : ((List<?>) req.get("gheIds")).stream()
                        .map(n -> {
                            if (n == null) throw new IllegalArgumentException("Danh sách gheIds có phần tử null");
                            return ((Number) n).longValue();
                        })
                        .collect(java.util.stream.Collectors.toList());

            List<Map<String, Object>> comboData = (List<Map<String, Object>>) req.get("comboData");
            String maKhuyenMai = (String) req.get("maKhuyenMai");
            Integer diemSuDung = req.get("diemSuDung") != null
                    ? ((Number) req.get("diemSuDung")).intValue() : null;

            DatVe datVe = datVeService.createBooking(userId, lichChieuId, gheIds, comboData, maKhuyenMai, diemSuDung);

            if (datVe.getTongTienThanhToan() != null
                    && datVe.getTongTienThanhToan().compareTo(java.math.BigDecimal.ZERO) == 0) {
                thanhToanService.markPaid(datVe, "Cash", null);
                datVe = datVeService.getBookingDetail(datVe.getId());
            }

            return ResponseEntity.status(HttpStatus.CREATED).body(datVe);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi tạo đơn đặt vé: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> getBookingHistory() {
        try {
            Long userId = authService.getUserIdFromToken();
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Vui lòng đăng nhập");
            }
            return ResponseEntity.ok(datVeService.getBookingHistory(userId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi lấy lịch sử: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getBookingDetail(@PathVariable Long id) {
        try {
            Long userId = authService.getUserIdFromToken();
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Vui lòng đăng nhập");
            }
            DatVe datVe = datVeService.getBookingDetail(id);
            if (datVe == null) return ResponseEntity.notFound().build();
            if (!datVe.getNguoiDung().getId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Bạn không có quyền xem đơn này");
            }
            return ResponseEntity.ok(datVe);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi lấy chi tiết: " + e.getMessage());
        }
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<?> cancelBooking(@PathVariable Long id) {
        try {
            Long userId = authService.getUserIdFromToken();
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Vui lòng đăng nhập");
            }
            datVeService.cancelBooking(id, userId);
            return ResponseEntity.ok("Đơn đặt vé đã bị hủy");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi hủy đơn: " + e.getMessage());
        }
    }

    @PostMapping("/lock-seat")
    public ResponseEntity<?> lockSeat(@RequestBody Map<String, Object> req) {
        try {
            Long userId = authService.getUserIdFromToken();
            if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Vui lòng đăng nhập");
            Object gheNgoiRaw   = req.get("gheNgoiId");
            Object lichChieuRaw = req.get("lichChieuId");
            if (gheNgoiRaw == null) return ResponseEntity.badRequest().body("Thiếu trường gheNgoiId");
            if (lichChieuRaw == null) return ResponseEntity.badRequest().body("Thiếu trường lichChieuId");
            Long gheNgoiId   = ((Number) gheNgoiRaw).longValue();
            Long lichChieuId = ((Number) lichChieuRaw).longValue();
            SeatLock lock = seatLockService.lockSeat(gheNgoiId, lichChieuId, userId);
            int lockMins  = seatLockService.getSeatLockDuration();
            return ResponseEntity.ok(Map.of("expiresAt", lock.getExpiresAt().toString(), "lockDurationMinutes", lockMins));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khóa ghế: " + e.getMessage());
        }
    }

    @DeleteMapping("/release-seat")
    public ResponseEntity<?> releaseSeat(@RequestBody Map<String, Object> req) {
        try {
            Long userId = authService.getUserIdFromToken();
            if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Vui lòng đăng nhập");
            Object gheNgoiRaw   = req.get("gheNgoiId");
            Object lichChieuRaw = req.get("lichChieuId");
            if (gheNgoiRaw == null) return ResponseEntity.badRequest().body("Thiếu trường gheNgoiId");
            if (lichChieuRaw == null) return ResponseEntity.badRequest().body("Thiếu trường lichChieuId");
            Long gheNgoiId   = ((Number) gheNgoiRaw).longValue();
            Long lichChieuId = ((Number) lichChieuRaw).longValue();
            seatLockService.releaseSeat(gheNgoiId, lichChieuId);
            return ResponseEntity.ok(Map.of("message", "Đã giải phóng ghế"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi giải phóng ghế: " + e.getMessage());
        }
    }
}
