-- Fix GheNgoi inserts with proper NVARCHAR N'' prefix for Vietnamese values
USE rapphim3;
GO

DECLARE @P1 INT = (SELECT TOP 1 Id FROM PhongChieu WHERE TenPhong = N'Phòng 1 – 2D');
DECLARE @P2 INT = (SELECT TOP 1 Id FROM PhongChieu WHERE TenPhong = N'Phòng 2 – 3D');

-- Check allowed values from constraint (display only)
SELECT name, definition FROM sys.check_constraints WHERE OBJECT_NAME(parent_object_id) = 'GheNgoi';
GO

-- Insert Phòng 1 seats using N'' prefix for all Vietnamese strings
DECLARE @P1b INT = (SELECT TOP 1 Id FROM PhongChieu WHERE TenPhong = N'Phòng 1 – 2D');

IF NOT EXISTS (SELECT 1 FROM GheNgoi WHERE PhongChieuId = @P1b AND HangGhe = 'A' AND SoGhe = 1)
BEGIN
    -- Rows A,B,C = thường (1.00)
    INSERT INTO GheNgoi (PhongChieuId, HangGhe, SoGhe, LoaiGhe, HeSoGia) SELECT @P1b,'A',n,N'thường',1.00 FROM (VALUES(1),(2),(3),(4),(5),(6),(7),(8),(9),(10)) v(n);
    INSERT INTO GheNgoi (PhongChieuId, HangGhe, SoGhe, LoaiGhe, HeSoGia) SELECT @P1b,'B',n,N'thường',1.00 FROM (VALUES(1),(2),(3),(4),(5),(6),(7),(8),(9),(10)) v(n);
    INSERT INTO GheNgoi (PhongChieuId, HangGhe, SoGhe, LoaiGhe, HeSoGia) SELECT @P1b,'C',n,N'thường',1.00 FROM (VALUES(1),(2),(3),(4),(5),(6),(7),(8),(9),(10)) v(n);
    -- Row D = vip (1.50)
    INSERT INTO GheNgoi (PhongChieuId, HangGhe, SoGhe, LoaiGhe, HeSoGia) SELECT @P1b,'D',n,N'vip',1.50 FROM (VALUES(1),(2),(3),(4),(5),(6),(7),(8),(9),(10)) v(n);
    -- Row E = cặp đôi (2.00)
    INSERT INTO GheNgoi (PhongChieuId, HangGhe, SoGhe, LoaiGhe, HeSoGia) SELECT @P1b,'E',n,N'cặp đôi',2.00 FROM (VALUES(1),(2),(3),(4),(5),(6),(7),(8),(9),(10)) v(n);
END;

-- Insert Phòng 2 seats
DECLARE @P2b INT = (SELECT TOP 1 Id FROM PhongChieu WHERE TenPhong = N'Phòng 2 – 3D');

IF NOT EXISTS (SELECT 1 FROM GheNgoi WHERE PhongChieuId = @P2b AND HangGhe = 'A' AND SoGhe = 1)
BEGIN
    INSERT INTO GheNgoi (PhongChieuId, HangGhe, SoGhe, LoaiGhe, HeSoGia) SELECT @P2b,'A',n,N'thường',1.00 FROM (VALUES(1),(2),(3),(4),(5),(6),(7),(8),(9),(10)) v(n);
    INSERT INTO GheNgoi (PhongChieuId, HangGhe, SoGhe, LoaiGhe, HeSoGia) SELECT @P2b,'B',n,N'thường',1.00 FROM (VALUES(1),(2),(3),(4),(5),(6),(7),(8),(9),(10)) v(n);
    INSERT INTO GheNgoi (PhongChieuId, HangGhe, SoGhe, LoaiGhe, HeSoGia) SELECT @P2b,'C',n,N'thường',1.00 FROM (VALUES(1),(2),(3),(4),(5),(6),(7),(8),(9),(10)) v(n);
    INSERT INTO GheNgoi (PhongChieuId, HangGhe, SoGhe, LoaiGhe, HeSoGia) SELECT @P2b,'D',n,N'vip',1.50 FROM (VALUES(1),(2),(3),(4),(5),(6),(7),(8),(9),(10)) v(n);
    INSERT INTO GheNgoi (PhongChieuId, HangGhe, SoGhe, LoaiGhe, HeSoGia) SELECT @P2b,'E',n,N'cặp đôi',2.00 FROM (VALUES(1),(2),(3),(4),(5),(6),(7),(8),(9),(10)) v(n);
END;
GO

SELECT 'GheNgoi count P1' AS info, COUNT(*) AS cnt FROM GheNgoi WHERE PhongChieuId = (SELECT TOP 1 Id FROM PhongChieu WHERE TenPhong = N'Phòng 1 – 2D')
UNION ALL
SELECT 'GheNgoi count P2', COUNT(*) FROM GheNgoi WHERE PhongChieuId = (SELECT TOP 1 Id FROM PhongChieu WHERE TenPhong = N'Phòng 2 – 3D');
GO
