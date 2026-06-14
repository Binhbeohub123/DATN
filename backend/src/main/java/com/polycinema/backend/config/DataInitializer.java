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
        // Fix #1: complete-set guard — only skip if all 50 seats are already present.
        // A threshold of >= 10 allowed partial dirty states (< 10 seats) to pass through
        // and then hit UNIQUE KEY violations on re-insert.
        List<GheNgoi> existingList = gheNgoiRepository.findByPhongChieuId(phong.getId());
        if (existingList.size() >= 50) {
            return; // Room is fully seeded
        }

        // Fix #2 + #3: load the existing seat set once, outside the loop, and trim
        // HangGhe values to neutralize SQL Server CHAR(2) trailing-space padding.
        // "A ".trim() == "A" so the equals check is reliable even against padded DB values.
        java.util.Set<String> existingKeys = new java.util.HashSet<>();
        for (GheNgoi g : existingList) {
            String hang = g.getHangGhe() != null ? g.getHangGhe().trim() : "";
            existingKeys.add(hang + "-" + g.getSoGhe());
        }

        String[] rows = {"A", "B", "C", "D", "E"};
        int seatsPerRow = 10;
        int created = 0;

        for (String row : rows) {
            for (int num = 1; num <= seatsPerRow; num++) {
                String key = row + "-" + num;
                if (existingKeys.contains(key)) {
                    continue; // Seat already present — skip without hitting the DB
                }

                // Fix #3 (value correctness): set LoaiGhe and HeSoGia per confirmed schema:
                //   A/B/C → 'thường'  HeSoGia 1.00
                //   D     → 'vip'     HeSoGia 1.50
                //   E     → 'cặp đôi' HeSoGia 2.00   (Fix #4: was incorrectly 'vip'/1.50)
                // CHECK constraint on GheNgoi.LoaiGhe only permits these three values.
                String loaiGhe;
                BigDecimal heSoGia;
                if ("D".equals(row)) {
                    loaiGhe = "vip";
                    heSoGia = new BigDecimal("1.50");
                } else if ("E".equals(row)) {
                    loaiGhe = "cặp đôi";         // Fix #4: must use diacritics — 'cặp đôi'
                    heSoGia = new BigDecimal("2.00");
                } else {
                    loaiGhe = "thường";           // A/B/C — must use diacritic 'thường'
                    heSoGia = BigDecimal.ONE;
                }

                GheNgoi ghe = new GheNgoi();
                ghe.setPhongChieu(phong);
                ghe.setHangGhe(row);
                ghe.setSoGhe(num);
                ghe.setLoaiGhe(loaiGhe);
                ghe.setHeSoGia(heSoGia);

                // Fix #5: wrap save() in try-catch so any remaining constraint violation
                // (e.g. a race condition or an edge case not yet identified) is logged
                // and skipped rather than crashing the entire application startup.
                try {
                    gheNgoiRepository.save(ghe);
                    existingKeys.add(key); // Keep the in-memory set consistent
                    created++;
                } catch (Exception e) {
                    System.out.println("[DataInitializer] Skipped seat " + row + num
                            + " in " + phong.getTenPhong() + ": " + e.getMessage());
                }
            }
        }
        if (created > 0) {
            System.out.println("[DataInitializer] Created " + created + " seats in " + phong.getTenPhong());
        }
    }
}
