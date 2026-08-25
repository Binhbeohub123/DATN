SET NOCOUNT ON;
IF OBJECT_ID('dbo.GheNgoi_backup_20260824') IS NOT NULL DROP TABLE dbo.GheNgoi_backup_20260824;
IF OBJECT_ID('dbo.PhongChieu_backup_20260824') IS NOT NULL DROP TABLE dbo.PhongChieu_backup_20260824;
SELECT * INTO GheNgoi_backup_20260824 FROM GheNgoi;
SELECT * INTO PhongChieu_backup_20260824 FROM PhongChieu;
PRINT 'Backup OK: GheNgoi_backup_20260824, PhongChieu_backup_20260824';

BEGIN TRY
BEGIN TRAN;

SET IDENTITY_INSERT GheNgoi ON;
INSERT INTO GheNgoi (Id, PhongChieuId, HangGhe, SoGhe, LoaiGhe, HeSoGia) VALUES
(1,1,N'A',1,N'thường',1.00),
(2,1,N'A',2,N'thường',1.00),
(230,1,N'A',3,N'thường',1.00),
(232,1,N'A',5,N'thường',1.00),
(233,1,N'A',6,N'thường',1.00),
(234,1,N'A',7,N'thường',1.00),
(235,1,N'A',8,N'thường',1.00),
(236,1,N'A',9,N'thường',1.00),
(237,1,N'A',10,N'thường',1.00),
(3,1,N'B',1,N'vip',1.50),
(4,1,N'B',2,N'vip',1.50),
(239,1,N'B',4,N'thường',1.00),
(240,1,N'B',5,N'thường',1.00),
(241,1,N'B',6,N'thường',1.00),
(242,1,N'B',7,N'thường',1.00),
(243,1,N'B',8,N'thường',1.00),
(244,1,N'B',9,N'thường',1.00),
(245,1,N'B',10,N'thường',1.00),
(246,1,N'C',1,N'thường',1.00),
(248,1,N'C',3,N'thường',1.00),
(249,1,N'C',4,N'thường',1.00),
(250,1,N'C',5,N'thường',1.00),
(251,1,N'C',6,N'thường',1.00),
(252,1,N'C',7,N'thường',1.00),
(253,1,N'C',8,N'thường',1.00),
(254,1,N'C',9,N'thường',1.00),
(256,1,N'D',1,N'vip',1.50),
(258,1,N'D',3,N'vip',1.50),
(259,1,N'D',4,N'vip',1.50),
(260,1,N'D',5,N'vip',1.50),
(261,1,N'D',6,N'vip',1.50),
(264,1,N'D',9,N'vip',1.50),
(265,1,N'D',10,N'vip',1.50),
(329,1,N'E',1,N'cặp đôi',2.00),
(330,1,N'E',2,N'cặp đôi',2.00),
(331,1,N'E',3,N'cặp đôi',2.00),
(332,1,N'E',4,N'cặp đôi',2.00),
(333,1,N'E',5,N'cặp đôi',2.00),
(334,1,N'E',6,N'cặp đôi',2.00),
(335,1,N'E',7,N'cặp đôi',2.00),
(336,1,N'E',8,N'cặp đôi',2.00),
(337,1,N'E',9,N'cặp đôi',2.00),
(338,1,N'E',10,N'cặp đôi',2.00);
SET IDENTITY_INSERT GheNgoi OFF;
PRINT 'Step1 inserted rows = ' + CAST(@@ROWCOUNT AS varchar);

UPDATE GheNgoi SET LoaiGhe = N'trống', HeSoGia = 0
WHERE PhongChieuId = 1 AND SoGhe IN (8,9)
  AND HangGhe IN (N'A',N'B',N'C',N'D')
  AND LoaiGhe <> N'trống';
PRINT 'Step2 flipped to trong = ' + CAST(@@ROWCOUNT AS varchar);

UPDATE PhongChieu SET SucChua = 42 WHERE Id = 1;

DECLARE @total int, @trong int, @eCouple int, @avail int, @badHang int;
SELECT @total   = COUNT(*) FROM GheNgoi WHERE PhongChieuId = 1;
SELECT @trong   = COUNT(*) FROM GheNgoi WHERE PhongChieuId = 1 AND LoaiGhe = N'trống';
SELECT @eCouple = COUNT(*) FROM GheNgoi WHERE PhongChieuId = 1 AND HangGhe = N'E' AND LoaiGhe = N'cặp đôi';
SELECT @avail   = COUNT(*) FROM GheNgoi WHERE PhongChieuId = 1 AND LoaiGhe <> N'trống';
SELECT @badHang = COUNT(*) FROM (SELECT HangGhe, COUNT(*) c FROM GheNgoi WHERE PhongChieuId = 1 GROUP BY HangGhe HAVING COUNT(*) <> 10) x;

PRINT 'VERIFY total=50?      -> ' + CAST(@total AS varchar);
PRINT 'VERIFY trong=8?       -> ' + CAST(@trong AS varchar);
PRINT 'VERIFY E couple=10?   -> ' + CAST(@eCouple AS varchar);
PRINT 'VERIFY available=42?  -> ' + CAST(@avail AS varchar);
PRINT 'VERIFY moi hang =10?  -> badHang=' + CAST(@badHang AS varchar);

IF @total = 50 AND @trong = 8 AND @eCouple = 10 AND @avail = 42 AND @badHang = 0
BEGIN
    COMMIT TRAN;
    PRINT 'COMMITTED - tat ca dieu kien verify dung';
END
ELSE
BEGIN
    ROLLBACK TRAN;
    RAISERROR('VERIFY FAIL - da ROLLBACK. total=%d trong=%d eCouple=%d avail=%d badHang=%d',
              16, 1, @total, @trong, @eCouple, @avail, @badHang);
END
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0 ROLLBACK TRAN;
    PRINT 'CATCH - ROLLBACK done. ERROR: ' + ERROR_MESSAGE();
    THROW;
END CATCH;

PRINT '--- Trang thai cuoi cung theo hang ---';
SELECT HangGhe,
       SUM(CASE WHEN LoaiGhe=N'trống' THEN 1 ELSE 0 END) AS Trong,
       SUM(CASE WHEN LoaiGhe=N'vip' THEN 1 ELSE 0 END) AS Vip,
       SUM(CASE WHEN LoaiGhe=N'cặp đôi' THEN 1 ELSE 0 END) AS CapDoi,
       SUM(CASE WHEN LoaiGhe=N'thường' THEN 1 ELSE 0 END) AS Thuong,
       COUNT(*) AS Tong
FROM GheNgoi WHERE PhongChieuId = 1 GROUP BY HangGhe ORDER BY HangGhe;
SELECT SucChua FROM PhongChieu WHERE Id = 1;
