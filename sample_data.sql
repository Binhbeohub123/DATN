-- ============================================================
-- PolyCinema Sample Data
-- Target: Azure SQL Server (rapphim3 database)
-- Run: paste into Azure Data Studio or SQL Server Management Studio
-- ============================================================
USE rapphim3;
GO

-- ============================================================
-- 1. BANNER (3 active banners)
-- ============================================================
IF NOT EXISTS (SELECT 1 FROM Banner WHERE TieuDe = N'Avengers: Doomsday – Đặt vé ngay')
INSERT INTO Banner (TieuDe, HinhAnh, LinkUrl, ThuTu, NgayBatDau, NgayKetThuc, DangHoatDong, NgayTao)
VALUES
(N'Avengers: Doomsday – Đặt vé ngay',
 'https://picsum.photos/seed/banner1/1280/480',
 '/phim/1', 1, '2026-01-01', '2026-12-31', 1, GETDATE()),
(N'Lật Mặt 8 – Siêu phẩm Việt Nam',
 'https://picsum.photos/seed/banner2/1280/480',
 '/phim/2', 2, '2026-01-01', '2026-12-31', 1, GETDATE()),
(N'Ưu đãi thứ 3 – Giảm 30% tất cả suất chiếu',
 'https://picsum.photos/seed/banner3/1280/480',
 '/khuyen-mai', 3, '2026-01-01', '2026-12-31', 1, GETDATE());
GO

-- ============================================================
-- 2. PHIM (5 movies – mix dang_chieu / sap_chieu)
-- ============================================================
IF NOT EXISTS (SELECT 1 FROM Phim WHERE TenPhim = N'Avengers: Doomsday')
INSERT INTO Phim (TenPhim, TenPhimTiengAnh, TheLoai, DaoDien, DienVienChinh, ThoiLuong, NgonNgu,
                  PhanLoaiDoTuoi, PosterUrl, TrailerUrl, MoTa, DiemDanhGia, SoLuongDanhGia,
                  TrangThai, IsDeleted, NgayCongChieu, NgayTao)
VALUES
(N'Avengers: Doomsday',
 'Avengers: Doomsday',
 N'Hành động, Siêu anh hùng',
 N'Russo Brothers',
 N'Robert Downey Jr., Chris Evans, Scarlett Johansson',
 160, N'Tiếng Anh (Phụ đề Việt)',
 'C13',
 'https://picsum.photos/seed/phim1/400/600',
 'https://www.youtube.com/watch?v=dQw4w9WgXcQ',
 N'Các siêu anh hùng hội tụ để đối mặt với mối đe doạ lớn nhất từ trước đến nay.',
 8.50, 1240, 'dang_chieu', 0, '2026-04-25', GETDATE()),

(N'Lật Mặt 8: Vòng Xoáy Tội Lỗi',
 'Lat Mat 8',
 N'Hành động, Tâm lý, Việt Nam',
 N'Lý Hải',
 N'Lý Hải, Minh Hà, Quốc Trường',
 130, N'Tiếng Việt',
 'C16',
 'https://picsum.photos/seed/phim2/400/600',
 'https://www.youtube.com/watch?v=dQw4w9WgXcQ',
 N'Phần tiếp theo của loạt phim Lật Mặt đình đám với những tình tiết gay cấn.',
 7.80, 890, 'dang_chieu', 0, '2026-05-01', GETDATE()),

(N'Quái Vật Biển Sâu',
 'Deep Sea Monster',
 N'Kinh dị, Phiêu lưu',
 N'James Cameron',
 N'Emily Blunt, Tom Hardy',
 145, N'Tiếng Anh (Phụ đề Việt)',
 'C16',
 'https://picsum.photos/seed/phim3/400/600',
 'https://www.youtube.com/watch?v=dQw4w9WgXcQ',
 N'Một đội thám hiểm chạm trán sinh vật bí ẩn dưới vực thẳm đại dương.',
 7.20, 450, 'dang_chieu', 0, '2026-05-10', GETDATE()),

(N'Hoàng Tử Rồng: Tái Sinh',
 'Dragon Prince: Reborn',
 N'Hoạt hình, Phiêu lưu, Gia đình',
 N'DreamWorks Animation',
 N'Lồng tiếng Việt',
 100, N'Tiếng Việt',
 'P',
 'https://picsum.photos/seed/phim4/400/600',
 'https://www.youtube.com/watch?v=dQw4w9WgXcQ',
 N'Hành trình của một hoàng tử trẻ khám phá sức mạnh rồng huyền thoại.',
 8.10, 320, 'sap_chieu', 0, '2026-06-20', GETDATE()),

