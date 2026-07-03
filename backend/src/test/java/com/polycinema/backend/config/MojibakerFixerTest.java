package com.polycinema.backend.config;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the mojibake detection and reversal logic in MojibakeFixer.
 * These run fully offline — no DB or Spring context required.
 *
 * Each test pair: [corrupted Latin-1-as-Unicode string] → [expected clean Vietnamese]
 *
 * How the corrupt values were generated (documented for reproducibility):
 *   original.getBytes(UTF-8) → treat each byte as Latin-1 char → that Latin-1 string
 *   is what ends up in the DB.  tryFixMojibake() reverses this.
 */
class MojibakerFixerTest {

    // ── Known corrupted → expected pairs ─────────────────────

    @Test
    void fix_HaNoiAddress() {
        // "Hà Nội" stored as Latin-1 bytes of its UTF-8 encoding
        String corrupt = corruptViaTwoPass("Hà Nội");
        String fixed   = MojibakeFixer.tryFixMojibake(corrupt);
        assertThat(fixed).isEqualTo("Hà Nội");
    }

    @Test
    void fix_PolyCinemaHaNoi() {
        String corrupt = corruptViaTwoPass("PolyCinema Hà Nội");
        String fixed   = MojibakeFixer.tryFixMojibake(corrupt);
        assertThat(fixed).isEqualTo("PolyCinema Hà Nội");
    }

    @Test
    void fix_TenPhong() {
        String corrupt = corruptViaTwoPass("Phòng 1 – 2D");
        String fixed   = MojibakeFixer.tryFixMojibake(corrupt);
        assertThat(fixed).isEqualTo("Phòng 1 – 2D");
    }

    @Test
    void fix_TenPhim() {
        String corrupt = corruptViaTwoPass("Lật Mặt 8: Vòng Xoáy Tội Lỗi");
        String fixed   = MojibakeFixer.tryFixMojibake(corrupt);
        assertThat(fixed).isEqualTo("Lật Mặt 8: Vòng Xoáy Tội Lỗi");
    }

    @Test
    void fix_LoaiGhe_thuong() {
        String corrupt = corruptViaTwoPass("thường");
        String fixed   = MojibakeFixer.tryFixMojibake(corrupt);
        assertThat(fixed).isEqualTo("thường");
    }

    @Test
    void fix_LoaiGhe_capDoi() {
        String corrupt = corruptViaTwoPass("cặp đôi");
        String fixed   = MojibakeFixer.tryFixMojibake(corrupt);
        assertThat(fixed).isEqualTo("cặp đôi");
    }

    @Test
    void fix_MoTa() {
        String corrupt = corruptViaTwoPass("Hành trình của hoàng tử khám phá sức mạnh rồng.");
        String fixed   = MojibakeFixer.tryFixMojibake(corrupt);
        assertThat(fixed).isEqualTo("Hành trình của hoàng tử khám phá sức mạnh rồng.");
    }

    // ── Clean strings must NOT be flagged ────────────────────

    @Test
    void cleanAsciiString_notFlagged() {
        String clean = "Avengers: Doomsday";
        assertThat(MojibakeFixer.tryFixMojibake(clean)).isNull();
    }

    @Test
    void cleanVietnamese_alreadyCorrect_notFlagged() {
        // Already correct Unicode Vietnamese must not be altered
        assertThat(MojibakeFixer.tryFixMojibake("Hà Nội")).isNull();
        assertThat(MojibakeFixer.tryFixMojibake("thường")).isNull();
        assertThat(MojibakeFixer.tryFixMojibake("cặp đôi")).isNull();
    }

    @Test
    void nullInput_returnsNull() {
        assertThat(MojibakeFixer.tryFixMojibake(null)).isNull();
    }

    @Test
    void emptyInput_returnsNull() {
        assertThat(MojibakeFixer.tryFixMojibake("")).isNull();
    }

    // ── looksLikeMojibake detection ───────────────────────────

    @Test
    void detection_corruptedString_detected() {
        String corrupt = corruptViaTwoPass("Hà Nội");
        assertThat(MojibakeFixer.looksLikeMojibake(corrupt)).isTrue();
    }

    @Test
    void detection_cleanString_notDetected() {
        assertThat(MojibakeFixer.looksLikeMojibake("PolyCinema Hanoi")).isFalse();
    }

    // ── Helper: simulate the corruption ──────────────────────
    /**
     * Produces the corrupted string the same way the DB received it:
     *   take clean UTF-8 bytes → treat each byte as Latin-1 character.
     * This is exactly what the JDBC driver did when sendStringParametersAsUnicode
     * was missing and the app wrote to a VARCHAR/Latin-1-collation column.
     */
    private static String corruptViaTwoPass(String clean) {
        byte[] utf8 = clean.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        return new String(utf8, java.nio.charset.Charset.forName("ISO-8859-1"));
    }
}
