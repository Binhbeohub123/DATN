package com.polycinema.backend.service;

import com.polycinema.backend.entity.NguoiDung;
import com.polycinema.backend.repository.NguoiDungRepository;
import com.polycinema.backend.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final NguoiDungRepository repo;
    private final EmailService emailService;
    private final JwtUtil jwtUtil;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    /**
     * Chuẩn hóa VaiTro từ DB sang role cho JWT.
     * DB lưu: 'admin', 'staff', 'customer'
     * JWT cần: 'ADMIN', 'STAFF', 'USER'
     */
    private String normalizeRole(String role) {
        if (role == null || role.isBlank()) return "USER";

        String normalized = role.trim().toUpperCase();
        if (normalized.equals("CUSTOMER")) return "USER";
        if (normalized.startsWith("ROLE_")) return normalized.substring(5);

        return normalized;
    }

    // ================= OTP STORE =================
    private final Map<String, OtpData> otpStore = new HashMap<>();
    private static final long EXPIRE_TIME = 15 * 60 * 1000;

    static class OtpData {
        String code;
        long expireAt;

        OtpData(String code, long expireAt) {
            this.code = code;
            this.expireAt = expireAt;
        }
    }

    // ================= VALIDATION =================
    private String validateEmail(String email) {
        if (email == null || email.isBlank()) return "Email không được để trống";

        String regex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        if (!Pattern.matches(regex, email)) return "Email không đúng định dạng";

        return null;
    }

    private String validatePhone(String phone) {
        if (phone == null || phone.isBlank()) return "Số điện thoại không được để trống";

        if (!phone.matches("^(03|05|07|08|09)\\d{8}$"))
            return "Số điện thoại không hợp lệ";

        return null;
    }

    private String validateName(String name) {
        if (name == null || name.isBlank()) return "Họ tên không được để trống";

        if (!name.matches("^[\\p{L} ]+$"))
            return "Họ tên không hợp lệ";

        return null;
    }

    public String register(String email, String password, String hoTen, String soDienThoai) {

    String emailError = validateEmail(email);
    if (emailError != null) return emailError;

    if (password == null || password.isBlank())
        return "Mật khẩu không được để trống";

    if (password.length() < 8)
        return "Mật khẩu phải từ 8 ký tự để đảm bảo bảo mật";

    String phoneError = validatePhone(soDienThoai);
    if (phoneError != null) return phoneError;

    String nameError = validateName(hoTen);
    if (nameError != null) return nameError;

    email = email.trim().toLowerCase();
    hoTen = hoTen.trim();
    soDienThoai = soDienThoai.trim();

    if (repo.findByEmail(email).isPresent())
        return "Email đã tồn tại";

    if (repo.findBySoDienThoai(soDienThoai).isPresent())
        return "Số điện thoại đã tồn tại";

    NguoiDung u = new NguoiDung();
    u.setEmail(email);
    u.setMatKhauHash(encoder.encode(password));
    u.setHoTen(hoTen);
    u.setSoDienThoai(soDienThoai);
    u.setVaiTro("customer");
    u.setTrangThai(true);
    u.setIsEmailVerified(false);

    repo.save(u);

    sendVerifyOtp(email);

    return "Đăng ký thành công. Vui lòng xác thực email";
}

    // ================= LOGIN =================
 public String login(String email, String password) {

    if (email == null || email.isBlank())
        return "Email không được để trống";

    if (password == null || password.isBlank())
        return "Mật khẩu không được để trống";

    email = email.trim().toLowerCase();

    NguoiDung u = repo.findByEmail(email).orElse(null);

    if (u == null)
        return "Sai email hoặc mật khẩu";

    if (!encoder.matches(password, u.getMatKhauHash()))
        return "Sai email hoặc mật khẩu";

    if (!Boolean.TRUE.equals(u.getIsEmailVerified()))
        return "Email chưa xác thực";

    // Locked accounts are still allowed to LOGIN — the token is issued, but
    // JwtAuthenticationFilter rejects every authenticated request with
    // ACCOUNT_LOCKED + the reason, so the user sees the "bị khóa" notice
    // and cannot do anything (frontend shows a full-screen lock overlay).

    return jwtUtil.generateToken(email, normalizeRole(u.getVaiTro()));
}

    // ================= SEND VERIFY OTP =================
    public String sendVerifyOtp(String email) {

        String emailError = validateEmail(email);
        if (emailError != null) return emailError;

        email = email.trim().toLowerCase();

        if (repo.findByEmail(email).isEmpty())
            return "Email không tồn tại";

        otpStore.remove(email);

        String otp = String.valueOf(100000 + new Random().nextInt(900000));

        otpStore.put(email, new OtpData(
                otp,
                System.currentTimeMillis() + EXPIRE_TIME
        ));

        try {
            emailService.sendOtp(email, otp);
        } catch (Exception e) {
            e.printStackTrace();
            otpStore.remove(email);
            return "Không gửi được OTP";
        }

        return "OTP đã gửi";
    }

    // ================= VERIFY EMAIL =================
    public String verifyEmail(String email, String otp) {

        String emailError = validateEmail(email);
        if (emailError != null) return emailError;

        if (otp == null || otp.isBlank()) return "Vui lòng nhập mã OTP";

        email = email.trim().toLowerCase();
        otp = otp.trim();

        OtpData data = otpStore.get(email);

        if (data == null) return "Không tìm thấy OTP";

        if (System.currentTimeMillis() > data.expireAt) {
            otpStore.remove(email);
            return "OTP hết hạn";
        }

        if (!data.code.equals(otp))
            return "Sai OTP";

        NguoiDung u = repo.findByEmail(email).orElse(null);
        if (u == null) return "Không tồn tại user";

        u.setIsEmailVerified(true);
        repo.save(u);

        otpStore.remove(email);

        return "Xác thực email thành công";
    }

    // ================= RESEND OTP =================
    public String resendVerifyOtp(String email) {
        return sendVerifyOtp(email);
    }

    // ================= FORGOT PASSWORD =================
    public String sendForgotOtp(String email) {
        return sendVerifyOtp(email);
    }

    // ================= RESET PASSWORD =================
    public String resetPassword(String email, String otp, String newPass) {

        String emailError = validateEmail(email);
        if (emailError != null) return emailError;

        if (otp == null || otp.isBlank()) return "Vui lòng nhập mã OTP";

        if (newPass == null || newPass.isBlank()) return "Mật khẩu mới không được để trống";

        if (newPass.length() < 8) return "Mật khẩu phải từ 8 ký tự để đảm bảo bảo mật";

        email = email.trim().toLowerCase();
        otp = otp.trim();

        OtpData data = otpStore.get(email);

        if (data == null) return "Không tìm thấy OTP";

        if (System.currentTimeMillis() > data.expireAt) {
            otpStore.remove(email);
            return "OTP hết hạn";
        }

        if (!data.code.equals(otp))
            return "Sai OTP";

        NguoiDung u = repo.findByEmail(email).orElse(null);

        if (u == null) return "Không tồn tại user";

        u.setMatKhauHash(encoder.encode(newPass));
        repo.save(u);

        otpStore.remove(email);

        return "Đổi mật khẩu thành công";
    }

    // ================= ADMIN: CREATE STAFF / ADMIN ACCOUNT =================
    /**
     * Called by an authenticated ADMIN to create a staff or admin account.
     * Bypasses the OTP email-verification flow — account is immediately active.
     * @param vaiTro  "staff" or "admin"
     * @param createdByAdminId  ID of the admin performing the creation (for audit trail)
     */
    public NguoiDung createStaffOrAdmin(String email, String password, String hoTen,
                                        String soDienThoai, String vaiTro, Long createdByAdminId) {
        // Validate allowed roles
        if (!"staff".equals(vaiTro) && !"admin".equals(vaiTro)) {
            throw new IllegalArgumentException("Vai trò không hợp lệ. Chỉ chấp nhận 'staff' hoặc 'admin'");
        }

        String emailError = validateEmail(email);
        if (emailError != null) throw new IllegalArgumentException(emailError);

        if (password == null || password.isBlank())
            throw new IllegalArgumentException("Mật khẩu không được để trống");
        if (password.length() < 8)
            throw new IllegalArgumentException("Mật khẩu phải từ 8 ký tự để đảm bảo bảo mật");

        if (hoTen == null || hoTen.isBlank())
            throw new IllegalArgumentException("Họ tên không được để trống");

        email = email.trim().toLowerCase();
        hoTen = hoTen.trim();

        if (repo.findByEmail(email).isPresent())
            throw new IllegalArgumentException("Email đã tồn tại");

        if (soDienThoai != null && !soDienThoai.isBlank()) {
            soDienThoai = soDienThoai.trim();
            if (repo.findBySoDienThoai(soDienThoai).isPresent())
                throw new IllegalArgumentException("Số điện thoại đã tồn tại");
        } else {
            soDienThoai = null;
        }

        NguoiDung u = new NguoiDung();
        u.setEmail(email);
        u.setMatKhauHash(encoder.encode(password));
        u.setHoTen(hoTen);
        u.setSoDienThoai(soDienThoai);
        u.setVaiTro(vaiTro);
        u.setTrangThai(true);
        u.setIsEmailVerified(true);   // admin-created accounts are immediately active
        u.setCreatedBy(createdByAdminId);

        return repo.save(u);
    }

    // ================= GET PROFILE =================
    public NguoiDung getProfile(String email) {
        if (email == null || email.isBlank()) return null;

        email = email.trim().toLowerCase();
        return repo.findByEmail(email).orElse(null);
    }

    // ================= UPDATE PROFILE =================
    public String updateProfile(String email, String hoTen, String soDienThoai, String ngaySinh, String anhDaiDien) {

        String emailError = validateEmail(email);
        if (emailError != null) return emailError;

        email = email.trim().toLowerCase();

        NguoiDung u = repo.findByEmail(email).orElse(null);
        if (u == null) return "Không tìm thấy người dùng";

        // Validate and update hoTen
        if (hoTen != null && !hoTen.isBlank()) {
            String nameError = validateName(hoTen);
            if (nameError != null) return nameError;
            u.setHoTen(hoTen.trim());
        }

        // Validate and update soDienThoai
        if (soDienThoai != null && !soDienThoai.isBlank()) {
            String phoneError = validatePhone(soDienThoai);
            if (phoneError != null) return phoneError;

            // Check if phone already exists for another user
            repo.findBySoDienThoai(soDienThoai.trim()).ifPresent(existing -> {
                if (!existing.getId().equals(u.getId())) {
                    throw new IllegalArgumentException("Số điện thoại đã được sử dụng");
                }
            });

            u.setSoDienThoai(soDienThoai.trim());
        }

        // Update ngaySinh
        if (ngaySinh != null && !ngaySinh.isBlank()) {
            try {
                u.setNgaySinh(java.time.LocalDate.parse(ngaySinh));
            } catch (Exception e) {
                return "Ngày sinh không đúng định dạng (yyyy-MM-dd)";
            }
        }

        // Update anhDaiDien
        if (anhDaiDien != null && !anhDaiDien.isBlank()) {
            u.setAnhDaiDien(anhDaiDien.trim());
        }

        repo.save(u);

        return "Cập nhật thông tin thành công";
    }
}