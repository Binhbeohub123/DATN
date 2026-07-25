package com.polycinema.backend.service;

import com.polycinema.backend.entity.SeatLock;
import com.polycinema.backend.repository.SeatLockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Runs every 60 seconds and deletes expired SeatLock records.
 * Seats are not stored on GheNgoi — the lock record's absence is
 * what makes a seat "available" again.
 */
@Service
@RequiredArgsConstructor
public class SeatLockCleanupService {

    private final SeatLockRepository seatLockRepository;

    @Scheduled(fixedRate = 60_000)
    @Transactional
    public void cleanExpiredLocks() {
        List<SeatLock> expired = seatLockRepository.findAllByExpiresAtBefore(LocalDateTime.now());
        if (!expired.isEmpty()) {
            seatLockRepository.deleteAll(expired);
            System.err.println("[SeatLockCleanup] Released " + expired.size() + " expired seat locks");
        }
    }
}
