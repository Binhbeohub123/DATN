package com.polycinema.backend.controller;

import com.polycinema.backend.entity.NguoiDung;
import com.polycinema.backend.repository.NguoiDungRepository;
import com.polycinema.backend.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class AuthController {

    private final AuthService service;
    private final NguoiDungRepository nguoiDungRepository;
    private final BCryptPasswordEncoder encoder;

    // ================= REGISTER =================
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> req) {

        String result = service.register(
                req.get("email"),
                req.get("password"),
                req.get("hoTen"),
                req.get("soDienThoai")
        );

        if (!result.startsWith("Đăng ký thành công")) {
            return ResponseEntity.badRequest().body(result);
        }

        return ResponseEntity.ok(result);
    }

    // ================= LOGIN =================
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> req) {

        String result = service.login(
                req.get("email"),
                req.get("password")
        );

        switch (result) {

            case "Sai email hoặc mật khẩu":
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(result);

            case "Email chưa xác thực":
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(result);

            case "Tài khoản đã bị khóa":
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(result);

            default:
                return ResponseEntity.ok(result);
        }
    }

    // ================= VERIFY EMAIL =================
    @PostMapping("/verify")
    public ResponseEntity<?> verify(@RequestBody Map<String, String> req) {

        String result = service.verifyEmail(
                req.get("email"),
                req.get("otp")
        );

        if (!result.equals("Xác thực email thành công")) {
            return ResponseEntity.badRequest().body(result);
        }

        return ResponseEntity.ok(result);
    }

    // ================= RESEND VERIFY OTP =================
    @PostMapping("/resend-verify")
    public ResponseEntity<?> resend(@RequestBody Map<String, String> req) {

        String result = service.resendVerifyOtp(
                req.get("email")
        );

        if (!result.equals("OTP đã gửi")) {
            return ResponseEntity.badRequest().body(result);
        }

        return ResponseEntity.ok(result);
    }

    // ================= FORGOT PASSWORD =================
    @PostMapping("/forgot")
    public ResponseEntity<?> forgot(@RequestBody Map<String, String> req) {

        String result = service.sendForgotOtp(
                req.get("email")
        );

        if (!result.equals("OTP đã gửi")) {
            return ResponseEntity.badRequest().body(result);
        }

        return ResponseEntity.ok(result);
    }

    // ================= RESET PASSWORD =================
    @PostMapping("/reset")
    public ResponseEntity<?> reset(@RequestBody Map<String, String> req) {

        String result = service.resetPassword(
                req.get("email"),
                req.get("otp"),
                req.get("newPassword")
        );

        if (!result.equals("Đổi mật khẩu thành công")) {
            return ResponseEntity.badRequest().body(result);
        }

        return ResponseEntity.ok(result);
    }

    // ================= GET PROFILE =================
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile() {
        try {
            String email = getEmailFromToken();
            if (email == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Vui lòng đăng nhập");
            }

            NguoiDung user = service.getProfile(email);
            if (user == null) {
                return ResponseEntity.notFound().build();
            }

            // Remove sensitive data before returning
            user.setMatKhauHash(null);

            return ResponseEntity.ok(user);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi lấy thông tin: " + e.getMessage());
        }
    }

    // ================= UPDATE PROFILE =================
    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestBody Map<String, String> req) {
        try {
            String email = getEmailFromToken();
            if (email == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Vui lòng đăng nhập");
            }

            String result = service.updateProfile(
                    email,
                    req.get("hoTen"),
                    req.get("soDienThoai"),
                    req.get("ngaySinh"),
                    req.get("anhDaiDien")
            );

            if (!result.equals("Cập nhật thông tin thành công")) {
                return ResponseEntity.badRequest().body(result);
            }

            return ResponseEntity.ok(result);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi cập nhật: " + e.getMessage());
        }
    }

    // ================= CHANGE PASSWORD =================
    /**
     * POST /api/auth/change-password
     * Body: { "currentPassword": "...", "newPassword": "..." }
     * Requires a valid JWT. Validates currentPassword against the stored hash,
     * then re-encodes and saves newPassword.
     */
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> req) {
        try {
            String email = getEmailFromToken();
            if (email == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Vui lòng đăng nhập");
            }

            String currentPassword = req.get("currentPassword");
            String newPassword     = req.get("newPassword");

            if (currentPassword == null || currentPassword.isBlank()) {
                return ResponseEntity.badRequest().body("Mật khẩu hiện tại không được để trống");
            }
            if (newPassword == null || newPassword.length() < 6) {
                return ResponseEntity.badRequest().body("Mật khẩu mới phải tối thiểu 6 ký tự");
            }

            NguoiDung user = nguoiDungRepository.findByEmail(email).orElse(null);
            if (user == null) {
                return ResponseEntity.notFound().build();
            }

            // Validate current password
            if (!encoder.matches(currentPassword, user.getMatKhauHash())) {
                return ResponseEntity.badRequest().body("Mật khẩu hiện tại không đúng");
            }

            // Encode and save new password
            user.setMatKhauHash(encoder.encode(newPassword));
            nguoiDungRepository.save(user);

            return ResponseEntity.ok("Đổi mật khẩu thành công");

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi đổi mật khẩu: " + e.getMessage());
        }
    }

    // ================= HELPER: GET EMAIL FROM JWT =================
    private String getEmailFromToken() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated()) {
                // Reject anonymous authentication
                if ("anonymousUser".equals(auth.getPrincipal())) {
                    return null;
                }
                Object principal = auth.getPrincipal();
                if (principal instanceof String) {
                    return principal.toString();
                }
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }
}