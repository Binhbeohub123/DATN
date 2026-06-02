-- =============================================
-- DATABASE QUẢN LÝ RẠP CHIẾU PHIM - SQL SERVER
-- Version: FINAL v4 (đã sửa lỗi, tối ưu, không thêm bảng mới)
-- =============================================

IF DB_ID('rapphim') IS NOT NULL
    DROP DATABASE rapphim;
GO

CREATE DATABASE rapphim3;
GO

USE rapphim;
GO

-- =============================================
-- 1. NGƯỜI DÙNG
-- =============================================
CREATE TABLE NguoiDung (
    Id              BIGINT IDENTITY(1,1) PRIMARY KEY,
    Email           VARCHAR(255) UNIQUE NOT NULL,
    SoDienThoai     VARCHAR(20) UNIQUE,
    MatKhauHash     NVARCHAR(MAX),
    HoTen           NVARCHAR(100),
    NgaySinh        DATE,
    AnhDaiDien      NVARCHAR(MAX),
    GoogleId        NVARCHAR(100) UNIQUE,
    FacebookId      NVARCHAR(100) UNIQUE,
    VaiTro          VARCHAR(20) DEFAULT 'customer'
                        CHECK (VaiTro IN ('admin','staff','customer')),
    TrangThai       BIT DEFAULT 1,               -- 1=active, 0=locked (RQ44-45)
    IsEmailVerified BIT DEFAULT 0,               -- RQ08
    TongTienDaChi   DECIMAL(15,2) DEFAULT 0,
    CapDoThanhVien  NVARCHAR(20) DEFAULT N'Thường'
                        CHECK (CapDoThanhVien IN (N'Thường',N'Bạc',N'Vàng',N'Kim Cương')),
    DiemTichLuy     INT DEFAULT 0,
    LyDoKhoa        NVARCHAR(200) NULL,           -- lý do khóa tài khoản (RQ44)
    NgayTao         DATETIME2 DEFAULT GETDATE(),
    NgayCapNhat     DATETIME2 DEFAULT GETDATE()
);
GO

-- Trigger tự động nâng cấp thành viên
CREATE TRIGGER trg_NangCapThanhVien
ON NguoiDung AFTER UPDATE
AS
BEGIN
    IF UPDATE(TongTienDaChi)
        UPDATE NguoiDung
        SET CapDoThanhVien =
            CASE
                WHEN i.TongTienDaChi >= 3000000 THEN N'Kim Cương'
                WHEN i.TongTienDaChi >= 1000000 THEN N'Vàng'
                WHEN i.TongTienDaChi >= 500000  THEN N'Bạc'
                ELSE N'Thường'
            END
        FROM NguoiDung u INNER JOIN inserted i ON u.Id = i.Id;
END;
GO

-- =============================================
-- 2. RESET MẬT KHẨU (RQ10,RQ40)
-- =============================================
CREATE TABLE PasswordResetToken (
    Id          BIGINT IDENTITY(1,1) PRIMARY KEY,
    NguoiDungId BIGINT REFERENCES NguoiDung(Id) ON DELETE CASCADE,
    Token       NVARCHAR(255) UNIQUE NOT NULL,
    NgayTao     DATETIME2 DEFAULT GETDATE(),
    NgayHetHan  DATETIME2 NOT NULL,
    DaSuDung    BIT DEFAULT 0
);
GO

