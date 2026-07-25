package com.polycinema.backend.controller;

import com.polycinema.backend.entity.DatVe;
import com.polycinema.backend.entity.SeatLock;
import com.polycinema.backend.repository.NguoiDungRepository;
import com.polycinema.backend.service.DatVeService;
import com.polycinema.backend.service.SeatLockService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dat-ve")
@RequiredArgsConstructor
public class DatVeController {

    private final DatVeService datVeService;
    private final NguoiDungRepository nguoiDungRepository;
    private final SeatLockService seatLockService;

    /**
     * POST /api/dat-ve
     * Tạo đơn đặt vé (cần auth)
     * Body: {
     *   "lichChieuId": 1,
     *   "gheIds": [1, 2, 3],
     *   "comboData": [
     *     {"id": 1, "soLuong": 2},
     *     {"id": 2, "soLuong": 1}
     *   ],
     *   "maKhuyenMai": "KHUYEN10" (optional),
     *   "diemSuDung": 100 (optional)
     * }
     */
    @PostMapping
    public ResponseEntity<?> createBooking(@RequestBody Map<String, Object> req) {
        try {
            // Lấy userId từ JWT token
            Long userId = getUserIdFromToken();
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Vui lòng đăng nhập");
            }

            Object lichChieuRaw = req.get("lichChieuId");
            if (lichChieuRaw == null) {
                return ResponseEntity.badRequest().body("Thiếu trường lichChieuId");
            }
            Long lichChieuId = ((Number) lichChieuRaw).longValue();

            // Safe conversion: Jackson deserialises small JSON integers as Integer, not Long
            @SuppressWarnings("unchecked")
            List<Long> gheIds = req.get("gheIds") == null ? java.util.Collections.emptyList()
                    : ((List<?>) req.get("gheIds")).stream()
                        .map(n -> {
                            if (n == null) {
                                throw new IllegalArgumentException("Danh sách gheIds có phần tử null");
                            }
                            return ((Number) n).longValue();
                        })
                        .collect(java.util.stream.Collectors.toList());

            List<Map<String, Object>> comboData = (List<Map<String, Object>>) req.get("comboData");
            String maKhuyenMai = (String) req.get("maKhuyenMai");
            Integer diemSuDung = req.get("diemSuDung") != null
                    ? ((Number) req.get("diemSuDung")).intValue()
                    : null;

            DatVe datVe = datVeService.createBooking(
                    userId,
                    lichChieuId,
                    gheIds,
                    comboData,
                    maKhuyenMai,
                    diemSuDung
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(datVe);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi tạo đơn đặt vé: " + e.getMessage());
        }
    }

    /**
     * GET /api/dat-ve
     * Lấy lịch sử đặt vé của user hiện tại
     */
    @GetMapping
    public ResponseEntity<?> getBookingHistory() {
        try {
            Long userId = getUserIdFromToken();
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Vui lòng đăng nhập");
            }

            List<DatVe> bookings = datVeService.getBookingHistory(userId);
            return ResponseEntity.ok(bookings);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi lấy lịch sử: " + e.getMessage());
        }
    }

    /**
     * GET /api/dat-ve/{id}
     * Lấy chi tiết một đơn đặt vé
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getBookingDetail(@PathVariable Long id) {
        try {
            Long userId = getUserIdFromToken();
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Vui lòng đăng nhập");
            }

            DatVe datVe = datVeService.getBookingDetail(id);
            if (datVe == null) {
                return ResponseEntity.notFound().build();
            }

            // Kiểm tra quyền: chỉ owner hoặc admin mới được xem
            if (!datVe.getNguoiDung().getId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Bạn không có quyền xem đơn này");
            }

            return ResponseEntity.ok(datVe);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi lấy chi tiết: " + e.getMessage());
        }
    }

    /**
     * PUT /api/dat-ve/{id}/cancel
     * Hủy một đơn đặt vé
     */
    @PutMapping("/{id}/cancel")
    public ResponseEntity<?> cancelBooking(@PathVariable Long id) {
        try {
            Long userId = getUserIdFromToken();
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Vui lòng đăng nhập");
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

    /**
     * POST /api/dat-ve/lock-seat
     * Body: { "gheNgoiId": 1, "lichChieuId": 5 }
     * Locks the seat for the current user for SEAT_LOCK_MINUTES minutes.
     */
    @PostMapping("/lock-seat")
    public ResponseEntity<?> lockSeat(@RequestBody Map<String, Object> req) {
        try {
            Long userId = getUserIdFromToken();
            if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Vui lòng đăng nhập");
            Object gheNgoiRaw = req.get("gheNgoiId");
            Object lichChieuRaw = req.get("lichChieuId");
            if (gheNgoiRaw == null) return ResponseEntity.badRequest().body("Thiếu trường gheNgoiId");
            if (lichChieuRaw == null) return ResponseEntity.badRequest().body("Thiếu trường lichChieuId");
            Long gheNgoiId  = ((Number) gheNgoiRaw).longValue();
            Long lichChieuId = ((Number) lichChieuRaw).longValue();
            SeatLock lock = seatLockService.lockSeat(gheNgoiId, lichChieuId, userId);
            int lockMins  = seatLockService.getSeatLockDuration();
            return ResponseEntity.ok(Map.of(
                    "expiresAt",          lock.getExpiresAt().toString(),
                    "lockDurationMinutes", lockMins));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khóa ghế: " + e.getMessage());
        }
    }

    /**
     * DELETE /api/dat-ve/release-seat
     * Body: { "gheNgoiId": 1, "lichChieuId": 5 }
     * Releases a seat lock held by the current user.
     */
    @DeleteMapping("/release-seat")
    public ResponseEntity<?> releaseSeat(@RequestBody Map<String, Object> req) {
        try {
            Long userId = getUserIdFromToken();
            if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Vui lòng đăng nhập");
            Object gheNgoiRaw = req.get("gheNgoiId");
            Object lichChieuRaw = req.get("lichChieuId");
            if (gheNgoiRaw == null) return ResponseEntity.badRequest().body("Thiếu trường gheNgoiId");
            if (lichChieuRaw == null) return ResponseEntity.badRequest().body("Thiếu trường lichChieuId");
            Long gheNgoiId  = ((Number) gheNgoiRaw).longValue();
            Long lichChieuId = ((Number) lichChieuRaw).longValue();
            seatLockService.releaseSeat(gheNgoiId, lichChieuId);
            return ResponseEntity.ok(Map.of("message", "Đã giải phóng ghế"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi giải phóng ghế: " + e.getMessage());
        }
    }

    /**
     * Helper: Lấy userId từ JWT token
     * Principal trong JWT là email — resolve sang userId qua DB
     */
    private Long getUserIdFromToken() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated()) {
                Object principal = auth.getPrincipal();
                String email = principal instanceof String ? (String) principal : null;
                if (email != null && !email.equals("anonymousUser")) {
                    return nguoiDungRepository.findByEmail(email)
                            .map(u -> u.getId())
                            .orElse(null);
                }
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }
}