(N'Bóng Tối Hà Nội',
 'Hanoi Shadows',
 N'Kinh dị, Tâm lý, Việt Nam',
 N'Trần Hữu Tấn',
 N'Kaity Nguyễn, Avin Lu, NSND Lê Khanh',
 115, N'Tiếng Việt',
 'C18',
 'https://picsum.photos/seed/phim5/400/600',
 'https://www.youtube.com/watch?v=dQw4w9WgXcQ',
 N'Bí ẩn rùng rợn ẩn sâu trong những con phố cổ Hà Nội.',
 7.60, 280, 'sap_chieu', 0, '2026-07-05', GETDATE());
GO

-- ============================================================
-- 3. RAP CHIEU (1 cinema)
-- ============================================================
IF NOT EXISTS (SELECT 1 FROM RapChieu WHERE TenRap = N'PolyCinema Hà Nội')
INSERT INTO RapChieu (TenRap, DiaChi, SoDienThoai, TrangThai)
VALUES (N'PolyCinema Hà Nội',
        N'123 Nguyễn Trãi, Quận Thanh Xuân, Hà Nội',
        '024-3333-8888',
        1);
GO

-- ============================================================
-- 4. PHONG CHIEU (2 rooms for that cinema)
-- ============================================================
DECLARE @RapId INT = (SELECT TOP 1 Id FROM RapChieu WHERE TenRap = N'PolyCinema Hà Nội');

IF NOT EXISTS (SELECT 1 FROM PhongChieu WHERE TenPhong = N'Phòng 1 – 2D' AND RapChieuId = @RapId)
INSERT INTO PhongChieu (RapChieuId, TenPhong, LoaiPhong, SucChua, TrangThai)
VALUES
(@RapId, N'Phòng 1 – 2D', '2D', 50, 1),
(@RapId, N'Phòng 2 – 3D', '3D', 50, 1);
GO

-- ============================================================
-- 5. GHE NGOI – 50 seats per room (rows A-E × 10 seats)
--    Rows A-C = thường (hệ số 1.0)
--    Row D    = vip    (hệ số 1.5)
--    Row E    = cặp đôi (hệ số 2.0)
-- ============================================================
DECLARE @P1 INT = (SELECT TOP 1 Id FROM PhongChieu WHERE TenPhong = N'Phòng 1 – 2D');
DECLARE @P2 INT = (SELECT TOP 1 Id FROM PhongChieu WHERE TenPhong = N'Phòng 2 – 3D');

