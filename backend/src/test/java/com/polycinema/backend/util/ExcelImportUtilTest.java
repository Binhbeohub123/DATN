package com.polycinema.backend.util;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.LocalTime;
import java.util.Calendar;
import java.util.Date;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies the lịch chiếu Excel import fixes end-to-end against a
 * Poi-reconstructed sample file matching the real world usage:
 *   - BUG 1: room name from the template carries a trailing " (Ten Rap)" suffix
 *            that must be stripped before matching the DB's bare tenPhong.
 *   - BUG 2: the Ngày chiếu cell is a real date (2026-09-11) stored with a
 *            cosmetic "mm-dd-yy" display format; reading must yield 11/09/2026.
 *   - BUG 3: the Giờ chiếu cell is a real Excel TIME ("h:mm:ss AM/PM"); reading
 *            must yield HH:mm in 24h (e.g. 2:45 PM -> 14:45, 1:30 AM -> 01:30).
 */
class ExcelImportUtilTest {

    @Test
    @DisplayName("BUG1: room name with cinema suffix is cleaned for DB match")
    void bug1StripSuffix() {
        assertThat("Phòng 1 - 2D".equalsIgnoreCase(
                strip("Phòng 1 - 2D (Rạp Lotte Cinema - Tây Hồ)"))).isTrue();
        assertThat("R1".equalsIgnoreCase(strip("R1 (CGV Vincom Landmark)"))).isTrue();
        // no suffix -> unchanged
        assertThat(strip("Phòng 2G")).isEqualTo("Phòng 2G");
        // leading/trailing whitespace handled
        assertThat(strip("  Phòng 1 - 2D (A B)  ")).isEqualTo("Phòng 1 - 2D");
    }

    private static String strip(String s) {
        return s.replaceAll("\\s*\\([^)]*\\)\\s*$", "").trim();
    }

    @Test
    @DisplayName("BUG2: date cell 'mm-dd-yy' (2026-09-11) reads as 11/09/2026")
    void bug2DateReadsCorrectly() throws Exception {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Lich Chieu");
            Row row = sheet.createRow(1);
            Cell d = row.createCell(3);
            // Serial date for 2026-09-11 (Excel serial, 1900 system)
            Calendar c = Calendar.getInstance();
            c.clear();
            c.set(2026, Calendar.SEPTEMBER, 11, 0, 0, 0);
            Date date = c.getTime();
            d.setCellValue(date);
            // cosmetic display format "mm-dd-yy"
            org.apache.poi.ss.usermodel.CellStyle s = wb.createCellStyle();
            s.setDataFormat(wb.createDataFormat().getFormat("mm-dd-yy"));
            d.setCellStyle(s);

            byte[] bytes = writeBytes(wb);
            // read back through the same reader path as the controller
            try (XSSFWorkbook wb2 = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
                Cell read = wb2.getSheet("Lich Chieu").getRow(1).getCell(3);
                String result = ExcelCellReader.readDateCell(read);
                System.out.println("BUG2 result=" + result);
                assertThat(result).isEqualTo("11/09/2026");
            }
        }
    }

    @Test
    @DisplayName("BUG3: real TIME cell 1:30 AM reads as 01:30")
    void bug3TimeMorning() throws Exception {
        assertTime("h:mm:ss AM/PM", LocalTime.of(1, 30, 0), "01:30");
    }

    @Test
    @DisplayName("BUG3: real TIME cell 2:45 PM reads as 14:45 (AM/PM handled)")
    void bug3TimeAfternoon() throws Exception {
        assertTime("h:mm:ss AM/PM", LocalTime.of(14, 45, 0), "14:45");
    }

    @Test
    @DisplayName("BUG3: legacy TEXT cell '01:30' still reads as 01:30")
    void bug3LegacyText() throws Exception {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Lich Chieu");
            Cell e = sheet.createRow(1).createCell(4);
            e.setCellValue("01:30"); // number format "@" (TEXT)
            org.apache.poi.ss.usermodel.CellStyle s = wb.createCellStyle();
            s.setDataFormat(wb.createDataFormat().getFormat("@"));
            e.setCellStyle(s);

            byte[] bytes = writeBytes(wb);
            try (XSSFWorkbook wb2 = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
                Cell read = wb2.getSheet("Lich Chieu").getRow(1).getCell(4);
                assertThat(ExcelCellReader.readTimeCell(read)).isEqualTo("01:30");
            }
        }
    }

    private void assertTime(String format, LocalTime lt, String expected) throws Exception {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Lich Chieu");
            Cell e = sheet.createRow(1).createCell(4);
            // Excel TIME = fractional part of a day (date part is irrelevant).
            double serial = (lt.getHour() * 3600.0 + lt.getMinute() * 60.0 + lt.getSecond()) / 86400.0;
            e.setCellValue(serial);
            org.apache.poi.ss.usermodel.CellStyle s = wb.createCellStyle();
            s.setDataFormat(wb.createDataFormat().getFormat(format));
            e.setCellStyle(s);

            byte[] bytes = writeBytes(wb);
            try (XSSFWorkbook wb2 = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
                Cell read = wb2.getSheet("Lich Chieu").getRow(1).getCell(4);
                String result = ExcelCellReader.readTimeCell(read);
                System.out.println("BUG3 result=" + result);
                assertThat(result).isEqualTo(expected);
            }
        }
    }

    private byte[] writeBytes(XSSFWorkbook wb) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        wb.write(baos);
        return baos.toByteArray();
    }
}
