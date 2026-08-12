package com.polycinema.backend.service;

import com.polycinema.backend.entity.DatVe;
import com.polycinema.backend.entity.NguoiDung;
import com.polycinema.backend.entity.ThanhToan;
import com.polycinema.backend.repository.DatVeRepository;
import com.polycinema.backend.repository.NguoiDungRepository;
import com.polycinema.backend.repository.ThanhToanRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import vn.payos.PayOS;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkRequest;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;
import vn.payos.model.v2.paymentRequests.PaymentLinkItem;
import vn.payos.model.webhooks.WebhookData;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ThanhToanService {

    private final DatVeRepository datVeRepository;
    private final ThanhToanRepository thanhToanRepository;
    private final NguoiDungRepository nguoiDungRepository;
    private final EmailService emailService;
    // PayOS SDK bean — injected from PayOSConfig
    private final PayOS payOS;
    // RestTemplate — injected from WebConfig for ZaloPay HTTP calls
    private final RestTemplate restTemplate;
    // DatVeService — for reusing cancel logic
    private final DatVeService datVeService;

    // ── VNPay config ──────────────────────────────────────────────
    @Value("${vnpay.tmn-code:POLYTEST}")
    private String vnpTmnCode;

    @Value("${vnpay.hash-secret:POLYCINEMA_SECRET_KEY_2026}")
    private String vnpHashSecret;

    @Value("${vnpay.url:https://sandbox.vnpayment.vn/paymentv2/vpcpay.html}")
    private String vnpUrl;

    @Value("${vnpay.return-url:http://localhost:5173/payment-result}")
    private String vnpReturnUrl;

    // ── MoMo config (kept for legacy IPN handling) ────────────────
    @Value("${momo.partner-code:MOMOBKUN20180529}")
    private String momoPartnerCode;

    @Value("${momo.access-key:klm05TvNBzhg7h7j}")
    private String momoAccessKey;

    @Value("${momo.secret-key:at67qH6mk8w5Y1nAyMoTKhpBoJMNIvW2}")
    private String momoSecretKey;

    @Value("${momo.return-url:http://localhost:5173/payment-result}")
    private String momoReturnUrl;

    @Value("${momo.notify-url:http://localhost:8080/api/thanh-toan/momo/callback}")
    private String momoNotifyUrl;

    // ── PayOS URL config (credentials handled by PayOS bean) ──────
    @Value("${payos.return-url:http://localhost:5173/payment-result}")
    private String payosReturnUrl;

    @Value("${payos.cancel-url:http://localhost:5173/checkout}")
    private String payosCancelUrl;

    // ── ZaloPay config ────────────────────────────────────────────
    @Value("${zalopay.app-id:2553}")
    private String zaloAppId;

    @Value("${zalopay.key1:PcY4iZIKFCIdgZvA6ueMcMHHUbRLYjPL}")
    private String zaloKey1;

    @Value("${zalopay.key2:eG4r0GcoNtRGbO8}")
    private String zaloKey2;

    @Value("${zalopay.create-url:https://sb-openapi.zalopay.vn/v2/create}")
    private String zaloCreateUrl;

    @Value("${zalopay.query-url:https://sb-openapi.zalopay.vn/v2/query}")
    private String zaloQueryUrl;

    @Value("${zalopay.callback-url:http://localhost:8080/api/thanh-toan/zalopay/callback}")
    private String zaloCallbackUrl;

    @Value("${zalopay.redirect-url:http://localhost:5173/payment-result}")
    private String zaloRedirectUrl;

    // ─────────────────────────────────────────────────────────────
    // VNPay: generate payment URL
    // ─────────────────────────────────────────────────────────────
    public String createVNPayUrl(Long datVeId, String ipAddr, String frontendOrigin) {
        DatVe datVe = datVeRepository.findById(datVeId)
                .orElseThrow(() -> new IllegalArgumentException("Đơn đặt vé không tồn tại"));

        long amount = datVe.getTongTienThanhToan().multiply(BigDecimal.valueOf(100)).longValue();
        String txnRef   = datVe.getMaDatVe();
        String orderInfo = "Thanh toan ve phim " + txnRef;

        Map<String, String> vnpParams = new TreeMap<>();
        vnpParams.put("vnp_Version",   "2.1.0");
        vnpParams.put("vnp_Command",   "pay");
        vnpParams.put("vnp_TmnCode",   vnpTmnCode);
        vnpParams.put("vnp_Amount",    String.valueOf(amount));
        vnpParams.put("vnp_CurrCode",  "VND");
        vnpParams.put("vnp_TxnRef",    txnRef);
        vnpParams.put("vnp_OrderInfo", orderInfo);
        vnpParams.put("vnp_OrderType", "other");
        vnpParams.put("vnp_Locale",    "vn");
        vnpParams.put("vnp_ReturnUrl", resolveFrontendUrl(frontendOrigin, vnpReturnUrl) + "/" + datVeId);
        vnpParams.put("vnp_IpAddr",    ipAddr != null ? ipAddr : "127.0.0.1");
        vnpParams.put("vnp_CreateDate", new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));

        StringBuilder hashData = new StringBuilder();
        StringBuilder query    = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, String> entry : vnpParams.entrySet()) {
            if (!first) { hashData.append('&'); query.append('&'); }
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

        String responseCode  = params.get("vnp_ResponseCode");
        String txnRef        = params.get("vnp_TxnRef");
        String transactionNo = params.get("vnp_TransactionNo");

        DatVe datVe = datVeRepository.findByMaDatVe(txnRef).orElse(null);
        if (datVe == null) return false;

        if ("00".equals(responseCode)) {
            markPaid(datVe, "VNPay", transactionNo);
            return true;
        } else {
            datVeService.performCancel(datVe);
            return false;
        }
    }

    // ─────────────────────────────────────────────────────────────
    // PayOS: create payment link (via official SDK)
    // ─────────────────────────────────────────────────────────────
    /**
     * Creates a PayOS payment link using the official vn.payos SDK.
     * The SDK handles HMAC-SHA256 signature generation internally.
     *
     * Handles the duplicate-orderCode case: if a PENDING link already exists
     * for this booking (e.g. user retrying payment), the existing checkoutUrl
     * is returned instead of creating a duplicate that PayOS would reject.
     *
     * @return map containing { checkoutUrl, orderCode, datVeId }
     */
    public Map<String, Object> createPayOSPayment(Long datVeId, String frontendOrigin) {
        DatVe datVe = datVeRepository.findById(datVeId)
                .orElseThrow(() -> new IllegalArgumentException("Đơn đặt vé không tồn tại"));

        long   orderCode  = Math.abs(datVe.getId() * 1_000_000L + (System.currentTimeMillis() % 1_000_000L));
        long   amount     = datVe.getTongTienThanhToan().longValue();

        // PayOS spec: description max 25 characters
        String rawDesc    = "PolyCinema " + datVe.getMaDatVe();
        String description = rawDesc.length() > 25 ? rawDesc.substring(0, 25) : rawDesc;

        // returnUrl uses the numeric booking id — GET /api/dat-ve/{id} expects a Long,
        // not the maDatVe string.
        String returnUrl  = resolveFrontendUrl(frontendOrigin, payosReturnUrl) + "/" + datVe.getId();
        // cancelUrl must be path-based (NOT a query param): PayOS appends its own
        // query params when redirecting, which wipes ?maDatVe= and breaks cancellation.
        String cancelUrl  = resolveFrontendUrl(frontendOrigin, payosCancelUrl) + "/" + datVe.getMaDatVe();

        log.debug("[PayOS] Creating payment link: orderCode={}, amount={}, description='{}'",
                orderCode, amount, description);

        try {
            CreatePaymentLinkRequest paymentData = CreatePaymentLinkRequest.builder()
                    .orderCode(orderCode)
                    .amount(amount)
                    .description(description)
                    .returnUrl(returnUrl)
                    .cancelUrl(cancelUrl)
                    .item(PaymentLinkItem.builder()
                            .name("Vé xem phim")
                            .quantity(1)
                            .price(amount)
                            .build())
                    .build();

            CreatePaymentLinkResponse response = payOS.paymentRequests().create(paymentData);

            log.info("[PayOS] Payment link created: checkoutUrl={}", response.getCheckoutUrl());

            // Store the unique orderCode so the webhook can map it back to this booking
            datVe.setMaQR(String.valueOf(orderCode));
            datVeRepository.save(datVe);

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("checkoutUrl", response.getCheckoutUrl());
            result.put("orderCode",   orderCode);
            result.put("datVeId",     datVeId);
            return result;

        } catch (Exception e) {
            // ── Handle duplicate orderCode ────────────────────────────
            // PayOS returns an error (code != "00") when a payment link for
            // this orderCode already exists. Catch it, fetch the existing link,
            // and return its checkoutUrl so the user can complete the payment.
            log.warn("[PayOS] Create failed (possibly duplicate): {}. Checking for existing link...",
                    e.getMessage());
            try {
                vn.payos.model.v2.paymentRequests.PaymentLink existing =
                        payOS.paymentRequests().get(orderCode);

                vn.payos.model.v2.paymentRequests.PaymentLinkStatus status = existing.getStatus();

                if (status == vn.payos.model.v2.paymentRequests.PaymentLinkStatus.PENDING
                 || status == vn.payos.model.v2.paymentRequests.PaymentLinkStatus.PROCESSING) {
                    // Reconstruct the hosted checkout URL from the payment link ID.
                    // PayOS hosted page URL pattern: https://pay.payos.vn/web/{paymentLinkId}
                    String checkoutUrl = "https://pay.payos.vn/web/" + existing.getId();
                    log.info("[PayOS] Reusing existing PENDING link: id={}, checkoutUrl={}",
                            existing.getId(), checkoutUrl);

                    Map<String, Object> result = new LinkedHashMap<>();
                    result.put("checkoutUrl", checkoutUrl);
                    result.put("orderCode",   orderCode);
                    result.put("datVeId",     datVeId);
                    return result;
                }

                // Link is PAID, CANCELLED, EXPIRED, etc. — cannot reuse.
                log.warn("[PayOS] Existing link is not reusable (status={}). Original error: {}",
                        status, e.getMessage());
                throw new RuntimeException(
                        "Không tạo được link PayOS (trạng thái hiện tại: " + status + "): "
                        + e.getMessage(), e);

            } catch (RuntimeException re) {
                // Re-throw RuntimeExceptions (including our own above)
                throw re;
            } catch (Exception fetchEx) {
                log.error("[PayOS] Failed to fetch existing link for orderCode={}: {}",
                        orderCode, fetchEx.getMessage());
                throw new RuntimeException(
                        "Lỗi tạo thanh toán PayOS: " + e.getMessage(), e);
            }
        }
    }

    // ─────────────────────────────────────────────────────────────
    // PayOS: handle webhook (via official SDK)
    // ─────────────────────────────────────────────────────────────
    /**
     * Verifies a PayOS webhook notification using the SDK's built-in
     * signature verification, then marks the booking as paid or cancelled.
     *
     * The controller must always return HTTP 200 regardless of outcome
     * — PayOS retries on non-200.
     *
     * @return true if processed successfully
     */
    @Transactional
    public boolean handlePayOSWebhook(Object body) {
        try {
            // SDK verifies HMAC-SHA256 signature automatically
            WebhookData data = payOS.webhooks().verify(body);

            DatVe datVe = datVeRepository.findByMaQR(String.valueOf(data.getOrderCode()))
                    .orElseGet(() -> datVeRepository.findById(data.getOrderCode()).orElse(null));
            if (datVe == null) {
                log.error("[PayOS] Webhook: DatVe not found orderCode={}", data.getOrderCode());
                return false;
            }

            // Idempotency guard — skip if already paid
            if ("paid".equals(datVe.getTrangThaiThanhToan())) return true;

            if ("00".equals(data.getCode())) {
                markPaid(datVe, "PayOS", data.getPaymentLinkId());
                log.info("[PayOS] Payment confirmed for booking: {}", datVe.getMaDatVe());
            } else {
                // Payment failed / user cancelled — fully cancel the booking so
                // seats are released and product inventory is restored (if paid).
                datVeService.performCancel(datVe);
                log.warn("[PayOS] Payment failed. orderCode={} code={}",
                        data.getOrderCode(), data.getCode());
            }
            return true;

        } catch (Exception e) {
            log.error("[PayOS] Webhook verify failed: {}", e.getMessage());
            return false;
        }
    }

    // ─────────────────────────────────────────────────────────────
    // ZaloPay: create order
    // ─────────────────────────────────────────────────────────────
    /**
     * Creates a ZaloPay sandbox order and returns the hosted order_url.
     * Uses HMAC-SHA256 with key1 for the request signature.
     * Stores appTransId in DatVe.maQR for callback matching.
     *
     * @return map containing { orderUrl, appTransId, datVeId }
     */
    public Map<String, Object> createZaloPayOrder(Long datVeId, String frontendOrigin) {
        DatVe datVe = datVeRepository.findById(datVeId)
                .orElseThrow(() -> new IllegalArgumentException("Đơn đặt vé không tồn tại"));

        long   amount     = datVe.getTongTienThanhToan().longValue();
        long   appTime    = System.currentTimeMillis();
        // app_trans_id must be unique per merchant+day — include a millisecond
        // suffix so retrying payment for the same booking creates a fresh order
        // instead of ZaloPay rejecting the duplicate ("Giao dịch thất bại").
        String appTransId = new java.text.SimpleDateFormat("yyMMdd").format(new java.util.Date())
                + "_" + datVe.getMaDatVe() + "_" + (appTime % 1_000_000_000L);
        String appUser    = datVe.getNguoiDung() != null ? datVe.getNguoiDung().getEmail() : "guest";

        String embedData  = "{\"redirecturl\":\"" + resolveFrontendUrl(frontendOrigin, zaloRedirectUrl) + "/" + datVe.getId() + "\"}";
        String items      = "[{\"itemid\":\"ve\",\"itemname\":\"Ve xem phim\","
                + "\"itemprice\":" + amount + ",\"itemquantity\":1}]";
        String description = "PolyCinema - Thanh toan don hang #" + datVe.getMaDatVe();

        // Signature: HMAC-SHA256(key1, app_id|app_trans_id|app_user|amount|app_time|embed_data|item)
        String hmacInput = zaloAppId + "|" + appTransId + "|" + appUser
                + "|" + amount + "|" + appTime + "|" + embedData + "|" + items;
        String mac = hmacSHA256(zaloKey1, hmacInput);

        // Build form-urlencoded body
        Map<String, String> params = new LinkedHashMap<>();
        params.put("app_id",       zaloAppId);
        params.put("app_user",     appUser);
        params.put("app_time",     String.valueOf(appTime));
        params.put("amount",       String.valueOf(amount));
        params.put("app_trans_id", appTransId);
        params.put("embed_data",   embedData);
        params.put("item",         items);
        params.put("description",  description);
        params.put("callback_url", zaloCallbackUrl);
        params.put("mac",          mac);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            StringBuilder formBody = new StringBuilder();
            for (Map.Entry<String, String> entry : params.entrySet()) {
                if (formBody.length() > 0) formBody.append('&');
                formBody.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8))
                        .append('=')
                        .append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
            }

            HttpEntity<String> request = new HttpEntity<>(formBody.toString(), headers);
            @SuppressWarnings("unchecked")
            ResponseEntity<Map> response = restTemplate.exchange(
                    zaloCreateUrl, HttpMethod.POST, request, Map.class);

            @SuppressWarnings("unchecked")
            Map<String, Object> body = response.getBody();
            if (body == null) throw new RuntimeException("ZaloPay trả về rỗng");

            int returnCode = ((Number) body.get("return_code")).intValue();
            if (returnCode != 1) {
                throw new RuntimeException("ZaloPay lỗi: " + body.get("return_message"));
            }

            // Save appTransId in maQR for callback lookup
            datVe.setMaQR(appTransId);
            datVeRepository.save(datVe);

            log.info("[ZaloPay] Order created: appTransId={}", appTransId);

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("orderUrl",   body.get("order_url"));
            result.put("appTransId", appTransId);
            result.put("datVeId",    datVeId);
            return result;

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Lỗi kết nối ZaloPay: " + e.getMessage(), e);
        }
    }

    // ─────────────────────────────────────────────────────────────
    // ZaloPay: handle IPN callback
    // ─────────────────────────────────────────────────────────────
    /**
     * Verifies the ZaloPay callback MAC using key2, then marks the booking paid.
     * ZaloPay sends: { data: "<json string>", mac: "<hmac>", type: 1 }
     * MAC = HMAC-SHA256(key2, data)
     *
     * @return true if processed successfully (caller always returns HTTP 200)
     */
    @Transactional
    public boolean handleZaloPayCallback(Map<String, Object> cbData) {
        try {
            String dataStr = String.valueOf(cbData.get("data"));
            String reqMac  = String.valueOf(cbData.get("mac"));

            // Verify: mac = HMAC-SHA256(key2, dataStr)
            String computed = hmacSHA256(zaloKey2, dataStr);
            if (!computed.equalsIgnoreCase(reqMac)) {
                log.error("[ZaloPay] Callback signature mismatch");
                return false;
            }

            // Parse the data JSON string
            ObjectMapper mapper = new ObjectMapper();
            @SuppressWarnings("unchecked")
            Map<String, Object> dataJson = mapper.readValue(dataStr, Map.class);
            String appTransId = String.valueOf(dataJson.get("app_trans_id"));

            // Look up DatVe by maQR (where we stored appTransId)
            DatVe datVe = datVeRepository.findByMaQR(appTransId).orElse(null);
            if (datVe == null) {
                log.error("[ZaloPay] DatVe not found for appTransId={}", appTransId);
                return false;
            }

            // Idempotency guard
            if ("paid".equals(datVe.getTrangThaiThanhToan())) return true;

            String zpTransId = String.valueOf(dataJson.get("zp_trans_id"));
            markPaid(datVe, "ZaloPay", zpTransId);
            log.info("[ZaloPay] Payment confirmed for booking: {}", datVe.getMaDatVe());
            return true;

        } catch (Exception e) {
            log.error("[ZaloPay] Callback error: {}", e.getMessage());
            return false;
        }
    }

    // ─────────────────────────────────────────────────────────────
    // PayOS: cancel booking when user cancels on PayOS page
    // ─────────────────────────────────────────────────────────────
    /**
     * Called when the user clicks Cancel/Back on the PayOS hosted payment page.
     * PayOS redirects to cancelUrl → frontend /payment-cancel → calls this method.
     *
     * Reuses DatVeService.cancelBookingByAdmin() which:
     *   - Sets TrangThai = 'cancelled'
     *   - Deletes ChiTietDatGhe (releases seats on the map)
     *   - Deletes SeatLock records
     *   - Decrements promo usage counter
     * No logic is duplicated here.
     */
    @Transactional
    public void cancelByPayOSOrderCode(Long orderCode) {
        DatVe datVe = datVeRepository.findByMaQR(String.valueOf(orderCode)).orElse(null);
        if (datVe == null) {
            log.warn("[PayOS] cancelByPayOSOrderCode: booking not found orderCode={}", orderCode);
            return;
        }
        cancelByPayOSCancel(datVe.getMaDatVe());
    }

    /**
     * Called by /payment-result when PayOS redirects back after a payment attempt.
     * PayOS appends orderCode to the returnUrl — we re-query PayOS for the link's
     * real status (authoritative, works even when the webhook cannot reach the
     * backend, e.g. local dev), then reconcile the booking accordingly.
     *
     * Lookup order: (1) by MaQR == orderCode; (2) by datVeId when MaQR was
     * overwritten by a later gateway attempt on the same booking. For (2) we
     * verify the orderCode belongs to that booking (orderCode = id*1_000_000
     * + millis%1_000_000, so orderCode / 1_000_000 must equal the booking id).
     */
    @Transactional
    public Map<String, Object> confirmPayOS(Long orderCode, Long datVeId) {
        DatVe datVe = datVeRepository.findByMaQR(String.valueOf(orderCode)).orElse(null);
        if (datVe == null && datVeId != null) {
            DatVe byId = datVeRepository.findById(datVeId).orElse(null);
            if (byId != null && orderCode / 1_000_000L == byId.getId()) {
                datVe = byId;
            }
        }
        if (datVe == null) {
            throw new IllegalArgumentException("Không tìm thấy đơn đặt vé cho mã thanh toán này");
        }
        try {
            vn.payos.model.v2.paymentRequests.PaymentLink link =
                    payOS.paymentRequests().get(orderCode);
            vn.payos.model.v2.paymentRequests.PaymentLinkStatus status = link.getStatus();
            log.info("[PayOS] confirm: orderCode={} status={} booking={}",
                    orderCode, status, datVe.getMaDatVe());

            if (status == vn.payos.model.v2.paymentRequests.PaymentLinkStatus.PAID) {
                markPaid(datVe, "PayOS", link.getId());
            } else if (status == vn.payos.model.v2.paymentRequests.PaymentLinkStatus.CANCELLED
                    || status == vn.payos.model.v2.paymentRequests.PaymentLinkStatus.EXPIRED
                    || status == vn.payos.model.v2.paymentRequests.PaymentLinkStatus.FAILED) {
                datVeService.performCancel(datVe);
            }
        } catch (Exception e) {
            log.warn("[PayOS] confirm: failed to fetch link orderCode={}: {}", orderCode, e.getMessage());
            // Non-fatal — fall through and report the current DB state.
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("datVeId",            datVe.getId());
        result.put("maDatVe",            datVe.getMaDatVe());
        result.put("trangThai",          datVe.getTrangThai());
        result.put("trangThaiThanhToan", datVe.getTrangThaiThanhToan());
        return result;
    }

    @Transactional
    public void cancelByPayOSCancel(Long datVeId) {
        DatVe datVe = datVeRepository.findById(datVeId).orElse(null);
        if (datVe == null) {
            log.warn("[PayOS] cancelByPayOSCancel: booking not found id={}", datVeId);
            return;
        }
        cancelByPayOSCancel(datVe.getMaDatVe());
    }

    @Transactional
    public void cancelByPayOSCancel(String maDatVe) {
        DatVe datVe = datVeRepository.findByMaDatVe(maDatVe).orElse(null);
        if (datVe == null) {
            log.warn("[PayOS] cancelByPayOSCancel: booking not found maDatVe={}", maDatVe);
            return;
        }
        // Guard: don't cancel already-paid or already-cancelled bookings
        if ("paid".equals(datVe.getTrangThaiThanhToan())) {
            log.info("[PayOS] cancelByPayOSCancel: booking {} is already paid — skipping", maDatVe);
            return;
        }
        if ("cancelled".equals(datVe.getTrangThai())) {
            log.debug("[PayOS] cancelByPayOSCancel: booking {} already cancelled", maDatVe);
            return;
        }
        datVeService.cancelBookingByAdmin(maDatVe);
        log.info("[PayOS] Booking {} cancelled via PayOS cancel redirect", maDatVe);
    }

    // ─────────────────────────────────────────────────────────────
    // MoMo: handle IPN callback (kept for backward compat)
    // ─────────────────────────────────────────────────────────────
    @Transactional
    public boolean handleMomoCallback(Map<String, Object> body) {
        String partnerCode  = str(body, "partnerCode");
        String orderId      = str(body, "orderId");
        String requestId    = str(body, "requestId");
        String amount       = str(body, "amount");
        String orderInfo    = str(body, "orderInfo");
        String orderType    = str(body, "orderType");
        String transId      = str(body, "transId");
        String resultCode   = str(body, "resultCode");
        String message      = str(body, "message");
        String payType      = str(body, "payType");
        String responseTime = str(body, "responseTime");
        String extraData    = str(body, "extraData");
        String signature    = str(body, "signature");

        String rawSignature = "accessKey="   + momoAccessKey
                + "&amount="       + amount
                + "&extraData="    + extraData
                + "&message="      + message
                + "&orderId="      + orderId
                + "&orderInfo="    + orderInfo
                + "&orderType="    + orderType
                + "&partnerCode="  + partnerCode
                + "&payType="      + payType
                + "&requestId="    + requestId
                + "&responseTime=" + responseTime
                + "&resultCode="   + resultCode
                + "&transId="      + transId;

        String computed = hmacSHA256(momoSecretKey, rawSignature);
        if (signature == null || !computed.equalsIgnoreCase(signature)) {
            log.error("[MoMo] IPN signature mismatch. orderId={}", orderId);
            return false;
        }

        if (orderId == null) return false;
        String maDatVe = orderId.contains("_") ? orderId.split("_")[0] : orderId;

        DatVe datVe = datVeRepository.findByMaDatVe(maDatVe).orElse(null);
        if (datVe == null) return false;

        if ("paid".equals(datVe.getTrangThaiThanhToan())) return true;

        if ("0".equals(resultCode)) {
            markPaid(datVe, "MoMo", transId);
        } else {
            datVeService.performCancel(datVe);
        }
        return true;
    }

    // ─────────────────────────────────────────────────────────────
    // ZaloPay: verify gateway redirect (return/cancel) via checksum
    // ─────────────────────────────────────────────────────────────
    /**
     * Called by /payment-result when ZaloPay redirects the browser back.
     * The query params (appid|apptransid|pmcid|bankcode|amount|discountamount|status)
     * carry a checksum, but sandbox traffic does not always reproduce it with the
     * configured key2, so we log mismatches instead of blocking.
     * Reconcile the booking from the redirect signal:
     *   status = "-49" → user cancelled → release booking/seats
     *   status =  "1"  → confirm via ZaloPay Query API (MAC with key1, the same
     *                    key that successfully creates orders) → mark paid
     */
    @Transactional
    public Map<String, Object> handleZaloPayRedirect(Map<String, Object> params, Long datVeId) {
        String apptransid    = str(params, "apptransid");
        String status        = str(params, "status");
        String appid         = str(params, "appid");
        String pmcid         = str(params, "pmcid");
        String bankcode      = str(params, "bankcode");
        String amount        = str(params, "amount");
        String discountamount = str(params, "discountamount");
        String checksum      = str(params, "checksum");

        // Best-effort checksum check — non-blocking (see javadoc above)
        String raw      = appid + "|" + apptransid + "|" + pmcid + "|" + bankcode
                        + "|" + amount + "|" + discountamount + "|" + status;
        String computed = hmacSHA256(zaloKey2, raw);
        if (!computed.equalsIgnoreCase(checksum)) {
            log.warn("[ZaloPay] Redirect checksum mismatch apptransid={} (proceeding)", apptransid);
        }

        DatVe datVe = datVeRepository.findByMaQR(apptransid).orElse(null);
        if (datVe == null && datVeId != null) {
            // MaQR may have been overwritten by a later gateway attempt on the same
            // booking, so fall back to the booking id the frontend had in its path.
            // apptransid is built as "<yyMMdd>_<maDatVe>_<millis>" — verify it really
            // belongs to that booking before trusting it.
            DatVe byId = datVeRepository.findById(datVeId).orElse(null);
            if (byId != null && apptransid.contains(byId.getMaDatVe())) {
                datVe = byId;
            }
        }
        if (datVe == null) {
            log.warn("[ZaloPay] Redirect: booking not found apptransid={}", apptransid);
            throw new IllegalArgumentException("Không tìm thấy đơn đặt vé");
        }

        if ("1".equals(status)) {
            String zpTransId = queryZaloPayOrder(apptransid);
            if (zpTransId != null) {
                markPaid(datVe, "ZaloPay", zpTransId);
                log.info("[ZaloPay] Payment confirmed via redirect for booking: {}", datVe.getMaDatVe());
            } else {
                log.warn("[ZaloPay] Redirect status=1 but query returned non-success for {}", apptransid);
            }
        } else if ("-49".equals(status)) {
            datVeService.performCancel(datVe);
            log.info("[ZaloPay] Booking {} cancelled via gateway redirect", datVe.getMaDatVe());
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("datVeId",            datVe.getId());
        result.put("maDatVe",            datVe.getMaDatVe());
        result.put("status",             status);
        result.put("trangThai",          datVe.getTrangThai());
        result.put("trangThaiThanhToan", datVe.getTrangThaiThanhToan());
        return result;
    }

    /**
     * Queries ZaloPay for the real status of an order.
     * POST form-urlencoded to the query URL with:
     *   mac = HMAC-SHA256(key1, app_id|app_trans_id|key1)
     * @return zp_trans_id when return_code == 1, otherwise null
     */
    private String queryZaloPayOrder(String appTransId) {
        try {
            String mac = hmacSHA256(zaloKey1,
                    zaloAppId + "|" + appTransId + "|" + zaloKey1);

            String formBody = "app_id=" + URLEncoder.encode(zaloAppId, StandardCharsets.UTF_8)
                    + "&app_trans_id=" + URLEncoder.encode(appTransId, StandardCharsets.UTF_8)
                    + "&mac=" + URLEncoder.encode(mac, StandardCharsets.UTF_8);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            HttpEntity<String> request = new HttpEntity<>(formBody, headers);

            @SuppressWarnings("unchecked")
            ResponseEntity<Map> response = restTemplate.exchange(
                    zaloQueryUrl, HttpMethod.POST, request, Map.class);
            Map<String, Object> body = response.getBody();
            if (body == null) return null;

            int returnCode = ((Number) body.get("return_code")).intValue();
            if (returnCode != 1) {
                log.info("[ZaloPay] Query order {} return_code={} ({})",
                        appTransId, returnCode, body.get("return_message"));
                return null;
            }
            Object zpTransId = body.get("zp_trans_id");
            return zpTransId == null ? appTransId : String.valueOf(zpTransId);
        } catch (Exception e) {
            log.error("[ZaloPay] Query order {} failed: {}", appTransId, e.getMessage());
            return null;
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Mark booking as paid + award loyalty points + send email
    // ─────────────────────────────────────────────────────────────
    @Transactional
    public void markPaid(DatVe datVe, String method, String transactionNo) {
        // Idempotency guard — a booking must only be marked paid once.
        // Prevents duplicate stock deduction / duplicate ThanhToan rows
        // when a payment gateway delivers the same callback multiple times.
        if ("paid".equalsIgnoreCase(datVe.getTrangThaiThanhToan())) {
            log.info("[ThanhToanService] Booking {} already paid — skipping markPaid", datVe.getMaDatVe());
            return;
        }

        datVe.setTrangThai("confirmed");
        datVe.setTrangThaiThanhToan("paid");
        datVeRepository.save(datVe);

        // Deduct product inventory — combos/food/drinks bundled with the ticket
        // are taken out of stock the moment the booking becomes paid.
        datVeService.deductStock(datVe);

        ThanhToan tt = new ThanhToan();
        tt.setDatVe(datVe);
        tt.setSoTien(datVe.getTongTienThanhToan());
        tt.setPhuongThucThanhToan(method);
        tt.setMaGiaoDich(transactionNo);
        tt.setTrangThai("success");
        tt.setThoiGianThanhToan(LocalDateTime.now());
        thanhToanRepository.save(tt);

        NguoiDung user = datVe.getNguoiDung();
        // Loyalty points [RQ]: earn 1 điểm / 1.000đ thực trả, chỉ khi đơn >= 100.000đ.
        long pointsEarned = 0;
        BigDecimal paid = datVe.getTongTienThanhToan();
        if (paid != null && paid.compareTo(new BigDecimal("100000")) >= 0) {
            pointsEarned = paid.divideToIntegralValue(BigDecimal.valueOf(1000)).longValue();
        }
        user.setDiemTichLuy((user.getDiemTichLuy() == null ? 0 : user.getDiemTichLuy()) + (int) pointsEarned);

        BigDecimal currentSpent = user.getTongTienDaChi() == null ? BigDecimal.ZERO : user.getTongTienDaChi();
        BigDecimal newSpent     = currentSpent.add(paid == null ? BigDecimal.ZERO : paid);
        user.setTongTienDaChi(newSpent);
        user.setCapDoThanhVien(calculateMemberLevel(newSpent));

        // Deduct only the points actually redeemed (already capped at creation time)
        if (datVe.getDiemSuDung() != null && datVe.getDiemSuDung() > 0) {
            int remaining = user.getDiemTichLuy() - datVe.getDiemSuDung();
            user.setDiemTichLuy(Math.max(0, remaining));
        }
        nguoiDungRepository.save(user);

        try {
            emailService.sendBookingConfirmation(datVe);
        } catch (Exception e) {
            log.error("[ThanhToanService] Booking confirmation email failed: {}", e.getMessage());
        }
    }

    private String calculateMemberLevel(BigDecimal tongTienDaChi) {
        double amount = tongTienDaChi.doubleValue();
        if (amount >= 30_000_000) return "Kim Cương";
        if (amount >= 15_000_000) return "Vàng";
        if (amount >= 5_000_000)  return "Bạc";
        return "Thường";
    }

    private static String str(Map<String, Object> map, String key) {
        Object v = map.get(key);
        return v == null ? "" : String.valueOf(v);
    }

    // ─────────────────────────────────────────────────────────────
    // Crypto helpers (used by VNPay and MoMo — NOT by PayOS)
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

    /**
     * Chọn frontend base URL để dựng return/cancel/redirect URL cho các cổng
     * thanh toán. Ưu tiên origin thực tế do frontend gửi lên (giúp chạy đúng
     * khi truy cập qua devtunnel / domain tùy chỉnh); fallback về config.
     */
    private String resolveFrontendUrl(String frontendOrigin, String configuredUrl) {
        if (frontendOrigin != null && !frontendOrigin.isBlank()) {
            String origin = frontendOrigin.replaceAll("/+$", "");
            // Preserve the path of the configured URL (e.g. /payment-result, /payment-cancel)
            // so redirects land on the right SPA route instead of the root → 404.
            String path = "";
            try {
                java.net.URI uri = new java.net.URI(configuredUrl);
                if (uri.getPath() != null) path = uri.getPath();
            } catch (Exception ignored) { /* keep path empty */ }
            return origin + path;
        }
        return configuredUrl;
    }
}
