package com.polycinema.backend.service;

import com.polycinema.backend.entity.DatVe;
import com.polycinema.backend.entity.NguoiDung;
import com.polycinema.backend.entity.ThanhToan;
import com.polycinema.backend.repository.DatVeRepository;
import com.polycinema.backend.repository.NguoiDungRepository;
import com.polycinema.backend.repository.ThanhToanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ThanhToanService {

    private final DatVeRepository datVeRepository;
    private final ThanhToanRepository thanhToanRepository;
    private final NguoiDungRepository nguoiDungRepository;
    private final EmailService emailService;

    // ── VNPay config (inject from application.properties or use defaults for dev) ──
    @Value("${vnpay.tmn-code:POLYTEST}")
    private String vnpTmnCode;

    @Value("${vnpay.hash-secret:POLYCINEMA_SECRET_KEY_2026}")
    private String vnpHashSecret;

    @Value("${vnpay.url:https://sandbox.vnpayment.vn/paymentv2/vpcpay.html}")
    private String vnpUrl;

    @Value("${vnpay.return-url:http://localhost:5173/payment-result}")
    private String vnpReturnUrl;

    // ── Momo config ──
    @Value("${momo.partner-code:MOMOBKUN20180529}")
    private String momoPartnerCode;

    @Value("${momo.access-key:klm05TvNBzhg7h7j}")
    private String momoAccessKey;

    @Value("${momo.secret-key:at67qH6mk8w5Y1nAyMoTKhpBoJMNIvW2}")
    private String momoSecretKey;

    @Value("${momo.endpoint:https://test-payment.momo.vn/v2/gateway/api/create}")
    private String momoEndpoint;

    @Value("${momo.return-url:http://localhost:5173/payment-result}")
    private String momoReturnUrl;

    @Value("${momo.notify-url:http://localhost:8080/api/thanh-toan/momo/callback}")
    private String momoNotifyUrl;

    // ─────────────────────────────────────────────────────────────
    // VNPay: generate payment URL
    // ─────────────────────────────────────────────────────────────
    public String createVNPayUrl(Long datVeId, String ipAddr) {
        DatVe datVe = datVeRepository.findById(datVeId)
                .orElseThrow(() -> new IllegalArgumentException("Đơn đặt vé không tồn tại"));

        long amount = datVe.getTongTienThanhToan().multiply(BigDecimal.valueOf(100)).longValue();
        String txnRef = datVe.getMaDatVe();
        String orderInfo = "Thanh toan ve phim " + txnRef;

        Map<String, String> vnpParams = new TreeMap<>();
        vnpParams.put("vnp_Version", "2.1.0");
        vnpParams.put("vnp_Command", "pay");
        vnpParams.put("vnp_TmnCode", vnpTmnCode);
        vnpParams.put("vnp_Amount", String.valueOf(amount));
        vnpParams.put("vnp_CurrCode", "VND");
        vnpParams.put("vnp_TxnRef", txnRef);
        vnpParams.put("vnp_OrderInfo", orderInfo);
        vnpParams.put("vnp_OrderType", "other");
        vnpParams.put("vnp_Locale", "vn");
        vnpParams.put("vnp_ReturnUrl", vnpReturnUrl + "/" + datVeId);
        vnpParams.put("vnp_IpAddr", ipAddr != null ? ipAddr : "127.0.0.1");
        vnpParams.put("vnp_CreateDate", new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));

        // Build hash data
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, String> entry : vnpParams.entrySet()) {
            if (!first) {
                hashData.append('&');
                query.append('&');
            }
            hashData.append(entry.getKey()).append('=')
                    .append(URLEncoder.encode(entry.getValue(), StandardCharsets.US_ASCII));
            query.append(URLEncoder.encode(entry.getKey(), StandardCharsets.US_ASCII))
                    .append('=')
                    .append(URLEncoder.encode(entry.getValue(), StandardCharsets.US_ASCII));
            first = false;
        }

        String secureHash = hmacSHA512(vnpHashSecret, hashData.toString());
        return vnpUrl + "?" + query + "&vnp_SecureHash=" + secureHash;
    }

    // ─────────────────────────────────────────────────────────────
    // VNPay: handle callback
    // ─────────────────────────────────────────────────────────────
    @Transactional
    public boolean handleVNPayCallback(Map<String, String> params) {
        String secureHash = params.remove("vnp_SecureHash");
        params.remove("vnp_SecureHashType");

        Map<String, String> sorted = new TreeMap<>(params);
        StringBuilder hashData = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, String> entry : sorted.entrySet()) {
            if (!first) hashData.append('&');
            hashData.append(entry.getKey()).append('=')
                    .append(URLEncoder.encode(entry.getValue(), StandardCharsets.US_ASCII));
            first = false;
        }

        String computed = hmacSHA512(vnpHashSecret, hashData.toString());
        if (!computed.equalsIgnoreCase(secureHash)) return false;

        String responseCode = params.get("vnp_ResponseCode");
        String txnRef = params.get("vnp_TxnRef");
        String transactionNo = params.get("vnp_TransactionNo");

        DatVe datVe = datVeRepository.findByMaDatVe(txnRef).orElse(null);
        if (datVe == null) return false;

        if ("00".equals(responseCode)) {
            markPaid(datVe, "VNPay", transactionNo);
            return true;
        } else {
            datVe.setTrangThai("cancelled");
            datVeRepository.save(datVe);
            return false;
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Momo: generate payment URL/QR
    // ─────────────────────────────────────────────────────────────
    public Map<String, Object> createMomoPayment(Long datVeId) {
        DatVe datVe = datVeRepository.findById(datVeId)
                .orElseThrow(() -> new IllegalArgumentException("Đơn đặt vé không tồn tại"));

        String orderId = datVe.getMaDatVe() + "_" + System.currentTimeMillis();
        String requestId = UUID.randomUUID().toString();
        long amount = datVe.getTongTienThanhToan().longValue();
        String orderInfo = "Thanh toan ve phim " + datVe.getMaDatVe();
        String redirectUrl = momoReturnUrl + "/" + datVeId;
        String ipnUrl = momoNotifyUrl;
        String requestType = "payWithMethod";
        String extraData = "";

        String rawSignature = "accessKey=" + momoAccessKey
                + "&amount=" + amount
                + "&extraData=" + extraData
                + "&ipnUrl=" + ipnUrl
                + "&orderId=" + orderId
                + "&orderInfo=" + orderInfo
                + "&partnerCode=" + momoPartnerCode
                + "&redirectUrl=" + redirectUrl
                + "&requestId=" + requestId
                + "&requestType=" + requestType;

        String signature = hmacSHA256(momoSecretKey, rawSignature);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("partnerCode", momoPartnerCode);
        body.put("requestId", requestId);
        body.put("amount", amount);
        body.put("orderId", orderId);
        body.put("orderInfo", orderInfo);
        body.put("redirectUrl", redirectUrl);
        body.put("ipnUrl", ipnUrl);
        body.put("lang", "vi");
        body.put("requestType", requestType);
        body.put("autoCapture", true);
        body.put("extraData", extraData);
        body.put("signature", signature);

        // In production, POST this to momoEndpoint and return the payUrl from response.
        // For dev/sandbox, return a mock response.
        Map<String, Object> result = new HashMap<>();
        result.put("paymentUrl", "https://test-payment.momo.vn/v2/gateway/pay?orderId=" + orderId);
        result.put("orderId", orderId);
        result.put("requestId", requestId);
        result.put("datVeId", datVeId);
        return result;
    }

    // ─────────────────────────────────────────────────────────────
    // Mark booking as paid + award loyalty points
    // ─────────────────────────────────────────────────────────────
    @Transactional
    public void markPaid(DatVe datVe, String method, String transactionNo) {
        datVe.setTrangThai("confirmed");
        datVe.setTrangThaiThanhToan("paid");
        datVeRepository.save(datVe);

        // Record payment
        ThanhToan tt = new ThanhToan();
        tt.setDatVe(datVe);
        tt.setSoTien(datVe.getTongTienThanhToan());
        tt.setPhuongThucThanhToan(method);
        tt.setMaGiaoDich(transactionNo);
        tt.setTrangThai("success");
        tt.setThoiGianThanhToan(LocalDateTime.now());
        thanhToanRepository.save(tt);

        // Award loyalty points: 1 point per 1000 VND spent
        NguoiDung user = datVe.getNguoiDung();
        long pointsEarned = datVe.getTongTienThanhToan().divide(BigDecimal.valueOf(1000)).longValue();
        user.setDiemTichLuy((user.getDiemTichLuy() == null ? 0 : user.getDiemTichLuy()) + (int) pointsEarned);

        // Update total spent
        BigDecimal currentSpent = user.getTongTienDaChi() == null ? BigDecimal.ZERO : user.getTongTienDaChi();
        BigDecimal newSpent = currentSpent.add(datVe.getTongTienThanhToan());
        user.setTongTienDaChi(newSpent);

        // Auto-upgrade member level
        user.setCapDoThanhVien(calculateMemberLevel(newSpent));

        // Deduct loyalty points used
        if (datVe.getDiemSuDung() != null && datVe.getDiemSuDung() > 0) {
            int remaining = user.getDiemTichLuy() - datVe.getDiemSuDung();
            user.setDiemTichLuy(Math.max(0, remaining));
        }

        nguoiDungRepository.save(user);

        // ── RQ29: Send booking confirmation email ──────────────────
        try {
            emailService.sendBookingConfirmation(datVe);
        } catch (Exception e) {
            // Non-fatal — log but do not roll back the transaction
            System.err.println("[ThanhToanService] Booking confirmation email failed: " + e.getMessage());
        }
    }

    private String calculateMemberLevel(BigDecimal tongTienDaChi) {
        if (tongTienDaChi == null) return "Thường";
        double amount = tongTienDaChi.doubleValue();
        if (amount >= 30_000_000) return "Kim Cương";
        if (amount >= 15_000_000) return "Vàng";
        if (amount >= 5_000_000) return "Bạc";
        return "Thường";
    }

    // ─────────────────────────────────────────────────────────────
    // Crypto helpers
    // ─────────────────────────────────────────────────────────────
    private String hmacSHA512(String key, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA512");
            mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512"));
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("HMAC-SHA512 error", e);
        }
    }

    private String hmacSHA256(String key, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("HMAC-SHA256 error", e);
        }
    }
}