-- Phòng 1
IF NOT EXISTS (SELECT 1 FROM GheNgoi WHERE PhongChieuId = @P1 AND HangGhe = 'A' AND SoGhe = 1)
BEGIN
    -- Row A – thường
    INSERT INTO GheNgoi (PhongChieuId, HangGhe, SoGhe, LoaiGhe, HeSoGia) VALUES
    (@P1,'A',1,'thường',1.00),(@P1,'A',2,'thường',1.00),(@P1,'A',3,'thường',1.00),
    (@P1,'A',4,'thường',1.00),(@P1,'A',5,'thường',1.00),(@P1,'A',6,'thường',1.00),
    (@P1,'A',7,'thường',1.00),(@P1,'A',8,'thường',1.00),(@P1,'A',9,'thường',1.00),
    (@P1,'A',10,'thường',1.00);
    -- Row B – thường
    INSERT INTO GheNgoi (PhongChieuId, HangGhe, SoGhe, LoaiGhe, HeSoGia) VALUES
    (@P1,'B',1,'thường',1.00),(@P1,'B',2,'thường',1.00),(@P1,'B',3,'thường',1.00),
    (@P1,'B',4,'thường',1.00),(@P1,'B',5,'thường',1.00),(@P1,'B',6,'thường',1.00),
    (@P1,'B',7,'thường',1.00),(@P1,'B',8,'thường',1.00),(@P1,'B',9,'thường',1.00),
    (@P1,'B',10,'thường',1.00);
    -- Row C – thường
    INSERT INTO GheNgoi (PhongChieuId, HangGhe, SoGhe, LoaiGhe, HeSoGia) VALUES
    (@P1,'C',1,'thường',1.00),(@P1,'C',2,'thường',1.00),(@P1,'C',3,'thường',1.00),
    (@P1,'C',4,'thường',1.00),(@P1,'C',5,'thường',1.00),(@P1,'C',6,'thường',1.00),
    (@P1,'C',7,'thường',1.00),(@P1,'C',8,'thường',1.00),(@P1,'C',9,'thường',1.00),
    (@P1,'C',10,'thường',1.00);
    -- Row D – vip
    INSERT INTO GheNgoi (PhongChieuId, HangGhe, SoGhe, LoaiGhe, HeSoGia) VALUES
    (@P1,'D',1,'vip',1.50),(@P1,'D',2,'vip',1.50),(@P1,'D',3,'vip',1.50),
    (@P1,'D',4,'vip',1.50),(@P1,'D',5,'vip',1.50),(@P1,'D',6,'vip',1.50),
    (@P1,'D',7,'vip',1.50),(@P1,'D',8,'vip',1.50),(@P1,'D',9,'vip',1.50),
    (@P1,'D',10,'vip',1.50);
    -- Row E – cặp đôi
    INSERT INTO GheNgoi (PhongChieuId, HangGhe, SoGhe, LoaiGhe, HeSoGia) VALUES
    (@P1,'E',1,'cặp đôi',2.00),(@P1,'E',2,'cặp đôi',2.00),(@P1,'E',3,'cặp đôi',2.00),
    (@P1,'E',4,'cặp đôi',2.00),(@P1,'E',5,'cặp đôi',2.00),(@P1,'E',6,'cặp đôi',2.00),
    (@P1,'E',7,'cặp đôi',2.00),(@P1,'E',8,'cặp đôi',2.00),(@P1,'E',9,'cặp đôi',2.00),
    (@P1,'E',10,'cặp đôi',2.00);
END;
GO

-- Phòng 2
DECLARE @P2b INT = (SELECT TOP 1 Id FROM PhongChieu WHERE TenPhong = N'Phòng 2 – 3D');
IF NOT EXISTS (SELECT 1 FROM GheNgoi WHERE PhongChieuId = @P2b AND HangGhe = 'A' AND SoGhe = 1)
BEGIN
    INSERT INTO GheNgoi (PhongChieuId, HangGhe, SoGhe, LoaiGhe, HeSoGia) VALUES
    (@P2b,'A',1,'thường',1.00),(@P2b,'A',2,'thường',1.00),(@P2b,'A',3,'thường',1.00),
    (@P2b,'A',4,'thường',1.00),(@P2b,'A',5,'thường',1.00),(@P2b,'A',6,'thường',1.00),
    (@P2b,'A',7,'thường',1.00),(@P2b,'A',8,'thường',1.00),(@P2b,'A',9,'thường',1.00),
    (@P2b,'A',10,'thường',1.00);
    INSERT INTO GheNgoi (PhongChieuId, HangGhe, SoGhe, LoaiGhe, HeSoGia) VALUES
    (@P2b,'B',1,'thường',1.00),(@P2b,'B',2,'thường',1.00),(@P2b,'B',3,'thường',1.00),
    (@P2b,'B',4,'thường',1.00),(@P2b,'B',5,'thường',1.00),(@P2b,'B',6,'thường',1.00),
    (@P2b,'B',7,'thường',1.00),(@P2b,'B',8,'thường',1.00),(@P2b,'B',9,'thường',1.00),
    (@P2b,'B',10,'thường',1.00);
    INSERT INTO GheNgoi (PhongChieuId, HangGhe, SoGhe, LoaiGhe, HeSoGia) VALUES
    (@P2b,'C',1,'thường',1.00),(@P2b,'C',2,'thường',1.00),(@P2b,'C',3,'thường',1.00),
    (@P2b,'C',4,'thường',1.00),(@P2b,'C',5,'thường',1.00),(@P2b,'C',6,'thường',1.00),
    (@P2b,'C',7,'thường',1.00),(@P2b,'C',8,'thường',1.00),(@P2b,'C',9,'thường',1.00),
    (@P2b,'C',10,'thường',1.00);
    INSERT INTO GheNgoi (PhongChieuId, HangGhe, SoGhe, LoaiGhe, HeSoGia) VALUES
    (@P2b,'D',1,'vip',1.50),(@P2b,'D',2,'vip',1.50),(@P2b,'D',3,'vip',1.50),
    (@P2b,'D',4,'vip',1.50),(@P2b,'D',5,'vip',1.50),(@P2b,'D',6,'vip',1.50),
    (@P2b,'D',7,'vip',1.50),(@P2b,'D',8,'vip',1.50),(@P2b,'D',9,'vip',1.50),
    (@P2b,'D',10,'vip',1.50);
    INSERT INTO GheNgoi (PhongChieuId, HangGhe, SoGhe, LoaiGhe, HeSoGia) VALUES
    (@P2b,'E',1,'cặp đôi',2.00),(@P2b,'E',2,'cặp đôi',2.00),(@P2b,'E',3,'cặp đôi',2.00),
    (@P2b,'E',4,'cặp đôi',2.00),(@P2b,'E',5,'cặp đôi',2.00),(@P2b,'E',6,'cặp đôi',2.00),
    (@P2b,'E',7,'cặp đôi',2.00),(@P2b,'E',8,'cặp đôi',2.00),(@P2b,'E',9,'cặp đôi',2.00),
    (@P2b,'E',10,'cặp đôi',2.00);
