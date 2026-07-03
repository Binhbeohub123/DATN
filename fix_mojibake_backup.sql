-- ============================================================
-- PolyCinema — Pre-fix Backup Script
-- Run this BEFORE fix_mojibake_dryrun.sql or MojibakeFixer apply.
-- Creates snapshot copies of every table that holds Vietnamese text.
-- Safe to run multiple times (drops and recreates backup tables).
-- ============================================================

USE rapphim3;   -- ← change to rapphim6 if needed
GO

-- ── Drop old backups if they exist (re-runnable) ────────────
IF OBJECT_ID('dbo._bak_RapChieu',   'U') IS NOT NULL DROP TABLE dbo._bak_RapChieu;
IF OBJECT_ID('dbo._bak_PhongChieu', 'U') IS NOT NULL DROP TABLE dbo._bak_PhongChieu;
IF OBJECT_ID('dbo._bak_Phim',       'U') IS NOT NULL DROP TABLE dbo._bak_Phim;
IF OBJECT_ID('dbo._bak_GheNgoi',    'U') IS NOT NULL DROP TABLE dbo._bak_GheNgoi;
IF OBJECT_ID('dbo._bak_NguoiDung',  'U') IS NOT NULL DROP TABLE dbo._bak_NguoiDung;
IF OBJECT_ID('dbo._bak_KhuyenMai',  'U') IS NOT NULL DROP TABLE dbo._bak_KhuyenMai;
IF OBJECT_ID('dbo._bak_SanPham',    'U') IS NOT NULL DROP TABLE dbo._bak_SanPham;
IF OBJECT_ID('dbo._bak_Banner',     'U') IS NOT NULL DROP TABLE dbo._bak_Banner;
GO

-- ── Create snapshot copies ───────────────────────────────────
SELECT * INTO dbo._bak_RapChieu   FROM dbo.RapChieu;
SELECT * INTO dbo._bak_PhongChieu FROM dbo.PhongChieu;
SELECT * INTO dbo._bak_Phim       FROM dbo.Phim;
SELECT * INTO dbo._bak_GheNgoi    FROM dbo.GheNgoi;
SELECT * INTO dbo._bak_NguoiDung  FROM dbo.NguoiDung;
SELECT * INTO dbo._bak_KhuyenMai  FROM dbo.KhuyenMai;
SELECT * INTO dbo._bak_SanPham    FROM dbo.SanPham;
SELECT * INTO dbo._bak_Banner     FROM dbo.Banner;
GO

-- ── Verify row counts match ──────────────────────────────────
SELECT 'RapChieu'   AS Tbl, COUNT(*) AS Live FROM RapChieu   UNION ALL
SELECT 'PhongChieu',        COUNT(*)         FROM PhongChieu UNION ALL
SELECT 'Phim',              COUNT(*)         FROM Phim       UNION ALL
SELECT 'GheNgoi',           COUNT(*)         FROM GheNgoi    UNION ALL
SELECT 'NguoiDung',         COUNT(*)         FROM NguoiDung  UNION ALL
SELECT 'KhuyenMai',         COUNT(*)         FROM KhuyenMai  UNION ALL
SELECT 'SanPham',           COUNT(*)         FROM SanPham    UNION ALL
SELECT 'Banner',            COUNT(*)         FROM Banner;

SELECT '_bak_RapChieu'   AS Tbl, COUNT(*) AS Backup FROM _bak_RapChieu   UNION ALL
SELECT '_bak_PhongChieu',         COUNT(*)           FROM _bak_PhongChieu UNION ALL
SELECT '_bak_Phim',               COUNT(*)           FROM _bak_Phim       UNION ALL
SELECT '_bak_GheNgoi',            COUNT(*)           FROM _bak_GheNgoi    UNION ALL
SELECT '_bak_NguoiDung',          COUNT(*)           FROM _bak_NguoiDung  UNION ALL
SELECT '_bak_KhuyenMai',          COUNT(*)           FROM _bak_KhuyenMai  UNION ALL
SELECT '_bak_SanPham',            COUNT(*)           FROM _bak_SanPham    UNION ALL
SELECT '_bak_Banner',             COUNT(*)           FROM _bak_Banner;
GO

PRINT N'Backup complete. Live vs _bak_ row counts shown above — they must match.';
PRINT N'To restore any table: INSERT INTO <Table> SELECT * FROM _bak_<Table>;';
GO

-- ── RESTORE SCRIPT (commented out — uncomment to roll back) ──
/*
DELETE FROM RapChieu;   INSERT INTO RapChieu   SELECT * FROM _bak_RapChieu;
DELETE FROM PhongChieu; INSERT INTO PhongChieu SELECT * FROM _bak_PhongChieu;
DELETE FROM Phim;       INSERT INTO Phim       SELECT * FROM _bak_Phim;
DELETE FROM GheNgoi;    INSERT INTO GheNgoi    SELECT * FROM _bak_GheNgoi;
DELETE FROM NguoiDung;  INSERT INTO NguoiDung  SELECT * FROM _bak_NguoiDung;
DELETE FROM KhuyenMai;  INSERT INTO KhuyenMai  SELECT * FROM _bak_KhuyenMai;
DELETE FROM SanPham;    INSERT INTO SanPham    SELECT * FROM _bak_SanPham;
DELETE FROM Banner;     INSERT INTO Banner     SELECT * FROM _bak_Banner;
*/
