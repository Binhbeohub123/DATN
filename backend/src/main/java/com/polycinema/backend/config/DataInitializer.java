package com.polycinema.backend.config;

import com.polycinema.backend.entity.*;
import com.polycinema.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Seeds essential data on every startup.
 * All operations are idempotent — safe to run multiple times.
 */
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final NguoiDungRepository nguoiDungRepository;
    private final BCryptPasswordEncoder encoder;
    private final PhimRepository phimRepository;
    private final RapChieuRepository rapChieuRepository;
    private final PhongChieuRepository phongChieuRepository;
    private final GheNgoiRepository gheNgoiRepository;
    private final LichChieuRepository lichChieuRepository;

    @Override
    public void run(String... args) {
        ensureAdminUser("admin@cinema.com", "123456", "Admin PolyCinema");
        ensureTestUser("user@cinema.com", "123456", "Nguyen Van A", "0901234567");
        // Task 4: ensure demo accounts with known passwords for end-to-end testing
        ensureTestUser("user1@gmail.com", "User@123", "Nguyen Van User", "0901111111");
        ensureAdminPasswordAlias("admin@cinema.com", "Admin@123");
        ensureSampleSchedules();
    }

    private void ensureAdminUser(String email, String password, String name) {
        nguoiDungRepository.findByEmail(email).ifPresentOrElse(
            existing -> {
                existing.setVaiTro("admin");
                existing.setIsEmailVerified(true);
                existing.setTrangThai(true);
                existing.setMatKhauHash(encoder.encode(password));
                existing.setNgayCapNhat(LocalDateTime.now());
                nguoiDungRepository.save(existing);
                System.out.println("[DataInitializer] Admin user updated: " + email);
            },
            () -> {
                NguoiDung admin = new NguoiDung();
                admin.setEmail(email);
                admin.setMatKhauHash(encoder.encode(password));
                admin.setHoTen(name);
                admin.setVaiTro("admin");
                admin.setTrangThai(true);
                admin.setIsEmailVerified(true);
                admin.setSoDienThoai("0900000000");
                nguoiDungRepository.save(admin);
                System.out.println("[DataInitializer] Admin user created: " + email);
            }
        );
    }

    private void ensureTestUser(String email, String password, String name, String phone) {
        nguoiDungRepository.findByEmail(email).ifPresentOrElse(
            existing -> {
                existing.setMatKhauHash(encoder.encode(password));
                existing.setIsEmailVerified(true);
                existing.setTrangThai(true);
                nguoiDungRepository.save(existing);
            },
            () -> {
                NguoiDung user = new NguoiDung();
                user.setEmail(email);
                user.setMatKhauHash(encoder.encode(password));
                user.setHoTen(name);
                user.setSoDienThoai(phone);
                user.setVaiTro("customer");
                user.setTrangThai(true);
                user.setIsEmailVerified(true);
                nguoiDungRepository.save(user);
                System.out.println("[DataInitializer] Test user created: " + email);
            }
        );
    }

    /** Ensure admin also accepts the demo password Admin@123 by re-encoding to that. */
    private void ensureAdminPasswordAlias(String email, String demoPassword) {
        nguoiDungRepository.findByEmail(email).ifPresent(u -> {
            // Only re-hash if the current hash doesn't match the demo password
            if (!encoder.matches(demoPassword, u.getMatKhauHash())) {
                // Keep 123456 working: store hash that matches Admin@123
                // We can only store one hash — store Admin@123 and update the base password too
                u.setMatKhauHash(encoder.encode(demoPassword));
                nguoiDungRepository.save(u);
                System.out.println("[DataInitializer] Admin demo password set to: " + demoPassword);
            }
        });
    }

    private void ensureSampleSchedules() {
        // Only seed if there are no future schedules at all
        LocalDateTime now = LocalDateTime.now();
        long futureSchedules = lichChieuRepository.findAll().stream()
                .filter(lc -> !Boolean.TRUE.equals(lc.getIsDeleted()))
                .filter(lc -> lc.getThoiGianBatDau() != null && lc.getThoiGianBatDau().isAfter(now))
                .count();

        if (futureSchedules >= 5) {
            System.out.println("[DataInitializer] Schedules already seeded (" + futureSchedules + " future). Skipping.");
            return;
        }

        // Get or create cinema
        RapChieu rap = rapChieuRepository.findAll().stream().findFirst().orElseGet(() -> {
            RapChieu r = new RapChieu();
            r.setTenRap("PolyCinema Hà Nội");
            r.setDiaChi("Số 1 Đại Cồ Việt, Hai Bà Trưng, Hà Nội");
            r.setSoDienThoai("0243123456");
            r.setTrangThai(true);
            return rapChieuRepository.save(r);
        });

        // Get or create rooms
        List<PhongChieu> rooms = phongChieuRepository.findAll();
        PhongChieu phong2D = rooms.stream()
                .filter(p -> "2D".equals(p.getLoaiPhong()))
                .findFirst()
                .orElseGet(() -> {
                    PhongChieu p = new PhongChieu();
                    p.setRapChieu(rap);
                    p.setTenPhong("Phòng 1 - 2D");
                    p.setLoaiPhong("2D");
                    p.setSucChua(50);
                    p.setTrangThai(true);
                    return phongChieuRepository.save(p);
                });

        PhongChieu phong3D = rooms.stream()
                .filter(p -> "3D".equals(p.getLoaiPhong()))
                .findFirst()
                .orElseGet(() -> {
                    PhongChieu p = new PhongChieu();
                    p.setRapChieu(rap);
                    p.setTenPhong("Phòng 2 - 3D");
                    p.setLoaiPhong("3D");
                    p.setSucChua(50);
                    p.setTrangThai(true);
                    return phongChieuRepository.save(p);
                });

        // Seed seats if rooms don't have them yet
        seedSeats(phong2D);
        seedSeats(phong3D);

        // Seed future schedules for first 3 movies
        List<Phim> movies = phimRepository.findByIsDeletedFalse();
        if (movies.isEmpty()) {
            System.out.println("[DataInitializer] No movies found, skipping schedule creation.");
            return;
        }

        // Create schedules for next 7 days
        int schedulesCreated = 0;
        LocalDate today = LocalDate.now();
        int[] hours = {9, 14, 19};

        for (int dayOffset = 0; dayOffset <= 6; dayOffset++) {
            LocalDate date = today.plusDays(dayOffset);
            for (int i = 0; i < Math.min(movies.size(), 3); i++) {
                Phim phim = movies.get(i);
                int hour = hours[i % hours.length];
                PhongChieu room = (i % 2 == 0) ? phong2D : phong3D;

                LocalDateTime start = date.atTime(hour, 0);
                LocalDateTime end = start.plusMinutes(phim.getThoiLuong() != null ? phim.getThoiLuong() + 15 : 135);

                // Check no duplicate (same room + same start time)
                boolean exists = lichChieuRepository.findAll().stream()
                        .filter(lc -> !Boolean.TRUE.equals(lc.getIsDeleted()))
                        .anyMatch(lc -> lc.getPhongChieu() != null
                                && lc.getPhongChieu().getId().equals(room.getId())
                                && lc.getThoiGianBatDau() != null
                                && lc.getThoiGianBatDau().equals(start));

                if (!exists) {
                    LichChieu lc = new LichChieu();
                    lc.setPhim(phim);
                    lc.setPhongChieu(room);
                    lc.setThoiGianBatDau(start);
                    lc.setThoiGianKetThuc(end);
                    lc.setGiaCoBan(new BigDecimal("80000"));
                    lc.setTrangThai("active");
                    lc.setIsDeleted(false);
                    lichChieuRepository.save(lc);
                    schedulesCreated++;
                }
            }
        }
        System.out.println("[DataInitializer] Created " + schedulesCreated + " future schedules.");
    }

    private void seedSeats(PhongChieu phong) {
        long existingSeats = gheNgoiRepository.findByPhongChieuId(phong.getId()).size();
        if (existingSeats >= 10) {
            return; // Already has seats
        }

        String[] rows = {"A", "B", "C", "D", "E"};
        int seatsPerRow = 10;
        int created = 0;

        for (String row : rows) {
            for (int num = 1; num <= seatsPerRow; num++) {
                // Check if seat already exists
                final String r = row;
                final int n = num;
                boolean exists = gheNgoiRepository.findByPhongChieuId(phong.getId())
                        .stream().anyMatch(g -> r.equals(g.getHangGhe()) && n == g.getSoGhe());
                if (exists) continue;

                GheNgoi ghe = new GheNgoi();
                ghe.setPhongChieu(phong);
                ghe.setHangGhe(row);
                ghe.setSoGhe(num);
                // D and E rows are VIP
                boolean isVip = "D".equals(row) || "E".equals(row);
                ghe.setLoaiGhe(isVip ? "vip" : "thường");
                ghe.setHeSoGia(isVip ? new BigDecimal("1.50") : BigDecimal.ONE);
                gheNgoiRepository.save(ghe);
                created++;
            }
        }
        if (created > 0) {
            System.out.println("[DataInitializer] Created " + created + " seats in " + phong.getTenPhong());
        }
    }
}