-- =============================================
-- 3. PHIM + LỊCH SỬ CHỈNH SỬA
-- =============================================
CREATE TABLE Phim (
    Id              BIGINT IDENTITY(1,1) PRIMARY KEY,
    TenPhim         NVARCHAR(255) NOT NULL,
    TenPhimTiengAnh NVARCHAR(255),
    TheLoai         NVARCHAR(500),
    DaoDien         NVARCHAR(200),
    DienVienChinh   NVARCHAR(500),
    ThoiLuong       INT CHECK (ThoiLuong > 0),
    NgonNgu         NVARCHAR(50),
    PhanLoaiDoTuoi  VARCHAR(10) CHECK (PhanLoaiDoTuoi IN ('P','C13','C16','C18')),
    PosterUrl       NVARCHAR(MAX),
    TrailerUrl      NVARCHAR(MAX),
    MoTa            NVARCHAR(MAX),
    DiemDanhGia     DECIMAL(3,2) DEFAULT 0,
    SoLuongDanhGia  INT DEFAULT 0,
    TrangThai       VARCHAR(20) DEFAULT 'sap_chieu'
                        CHECK (TrangThai IN ('sap_chieu','dang_chieu','ngung_chieu')),
    IsDeleted       BIT DEFAULT 0,
    NgayCongChieu   DATE,
    NgayTao         DATETIME2 DEFAULT GETDATE()
);
GO

CREATE TABLE LichSuCapNhatPhim (
    Id          BIGINT IDENTITY(1,1) PRIMARY KEY,
    PhimId      BIGINT REFERENCES Phim(Id) ON DELETE CASCADE,
    NguoiSuaId  BIGINT REFERENCES NguoiDung(Id),
    TruongSua   NVARCHAR(100) NOT NULL,
    GiaTriCu    NVARCHAR(MAX),
    GiaTriMoi   NVARCHAR(MAX),
    ThoiGian    DATETIME2 DEFAULT GETDATE()
);
GO

CREATE TABLE DanhGiaPhim (
    Id          BIGINT IDENTITY(1,1) PRIMARY KEY,
    PhimId      BIGINT REFERENCES Phim(Id) ON DELETE CASCADE,
    NguoiDungId BIGINT REFERENCES NguoiDung(Id),
    Diem        INT CHECK (Diem BETWEEN 1 AND 5),
    BinhLuan    NVARCHAR(MAX),
    NgayTao     DATETIME2 DEFAULT GETDATE(),
    UNIQUE(PhimId, NguoiDungId)
);
GO

-- =============================================
-- 4. RẠP & PHÒNG CHIẾU
-- =============================================
CREATE TABLE RapChieu (
    Id          BIGINT IDENTITY(1,1) PRIMARY KEY,
    TenRap      NVARCHAR(100) NOT NULL,
    DiaChi      NVARCHAR(MAX) NOT NULL,
    SoDienThoai VARCHAR(20),
    BanDoUrl    NVARCHAR(MAX),
    TrangThai   BIT DEFAULT 1
);
GO

CREATE TABLE PhongChieu (
    Id          BIGINT IDENTITY(1,1) PRIMARY KEY,
    RapChieuId  BIGINT REFERENCES RapChieu(Id) ON DELETE CASCADE,
    TenPhong    NVARCHAR(50) NOT NULL,
    LoaiPhong   NVARCHAR(30) NOT NULL CHECK (LoaiPhong IN (N'2D',N'3D',N'IMAX')),
    SucChua     INT NOT NULL,
    SoDoGhe     NVARCHAR(MAX),
    TrangThai   BIT DEFAULT 1
);
GO

CREATE TABLE GheNgoi (
    Id           BIGINT IDENTITY(1,1) PRIMARY KEY,
    PhongChieuId BIGINT REFERENCES PhongChieu(Id) ON DELETE CASCADE,
    HangGhe      CHAR(2) NOT NULL,
    SoGhe        INT NOT NULL,
    LoaiGhe      NVARCHAR(20) DEFAULT N'thường'
                     CHECK (LoaiGhe IN (N'thường',N'vip',N'cặp đôi',N'khuyết tật')),
    HeSoGia      DECIMAL(3,2) DEFAULT 1.0 CHECK (HeSoGia > 0),
    UNIQUE(PhongChieuId, HangGhe, SoGhe)
);
GO

