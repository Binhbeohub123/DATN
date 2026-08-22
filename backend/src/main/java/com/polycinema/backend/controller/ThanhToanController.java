package com.polycinema.backend.controller;

import com.polycinema.backend.service.AuthService;
import com.polycinema.backend.service.ThanhToanService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/thanh-toan")
@RequiredArgsConstructor
public class ThanhToanController {

    private final ThanhToanService thanhToanService;
    private final AuthService authService;

    @PostMapping("/vnpay")
    public ResponseEntity<?> createVNPay(@RequestBody Map<String, Object> req, HttpServletRequest httpRequest) {
        try {
            Long userId = authService.getUserIdFromToken();
            if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Vui lòng đăng nhập");
            Object datVeRaw = req.get("datVeId");
            if (datVeRaw == null) return ResponseEntity.badRequest().body("Thiếu trường datVeId");
            Long datVeId = ((Number) datVeRaw).longValue();
            String ipAddr = getClientIp(httpRequest);
            String frontendOrigin = req.get("frontendOrigin") != null ? String.valueOf(req.get("frontendOrigin")) : null;
            return ResponseEntity.ok(Map.of("paymentUrl", thanhToanService.createVNPayUrl(datVeId, ipAddr, frontendOrigin)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi tạo thanh toán VNPay: " + e.getMessage());
        }
    }

    @GetMapping("/vnpay/callback")
    public ResponseEntity<?> vnpayCallback(@RequestParam Map<String, String> params) {
        try {
            boolean success = thanhToanService.handleVNPayCallback(new HashMap<>(params));
            if (success) return ResponseEntity.ok(Map.of("success", true, "message", "Thanh toán thành công"));
            else return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Thanh toán thất bại"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PostMapping("/payos/create")
    public ResponseEntity<?> createPayOS(@RequestBody Map<String, Object> req) {
        try {
            Long userId = authService.getUserIdFromToken();
            if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Vui lòng đăng nhập");
            Object datVeRaw = req.get("datVeId");
            if (datVeRaw == null) return ResponseEntity.badRequest().body("Thiếu trường datVeId");
            Long datVeId = ((Number) datVeRaw).longValue();
            String frontendOrigin = req.get("frontendOrigin") != null ? String.valueOf(req.get("frontendOrigin")) : null;
            return ResponseEntity.ok(thanhToanService.createPayOSPayment(datVeId, frontendOrigin));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi tạo thanh toán PayOS: " + e.getMessage());
        }
    }

    @PostMapping("/payos/cancel")
    public ResponseEntity<?> payosCancel(@RequestBody Map<String, Object> body) {
        String maDatVe = body.get("maDatVe") != null ? String.valueOf(body.get("maDatVe")) : null;
        Long datVeId = parseLong(body.get("datVeId"));
        Long orderCode = parseLong(body.get("orderCode"));
        boolean hasMaDatVe = maDatVe != null && !maDatVe.isBlank() && !"null".equals(maDatVe);
        if (!hasMaDatVe && datVeId == null && orderCode == null) {
            return ResponseEntity.ok(Map.of("message", "no maDatVe provided"));
        }
        try {
            if (hasMaDatVe) thanhToanService.cancelByPayOSCancel(maDatVe);
            else if (orderCode != null) thanhToanService.cancelByPayOSOrderCode(orderCode);
            else thanhToanService.cancelByPayOSCancel(datVeId);
            return ResponseEntity.ok(Map.of("message", "cancelled"));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("message", "error: " + e.getMessage()));
        }
    }

    @PostMapping("/payos/confirm")
    public ResponseEntity<?> payosConfirm(@RequestBody Map<String, Object> body) {
        Long orderCode = parseLong(body.get("orderCode"));
        Long datVeId = parseLong(body.get("datVeId"));
        if (orderCode == null) return ResponseEntity.badRequest().body(Map.of("message", "missing orderCode"));
        try {
            return ResponseEntity.ok(thanhToanService.confirmPayOS(orderCode, datVeId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/zalopay/redirect")
    public ResponseEntity<?> zaloPayRedirect(@RequestBody Map<String, Object> body) {
        Long datVeId = parseLong(body.get("datVeId"));
        try {
            return ResponseEntity.ok(thanhToanService.handleZaloPayRedirect(body, datVeId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", e.getMessage()));
        }
    }

    private Long parseLong(Object value) {
        if (value == null) return null;
        try { return Long.valueOf(String.valueOf(value)); }
        catch (NumberFormatException ignored) { return null; }
    }

    @PostMapping("/payos/webhook")
    public ResponseEntity<?> payosWebhook(@RequestBody Object body) {
        try { thanhToanService.handlePayOSWebhook(body); }
        catch (Exception e) { System.err.println("[ThanhToanController] PayOS webhook error: " + e.getMessage()); }
        return ResponseEntity.ok(Map.of("message", "OK"));
    }

    @PostMapping("/zalopay/create")
    public ResponseEntity<?> createZaloPay(@RequestBody Map<String, Object> req) {
        try {
            Long userId = authService.getUserIdFromToken();
            if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Vui lòng đăng nhập");
            Object datVeRaw = req.get("datVeId");
            if (datVeRaw == null) return ResponseEntity.badRequest().body("Thiếu trường datVeId");
            Long datVeId = ((Number) datVeRaw).longValue();
            String frontendOrigin = req.get("frontendOrigin") != null ? String.valueOf(req.get("frontendOrigin")) : null;
            return ResponseEntity.ok(thanhToanService.createZaloPayOrder(datVeId, frontendOrigin));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi tạo thanh toán ZaloPay: " + e.getMessage());
        }
    }

    @PostMapping("/zalopay/callback")
    public ResponseEntity<?> zaloPayCallback(@RequestBody Map<String, Object> body) {
        boolean ok = thanhToanService.handleZaloPayCallback(body);
        Map<String, Object> result = new java.util.LinkedHashMap<>();
        result.put("return_code", ok ? 1 : -1);
        result.put("return_message", ok ? "success" : "failed");
        return ResponseEntity.ok(result);
    }

    @PostMapping("/momo/callback")
    public ResponseEntity<?> momoCallback(@RequestBody Map<String, Object> body) {
        try { thanhToanService.handleMomoCallback(body); }
        catch (Exception e) { System.err.println("[ThanhToanController] MoMo callback error: " + e.getMessage()); }
        return ResponseEntity.ok(Map.of("message", "OK"));
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) ip = request.getHeader("Proxy-Client-IP");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) ip = request.getRemoteAddr();
        if (ip != null && ip.contains(",")) ip = ip.split(",")[0].trim();
        return ip;
    }
}
