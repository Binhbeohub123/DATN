package com.polycinema.backend.config;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * One-off utility to detect and (optionally) fix mojibake in the PolyCinema database.
 *
 * BACKGROUND:
 *   Vietnamese text was stored as raw UTF-8 bytes misinterpreted as Latin-1/Windows-1252.
 *   e.g. "Hà Nội" (U+00E0, U+1ED9, U+1B9) was stored as the 6 Latin-1 characters
 *   that represent those UTF-8 bytes: "HÃ  Ná»™i"
 *
 * FIX APPROACH:
 *   1. Read the corrupted String from DB via JDBC (comes back as the wrong Unicode chars).
 *   2. Re-encode as Latin-1 bytes (reverting the mis-interpretation step).
 *   3. Decode those bytes as UTF-8 (recovering the original Vietnamese Unicode text).
 *
 * USAGE:
 *   a) Dry-run (default, safe):
 *      Set DRY_RUN = true  → prints preview, touches nothing.
 *   b) Apply:
 *      Set DRY_RUN = false → performs UPDATEs after printing the same preview.
 *
 * TRIGGERING:
 *   Run the Spring app once with the system property:
 *     -Dpolycinema.fix-mojibake=dryrun    (preview only)
 *     -Dpolycinema.fix-mojibake=apply     (actually fix)
 *   The bean only activates when that property is present.
 *   It does NOT run on every startup — it is fully opt-in.
 */
@Component
public class MojibakeFixer {

    private static final Charset LATIN1 = Charset.forName("ISO-8859-1");
    private static final Charset UTF8   = StandardCharsets.UTF_8;

    /**
     * Column descriptor for a single NVARCHAR column to inspect.
     */
    private record Col(String table, String pkCol, String col) {}

    /** All NVARCHAR columns that may hold Vietnamese text. */
    private static final List<Col> COLUMNS = List.of(
        new Col("RapChieu",   "Id", "TenRap"),
        new Col("RapChieu",   "Id", "DiaChi"),
        new Col("PhongChieu", "Id", "TenPhong"),
        new Col("Phim",       "Id", "TenPhim"),
        new Col("Phim",       "Id", "TenPhimTiengAnh"),
        new Col("Phim",       "Id", "TheLoai"),
        new Col("Phim",       "Id", "DaoDien"),
        new Col("Phim",       "Id", "DienVienChinh"),
        new Col("Phim",       "Id", "NgonNgu"),
        new Col("Phim",       "Id", "MoTa"),
        new Col("GheNgoi",    "Id", "LoaiGhe"),
        new Col("NguoiDung",  "Id", "HoTen"),
        new Col("NguoiDung",  "Id", "DiaChi"),
        new Col("KhuyenMai",  "Id", "TenKhuyenMai"),
        new Col("KhuyenMai",  "Id", "MoTa"),
        new Col("SanPham",    "Id", "TenSanPham"),
        new Col("SanPham",    "Id", "MoTa"),
        new Col("Banner",     "Id", "TieuDe"),
        new Col("Banner",     "Id", "MoTa")
    );

    private final JdbcTemplate jdbc;

    public MojibakeFixer(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
        String mode = System.getProperty("polycinema.fix-mojibake", "");
        if ("dryrun".equalsIgnoreCase(mode)) {
            run(true);
        } else if ("apply".equalsIgnoreCase(mode)) {
            run(false);
        }
        // Default: do nothing — bean is safe to keep in production code.
    }

    // ──────────────────────────────────────────────────────────────────
    // Exclusion rules (rows with confirmed data loss or false-positive risk)
    // ──────────────────────────────────────────────────────────────────

    /**
     * Returns true if this row/column combination should be SKIPPED by the
     * automatic fixer.  Skipped rows are listed separately in the output.
     *
     * Current exclusions:
     *   Phim.MoTa id=1 and id=2 — contain '?' replacement characters,
     *     meaning the original bytes were already lost before they reached
     *     the database.  The text cannot be recovered algorithmically.
     *     These must be re-entered manually.
     */
    private static boolean isExcluded(Col c, long pk) {
        return "Phim".equals(c.table())
            && "MoTa".equals(c.col())
            && (pk == 1L || pk == 2L);
    }

    // ──────────────────────────────────────────────────────────────────
    // Core logic
    // ──────────────────────────────────────────────────────────────────

    private void run(boolean dryRun) {
        System.out.println("=============================================================");
        System.out.println(" MojibakeFixer — " + (dryRun ? "DRY RUN (no changes)" : "APPLYING FIXES"));
        System.out.println("=============================================================");

        int totalFixed = 0;
        List<String> report = new ArrayList<>();
        List<String> excluded = new ArrayList<>();   // data-loss rows listed separately

        for (Col c : COLUMNS) {
            String sql = "SELECT " + c.pkCol() + ", " + c.col() +
                         " FROM " + c.table() +
                         " WHERE " + c.col() + " IS NOT NULL";

            List<Object[]> rows = jdbc.query(sql, (rs, rowNum) ->
                new Object[]{ rs.getLong(1), rs.getString(2) }
            );

            for (Object[] row : rows) {
                long pk      = (long) row[0];
                String value = (String) row[1];

                if (value == null) continue;

                // ── Exclusion check (confirmed data-loss rows) ────────
                if (isExcluded(c, pk)) {
                    if (looksLikeMojibake(value) || value.contains("?")) {
                        String note = String.format(
                            "[EXCLUDED — data loss] [%s.%s] PK=%d | VALUE: %s",
                            c.table(), c.col(), pk, truncate(value, 120)
                        );
                        System.out.println(note);
                        excluded.add(note);
                    }
                    continue;   // never auto-fix these rows
                }

                String fixed = tryFixMojibake(value);
                if (fixed == null) continue;  // not corrupted / false-positive

                totalFixed++;
                String line = String.format(
                    "[%s.%s] PK=%d | OLD: %-60s | FIX: %s",
                    c.table(), c.col(), pk,
                    truncate(value, 60), fixed
                );
                System.out.println(line);
                report.add(line);

                if (!dryRun) {
                    String updateSql = "UPDATE " + c.table() +
                                       " SET " + c.col() + " = ? WHERE " + c.pkCol() + " = ?";
                    jdbc.update(updateSql, fixed, pk);
                }
            }
        }

        System.out.println("-------------------------------------------------------------");
        System.out.println(" Total corrupted cells fixed/previewed: " + totalFixed);
        System.out.println(" Excluded (data-loss, must re-enter manually): " + excluded.size());
        if (!excluded.isEmpty()) {
            System.out.println(" EXCLUDED ROWS:");
            excluded.forEach(System.out::println);
        }
        if (dryRun) {
            System.out.println(" DRY RUN — no rows were modified.");
            System.out.println(" To apply: restart with -Dpolycinema.fix-mojibake=apply");
        } else {
            System.out.println(" APPLIED — " + totalFixed + " cells updated.");
        }
        System.out.println("=============================================================");
    }