-- =============================================
-- 5. LỊCH CHIẾU (có IsDeleted + trigger chống trùng)
-- =============================================
CREATE TABLE LichChieu (
    Id              BIGINT IDENTITY(1,1) PRIMARY KEY,
    PhimId          BIGINT REFERENCES Phim(Id) ON DELETE CASCADE,
    PhongChieuId    BIGINT REFERENCES PhongChieu(Id),
    ThoiGianBatDau  DATETIME2 NOT NULL,
    ThoiGianKetThuc DATETIME2 NOT NULL,
    GiaCoBan        DECIMAL(12,2) NOT NULL,
    TrangThai       VARCHAR(20) DEFAULT 'active' CHECK (TrangThai IN ('active','cancelled','completed')),
    IsDeleted       BIT NOT NULL DEFAULT 0,
    NgayTao         DATETIME2 DEFAULT GETDATE(),
    CONSTRAINT CK_ThoiGian CHECK (ThoiGianKetThuc > ThoiGianBatDau),
    CONSTRAINT UC_NoOverlap UNIQUE(PhongChieuId, ThoiGianBatDau)
);
GO

-- Trigger chống trùng lịch chiếu (overlap)
CREATE TRIGGER trg_KiemTraLichChieu
ON LichChieu AFTER INSERT, UPDATE
AS
BEGIN
    IF EXISTS (
        SELECT 1
        FROM LichChieu lc
        INNER JOIN inserted i ON lc.PhongChieuId = i.PhongChieuId
        WHERE lc.Id != i.Id
          AND lc.IsDeleted = 0
          AND lc.TrangThai = 'active'
          AND lc.ThoiGianBatDau < i.ThoiGianKetThuc
          AND lc.ThoiGianKetThuc > i.ThoiGianBatDau
    )
    BEGIN
        RAISERROR(N'Phòng chiếu đã có lịch trong khung giờ này.', 16, 1);
        ROLLBACK TRANSACTION;
    END
END;
GO

-- =============================================
-- 6. KHUYẾN MÃI
-- =============================================
CREATE TABLE KhuyenMai (
    Id              BIGINT IDENTITY(1,1) PRIMARY KEY,
    MaKhuyenMai     VARCHAR(50) UNIQUE NOT NULL,
    TenKhuyenMai    NVARCHAR(200),
    MoTa            NVARCHAR(MAX),
    LoaiGiamGia     VARCHAR(20) CHECK (LoaiGiamGia IN ('percent','fixed')),
    GiaTriGiam      DECIMAL(12,2) CHECK (GiaTriGiam > 0),
    GiaTriGiamToiDa DECIMAL(12,2) CHECK (GiaTriGiamToiDa > 0),
    DonHangToiThieu DECIMAL(12,2),
    CapDoApDung     NVARCHAR(20) NULL CHECK (CapDoApDung IN (N'Thường',N'Bạc',N'Vàng',N'Kim Cương')),
    NgayBatDau      DATE,
    NgayKetThuc     DATE,
    GioiHanSuDung   INT,
    DaSuDung        INT DEFAULT 0,
    DangHoatDong    BIT DEFAULT 1,
    CONSTRAINT CK_KhuyenMai_Ngay CHECK (NgayKetThuc >= NgayBatDau)
);
GO

