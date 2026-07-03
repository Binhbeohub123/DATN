-- ============================================================
-- PolyCinema — Mojibake Dry-Run Preview Script
-- Database: rapphim3 / rapphim6 (adjust USE statement below)
-- Purpose: SCAN ONLY — no data is modified.
--          Shows every row/column where Vietnamese text appears
--          to be stored as UTF-8 bytes mis-interpreted as Latin1.
--
-- HOW TO USE:
--   1. Run this script in SSMS or Azure Data Studio.
--   2. Review the output — OldValue vs ProposedFix columns.
--   3. If output looks correct, run fix_mojibake_apply.sql (to be
--      generated after you confirm this preview).
--
-- MOJIBAKE SIGNATURE:
--   Vietnamese UTF-8 characters stored as Latin1 produce
--   characteristic sequences. The key marker bytes are:
--     à, á, â, ã, ä, å, æ, ç, è, é, ê, ë  (0xC3 xx — 2-byte UTF-8 lead)
--     Ã (0xC3), Ä (0xC4), Å (0xC5), Æ (0xC6)
--     followed by bytes 0x80-0xBF rendered as box-drawing or Latin
--     Extended chars like ┼, ║, ╗, »,  ¬, etc.
--   The most reliable single detector for Vietnamese mojibake:
--     NCHAR(0xC3) = 'Ã'  followed by chars in range 0x80-0xBF
--   We check for the most common broken sequences.
-- ============================================================

USE rapphim6;   -- active database from application.properties JDBC URL default
GO

-- ── Detection helper: does the string contain mojibake? ──────
-- We look for the UTF-8 2-byte lead byte 0xC3 (rendered as Ã in Latin1)
-- which is the lead byte for all Vietnamese tonal characters in the
-- range U+00C0–U+00FF.  Any legitimate Vietnamese text stored as
-- proper Unicode in NVARCHAR will NOT contain the literal character 'Ã'
-- followed by a continuation byte rendered as a Latin1 symbol.
-- Additional markers: 'á»' (0xC3 0xA3 = ã, but in Vietnamese context),
-- 'â€', 'Ã¢', 'Ä', box-drawing chars ║ ┼ ╗ etc.

DECLARE @markers NVARCHAR(200) =
    N'Ã|ß|â€|á»|á»™|á»|Ä|Ã|ÃÂ|HÃ|Ná»|Rß|Sá»|Phß|Thß|Bß|Cß|Tß|Dß|Lß|Nß|Mß|Vß';
-- (The pipe-separated list above is for documentation only; actual checks
--  use CHARINDEX against individual known-corrupt substrings.)

PRINT N'';
PRINT N'============================================================';
PRINT N'  PolyCinema Mojibake Dry-Run — SELECT only, no changes';
PRINT N'============================================================';
PRINT N'';

-- ── Inline function: detect mojibake ────────────────────────
-- We define detection as: string contains Ã (U+00C3) OR ß (U+00DF)
-- OR the 3-byte sequence Ã« / Ã¡ etc., OR box-drawing chars.
-- For SQL Server we use CHARINDEX on the literal Unicode chars.

-- ============================================================
-- TABLE: RapChieu
-- ============================================================
PRINT N'--- TABLE: RapChieu ---';
SELECT
    'RapChieu'            AS [TableName],
    Id                    AS [PK],
    'TenRap'              AS [Column],
    TenRap                AS [OldValue],
    -- ProposedFix: reverse the double-encoding.
    -- We cannot auto-reverse in T-SQL without a CLR function, so we
    -- flag it for manual review.  The Java migration (EncodingFixer)
    -- will produce the actual corrected string.
    N'[See Java migration output]' AS [ProposedFix]
FROM RapChieu
WHERE
    CHARINDEX(NCHAR(0x00C3), TenRap) > 0   -- Ã  (UTF-8 lead byte 0xC3 as Latin1)
    OR CHARINDEX(NCHAR(0x00DF), TenRap) > 0 -- ß  (0xC3 0x9F → ß in Latin1 = part of UTF-8 seq)
    OR CHARINDEX(N'á»', TenRap) > 0         -- common Vietnamese suffix fragment
    OR CHARINDEX(N'Ä', TenRap)  > 0
    OR CHARINDEX(N'â€', TenRap) > 0

UNION ALL

SELECT
    'RapChieu', Id, 'DiaChi', DiaChi, N'[See Java migration output]'
FROM RapChieu
WHERE
    CHARINDEX(NCHAR(0x00C3), DiaChi) > 0
    OR CHARINDEX(NCHAR(0x00DF), DiaChi) > 0
    OR CHARINDEX(N'á»', DiaChi) > 0
    OR CHARINDEX(N'Ä', DiaChi)  > 0
    OR CHARINDEX(N'â€', DiaChi) > 0;

-- ============================================================
-- TABLE: PhongChieu
-- ============================================================
PRINT N'--- TABLE: PhongChieu ---';
SELECT
    'PhongChieu', Id, 'TenPhong', TenPhong, N'[See Java migration output]'
