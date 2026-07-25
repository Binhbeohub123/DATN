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

            Object datVeRaw = req.get("datVeId");
            if (datVeRaw == null) {
                return ResponseEntity.badRequest().body("Thiếu trường datVeId");
            }
            Long datVeId = ((Number) datVeRaw).longValue();
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
     * POST /api/thanh-toan/payos/create
     * Body: { "datVeId": 123 }
     * Returns: { "checkoutUrl": "https://pay.payos.vn/..." }
     */
    @PostMapping("/payos/create")
    public ResponseEntity<?> createPayOS(@RequestBody Map<String, Object> req) {
        try {
            Long userId = getUserIdFromToken();
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Vui lòng đăng nhập");
            }
            Object datVeRaw = req.get("datVeId");
            if (datVeRaw == null) {
                return ResponseEntity.badRequest().body("Thiếu trường datVeId");
            }
            Long datVeId = ((Number) datVeRaw).longValue();
            Map<String, Object> result = thanhToanService.createPayOSPayment(datVeId);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi tạo thanh toán PayOS: " + e.getMessage());
        }
    }

    /**
     * POST /api/thanh-toan/payos/cancel
     * Called from /payment-cancel frontend page when PayOS redirects user back.
     * PUBLIC — no auth required (user may not have a valid session after redirect).
     * Cancels the booking so seats are released and orderCode can be reused.
     */
    @PostMapping("/payos/cancel")
    public ResponseEntity<?> payosCancel(@RequestBody Map<String, Object> body) {
        String maDatVe = body.get("maDatVe") != null ? String.valueOf(body.get("maDatVe")) : null;
        if (maDatVe == null || maDatVe.isBlank() || "null".equals(maDatVe)) {
            return ResponseEntity.ok(Map.of("message", "no maDatVe provided"));
        }
        try {
            thanhToanService.cancelByPayOSCancel(maDatVe);
            return ResponseEntity.ok(Map.of("message", "cancelled"));
        } catch (Exception e) {
            // Always return 200 — the frontend redirect should succeed regardless
            return ResponseEntity.ok(Map.of("message", "error: " + e.getMessage()));
        }
    }

    /**
     * POST /api/thanh-toan/payos/webhook
     * PayOS IPN (Instant Payment Notification) — PUBLIC endpoint.
     * Parameter type is Object so the PayOS SDK can deserialize the body correctly.
     * PayOS requires HTTP 200 even on failure, otherwise it retries.
     */
    @PostMapping("/payos/webhook")
    public ResponseEntity<?> payosWebhook(@RequestBody Object body) {
        try {
            thanhToanService.handlePayOSWebhook(body);
        } catch (Exception e) {
            System.err.println("[ThanhToanController] PayOS webhook error: " + e.getMessage());
        }
        return ResponseEntity.ok(Map.of("message", "OK"));
    }

    /**
     * POST /api/thanh-toan/zalopay/create
     * Body: { "datVeId": 123 }
     * Returns: { "orderUrl": "https://...", "appTransId": "...", "datVeId": ... }
     */
    @PostMapping("/zalopay/create")
    public ResponseEntity<?> createZaloPay(@RequestBody Map<String, Object> req) {
        try {
            Long userId = getUserIdFromToken();
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Vui lòng đăng nhập");
            }
            Object datVeRaw = req.get("datVeId");
            if (datVeRaw == null) {
                return ResponseEntity.badRequest().body("Thiếu trường datVeId");
            }
            Long datVeId = ((Number) datVeRaw).longValue();
            Map<String, Object> result = thanhToanService.createZaloPayOrder(datVeId);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi tạo thanh toán ZaloPay: " + e.getMessage());
        }
    }

    /**
     * POST /api/thanh-toan/zalopay/callback
     * ZaloPay IPN — PUBLIC endpoint.
     * ZaloPay requires a JSON response: { return_code: 1, return_message: "success" }
     */
    @PostMapping("/zalopay/callback")
    public ResponseEntity<?> zaloPayCallback(@RequestBody Map<String, Object> body) {
        boolean ok = thanhToanService.handleZaloPayCallback(body);
        Map<String, Object> result = new java.util.LinkedHashMap<>();
        result.put("return_code",    ok ? 1 : -1);
        result.put("return_message", ok ? "success" : "failed");
        return ResponseEntity.ok(result); // always HTTP 200
    }

    /**
     * POST /api/thanh-toan/momo/callback
     * MoMo IPN — PUBLIC, always returns HTTP 200.
     */
    @PostMapping("/momo/callback")
    public ResponseEntity<?> momoCallback(@RequestBody Map<String, Object> body) {
        try {
            thanhToanService.handleMomoCallback(body);
        } catch (Exception e) {
            // Log but always return 200 — MoMo retries on non-200
            System.err.println("[ThanhToanController] MoMo callback error: " + e.getMessage());
        }
        // MoMo spec: always respond HTTP 200 with { message: "OK" }
        return ResponseEntity.ok(Map.of("message", "OK"));
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