END;
GO

-- ============================================================
-- 6. LICH CHIEU – 10 showtimes across next 7 days
--    5 in Phòng 1 (Avengers + Lật Mặt), 5 in Phòng 2 (same films)
-- ============================================================
DECLARE @Phim1 INT = (SELECT TOP 1 Id FROM Phim WHERE TenPhim = N'Avengers: Doomsday');
DECLARE @Phim2 INT = (SELECT TOP 1 Id FROM Phim WHERE TenPhim = N'Lật Mặt 8: Vòng Xoáy Tội Lỗi');
DECLARE @PC1   INT = (SELECT TOP 1 Id FROM PhongChieu WHERE TenPhong = N'Phòng 1 – 2D');
DECLARE @PC2   INT = (SELECT TOP 1 Id FROM PhongChieu WHERE TenPhong = N'Phòng 2 – 3D');

IF NOT EXISTS (SELECT 1 FROM LichChieu WHERE PhimId = @Phim1 AND PhongChieuId = @PC1
               AND ThoiGianBatDau = DATEADD(day,0,CAST(CAST(GETDATE() AS DATE) AS DATETIME) + '09:00'))
BEGIN
    -- Day 0 (today)
    INSERT INTO LichChieu (PhimId,PhongChieuId,ThoiGianBatDau,ThoiGianKetThuc,GiaCoBan,TrangThai,IsDeleted,NgayTao) VALUES
    (@Phim1,@PC1,
     DATEADD(day,0,CAST(CAST(GETDATE() AS DATE) AS DATETIME)+CAST('09:00' AS DATETIME)),
     DATEADD(day,0,CAST(CAST(GETDATE() AS DATE) AS DATETIME)+CAST('11:40' AS DATETIME)),
     80000,'active',0,GETDATE()),
    (@Phim2,@PC2,
     DATEADD(day,0,CAST(CAST(GETDATE() AS DATE) AS DATETIME)+CAST('10:00' AS DATETIME)),
     DATEADD(day,0,CAST(CAST(GETDATE() AS DATE) AS DATETIME)+CAST('12:10' AS DATETIME)),
     90000,'active',0,GETDATE()),
    -- Day 1
    (@Phim1,@PC1,
     DATEADD(day,1,CAST(CAST(GETDATE() AS DATE) AS DATETIME)+CAST('14:00' AS DATETIME)),
     DATEADD(day,1,CAST(CAST(GETDATE() AS DATE) AS DATETIME)+CAST('16:40' AS DATETIME)),
     80000,'active',0,GETDATE()),
    (@Phim2,@PC2,
     DATEADD(day,1,CAST(CAST(GETDATE() AS DATE) AS DATETIME)+CAST('15:00' AS DATETIME)),
     DATEADD(day,1,CAST(CAST(GETDATE() AS DATE) AS DATETIME)+CAST('17:10' AS DATETIME)),
     90000,'active',0,GETDATE()),
    -- Day 2
    (@Phim1,@PC2,
     DATEADD(day,2,CAST(CAST(GETDATE() AS DATE) AS DATETIME)+CAST('18:30' AS DATETIME)),
     DATEADD(day,2,CAST(CAST(GETDATE() AS DATE) AS DATETIME)+CAST('21:10' AS DATETIME)),
     90000,'active',0,GETDATE()),
    -- Day 3
    (@Phim2,@PC1,
     DATEADD(day,3,CAST(CAST(GETDATE() AS DATE) AS DATETIME)+CAST('09:30' AS DATETIME)),
     DATEADD(day,3,CAST(CAST(GETDATE() AS DATE) AS DATETIME)+CAST('11:40' AS DATETIME)),
     80000,'active',0,GETDATE()),
    (@Phim1,@PC1,
     DATEADD(day,3,CAST(CAST(GETDATE() AS DATE) AS DATETIME)+CAST('19:00' AS DATETIME)),
     DATEADD(day,3,CAST(CAST(GETDATE() AS DATE) AS DATETIME)+CAST('21:40' AS DATETIME)),
     80000,'active',0,GETDATE()),
    -- Day 5
    (@Phim2,@PC2,
     DATEADD(day,5,CAST(CAST(GETDATE() AS DATE) AS DATETIME)+CAST('13:00' AS DATETIME)),
     DATEADD(day,5,CAST(CAST(GETDATE() AS DATE) AS DATETIME)+CAST('15:10' AS DATETIME)),
     90000,'active',0,GETDATE()),
    -- Day 6
    (@Phim1,@PC2,
     DATEADD(day,6,CAST(CAST(GETDATE() AS DATE) AS DATETIME)+CAST('10:30' AS DATETIME)),
     DATEADD(day,6,CAST(CAST(GETDATE() AS DATE) AS DATETIME)+CAST('13:10' AS DATETIME)),
     90000,'active',0,GETDATE()),
    (@Phim2,@PC1,
     DATEADD(day,6,CAST(CAST(GETDATE() AS DATE) AS DATETIME)+CAST('20:00' AS DATETIME)),
     DATEADD(day,6,CAST(CAST(GETDATE() AS DATE) AS DATETIME)+CAST('22:10' AS DATETIME)),
     80000,'active',0,GETDATE());
