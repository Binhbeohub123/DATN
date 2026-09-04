package com.polycinema.backend.service;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.polycinema.backend.repository.ChiTietDatGheRepository;
import com.polycinema.backend.repository.GheNgoiRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for EmailService.generateQrPng()  [Feature 2]
 *
 * Verifies the QR PNG helper produces a decodable image that round-trips back
 * to the exact maDatVe string — the same format StaffController.checkinQR reads.
 * All Spring deps are mocked; runs in milliseconds (no Spring context, no SMTP).
 */
@ExtendWith(MockitoExtension.class)
class EmailServiceQrTest {

    @Mock JavaMailSender              mailSender;
    @Mock ChiTietDatGheRepository     chiTietDatGheRepository;
    @Mock GheNgoiRepository           gheNgoiRepository;

    private EmailService newEmailService() {
        return new EmailService(mailSender, chiTietDatGheRepository, gheNgoiRepository);
    }

    @Test
    @DisplayName("generateQrPng returns a decodable PNG that round-trips to maDatVe")
    void qrRoundTripsToMaDatVe() throws Exception {
        EmailService svc = newEmailService();
        String maDatVe = "BK000123-ABC";

        byte[] png = svc.generateQrPng(maDatVe);

        assertThat(png).isNotNull();
        assertThat(png.length).isGreaterThan(0);

        // PNG magic bytes
        assertThat(png[0]).isEqualTo((byte) 0x89);
        assertThat(png[1]).isEqualTo((byte) 0x50); // 'P'
        assertThat(png[2]).isEqualTo((byte) 0x4E); // 'N'
        assertThat(png[3]).isEqualTo((byte) 0x47); // 'G'

        // Decode the QR back and confirm it equals the original maDatVe
        BufferedImage img = ImageIO.read(new ByteArrayInputStream(png));
        assertThat(img).isNotNull();

        BinaryBitmap bitmap = new BinaryBitmap(
                new HybridBinarizer(new BufferedImageLuminanceSource(img)));
        Result result = new MultiFormatReader().decode(bitmap,
                Map.of(DecodeHintType.CHARACTER_SET, "UTF-8"));

        assertThat(result.getText()).isEqualTo(maDatVe);
        assertThat(result.getBarcodeFormat().toString()).isEqualTo("QR_CODE");
    }

    @Test
    @DisplayName("generateQrPng returns null for null or blank maDatVe")
    void qrNullWhenBlank() {
        EmailService svc = newEmailService();
        assertThat(svc.generateQrPng(null)).isNull();
        assertThat(svc.generateQrPng("   ")).isNull();
        assertThat(svc.generateQrPng("")).isNull();
    }
}