-- =============================================
-- 7. ĐẶT VÉ (tách biệt các khoản tiền)
-- =============================================
CREATE TABLE DatVe (
    Id                  BIGINT IDENTITY(1,1) PRIMARY KEY,
    MaDatVe             VARCHAR(20) UNIQUE NOT NULL,
    NguoiDungId         BIGINT REFERENCES NguoiDung(Id),
    LichChieuId         BIGINT REFERENCES LichChieu(Id),
    KhuyenMaiId         BIGINT REFERENCES KhuyenMai(Id),
    TongTienGoc         DECIMAL(15,2) NOT NULL DEFAULT 0,     -- tổng tiền vé + combo gốc
    TienGiamKhuyenMai   DECIMAL(12,2) DEFAULT 0,              -- giảm từ mã KM (RQ32)
    DiemSuDung          INT DEFAULT 0,                        -- điểm dùng để giảm (RQ17)
    TienGiamTuDiem      DECIMAL(12,2) DEFAULT 0,              -- tiền giảm từ điểm
    TongTienThanhToan   DECIMAL(15,2) NOT NULL,               -- số tiền khách phải trả
    TrangThai           VARCHAR(20) DEFAULT 'pending'
                            CHECK (TrangThai IN ('pending','confirmed','cancelled','completed')),
    TrangThaiThanhToan  VARCHAR(20) DEFAULT 'unpaid'
                            CHECK (TrangThaiThanhToan IN ('unpaid','paid','refunded')),
    MaQR                NVARCHAR(MAX),
    NgayTao             DATETIME2 DEFAULT GETDATE(),
    HetHanGiuGhe        DATETIME2
);
GO

-- =============================================
-- 8. CHI TIẾT ĐẶT GHẾ (thêm LichChieuId)
-- =============================================
CREATE TABLE ChiTietDatGhe (
    DatVeId        BIGINT REFERENCES DatVe(Id) ON DELETE CASCADE,
    GheNgoiId      BIGINT REFERENCES GheNgoi(Id),
    LichChieuId    BIGINT REFERENCES LichChieu(Id),   -- truy vấn ghế nhanh (RQ24)
    GiaTien        DECIMAL(12,2) NOT NULL,
    HeSoGiaLucDat  DECIMAL(3,2) NOT NULL,
    PRIMARY KEY (DatVeId, GheNgoiId)
);
GO

-- =============================================
-- 9. SẢN PHẨM (đổi DanhMuc -> LoaiSanPham)
-- =============================================
CREATE TABLE SanPham (
    Id           BIGINT IDENTITY(1,1) PRIMARY KEY,
    TenSanPham   NVARCHAR(100) NOT NULL,
    LoaiSanPham  VARCHAR(20) DEFAULT 'food' CHECK (LoaiSanPham IN ('combo','food','drink')),
    MoTa         NVARCHAR(MAX),
    Gia          DECIMAL(12,2) NOT NULL,
    TonKho       INT DEFAULT 0,
    AnhUrl       NVARCHAR(MAX),
    DangHoatDong BIT DEFAULT 1
);
GO

CREATE TABLE ChiTietDatSanPham (
    DatVeId   BIGINT REFERENCES DatVe(Id) ON DELETE CASCADE,
    SanPhamId BIGINT REFERENCES SanPham(Id),
    SoLuong   INT NOT NULL CHECK (SoLuong > 0),
    GiaLucMua DECIMAL(12,2) NOT NULL,
    PRIMARY KEY (DatVeId, SanPhamId)
);
GO

-- =============================================
-- 10. THANH TOÁN
-- =============================================
CREATE TABLE ThanhToan (
    Id                  BIGINT IDENTITY(1,1) PRIMARY KEY,
    DatVeId             BIGINT REFERENCES DatVe(Id),
    SoTien              DECIMAL(15,2) NOT NULL,
    PhuongThucThanhToan VARCHAR(30) CHECK (PhuongThucThanhToan IN ('VNPay','Momo','ATM','Visa','MasterCard')),
    MaGiaoDich          NVARCHAR(100),
    TrangThai           NVARCHAR(20) DEFAULT 'pending' CHECK (TrangThai IN ('pending','success','failed','refunded')),
    ThoiGianThanhToan   DATETIME2,
    LyDoHoan            NVARCHAR(MAX),
    NgayHoan            DATETIME2,
    NguoiXuLyId         BIGINT REFERENCES NguoiDung(Id)
);
GO

-- =============================================
-- 11. BANNER
-- =============================================
CREATE TABLE Banner (
    Id           BIGINT IDENTITY(1,1) PRIMARY KEY,
    TieuDe       NVARCHAR(200),
    HinhAnh      NVARCHAR(MAX) NOT NULL,
    LinkUrl      NVARCHAR(MAX),
    ThuTu        INT DEFAULT 0,
    NgayBatDau   DATE,
    NgayKetThuc  DATE,
    DangHoatDong BIT DEFAULT 1,
    NgayTao      DATETIME2 DEFAULT GETDATE()
);
GO

