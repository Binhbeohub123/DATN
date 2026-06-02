package com.polycinema.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

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

            // Gather booking details
            String maDatVe = datVe.getMaDatVe();
            String tenPhim = "—";
            String tenPhong = "—";
            String gioChieu = "—";
            if (datVe.getLichChieu() != null) {
                if (datVe.getLichChieu().getPhim() != null) {
                    tenPhim = datVe.getLichChieu().getPhim().getTenPhim();
                }
                if (datVe.getLichChieu().getPhongChieu() != null) {
                    tenPhong = datVe.getLichChieu().getPhongChieu().getTenPhong();
                }
                if (datVe.getLichChieu().getThoiGianBatDau() != null) {
                    gioChieu = datVe.getLichChieu().getThoiGianBatDau().toString().replace("T", " ");
                }
            }

            // Collect seat list
            StringBuilder seats = new StringBuilder();
            if (datVe.getChiTietDatGhe() != null && !datVe.getChiTietDatGhe().isEmpty()) {
                datVe.getChiTietDatGhe().forEach(ct -> {
                    if (ct.getGheNgoi() != null) {
                        if (seats.length() > 0) seats.append(", ");
                        seats.append(ct.getGheNgoi().getHangGhe().trim())
                             .append(ct.getGheNgoi().getSoGhe());
                    }
                });
            }

            String tongTien = datVe.getTongTienThanhToan() != null
                    ? String.format("%,.0f VND", datVe.getTongTienThanhToan().doubleValue())
                    : "—";

            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setTo(toEmail);
            msg.setSubject("PolyCinema - Xác nhận đặt vé #" + maDatVe);
            msg.setText(
                    "Xin chào " + (datVe.getNguoiDung().getHoTen() != null ? datVe.getNguoiDung().getHoTen() : toEmail) + ",\n\n" +
                    "Đặt vé của bạn đã được xác nhận thành công!\n\n" +
                    "═══════════════════════════════\n" +
                    "  THÔNG TIN VÉ XEM PHIM\n" +
                    "═══════════════════════════════\n" +
                    "Mã đặt vé : " + maDatVe + "\n" +
                    "Phim       : " + tenPhim + "\n" +
                    "Phòng chiếu: " + tenPhong + "\n" +
                    "Suất chiếu : " + gioChieu + "\n" +
                    "Ghế        : " + (seats.length() > 0 ? seats.toString() : "—") + "\n" +
                    "Tổng tiền  : " + tongTien + "\n" +
                    "═══════════════════════════════\n\n" +
                    "Vui lòng xuất trình mã đặt vé khi đến rạp.\n" +
                    "Cảm ơn bạn đã sử dụng dịch vụ PolyCinema!"
            );

            mailSender.send(msg);

        } catch (Exception e) {
            System.err.println("❌ Send booking confirmation email failed: " + e.getMessage());
        }
    }
}
