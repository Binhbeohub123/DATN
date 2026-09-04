package com.polycinema.backend.util;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * Reads Excel cells into plain strings for the lịch chiếu import feature.
 *
 * Split into dedicated readers so a date cell (Ngày chiếu → dd/MM/yyyy) and a
 * time cell (Giờ chiếu → HH:mm) are never mistaken for one another. Excel
 * stores both dates and times as numeric serials flagged "date formatted", so
 * a single generic reader would corrupt the time column (the whole point of
 * BUG 3, where the Giờ Chiếu column became a real Excel TIME cell).
 */
public final class ExcelCellReader {

    private ExcelCellReader() {
    }

    /**
     * Reads a Ngày chiếu cell as dd/MM/yyyy. The Excel display format
     * (mm-dd-yy, dd-mm-yy, …) is cosmetic only — the underlying serial date
     * is format-independent, so getDayOfMonth/getMonthValue always yield the
     * true calendar values. Formatting for display only, never re-parsed.
     */
    public static String readDateCell(Cell c) {
        if (c == null || c.getCellType() == CellType.BLANK) return "";
        if (DateUtil.isCellDateFormatted(c)) {
            Date d = c.getDateCellValue();
            LocalDate ld = d.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            return String.format("%02d/%02d/%04d",
                    ld.getDayOfMonth(), ld.getMonthValue(), ld.getYear());
        }
        // Fallback: non-date cell — return raw string/number
        return readRaw(c);
    }

    /**
     * Reads a Giờ chiếu cell as HH:mm (24h). Handles both:
     *  - a real Excel TIME cell (BUG 3): Excel stores a time as the fractional
     *    part of a day, so the time is read straight off the numeric serial —
     *    timezone-independent and immune to the 1899/1900 date prefix.
     *  - a legacy TEXT cell ('01:30', number format "@"): trim as-is.
     * Returns "" for blank cells so callers can detect a missing value.
     */
    public static String readTimeCell(Cell c) {
        if (c == null || c.getCellType() == CellType.BLANK) return "";
        if (c.getCellType() == CellType.NUMERIC) {
            double serial = c.getNumericCellValue();
            double frac = serial - Math.floor(serial);
            int totalSeconds = (int) Math.round(frac * 86400.0);
            if (totalSeconds >= 86400) totalSeconds = 86399; // rounding edge on 24:00
            int hh = totalSeconds / 3600;
            int mm = (totalSeconds % 3600) / 60;
            return String.format("%02d:%02d", hh, mm);
        }
        // TEXT cell (legacy template) or fallback
        return readRaw(c);
    }

    /**
     * Generic reader used for non-date/non-time columns (Tên Rạp, Tên Phim, …).
     */
    public static String readStringCell(Cell c) {
        if (c == null || c.getCellType() == CellType.BLANK) return "";
        return readRaw(c).trim();
    }

    private static String readRaw(Cell c) {
        if (c == null) return "";
        switch (c.getCellType()) {
            case STRING:  return c.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(c)) {
                    return String.valueOf(c.getNumericCellValue());
                }
                double dv = c.getNumericCellValue();
                return dv == Math.floor(dv) ? String.valueOf((long) dv) : String.valueOf(dv);
            case BOOLEAN: return String.valueOf(c.getBooleanCellValue());
            case FORMULA:
                try { return String.valueOf(c.getStringCellValue()).trim(); }
                catch (Exception ignored) {
                    return String.valueOf(c.getNumericCellValue());
                }
            default: return "";
        }
    }
}