    /**
     * Attempt to reverse the UTF-8→Latin1 mis-encoding.
     *
     * Returns the corrected string if the value was corrupted, or null if it
     * looked clean (no fix needed).
     *
     * The heuristic:
     *   1. Re-encode the Java String as Latin-1 bytes (each char → its 0x00–0xFF byte value).
     *   2. Decode those bytes as UTF-8.
     *   3. If decoding succeeds WITHOUT replacement characters (U+FFFD), AND the result
     *      differs from the input, AND the result does not itself look like mojibake,
     *      treat the result as the correct string.
     */
    static String tryFixMojibake(String s) {
        if (s == null || s.isEmpty()) return null;

        // Quick gate: does the string contain any of the signature characters?
        if (!looksLikeMojibake(s)) return null;

        // ── False-positive guard ─────────────────────────────────────────
        // If the string's UTF-16 code units, when re-encoded as Latin-1, then
        // decoded as UTF-8, produce the SAME string, the input was already valid
        // Unicode (it just happens to contain chars like Ã that are legitimate in
        // the text).  Also: if the string contains U+FFFD replacement chars it was
        // already decoded with loss — skip it.
        if (s.contains("\uFFFD")) return null;

        try {
            // Step 1: encode as Latin-1 — each char is treated as a byte value
            byte[] latin1Bytes = s.getBytes(LATIN1);

            // Step 2: decode those bytes as UTF-8
            String candidate = new String(latin1Bytes, UTF8);

            // Step 3: validate
            if (candidate.equals(s)) return null;                   // no change — already correct
            if (candidate.contains("\uFFFD")) return null;           // invalid UTF-8 bytes — data loss
            if (looksLikeMojibake(candidate)) return null;           // still corrupt after decode
            if (!containsVietnamese(candidate)
                    && !candidate.matches(".*[\\p{L}].*")) return null; // sanity: must have letters

            // ── Additional false-positive guard for RapChieu id=3 style ──
            // If re-encoding candidate back via the same process yields the input,
            // AND candidate is already valid readable text, the fix is safe.
            // If candidate.getBytes(LATIN1) → new String(UTF8) != candidate,
            // then we have a second round of encoding — skip.
            try {
                byte[] roundTrip = candidate.getBytes(LATIN1);
                String roundTripped = new String(roundTrip, UTF8);
                if (!roundTripped.equals(candidate) && !roundTripped.contains("\uFFFD")) {
                    // The candidate would itself be corrupted by a second pass — not safe.
                    // This means the input was already correct Unicode, not mojibake.
                    return null;
                }
            } catch (Exception ignored) {}

            return candidate;
        } catch (Exception e) {
            return null; // can't fix — leave it alone
        }
    }

    /**
     * Detects the mojibake signature: presence of Latin-1 characters that
     * represent the lead bytes of multi-byte UTF-8 sequences (0xC0–0xFF range)
     * combined with continuation bytes (0x80–0xBF range rendered as misc Latin chars).
     *
     * The key indicator: 'Ã' (U+00C3) is the Latin-1 rendering of the UTF-8
     * lead byte 0xC3, which is the lead byte for Vietnamese vowels like ã, â, á,
     * and accented forms like ầ, ấ, ẩ, ẫ, ậ.
     */
    static boolean looksLikeMojibake(String s) {
        if (s == null) return false;
        for (int i = 0; i < s.length() - 1; i++) {
            char c = s.charAt(i);
            // Lead byte range: 0xC0–0xFF (the Latin Extended chars Ã, Ä, Å, etc.)
            if (c >= 0x00C0 && c <= 0x00FF) {
                char next = s.charAt(i + 1);
                // Continuation byte range: 0x80–0xBF (box-drawing, misc symbols in Latin-1)
                if (next >= 0x0080 && next <= 0x00BF) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Sanity check: does the candidate string contain at least some
     * Vietnamese-range Unicode characters? (U+0300–U+036F combining diacritics,
     * or U+1E00–U+1EFF Latin Extended Additional where Vietnamese sits.)
     */
    static boolean containsVietnamese(String s) {
        for (char c : s.toCharArray()) {
            if ((c >= 0x00C0 && c <= 0x024F) ||   // Latin Extended
                (c >= 0x1E00 && c <= 0x1EFF)) {    // Latin Extended Additional (Vietnamese)
                return true;
            }
        }
        return false;
    }

    private static String truncate(String s, int max) {
        if (s == null) return "(null)";
        return s.length() <= max ? s : s.substring(0, max) + "…";
    }
}
