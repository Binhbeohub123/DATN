package com.polycinema.backend.controller;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.polycinema.backend.entity.DatVe;
import com.polycinema.backend.service.AuthService;
import com.polycinema.backend.service.DatVeService;
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

@RestController
@RequestMapping("/api/ve")
@RequiredArgsConstructor
public class VeController {

    private final DatVeService datVeService;
    private final AuthService authService;

    @GetMapping(value = "/{maVe}/qr", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> getQrCode(@PathVariable String maVe) {
        Long callerUserId = authService.getUserIdFromToken();
        String callerRole = getRoleFromToken();
        boolean isAdmin = "ADMIN".equals(callerRole);

        DatVe datVe = datVeService.findByMaDatVe(maVe);
        if (datVe == null) return ResponseEntity.notFound().build();

        if (!isAdmin && (callerUserId == null || !callerUserId.equals(datVe.getNguoiDung().getId()))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        String qrContent = datVe.getMaDatVe();

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
            headers.set(HttpHeaders.CACHE_CONTROL, "max-age=3600, private");
            return new ResponseEntity<>(png, headers, HttpStatus.OK);
        } catch (WriterException | java.io.IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
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
        } catch (Exception e) { return ""; }
    }
}
