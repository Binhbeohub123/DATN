package com.polycinema.backend.service;

import com.polycinema.backend.entity.ChiTietDatGhe;
import com.polycinema.backend.repository.ChiTietDatGheRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final ChiTietDatGheRepository chiTietDatGheRepository;

    /** Frontend base URL — reuses the same CORS origin config so it's consistent. */
    @Value("${cors.allowed-origins:http://localhost:5173}")
    private String frontendBaseUrl;

    // ================= OTP =================
    public void sendOtp(String email, String otp) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(email);
        msg.setSubject("PolyCinema - Mã OTP xác thực");
        msg.setText(
                "Xin chào,\n\n" +
                "Mã OTP của bạn là: " + otp + "\n" +
                "Hiệu lực: 15 phút\n\n" +
                "Nếu bạn không yêu cầu, hãy bỏ qua email này."
        );
        mailSender.send(msg);
    }

    // ================= WELCOME =================
    public void sendWelcome(String email, String name) {
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setTo(email);
            msg.setSubject("Chào mừng bạn đến PolyCinema");
            msg.setText(
                    "Xin chào " + name + ",\n\n" +
                    "Tài khoản của bạn đã được tạo thành công.\n" +
                    "Chúc bạn trải nghiệm vui vẻ!"
            );
            mailSender.send(msg);
        } catch (Exception e) {
            System.out.println("❌ Send welcome email failed: " + e.getMessage());
        }
    }

    // ================= BOOKING CONFIRMATION =================
    public void sendBookingConfirmation(com.polycinema.backend.entity.DatVe datVe) {
        try {
            String toEmail = datVe.getNguoiDung() != null ? datVe.getNguoiDung().getEmail() : null;
            if (toEmail == null || toEmail.isBlank()) return;

            // ── Gather booking fields ────────────────────────────────
            String maDatVe  = datVe.getMaDatVe();
            String tenPhim  = "—";
            String tenPhong = "—";
            String gioChieu = "—";
            if (datVe.getLichChieu() != null) {
                if (datVe.getLichChieu().getPhim() != null)
                    tenPhim = datVe.getLichChieu().getPhim().getTenPhim();
                if (datVe.getLichChieu().getPhongChieu() != null)
                    tenPhong = datVe.getLichChieu().getPhongChieu().getTenPhong();
                if (datVe.getLichChieu().getThoiGianBatDau() != null) {
                    java.time.LocalDateTime ldt = datVe.getLichChieu().getThoiGianBatDau();
                    gioChieu = String.format("%02d:%02d %02d/%02d/%04d",
                            ldt.getHour(), ldt.getMinute(),
                            ldt.getDayOfMonth(), ldt.getMonthValue(), ldt.getYear());
                }
            }

            // ── Fix: query seats from DB (in-memory collection is stale) ──
            List<ChiTietDatGhe> seats = chiTietDatGheRepository.findByDatVeId(datVe.getId());
            StringBuilder seatsSb = new StringBuilder();
            for (ChiTietDatGhe ct : seats) {
                if (ct.getGheNgoi() != null) {
                    if (seatsSb.length() > 0) seatsSb.append(", ");
                    seatsSb.append(ct.getGheNgoi().getHangGhe().trim())
                           .append(ct.getGheNgoi().getSoGhe());
                }
            }
            String seatsStr = seatsSb.length() > 0 ? seatsSb.toString() : "—";

            String tongTien = datVe.getTongTienThanhToan() != null
                    ? String.format("%,.0f VND", datVe.getTongTienThanhToan().doubleValue())
                    : "—";

            String customerName = datVe.getNguoiDung().getHoTen() != null
                    ? datVe.getNguoiDung().getHoTen() : toEmail;

            // Strip potential trailing slash from frontendBaseUrl
            String base = frontendBaseUrl.endsWith("/")
                    ? frontendBaseUrl.substring(0, frontendBaseUrl.length() - 1)
                    : frontendBaseUrl;
            String ticketUrl = base + "/payment-result/" + datVe.getId();

            // ── Build HTML body ──────────────────────────────────────
            String html = buildBookingHtml(customerName, maDatVe, tenPhim,
                    tenPhong, gioChieu, seatsStr, tongTien, ticketUrl);

            // ── Send as MimeMessage (HTML) ───────────────────────────
            MimeMessage mime = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mime, false, "UTF-8");
            helper.setTo(toEmail);
            helper.setSubject("PolyCinema - Xác nhận đặt vé #" + maDatVe);
            helper.setText(html, true);   // true = isHtml
            mailSender.send(mime);

        } catch (Exception e) {
            System.err.println("❌ Send booking confirmation email failed: " + e.getMessage());
        }
    }

    // ── HTML template ────────────────────────────────────────────
    private String buildBookingHtml(String name, String maDatVe, String tenPhim,
                                     String tenPhong, String gioChieu,
                                     String seats, String tongTien, String ticketUrl) {
        return "<!DOCTYPE html>" +
            "<html lang='vi'><head><meta charset='UTF-8'/>" +
            "<meta name='viewport' content='width=device-width,initial-scale=1'/>" +
            "<title>Xác nhận đặt vé PolyCinema</title></head>" +
            "<body style='margin:0;padding:0;background:#0a0a12;font-family:Inter,Segoe UI,Arial,sans-serif'>" +
            "<table width='100%' cellpadding='0' cellspacing='0' border='0'>" +
            "<tr><td align='center' style='padding:32px 16px'>" +
            "<table width='600' cellpadding='0' cellspacing='0' border='0' style='max-width:600px;width:100%;background:#111827;border-radius:16px;overflow:hidden;border:1px solid #1f2937'>" +

            // Header
            "<tr><td style='background:linear-gradient(135deg,#1a1a2e 0%,#0d1117 100%);padding:32px 36px;border-bottom:1px solid #C9A84C40'>" +
            "<p style='margin:0;font-size:28px;font-weight:700;color:#C9A84C;letter-spacing:-0.5px'>🎬 PolyCinema</p>" +
            "<p style='margin:8px 0 0;font-size:13px;color:#9ca3af'>Hệ thống rạp chiếu phim hiện đại</p>" +
            "</td></tr>" +

            // Greeting
            "<tr><td style='padding:28px 36px 16px'>" +
            "<p style='margin:0;font-size:16px;color:#e5e5e5'>Xin chào <strong style='color:#f1f5f9'>" + escHtml(name) + "</strong>,</p>" +
            "<p style='margin:10px 0 0;font-size:15px;color:#9ca3af'>Đặt vé của bạn đã được <strong style='color:#10b981'>xác nhận thành công</strong>! 🎉</p>" +
            "</td></tr>" +

            // Booking code banner
            "<tr><td style='padding:0 36px'>" +
            "<div style='background:rgba(201,168,76,0.08);border:1px solid #C9A84C60;border-radius:10px;padding:16px;text-align:center'>" +
            "<p style='margin:0;font-size:11px;font-weight:700;letter-spacing:1px;text-transform:uppercase;color:#9ca3af'>Mã đặt vé</p>" +
            "<p style='margin:6px 0 0;font-size:26px;font-weight:700;color:#C9A84C;font-family:monospace;letter-spacing:3px'>" + escHtml(maDatVe) + "</p>" +
            "</div></td></tr>" +

            // Info table
            "<tr><td style='padding:20px 36px'>" +
            "<table width='100%' cellpadding='0' cellspacing='0' border='0' style='border-collapse:collapse'>" +
            infoRow("🎞️ Phim",        tenPhim) +
            infoRow("🏠 Phòng chiếu", tenPhong) +
            infoRow("🕐 Suất chiếu",  gioChieu) +
            infoRow("💺 Ghế",         seats) +
            infoRowHighlight("💰 Tổng tiền",     tongTien) +
            "</table></td></tr>" +

            // CTA button
            "<tr><td style='padding:8px 36px 28px;text-align:center'>" +
            "<a href='" + ticketUrl + "' style='display:inline-block;padding:14px 32px;" +
            "background:#C9A84C;color:#0d0d0d;text-decoration:none;border-radius:8px;" +
            "font-weight:700;font-size:15px;letter-spacing:0.2px'>" +
            "🎟️ Xem vé &amp; mã QR" +
            "</a>" +
            "<p style='margin:12px 0 0;font-size:12px;color:#6b7280'>Hoặc truy cập: " +
            "<a href='" + ticketUrl + "' style='color:#C9A84C;text-decoration:none'>" + ticketUrl + "</a></p>" +
            "</td></tr>" +

            // Footer
            "<tr><td style='background:#0d0d0d;padding:20px 36px;border-top:1px solid #1f2937;text-align:center'>" +
            "<p style='margin:0;font-size:12px;color:#6b7280'>Vui lòng xuất trình mã QR hoặc mã đặt vé khi đến rạp.</p>" +
            "<p style='margin:6px 0 0;font-size:12px;color:#6b7280'>© 2026 PolyCinema. Cảm ơn bạn đã sử dụng dịch vụ!</p>" +
            "</td></tr>" +

            "</table></td></tr></table></body></html>";
    }

    private String infoRow(String label, String value) {
        return "<tr>" +
            "<td style='padding:10px 0;font-size:13px;color:#9ca3af;font-weight:600;border-bottom:1px solid #1f2937;width:40%'>" + escHtml(label) + "</td>" +
            "<td style='padding:10px 0;font-size:14px;color:#e5e5e5;border-bottom:1px solid #1f2937;text-align:right'>" + escHtml(value) + "</td>" +
            "</tr>";
    }

    private String infoRowHighlight(String label, String value) {
        return "<tr>" +
            "<td style='padding:12px 0 4px;font-size:13px;color:#9ca3af;font-weight:600;width:40%'>" + escHtml(label) + "</td>" +
            "<td style='padding:12px 0 4px;font-size:16px;color:#C9A84C;font-weight:700;text-align:right'>" + escHtml(value) + "</td>" +
            "</tr>";
    }

    /** Minimal HTML escaping — prevents XSS in data fields. */
    private static String escHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
