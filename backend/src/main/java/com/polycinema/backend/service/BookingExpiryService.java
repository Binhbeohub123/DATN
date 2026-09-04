package com.polycinema.backend.service;

import com.polycinema.backend.entity.DatVe;
import com.polycinema.backend.repository.DatVeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Runs every 60 seconds and auto-cancels bookings that were created more than
 * 10 minutes ago and are still unpaid/unconfirmed.
 *
 * Delegates all cancellation and seat-release logic to DatVeService.performCancel()
 * so both paths (manual cancel + auto-cancel) stay consistent and DRY.
 *
 * DB CHECK constraint on TrangThaiThanhToan only allows 'unpaid' and 'paid'.
 * DatVeService.performCancel() NEVER calls setTrangThaiThanhToan() — it only
 * sets TrangThai = 'cancelled', deletes ChiTietDatGhe rows, and releases locks.
 *
 * @EnableScheduling is on CinemaApplication.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BookingExpiryService {

    private static final int EXPIRY_MINUTES = 2;

    private final DatVeRepository datVeRepository;
    private final DatVeService    datVeService;

    @Scheduled(fixedRate = 60_000)
    @Transactional
    public void cancelExpiredBookings() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(EXPIRY_MINUTES);
        List<DatVe> expired  = datVeRepository.findExpiredUnpaidBookings(cutoff);

        if (expired.isEmpty()) {
            log.debug("Auto-cancel run: no expired bookings found");
            return;
        }

        int cancelled = 0;
        for (DatVe datVe : expired) {
            try {
                // Delegate to DatVeService.performCancel() which:
                //  1. Sets TrangThai = 'cancelled' (NEVER touches TrangThaiThanhToan)
                //  2. Deletes ChiTietDatGhe rows → seats show as available in seat map
                //  3. Deletes SeatLock records
                //  4. Decrements promo usage counter if applicable
                datVeService.performCancel(datVe);

                log.info("Auto-cancelled expired booking: {} (created at {})",
                        datVe.getMaDatVe(), datVe.getNgayTao());
                cancelled++;

            } catch (Exception e) {
                // Log and continue — one failed booking must not abort the others
                log.error("Failed to auto-cancel booking {}: {}",
                        datVe.getMaDatVe(), e.getMessage());
            }
        }

        log.info("Auto-cancel run complete: {}/{} bookings cancelled",
                cancelled, expired.size());
    }
}
