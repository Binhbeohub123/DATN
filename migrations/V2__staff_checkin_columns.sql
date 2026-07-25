-- =====================================================================
-- Migration: V2 — Add staff & check-in columns to DatVe
-- Purpose : Support counter-sale attribution and ticket check-in (Phase 1)
-- Database: SQL Server (rapphim6)
-- =====================================================================

USE [rapphim6]
GO

-- 1) NhanVienId — staff member who created a counter sale
IF NOT EXISTS (
    SELECT 1 FROM sys.columns
    WHERE object_id = OBJECT_ID(N'dbo.DatVe') AND name = N'NhanVienId'
)
BEGIN
    ALTER TABLE [dbo].[DatVe]
        ADD [NhanVienId] [bigint] NULL;

    ALTER TABLE [dbo].[DatVe]
        ADD CONSTRAINT [FK_DatVe_NhanVien]
        FOREIGN KEY ([NhanVienId]) REFERENCES [dbo].[NguoiDung]([Id]);
END
GO

-- 2) TrangThaiCheckIn — check-in status
IF NOT EXISTS (
    SELECT 1 FROM sys.columns
    WHERE object_id = OBJECT_ID(N'dbo.DatVe') AND name = N'TrangThaiCheckIn'
)
BEGIN
    ALTER TABLE [dbo].[DatVe]
        ADD [TrangThaiCheckIn] [nvarchar](20) NOT NULL
        CONSTRAINT [DF_DatVe_TrangThaiCheckIn] DEFAULT N'chưa sử dụng';
END
GO

-- 3) ThoiGianCheckIn — timestamp of check-in scan
IF NOT EXISTS (
    SELECT 1 FROM sys.columns
    WHERE object_id = OBJECT_ID(N'dbo.DatVe') AND name = N'ThoiGianCheckIn'
)
BEGIN
    ALTER TABLE [dbo].[DatVe]
        ADD [ThoiGianCheckIn] [datetime2](7) NULL;
END
GO

-- 4) NhanVienCheckInId — staff member who scanned the ticket
IF NOT EXISTS (
    SELECT 1 FROM sys.columns
    WHERE object_id = OBJECT_ID(N'dbo.DatVe') AND name = N'NhanVienCheckInId'
)
BEGIN
    ALTER TABLE [dbo].[DatVe]
        ADD [NhanVienCheckInId] [bigint] NULL;

    ALTER TABLE [dbo].[DatVe]
        ADD CONSTRAINT [FK_DatVe_NhanVienCheckIn]
        FOREIGN KEY ([NhanVienCheckInId]) REFERENCES [dbo].[NguoiDung]([Id]);
END
GO

-- 5) MaQR — already exists in schema as NVARCHAR(MAX), but add unique index
--    Note: MaQR column already exists per NEWS.sql. We only add a filtered
--    unique index if it doesn't exist yet (MAX columns need special handling
--    so we create a computed hash column for uniqueness).
IF NOT EXISTS (
    SELECT 1 FROM sys.columns
    WHERE object_id = OBJECT_ID(N'dbo.DatVe') AND name = N'MaQR_Hash'
)
BEGIN
    -- Add a persisted computed column with hash for unique constraint on NVARCHAR(MAX)
    ALTER TABLE [dbo].[DatVe]
        ADD [MaQR_Hash] AS (CONVERT(VARBINARY(32), HASHBYTES('SHA2_256', [MaQR]))) PERSISTED;
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE object_id = OBJECT_ID(N'dbo.DatVe') AND name = N'UX_DatVe_MaQR'
)
BEGIN
    CREATE UNIQUE NONCLUSTERED INDEX [UX_DatVe_MaQR]
        ON [dbo].[DatVe] ([MaQR_Hash])
        WHERE [MaQR_Hash] IS NOT NULL;
END
GO

-- =====================================================================
-- ROLLBACK / DOWN script (run manually if needed)
-- =====================================================================
-- DROP INDEX IF EXISTS [UX_DatVe_MaQR] ON [dbo].[DatVe];
-- ALTER TABLE [dbo].[DatVe] DROP COLUMN IF EXISTS [MaQR_Hash];
-- ALTER TABLE [dbo].[DatVe] DROP CONSTRAINT IF EXISTS [FK_DatVe_NhanVienCheckIn];
-- ALTER TABLE [dbo].[DatVe] DROP COLUMN IF EXISTS [NhanVienCheckInId];
-- ALTER TABLE [dbo].[DatVe] DROP COLUMN IF EXISTS [ThoiGianCheckIn];
-- ALTER TABLE [dbo].[DatVe] DROP CONSTRAINT IF EXISTS [DF_DatVe_TrangThaiCheckIn];
-- ALTER TABLE [dbo].[DatVe] DROP COLUMN IF EXISTS [TrangThaiCheckIn];
-- ALTER TABLE [dbo].[DatVe] DROP CONSTRAINT IF EXISTS [FK_DatVe_NhanVien];
-- ALTER TABLE [dbo].[DatVe] DROP COLUMN IF EXISTS [NhanVienId];