-- =============================================
-- TRIGGER TÍCH ĐIỂM SAU THANH TOÁN (RQ23)
-- =============================================
CREATE TRIGGER trg_TichDiemSauThanhToan
ON ThanhToan AFTER UPDATE
AS
BEGIN
    IF UPDATE(TrangThai)
    BEGIN
        DECLARE @TiLe DECIMAL(12,2) = 1000;  -- 1000đ = 1 điểm

        UPDATE NguoiDung
        SET DiemTichLuy   = nd.DiemTichLuy + FLOOR((tt.SoTien - ISNULL(dv.TienGiamTuDiem,0)) / @TiLe),
            TongTienDaChi = nd.TongTienDaChi + tt.SoTien,
            NgayCapNhat   = GETDATE()
        FROM NguoiDung nd
        INNER JOIN DatVe dv ON dv.NguoiDungId = nd.Id
        INNER JOIN inserted tt ON tt.DatVeId = dv.Id
        INNER JOIN deleted old ON old.Id = tt.Id
        WHERE tt.TrangThai = 'success'
          AND old.TrangThai != 'success';
    END
END;
GO

-- =============================================
-- INDEX HỖ TRỢ
-- =============================================
CREATE NONCLUSTERED INDEX IX_Phim_TrangThai_IsDeleted ON Phim(TrangThai, IsDeleted);
CREATE NONCLUSTERED INDEX IX_Phim_TenPhim ON Phim(TenPhim);
CREATE NONCLUSTERED INDEX IX_LichChieu_ThoiGian ON LichChieu(ThoiGianBatDau, ThoiGianKetThuc) INCLUDE (PhongChieuId, TrangThai);
CREATE NONCLUSTERED INDEX IX_LichChieu_Phim ON LichChieu(PhimId, TrangThai);
CREATE NONCLUSTERED INDEX IX_DatVe_NguoiDung ON DatVe(NguoiDungId, TrangThai);
CREATE NONCLUSTERED INDEX IX_DatVe_LichChieu ON DatVe(LichChieuId);
CREATE NONCLUSTERED INDEX IX_DatVe_KhuyenMai ON DatVe(KhuyenMaiId);
CREATE NONCLUSTERED INDEX IX_DatVe_TrangThai ON DatVe(TrangThai, TrangThaiThanhToan);
CREATE NONCLUSTERED INDEX IX_ChiTietDatGhe_GheNgoi ON ChiTietDatGhe(GheNgoiId) INCLUDE (DatVeId);
CREATE NONCLUSTERED INDEX IX_ChiTietDatGhe_LichChieu ON ChiTietDatGhe(LichChieuId, GheNgoiId) INCLUDE (DatVeId);
CREATE NONCLUSTERED INDEX IX_GheNgoi_Phong ON GheNgoi(PhongChieuId, LoaiGhe);
CREATE NONCLUSTERED INDEX IX_DanhGia_Phim ON DanhGiaPhim(PhimId);
CREATE NONCLUSTERED INDEX IX_DanhGia_NguoiDung ON DanhGiaPhim(NguoiDungId);
CREATE NONCLUSTERED INDEX IX_NguoiDung_CapDo_TrangThai ON NguoiDung(CapDoThanhVien, TrangThai);
CREATE NONCLUSTERED INDEX IX_ThanhToan_DatVe ON ThanhToan(DatVeId);
CREATE NONCLUSTERED INDEX IX_ThanhToan_TrangThai ON ThanhToan(TrangThai, ThoiGianThanhToan);
CREATE NONCLUSTERED INDEX IX_PasswordReset_Token ON PasswordResetToken(Token, DaSuDung, NgayHetHan);
CREATE NONCLUSTERED INDEX IX_Banner_HoatDong ON Banner(DangHoatDong, NgayBatDau, NgayKetThuc, ThuTu);
CREATE NONCLUSTERED INDEX IX_RapChieu_TrangThai ON RapChieu(TrangThai);
CREATE NONCLUSTERED INDEX IX_SanPham_HoatDong ON SanPham(DangHoatDong);
CREATE NONCLUSTERED INDEX IX_DatVe_HetHanGiuGhe ON DatVe(HetHanGiuGhe) WHERE TrangThai = 'pending';
CREATE NONCLUSTERED INDEX IX_PasswordReset_User ON PasswordResetToken(NguoiDungId, DaSuDung);
GO