END;
GO

-- ============================================================
-- 7. SAN PHAM (1 combo, 1 food, 1 drink)
-- ============================================================
IF NOT EXISTS (SELECT 1 FROM SanPham WHERE TenSanPham = N'Combo Đôi Bắp + Nước')
INSERT INTO SanPham (TenSanPham, LoaiSanPham, MoTa, Gia, TonKho, AnhUrl, DangHoatDong)
VALUES
(N'Combo Đôi Bắp + Nước',
 'combo',
 N'2 bắp rang bơ cỡ lớn + 2 nước ngọt 500ml',
 89000, 200,
 'https://picsum.photos/seed/combo1/300/300',
 1),
(N'Bắp Rang Bơ Lớn',
 'food',
 N'Bắp rang bơ thơm ngon, kích thước lớn 130g',
 45000, 300,
 'https://picsum.photos/seed/food1/300/300',
 1),
(N'Pepsi 500ml',
 'drink',
 N'Nước ngọt Pepsi lon 500ml ướp lạnh',
 30000, 500,
 'https://picsum.photos/seed/drink1/300/300',
 1);
GO

-- ============================================================
-- 8. KHUYEN MAI (1 percent, 1 fixed)
-- ============================================================
IF NOT EXISTS (SELECT 1 FROM KhuyenMai WHERE MaKhuyenMai = 'POLY10')
INSERT INTO KhuyenMai (MaKhuyenMai, TenKhuyenMai, MoTa, LoaiGiamGia, GiaTriGiam,
                        GiaTriGiamToiDa, DonHangToiThieu, NgayBatDau, NgayKetThuc,
                        GioiHanSuDung, DaSuDung, DangHoatDong)
VALUES
('POLY10',
 N'Giảm 10% – Khách mới',
 N'Giảm 10% cho lần đặt vé đầu tiên, tối đa 50.000đ',
 'percent', 10.00, 50000.00, 100000.00,
 '2026-01-01', '2026-12-31',
 500, 0, 1),
('WELCOME50K',
 N'Tặng 50.000đ – Chào mừng',
 N'Giảm thẳng 50.000đ cho đơn từ 200.000đ trở lên',
 'fixed', 50000.00, NULL, 200000.00,
 '2026-01-01', '2026-12-31',
 200, 0, 1);
GO

PRINT N'Sample data inserted successfully!';
GO
