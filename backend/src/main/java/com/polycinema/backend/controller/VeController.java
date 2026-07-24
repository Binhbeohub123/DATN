package com.polycinema.backend.controller;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.polycinema.backend.entity.DatVe;
import com.polycinema.backend.repository.DatVeRepository;
import com.polycinema.backend.repository.NguoiDungRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * GET /api/ve/{maVe}/qr
 * Generates a QR code PNG for the given booking code.
 * Only the booking owner or an admin may request it.
 */
@RestController
@RequestMapping("/api/ve")
@RequiredArgsConstructor
public class VeController {

    private final DatVeRepository datVeRepository;
    private final NguoiDungRepository nguoiDungRepository;

    @GetMapping(value = "/{maVe}/qr", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> getQrCode(@PathVariable String maVe) {
        // ── Resolve caller identity ──────────────────────────────
        Long callerUserId = getUserIdFromToken();
        String callerRole  = getRoleFromToken();
        boolean isAdmin    = "ADMIN".equals(callerRole);

        // ── Find booking ─────────────────────────────────────────
        DatVe datVe = datVeRepository.findByMaDatVe(maVe).orElse(null);
        if (datVe == null) {
            return ResponseEntity.notFound().build();
        }

        // ── Authorization: owner or admin ─────────────────────────
        if (!isAdmin && (callerUserId == null || !callerUserId.equals(datVe.getNguoiDung().getId()))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // ── QR content: booking code + movie + showtime ───────────
        String qrContent = buildQrContent(datVe);

        // ── Generate QR PNG via ZXing ─────────────────────────────
        try {
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
            hints.put(EncodeHintType.MARGIN, 2);
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");

            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix matrix = writer.encode(qrContent, BarcodeFormat.QR_CODE, 300, 300, hints);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, "PNG", out);
            byte[] png = out.toByteArray();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_PNG);
            headers.setContentLength(png.length);
            // Cache for 1 hour — QR content doesn't change after booking is created
            headers.set(HttpHeaders.CACHE_CONTROL, "max-age=3600, private");
            return new ResponseEntity<>(png, headers, HttpStatus.OK);

        } catch (WriterException | java.io.IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ── Build human-readable QR content ──────────────────────────
    private String buildQrContent(DatVe datVe) {
        StringBuilder sb = new StringBuilder();
        sb.append("POLYCINEMA TICKET\n");
        sb.append("Mã đặt vé: ").append(datVe.getMaDatVe()).append("\n");

        if (datVe.getLichChieu() != null) {
            if (datVe.getLichChieu().getPhim() != null) {
                sb.append("Phim: ").append(datVe.getLichChieu().getPhim().getTenPhim()).append("\n");
            }
            if (datVe.getLichChieu().getThoiGianBatDau() != null) {
                sb.append("Suất: ").append(datVe.getLichChieu().getThoiGianBatDau()).append("\n");
            }
            if (datVe.getLichChieu().getPhongChieu() != null) {
                sb.append("Phòng: ").append(datVe.getLichChieu().getPhongChieu().getTenPhong()).append("\n");
            }
        }

        sb.append("Tổng tiền: ").append(datVe.getTongTienThanhToan()).append(" VND");
        return sb.toString();
    }

    // ── JWT helpers (principal = email string) ────────────────────
    private Long getUserIdFromToken() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) return null;
            Object principal = auth.getPrincipal();
            if (!(principal instanceof String)) return null;
            String email = (String) principal;
            if ("anonymousUser".equals(email)) return null;
            return nguoiDungRepository.findByEmail(email).map(u -> u.getId()).orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    private String getRoleFromToken() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) return "";
            return auth.getAuthorities().stream()
                    .findFirst()
                    .map(a -> a.getAuthority().replace("ROLE_", ""))
                    .orElse("");
        } catch (Exception e) {
            return "";
        }
    }
}