FROM PhongChieu
WHERE
    CHARINDEX(NCHAR(0x00C3), TenPhong) > 0
    OR CHARINDEX(NCHAR(0x00DF), TenPhong) > 0
    OR CHARINDEX(N'á»', TenPhong) > 0
    OR CHARINDEX(N'Ä', TenPhong)  > 0
    OR CHARINDEX(N'â€', TenPhong) > 0;

-- ============================================================
-- TABLE: Phim
-- ============================================================
PRINT N'--- TABLE: Phim ---';
SELECT
    'Phim', Id, 'TenPhim', TenPhim, N'[See Java migration output]'
FROM Phim
WHERE
    CHARINDEX(NCHAR(0x00C3), TenPhim) > 0
    OR CHARINDEX(NCHAR(0x00DF), TenPhim) > 0
    OR CHARINDEX(N'á»', TenPhim) > 0
    OR CHARINDEX(N'Ä', TenPhim)  > 0

UNION ALL

SELECT 'Phim', Id, 'TheLoai', TheLoai, N'[See Java migration]'
FROM Phim
WHERE TheLoai IS NOT NULL AND (
    CHARINDEX(NCHAR(0x00C3), TheLoai) > 0
    OR CHARINDEX(NCHAR(0x00DF), TheLoai) > 0
    OR CHARINDEX(N'á»', TheLoai) > 0)

UNION ALL

SELECT 'Phim', Id, 'MoTa', LEFT(MoTa,120), N'[See Java migration]'
FROM Phim
WHERE MoTa IS NOT NULL AND (
    CHARINDEX(NCHAR(0x00C3), MoTa) > 0
    OR CHARINDEX(NCHAR(0x00DF), MoTa) > 0
    OR CHARINDEX(N'á»', MoTa) > 0)

UNION ALL

SELECT 'Phim', Id, 'DaoDien', DaoDien, N'[See Java migration]'
FROM Phim
WHERE DaoDien IS NOT NULL AND (
    CHARINDEX(NCHAR(0x00C3), DaoDien) > 0
    OR CHARINDEX(NCHAR(0x00DF), DaoDien) > 0)

UNION ALL

SELECT 'Phim', Id, 'DienVienChinh', DienVienChinh, N'[See Java migration]'
FROM Phim
WHERE DienVienChinh IS NOT NULL AND (
    CHARINDEX(NCHAR(0x00C3), DienVienChinh) > 0
    OR CHARINDEX(NCHAR(0x00DF), DienVienChinh) > 0)

UNION ALL

SELECT 'Phim', Id, 'NgonNgu', NgonNgu, N'[See Java migration]'
FROM Phim
WHERE NgonNgu IS NOT NULL AND (
    CHARINDEX(NCHAR(0x00C3), NgonNgu) > 0
    OR CHARINDEX(NCHAR(0x00DF), NgonNgu) > 0);

-- ============================================================
-- TABLE: GheNgoi
-- ============================================================
PRINT N'--- TABLE: GheNgoi ---';
SELECT
    'GheNgoi', Id, 'LoaiGhe', LoaiGhe, N'[See Java migration]'
FROM GheNgoi
WHERE LoaiGhe IS NOT NULL AND (
    CHARINDEX(NCHAR(0x00C3), LoaiGhe) > 0
    OR CHARINDEX(NCHAR(0x00DF), LoaiGhe) > 0
    OR CHARINDEX(N'á»', LoaiGhe) > 0);

-- ============================================================
-- TABLE: NguoiDung
-- ============================================================
PRINT N'--- TABLE: NguoiDung ---';
SELECT
    'NguoiDung', Id, 'HoTen', HoTen, N'[See Java migration]'
FROM NguoiDung
WHERE HoTen IS NOT NULL AND (
    CHARINDEX(NCHAR(0x00C3), HoTen) > 0
    OR CHARINDEX(NCHAR(0x00DF), HoTen) > 0
    OR CHARINDEX(N'á»', HoTen) > 0);

-- ============================================================
-- TABLE: KhuyenMai
-- ============================================================
PRINT N'--- TABLE: KhuyenMai ---';
SELECT
    'KhuyenMai', Id, 'TenKhuyenMai', TenKhuyenMai, N'[See Java migration]'
FROM KhuyenMai
WHERE TenKhuyenMai IS NOT NULL AND (
    CHARINDEX(NCHAR(0x00C3), TenKhuyenMai) > 0
    OR CHARINDEX(NCHAR(0x00DF), TenKhuyenMai) > 0
    OR CHARINDEX(N'á»', TenKhuyenMai) > 0)

UNION ALL

SELECT 'KhuyenMai', Id, 'MoTa', LEFT(MoTa,120), N'[See Java migration]'
FROM KhuyenMai
WHERE MoTa IS NOT NULL AND (
    CHARINDEX(NCHAR(0x00C3), MoTa) > 0
    OR CHARINDEX(NCHAR(0x00DF), MoTa) > 0
    OR CHARINDEX(N'á»', MoTa) > 0);