-- =============================================
-- DỮ LIỆU MẪU (cơ bản)
-- =============================================
INSERT Phim (TenPhim, TheLoai, DaoDien, DienVienChinh, ThoiLuong, NgonNgu, PhanLoaiDoTuoi, TrangThai, NgayCongChieu) VALUES
(N'Đào, Phở và Piano', N'Tình cảm,Lịch sử', N'Phi Tiến Sơn', N'Thanh Sơn, Thúy Ngân', 120, N'Tiếng Việt', 'C16', 'dang_chieu', '2025-03-01'),
(N'Mắt Biếc', N'Tình cảm', N'Victor Vũ', N'Trần Nghĩa, Trúc Anh', 135, N'Tiếng Việt', 'C13', 'dang_chieu', '2025-02-15'),
(N'Nhà Bà Nữ', N'Hài', N'Trấn Thành', N'NSND Trung Anh, Uyển Ân', 110, N'Tiếng Việt', 'C13', 'sap_chieu', '2025-06-01');

INSERT RapChieu (TenRap, DiaChi) VALUES
(N'CGV Vincom Landmark', N'Vincom Landmark 81, TP.HCM'),
(N'Lotte Cinema Thủ Đức', N'Landmark 81, TP.HCM');

INSERT PhongChieu (RapChieuId, TenPhong, LoaiPhong, SucChua) VALUES
(1, 'R1', N'2D', 200), (1, 'R2', N'3D', 180), (2, 'P1', N'IMAX', 250);

INSERT GheNgoi (PhongChieuId, HangGhe, SoGhe, LoaiGhe, HeSoGia) VALUES
(1, 'A', 1, N'thường', 1.0), (1, 'A', 2, N'thường', 1.0),
(1, 'B', 1, N'vip', 1.5), (1, 'B', 2, N'vip', 1.5);

INSERT LichChieu (PhimId, PhongChieuId, ThoiGianBatDau, ThoiGianKetThuc, GiaCoBan) VALUES
(1, 1, '2025-05-28 19:00', '2025-05-28 21:00', 90000),
(2, 1, '2025-05-28 14:00', '2025-05-28 16:15', 85000);

INSERT SanPham (TenSanPham, LoaiSanPham, Gia) VALUES
(N'Combo Bắp + Nước', 'combo', 65000),
(N'Bắp Rang Bơ Lớn', 'food', 45000),
(N'Nước Ngọt Pepsi', 'drink', 25000);

INSERT KhuyenMai (MaKhuyenMai, TenKhuyenMai, LoaiGiamGia, GiaTriGiam, DonHangToiThieu, NgayBatDau, NgayKetThuc, GioiHanSuDung) VALUES
('WELCOME20', N'Giảm 20% cho thành viên mới', 'percent', 20, 100000, '2025-05-01', '2025-06-30', 100);

INSERT NguoiDung (Email, MatKhauHash, HoTen, VaiTro, CapDoThanhVien) VALUES
('admin@cinema.com', 'hash', N'Admin', 'admin', N'Kim Cương'),
('user1@gmail.com', 'hash', N'Nguyễn Văn A', 'customer', N'Thường');

PRINT N'Database rapphim đã sẵn sàng.';
GO