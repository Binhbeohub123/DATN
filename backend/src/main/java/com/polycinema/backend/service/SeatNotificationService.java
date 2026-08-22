package com.polycinema.backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class SeatNotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    public void broadcastSeatLocked(Long lichChieuId, Long gheNgoiId, long expiresAtMillis) {
        Map<String, Object> payload = Map.of(
                "gheNgoiId", gheNgoiId,
                "event", "locked",
                "expiresAt", expiresAtMillis
        );
        messagingTemplate.convertAndSend("/topic/seats/" + lichChieuId, payload);
        log.debug("Broadcast seat locked: lichChieuId={}, gheNgoiId={}", lichChieuId, gheNgoiId);
    }

    public void broadcastSeatUnlocked(Long lichChieuId, Long gheNgoiId) {
        Map<String, Object> payload = Map.of(
                "gheNgoiId", gheNgoiId,
                "event", "unlocked"
        );
        messagingTemplate.convertAndSend("/topic/seats/" + lichChieuId, payload);
        log.debug("Broadcast seat unlocked: lichChieuId={}, gheNgoiId={}", lichChieuId, gheNgoiId);
    }
}
