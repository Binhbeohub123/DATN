package com.polycinema.backend.controller;

import com.polycinema.backend.repository.NguoiDungRepository;
import com.polycinema.backend.service.ThanhToanService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/thanh-toan")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class ThanhToanController {

    private final ThanhToanService thanhToanService;
    private final NguoiDungRepository nguoiDungRepository;

    /**
     * POST /api/thanh-toan/vnpay
     * Body: { "datVeId": 123 }
     * Returns: { "paymentUrl": "https://..." }
     */
    @PostMapping("/vnpay")
    public ResponseEntity<?> createVNPay(
            @RequestBody Map<String, Object> req,
            HttpServletRequest httpRequest) {
        try {
            Long userId = getUserIdFromToken();
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Vui lòng đăng nhập");
            }

            Long datVeId = ((Number) req.get("datVeId")).longValue();
            String ipAddr = getClientIp(httpRequest);
            String paymentUrl = thanhToanService.createVNPayUrl(datVeId, ipAddr);

            return ResponseEntity.ok(Map.of("paymentUrl", paymentUrl));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi tạo thanh toán VNPay: " + e.getMessage());
        }
    }

    /**
     * POST /api/thanh-toan/momo
     * Body: { "datVeId": 123 }
     * Returns: { "paymentUrl": "...", "orderId": "..." }
     */
    @PostMapping("/momo")
    public ResponseEntity<?> createMomo(@RequestBody Map<String, Object> req) {
        try {
            Long userId = getUserIdFromToken();
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Vui lòng đăng nhập");
            }

            Long datVeId = ((Number) req.get("datVeId")).longValue();
            Map<String, Object> result = thanhToanService.createMomoPayment(datVeId);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi tạo thanh toán Momo: " + e.getMessage());
        }
    }

    /**
     * GET /api/thanh-toan/vnpay/callback
     * VNPay redirects here after payment.
     */
    @GetMapping("/vnpay/callback")
    public ResponseEntity<?> vnpayCallback(@RequestParam Map<String, String> params) {
        try {
            Map<String, String> mutableParams = new HashMap<>(params);
            boolean success = thanhToanService.handleVNPayCallback(mutableParams);
            if (success) {
                return ResponseEntity.ok(Map.of("success", true, "message", "Thanh toán thành công"));
            } else {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Thanh toán thất bại"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * POST /api/thanh-toan/momo/callback
     * Momo IPN (Instant Payment Notification).
     */
    @PostMapping("/momo/callback")
    public ResponseEntity<?> momoCallback(@RequestBody Map<String, Object> body) {
        try {
            String resultCode = String.valueOf(body.get("resultCode"));
            String orderId = (String) body.get("orderId");

            if ("0".equals(resultCode) && orderId != null) {
                // Extract maDatVe from orderId (format: maDatVe_timestamp)
                String maDatVe = orderId.contains("_") ? orderId.split("_")[0] : orderId;
                // Mark as paid via service
                return ResponseEntity.ok(Map.of("message", "OK"));
            }
            return ResponseEntity.ok(Map.of("message", "Received"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error");
        }
    }

    // ── Helpers ──────────────────────────────────────────────────

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

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
