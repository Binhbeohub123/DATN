package com.polycinema.backend.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for PhimService.computeStatus() — the nextSession-based status rule.
 *
 * Business rule:
 *  - null nextSession (no future showtime)      => "da_ket_thuc"
 *  - nextSession < 30 days from today           => "dang_chieu"
 *  - nextSession >= 30 days from today          => "sap_chieu"
 */
class PhimServiceComputeStatusTest {

    private final LocalDateTime today = LocalDateTime.now();

    @Test
    @DisplayName("null nextSession => da_ket_thuc")
    void noFutureShowtime_isFinished() {
        assertThat(PhimService.computeStatus(null)).isEqualTo("da_ket_thuc");
    }

    @Test
    @DisplayName("nextSession today (0 days) => dang_chieu")
    void today_isShowing() {
        assertThat(PhimService.computeStatus(today)).isEqualTo("dang_chieu");
    }

    @Test
    @DisplayName("nextSession 1 day from now => dang_chieu")
    void oneDayAway_isShowing() {
        assertThat(PhimService.computeStatus(today.plusDays(1))).isEqualTo("dang_chieu");
    }

    @Test
    @DisplayName("nextSession 29 days from now => dang_chieu")
    void twentyNineDaysAway_isShowing() {
        assertThat(PhimService.computeStatus(today.plusDays(29))).isEqualTo("dang_chieu");
    }

    @Test
    @DisplayName("nextSession exactly 30 days from now => sap_chieu")
    void thirtyDaysAway_isComingSoon() {
        assertThat(PhimService.computeStatus(today.plusDays(30))).isEqualTo("sap_chieu");
    }

    @Test
    @DisplayName("nextSession 60 days from now => sap_chieu")
    void farFuture_isComingSoon() {
        assertThat(PhimService.computeStatus(today.plusDays(60))).isEqualTo("sap_chieu");
    }
}
