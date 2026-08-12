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

    // ================= EMAIL CHANGED NOTIFICATION =================
    /**
     * Sends a plain-text security notice to BOTH the old and new email addresses
     * when an admin changes a user's email. No links, no tokens — purely informational.
     * Errors are silently swallowed (same pattern as sendWelcome) so a mail failure
     * never blocks the actual profile update.
     */
    public void sendEmailChangedNotification(String oldEmail, String newEmail, String name) {
        String displayName = (name != null && !name.isBlank()) ? name : oldEmail;
        String body =
            "Xin chào " + displayName + ",\n\n" +
            "Địa chỉ email của tài khoản PolyCinema của bạn vừa được quản trị viên " +
            "thay đổi.\n\n" +
            "  Email cũ : " + oldEmail + "\n" +
            "  Email mới: " + newEmail + "\n\n" +
            "Bạn sẽ cần đăng nhập lại bằng địa chỉ email mới từ lần sau.\n\n" +
            "Nếu bạn KHÔNG yêu cầu thay đổi này, vui lòng liên hệ bộ phận hỗ trợ " +
            "PolyCinema ngay lập tức.\n\n" +
            "Trân trọng,\nĐội ngũ PolyCinema";

        // Notify old address
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setTo(oldEmail);
            msg.setSubject("PolyCinema - Thông báo thay đổi email tài khoản");
            msg.setText(body);
            mailSender.send(msg);
        } catch (Exception e) {
            System.err.println("sendEmailChangedNotification to OLD failed: " + e.getMessage());
        }

        // Notify new address
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setTo(newEmail);
            msg.setSubject("PolyCinema - Thông báo thay đổi email tài khoản");
            msg.setText(body);
            mailSender.send(msg);
        } catch (Exception e) {
            System.err.println("sendEmailChangedNotification to NEW failed: " + e.getMessage());
        }
    }

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
            String tenRap   = "—";
            String tenPhong = "—";
            String gioChieu = "—";
            if (datVe.getLichChieu() != null) {
                if (datVe.getLichChieu().getPhim() != null)
                    tenPhim = datVe.getLichChieu().getPhim().getTenPhim();
                if (datVe.getLichChieu().getPhongChieu() != null) {
                    tenPhong = datVe.getLichChieu().getPhongChieu().getTenPhong();
                    if (datVe.getLichChieu().getPhongChieu().getRapChieu() != null
                            && datVe.getLichChieu().getPhongChieu().getRapChieu().getTenRap() != null)
                        tenRap = datVe.getLichChieu().getPhongChieu().getRapChieu().getTenRap();
                }
                if (datVe.getLichChieu().getThoiGianBatDau() != null) {
                    gioChieu = formatShowtime(datVe.getLichChieu().getThoiGianBatDau());
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
                    ? formatVnd(datVe.getTongTienThanhToan().doubleValue())
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
                    tenRap, tenPhong, gioChieu, seatsStr, tongTien, ticketUrl);

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
                                     String tenRap, String tenPhong, String gioChieu,
                                     String seats, String tongTien, String ticketUrl) {
        return "<!DOCTYPE html>" +
            "<html lang='vi'><head><meta charset='UTF-8'/>" +
            "<meta name='viewport' content='width=device-width,initial-scale=1'/>" +
            "<title>Xác nhận đặt vé PolyCinema</title></head>" +
            "<body style='margin:0;padding:0;background:#0a0a12;font-family:Inter,Segoe UI,Arial,sans-serif'>" +
            "<table width='100%' cellpadding='0' cellspacing='0' border='0'>" +
            "<tr><td align='center' style='padding:32px 16px'>" +
            "<table width='600' cellpadding='0' cellspacing='0' border='0' style='max-width:600px;width:100%;background:#111827;border-radius:16px;overflow:hidden;border:1px solid #1f2937'>" +

            // Header — same logo mark as the site navbar, wordmark in navbar colors
            "<tr><td style='background:linear-gradient(135deg,#1a1a2e 0%,#0d1117 100%);padding:30px 36px;border-bottom:1px solid #C9A84C40'>" +
            "<table cellpadding='0' cellspacing='0' border='0'><tr>" +
            "<td style='vertical-align:middle'>" +
            "<svg width='36' height='36' viewBox='0 0 24 24' fill='none' stroke='#C9A84C' stroke-width='1.5' stroke-linecap='round' stroke-linejoin='round' xmlns='http://www.w3.org/2000/svg' aria-hidden='true'>" +
            "<path d='M7 4v16M17 4v16M3 8h4m10 0h4M3 16h4m10 0h4M4 4h16a1 1 0 0 1 1 1v14a1 1 0 0 1-1 1H4a1 1 0 0 1-1-1V5a1 1 0 0 1 1-1z'/></svg>" +
            "</td>" +
            "<td style='vertical-align:middle;padding-left:14px'>" +
            "<p style='margin:0;font-size:26px;font-weight:700;letter-spacing:-0.5px'><span style='color:#f1f5f9'>Poly</span><span style='color:#C9A84C'>Cinema</span></p>" +
            "<p style='margin:4px 0 0;font-size:12px;color:#9ca3af'>Hệ thống rạp chiếu phim hiện đại</p>" +
            "</td></tr></table>" +
            "</td></tr>" +

            // Success heading
            "<tr><td style='padding:28px 36px 6px'>" +
            "<p style='margin:0;font-size:22px;font-weight:700;color:#f1f5f9'>Đặt vé thành công! 🎉</p>" +
            "</td></tr>" +

            // Greeting
            "<tr><td style='padding:12px 36px 6px'>" +
            "<p style='margin:0;font-size:15px;color:#e5e5e5'>Xin chào <strong style='color:#f1f5f9'>" + escHtml(name) + "</strong>,</p>" +
            "<p style='margin:8px 0 0;font-size:14px;color:#9ca3af;line-height:1.6'>Đơn đặt vé của bạn đã được xác nhận thành công. Cảm ơn bạn đã lựa chọn PolyCinema.</p>" +
            "</td></tr>" +

            // Ticket info
            "<tr><td style='padding:22px 36px 6px'>" +
            "<p style='margin:0;font-size:15px;font-weight:700;color:#e5e5e5'>🎟️ Thông tin đặt vé</p>" +
            "</td></tr>" +
            "<tr><td style='padding:10px 36px'>" +
            "<table width='100%' cellpadding='0' cellspacing='0' border='0' style='border-collapse:collapse'>" +
            infoRow("Mã đặt vé",       maDatVe) +
            infoRow("Phim",            tenPhim) +
            infoRow("Rạp chiếu",       tenRap) +
            infoRow("Phòng chiếu",     tenPhong) +
            infoRow("Suất chiếu",      gioChieu) +
            infoRow("Ghế",             seats) +
            infoRowHighlight("Tổng thanh toán", tongTien) +
            infoRowPaid("Trạng thái",  "Đã thanh toán") +
            "</table></td></tr>" +

            // E-ticket CTA
            "<tr><td style='padding:10px 36px 4px'>" +
            "<p style='margin:0;font-size:15px;font-weight:700;color:#e5e5e5'>📱 Vé điện tử</p>" +
            "<p style='margin:8px 0 0;font-size:13px;color:#9ca3af;line-height:1.6'>Nhấn vào nút bên dưới để xem vé điện tử và mã QR dùng khi vào rạp.</p>" +
            "</td></tr>" +
            "<tr><td style='padding:18px 36px 10px;text-align:center'>" +
            "<a href='" + ticketUrl + "' style='display:inline-block;padding:14px 34px;" +
            "background:#C9A84C;color:#0d0d0d;text-decoration:none;border-radius:8px;" +
            "font-weight:700;font-size:14px;letter-spacing:1px'>" +
            "🎟️ XEM VÉ ĐIỆN TỬ" +
            "</a>" +
            "</td></tr>" +

            // Notes
            "<tr><td style='padding:10px 36px 6px'>" +
            "<p style='margin:0;font-size:12px;font-weight:700;letter-spacing:0.5px;color:#e5e5e5;text-transform:uppercase'>Lưu ý</p>" +
            "<p style='margin:10px 0 0;font-size:13px;color:#9ca3af;line-height:1.8'>" +
            "• Vui lòng đến rạp trước giờ chiếu từ 15–20 phút.<br/>" +
            "• Mã QR chỉ được sử dụng 01 lần để vào rạp.<br/>" +
            "• Không chia sẻ mã QR hoặc đường dẫn vé cho người khác.<br/>" +
            "• Nếu cần hỗ trợ, vui lòng liên hệ bộ phận Chăm sóc khách hàng của PolyCinema." +
            "</p>" +
            "</td></tr>" +

            // Footer
            "<tr><td style='background:#0d0d0d;padding:24px 36px;border-top:1px solid #1f2937;text-align:center'>" +
            "<p style='margin:0;font-size:13px;color:#9ca3af'>Trân trọng,</p>" +
            "<p style='margin:6px 0 0;font-size:15px;font-weight:700;color:#e5e5e5'>PolyCinema</p>" +
            "<p style='margin:4px 0 0;font-size:12px;color:#6b7280'>Hệ thống rạp chiếu phim hiện đại</p>" +
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

    private String infoRowPaid(String label, String value) {
        return "<tr>" +
            "<td style='padding:12px 0 4px;font-size:13px;color:#9ca3af;font-weight:600;width:40%'>" + escHtml(label) + "</td>" +
            "<td style='padding:12px 0 4px;font-size:14px;color:#10b981;font-weight:700;text-align:right'>" + escHtml(value) + "</td>" +
            "</tr>";
    }

    /** Formats "21:00 • Thứ Sáu, 07/08/2026" using the Vietnamese weekday name. */
    private static String formatShowtime(java.time.LocalDateTime ldt) {
        String[] dow = {"Thứ Hai", "Thứ Ba", "Thứ Tư", "Thứ Năm", "Thứ Sáu", "Thứ Bảy", "Chủ Nhật"};
        String day = dow[ldt.getDayOfWeek().getValue() - 1];
        return String.format("%02d:%02d • %s, %02d/%02d/%04d",
                ldt.getHour(), ldt.getMinute(), day,
                ldt.getDayOfMonth(), ldt.getMonthValue(), ldt.getYear());
    }

    /** Formats an amount as "180.000 VNĐ" (dot thousands separator). */
    private static String formatVnd(double value) {
        java.text.DecimalFormat nf = new java.text.DecimalFormat("#,##0");
        java.text.DecimalFormatSymbols syms = new java.text.DecimalFormatSymbols(java.util.Locale.US);
        syms.setGroupingSeparator('.');
        nf.setDecimalFormatSymbols(syms);
        return nf.format(value) + " VNĐ";
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