-- ============================================================
-- TABLE: SanPham
-- ============================================================
PRINT N'--- TABLE: SanPham ---';
SELECT
    'SanPham', Id, 'TenSanPham', TenSanPham, N'[See Java migration]'
FROM SanPham
WHERE TenSanPham IS NOT NULL AND (
    CHARINDEX(NCHAR(0x00C3), TenSanPham) > 0
    OR CHARINDEX(NCHAR(0x00DF), TenSanPham) > 0
    OR CHARINDEX(N'á»', TenSanPham) > 0)

UNION ALL

SELECT 'SanPham', Id, 'MoTa', LEFT(MoTa,120), N'[See Java migration]'
FROM SanPham
WHERE MoTa IS NOT NULL AND (
    CHARINDEX(NCHAR(0x00C3), MoTa) > 0
    OR CHARINDEX(NCHAR(0x00DF), MoTa) > 0
    OR CHARINDEX(N'á»', MoTa) > 0);

-- ============================================================
-- TABLE: Banner
-- ============================================================
PRINT N'--- TABLE: Banner ---';
SELECT
    'Banner', Id, 'TieuDe', TieuDe, N'[See Java migration]'
FROM Banner
WHERE TieuDe IS NOT NULL AND (
    CHARINDEX(NCHAR(0x00C3), TieuDe) > 0
    OR CHARINDEX(NCHAR(0x00DF), TieuDe) > 0
    OR CHARINDEX(N'á»', TieuDe) > 0);

-- ============================================================
-- SUMMARY COUNT
-- ============================================================
PRINT N'';
PRINT N'--- SUMMARY: total corrupted cells detected ---';

SELECT SUM(cnt) AS TotalCorruptedCells FROM (
    SELECT COUNT(*) AS cnt FROM RapChieu  WHERE CHARINDEX(NCHAR(0x00C3),TenRap)>0 OR CHARINDEX(NCHAR(0x00DF),TenRap)>0 OR CHARINDEX(N'á»',TenRap)>0
    UNION ALL
    SELECT COUNT(*) FROM RapChieu  WHERE CHARINDEX(NCHAR(0x00C3),DiaChi)>0 OR CHARINDEX(NCHAR(0x00DF),DiaChi)>0
    UNION ALL
    SELECT COUNT(*) FROM PhongChieu WHERE CHARINDEX(NCHAR(0x00C3),TenPhong)>0 OR CHARINDEX(NCHAR(0x00DF),TenPhong)>0
    UNION ALL
    SELECT COUNT(*) FROM Phim WHERE CHARINDEX(NCHAR(0x00C3),TenPhim)>0 OR CHARINDEX(NCHAR(0x00DF),TenPhim)>0
    UNION ALL
    SELECT COUNT(*) FROM Phim WHERE TheLoai IS NOT NULL AND (CHARINDEX(NCHAR(0x00C3),TheLoai)>0 OR CHARINDEX(NCHAR(0x00DF),TheLoai)>0)
    UNION ALL
    SELECT COUNT(*) FROM Phim WHERE MoTa IS NOT NULL AND (CHARINDEX(NCHAR(0x00C3),MoTa)>0 OR CHARINDEX(NCHAR(0x00DF),MoTa)>0)
    UNION ALL
    SELECT COUNT(*) FROM GheNgoi WHERE LoaiGhe IS NOT NULL AND (CHARINDEX(NCHAR(0x00C3),LoaiGhe)>0 OR CHARINDEX(NCHAR(0x00DF),LoaiGhe)>0)
    UNION ALL
    SELECT COUNT(*) FROM NguoiDung WHERE HoTen IS NOT NULL AND (CHARINDEX(NCHAR(0x00C3),HoTen)>0 OR CHARINDEX(NCHAR(0x00DF),HoTen)>0)
    UNION ALL
    SELECT COUNT(*) FROM KhuyenMai WHERE TenKhuyenMai IS NOT NULL AND (CHARINDEX(NCHAR(0x00C3),TenKhuyenMai)>0 OR CHARINDEX(NCHAR(0x00DF),TenKhuyenMai)>0)
    UNION ALL
    SELECT COUNT(*) FROM SanPham WHERE TenSanPham IS NOT NULL AND (CHARINDEX(NCHAR(0x00C3),TenSanPham)>0 OR CHARINDEX(NCHAR(0x00DF),TenSanPham)>0)
    UNION ALL
    SELECT COUNT(*) FROM Banner WHERE TieuDe IS NOT NULL AND (CHARINDEX(NCHAR(0x00C3),TieuDe)>0 OR CHARINDEX(NCHAR(0x00DF),TieuDe)>0)
) t;

PRINT N'';
PRINT N'DRY RUN COMPLETE — no rows were modified.';
PRINT N'Share the results above, then confirm to proceed with the Java migration.';
GO
