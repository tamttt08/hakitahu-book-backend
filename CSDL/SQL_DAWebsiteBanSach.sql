create database hakitahu_book
use hakitahu_book
go

drop database hakitahu_book_1

-- Tạo bảng Users
CREATE TABLE Users (
UserID INT IDENTITY PRIMARY KEY,
ProviderID NVARCHAR(100) NULL,         -- ID Google trả về
Username NVARCHAR(50) NOT NULL,
Password NVARCHAR(255) NOT NULL,
FullName NVARCHAR(100),
Email NVARCHAR(100),
Provider NVARCHAR(20) DEFAULT 'LOCAL', -- 'LOCAL' hoặc 'GOOGLE'
IsEmailVerified BIT DEFAULT 0,
Phone NVARCHAR(20),
Address NVARCHAR(255),
Role NVARCHAR(20), -- user / admin / shipper
CreatedAt DATETIME DEFAULT GETDATE(),
Status BIT DEFAULT 1,
);
go

CREATE TABLE VerificationTokens (
    TokenID INT IDENTITY PRIMARY KEY,
    UserID INT NOT NULL,
    Token NVARCHAR(255) NOT NULL,
    ExpiryDate DATETIME NOT NULL,
    FOREIGN KEY (UserID) REFERENCES Users(UserID)
);
GO


-- Tạo bảng Categories
CREATE TABLE Categories (
CategoryID INT IDENTITY PRIMARY KEY,
CategoryName NVARCHAR(100) NOT NULL,
Status BIT NOT NULL DEFAULT 1,
);
go


-- Tạo bảng	Books
CREATE TABLE Books (
BookID INT IDENTITY PRIMARY KEY,
Title NVARCHAR(200),
Author NVARCHAR(100),
Price DECIMAL(10,2),
Quantity INT,
Description NVARCHAR(MAX),
Image NVARCHAR(255),
CategoryID INT,
Status BIT NOT NULL DEFAULT 1,
FOREIGN KEY (CategoryID) REFERENCES Categories(CategoryID)
);
go

-- Tạo bảng Carts
CREATE TABLE Carts (
    CartID INT IDENTITY PRIMARY KEY,
    UserID INT UNIQUE,
    CreatedAt DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (UserID) REFERENCES Users(UserID)
);
go


-- Tạo bảng CartItems
CREATE TABLE CartItems (
    CartItemID INT IDENTITY PRIMARY KEY,
    CartID INT,
    BookID INT,
    Quantity INT NOT NULL,
    FOREIGN KEY (CartID) REFERENCES Carts(CartID),
    FOREIGN KEY (BookID) REFERENCES Books(BookID)
);
go


-- Tạo bảng Orders
CREATE TABLE Orders (
OrderID INT IDENTITY PRIMARY KEY,
UserID INT,
TotalPrice DECIMAL(10,2),
Status NVARCHAR(30), -- Pending (Chờ xử lý) / Completed (Đã xác nhận) / Shipping (Đang giao) / Completed (Đã hoàn thành) / Canceled (Đã hủy)
OrderDate DATETIME DEFAULT GETDATE(),
FOREIGN KEY (UserID) REFERENCES Users(UserID)
);
go
-- Thêm "Pending" cho cột Status
ALTER TABLE Orders
ADD CONSTRAINT DF_Orders_Status
DEFAULT 'PENDING' FOR Status;
go
-- Thêm cột ShipperID vào Order
ALTER TABLE Orders
ADD ShipperID INT NULL;

ALTER TABLE Orders
ADD CONSTRAINT FK_Orders_Shipper
FOREIGN KEY (ShipperID) REFERENCES Users(UserID);
go


-- Tạo bảng OrderDetails
CREATE TABLE OrderDetails (
OrderDetailID INT IDENTITY PRIMARY KEY,
OrderID INT,
BookID INT,
Quantity INT,
Price DECIMAL(10,2),
FOREIGN KEY (OrderID) REFERENCES Orders(OrderID),
FOREIGN KEY (BookID) REFERENCES Books(BookID)
);
go

-- Tạo bảng Payments
CREATE TABLE Payments (
    PaymentID INT IDENTITY(1,1) PRIMARY KEY,
    OrderID INT NOT NULL,
    PaymentMethod VARCHAR(20) NOT NULL,
    Amount DECIMAL(18,2) NOT NULL,
    PaymentStatus VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    TransactionCode VARCHAR(100) NULL,
    PaidAt DATETIME2 NULL,
    CreatedAt DATETIME2 NOT NULL DEFAULT SYSDATETIME(),

    CONSTRAINT FK_Payments_Orders
        FOREIGN KEY (OrderID) REFERENCES Orders(OrderID),

    CONSTRAINT CK_Payments_Method
        CHECK (PaymentMethod IN ('COD', 'BANK_TRANSFER',
                                 'VNPAY', 'MOMO')),

    CONSTRAINT CK_Payments_Status
        CHECK (PaymentStatus IN ('PENDING', 'SUCCESS',
                                 'FAILED', 'REFUNDED'))
);

DROP TABLE IF EXISTS Reviews;
GO

CREATE TABLE Reviews (
    ReviewID INT IDENTITY PRIMARY KEY,
    UserID INT NOT NULL,
    OrderDetailID INT NOT NULL,
    Rating INT CHECK (Rating BETWEEN 1 AND 5),
    Comment NVARCHAR(MAX),
    CreatedAt DATETIME DEFAULT GETDATE(),

    FOREIGN KEY (UserID) REFERENCES Users(UserID),
    FOREIGN KEY (OrderDetailID) REFERENCES OrderDetails(OrderDetailID)
);
GO


-- Tạo bảng OrderStatusHistory
CREATE TABLE OrderStatusHistory (
    HistoryID INT IDENTITY PRIMARY KEY,
    OrderID INT,
    OldStatus NVARCHAR(30),
    NewStatus NVARCHAR(30),
    ChangedAt DATETIME DEFAULT GETDATE(),
    ChangedBy NVARCHAR(50),

    FOREIGN KEY (OrderID) REFERENCES Orders(OrderID)
);
go


-- Tạo bảng Vouchers
CREATE TABLE Vouchers (
    VoucherID INT IDENTITY PRIMARY KEY,
    Code NVARCHAR(50) NOT NULL UNIQUE,      -- Mã voucher (VD: SALE10)
    DiscountType NVARCHAR(20) NOT NULL,     -- PERCENT / FIXED
    DiscountValue DECIMAL(10,2) NOT NULL,   -- % hoặc số tiền
    MinOrderValue DECIMAL(10,2) DEFAULT 0,  -- Đơn tối thiểu
    MaxDiscount DECIMAL(10,2),              -- Giảm tối đa (dùng cho %)
    Quantity INT NOT NULL,                  -- Số lượt dùng
    UsedCount INT DEFAULT 0,                -- Đã dùng bao nhiêu lần
    StartDate DATETIME NOT NULL,             -- Ngày bắt đầu
    EndDate DATETIME NOT NULL,               -- Ngày kết thúc
    Status BIT DEFAULT 1,                    -- 1: hoạt động, 0: ngưng
    CreatedAt DATETIME DEFAULT GETDATE()
);
go


-- Tạo bảng UserVouchers
CREATE TABLE UserVouchers (
    UserVoucherID INT IDENTITY PRIMARY KEY,
    UserID INT,
    VoucherID INT,
    UsedAt DATETIME DEFAULT GETDATE(),

    FOREIGN KEY (UserID) REFERENCES Users(UserID),
    FOREIGN KEY (VoucherID) REFERENCES Vouchers(VoucherID),
    CONSTRAINT UQ_User_Voucher UNIQUE (UserID, VoucherID)
);
go


-- Tạo bảng StockReceipts (Phiếu nhập kho)
CREATE TABLE StockReceipts (
    ReceiptID INT IDENTITY PRIMARY KEY,
    CreatedBy INT,               -- Admin tạo phiếu
    ReceiptDate DATETIME DEFAULT GETDATE(),
    Note NVARCHAR(255),

    FOREIGN KEY (CreatedBy) REFERENCES Users(UserID)
);
go


-- Tạo bảng StockReceiptDetails (Chi tiết nhập hàng)
CREATE TABLE StockReceiptDetails (
    ReceiptDetailID INT IDENTITY PRIMARY KEY,
    ReceiptID INT,
    BookID INT,
    Quantity INT NOT NULL,
    ImportPrice DECIMAL(10,2),    -- Giá nhập

    FOREIGN KEY (ReceiptID) REFERENCES StockReceipts(ReceiptID),
    FOREIGN KEY (BookID) REFERENCES Books(BookID)
);
go


-- Tạo bảng DeliveryHistory
CREATE TABLE DeliveryHistory (
    DeliveryID INT IDENTITY PRIMARY KEY,
    OrderID INT,
    ShipperID INT,
    Status NVARCHAR(30),
    UpdatedAt DATETIME DEFAULT GETDATE(),
    Note NVARCHAR(255),

    FOREIGN KEY (OrderID) REFERENCES Orders(OrderID),
    FOREIGN KEY (ShipperID) REFERENCES Users(UserID)
);
go


-- Tạo bảng DeliveryProof
CREATE TABLE DeliveryProof (
    ProofID INT IDENTITY PRIMARY KEY,
    OrderID INT NOT NULL,
    ShipperID INT NOT NULL,
    ImageUrl NVARCHAR(255) NOT NULL,
    CreatedAt DATETIME DEFAULT GETDATE(),

    FOREIGN KEY (OrderID) REFERENCES Orders(OrderID),
    FOREIGN KEY (ShipperID) REFERENCES Users(UserID)
);
go

CREATE TABLE Promotions (
    PromotionID INT IDENTITY PRIMARY KEY,
    Name NVARCHAR(150) NOT NULL,            -- Tên chương trình (VD: Mừng ngày Nhà giáo 20/11)
    Description NVARCHAR(500),              -- Mô tả ngắn hiển thị ngoài trang Khuyến mãi
    DiscountType NVARCHAR(20) NOT NULL,     -- 'PERCENT' hoặc 'FIXED'
    DiscountValue DECIMAL(10,2) NOT NULL,   -- VD: 20 (%) hoặc 30000 (VNĐ)
    StartDate DATETIME NOT NULL,            -- Thời gian bắt đầu
    EndDate DATETIME NOT NULL,              -- Thời gian kết thúc
    Status BIT DEFAULT 1,                   -- 1: Bật, 0: Tắt khẩn cấp
    CreatedAt DATETIME DEFAULT GETDATE()
);
GO

CREATE TABLE PromotionBooks (
    PromotionBookID INT IDENTITY PRIMARY KEY,
    PromotionID INT NOT NULL,
    BookID INT NOT NULL,
    FOREIGN KEY (PromotionID) REFERENCES Promotions(PromotionID) ON DELETE CASCADE,
    FOREIGN KEY (BookID) REFERENCES Books(BookID) ON DELETE CASCADE,
    CONSTRAINT UQ_Promotion_Book UNIQUE (PromotionID, BookID)
);
GO


-- Doanh thu theo tháng
SELECT 
    MONTH(OrderDate) AS Thang,
    SUM(TotalPrice) AS DoanhThu
FROM Orders
WHERE Status = 'COMPLETED'
GROUP BY MONTH(OrderDate);
go


-- Số đơn theo trạng thái
SELECT Status, COUNT(*) AS SoLuong
FROM Orders
GROUP BY Status;
go


-- Top 5 sách bán chạy
SELECT TOP 5 
    B.Title,
    SUM(OD.Quantity) AS SoLuongBan
FROM OrderDetails OD
JOIN Books B ON OD.BookID = B.BookID
GROUP BY B.Title
ORDER BY SoLuongBan DESC;
go


-- Sách sắp hết hàng
SELECT Title, Quantity
FROM Books
WHERE Quantity < 10;
go

-- ===================================================================================
-- THÊM DỮ LIỆU MẪU (SEED DATA)
-- 1. 5 Tài khoản (1 Admin, 1 Shipper, 3 User) - Mật khẩu: 123456
-- 2. 5 Danh mục sách
-- 3. 50 Cuốn sách phong phú có hình ảnh đẹp, giá bán và mô tả chi tiết
-- ===================================================================================

-- 1. THÊM 5 DANH MỤC SÁCH
IF NOT EXISTS (SELECT 1 FROM Categories WHERE CategoryName = N'Văn Học - Tiểu Thuyết')
    INSERT INTO Categories (CategoryName, Status) VALUES (N'Văn Học - Tiểu Thuyết', 1);

IF NOT EXISTS (SELECT 1 FROM Categories WHERE CategoryName = N'Kinh Tế - Kinh Doanh')
    INSERT INTO Categories (CategoryName, Status) VALUES (N'Kinh Tế - Kinh Doanh', 1);

IF NOT EXISTS (SELECT 1 FROM Categories WHERE CategoryName = N'Kỹ Năng Sống - Tâm Lý')
    INSERT INTO Categories (CategoryName, Status) VALUES (N'Kỹ Năng Sống - Tâm Lý', 1);

IF NOT EXISTS (SELECT 1 FROM Categories WHERE CategoryName = N'Công Nghệ - Khoa Học')
    INSERT INTO Categories (CategoryName, Status) VALUES (N'Công Nghệ - Khoa Học', 1);

IF NOT EXISTS (SELECT 1 FROM Categories WHERE CategoryName = N'Truyện Tranh - Thiếu Nhi')
    INSERT INTO Categories (CategoryName, Status) VALUES (N'Truyện Tranh - Thiếu Nhi', 1);
GO

-- 2. THÊM 5 TÀI KHOẢN (Mật khẩu đã mã hóa BCrypt cho "123456", đã kích hoạt Email)
-- Tài khoản 1: Admin
IF NOT EXISTS (SELECT 1 FROM Users WHERE Username = 'admin')
BEGIN
    INSERT INTO Users (Username, Password, FullName, Email, Provider, IsEmailVerified, Phone, Address, Role, Status)
    VALUES (N'admin', N'$2a$10$s7hvVL8HeHQZ6bgRl0y0C.2TYZ1wsFd262P8/XqOJuac6LphLIusG', N'Quản Trị Viên', N'admin@hakitahu.com', 'LOCAL', 1, N'0901111111', N'Số 123 Đường Nguyễn Huệ, Phường Bến Nghé, Quận 1, TP. Hồ Chí Minh', N'admin', 1);
END
ELSE
BEGIN
    UPDATE Users SET Role = 'admin', IsEmailVerified = 1, Password = N'$2a$10$s7hvVL8HeHQZ6bgRl0y0C.2TYZ1wsFd262P8/XqOJuac6LphLIusG' WHERE Username = 'admin';
END

-- Tài khoản 2: Shipper
IF NOT EXISTS (SELECT 1 FROM Users WHERE Username = 'shipper')
BEGIN
    INSERT INTO Users (Username, Password, FullName, Email, Provider, IsEmailVerified, Phone, Address, Role, Status)
    VALUES (N'shipper', N'$2a$10$s7hvVL8HeHQZ6bgRl0y0C.2TYZ1wsFd262P8/XqOJuac6LphLIusG', N'Nguyễn Văn Giao Hàng', N'shipper@hakitahu.com', 'LOCAL', 1, N'0902222222', N'Số 456 Đường CMT8, Phường 11, Quận 3, TP. Hồ Chí Minh', N'shipper', 1);
END
ELSE
BEGIN
    UPDATE Users SET Role = 'shipper', IsEmailVerified = 1, Password = N'$2a$10$s7hvVL8HeHQZ6bgRl0y0C.2TYZ1wsFd262P8/XqOJuac6LphLIusG' WHERE Username = 'shipper';
END

-- Tài khoản 3: Khách hàng 1 (User)
IF NOT EXISTS (SELECT 1 FROM Users WHERE Username = 'user1')
BEGIN
    INSERT INTO Users (Username, Password, FullName, Email, Provider, IsEmailVerified, Phone, Address, Role, Status)
    VALUES (N'user1', N'$2a$10$s7hvVL8HeHQZ6bgRl0y0C.2TYZ1wsFd262P8/XqOJuac6LphLIusG', N'Trần Thị Mai', N'user1@gmail.com', 'LOCAL', 1, N'0903333333', N'78 Đường Lê Duẩn, Phường Bến Nghé, Quận 1, TP. Hồ Chí Minh', N'user', 1);
END

-- Tài khoản 4: Khách hàng 2 (User)
IF NOT EXISTS (SELECT 1 FROM Users WHERE Username = 'user2')
BEGIN
    INSERT INTO Users (Username, Password, FullName, Email, Provider, IsEmailVerified, Phone, Address, Role, Status)
    VALUES (N'user2', N'$2a$10$s7hvVL8HeHQZ6bgRl0y0C.2TYZ1wsFd262P8/XqOJuac6LphLIusG', N'Lê Hoàng Long', N'user2@gmail.com', 'LOCAL', 1, N'0904444444', N'12/3 Đường Hoàng Hoa Thám, Phường 13, Quận Tân Bình, TP. Hồ Chí Minh', N'user', 1);
END

-- Tài khoản 5: Khách hàng 3 (User)
IF NOT EXISTS (SELECT 1 FROM Users WHERE Username = 'user3')
BEGIN
    INSERT INTO Users (Username, Password, FullName, Email, Provider, IsEmailVerified, Phone, Address, Role, Status)
    VALUES (N'user3', N'$2a$10$s7hvVL8HeHQZ6bgRl0y0C.2TYZ1wsFd262P8/XqOJuac6LphLIusG', N'Phạm Thu Hà', N'user3@gmail.com', 'LOCAL', 1, N'0905555555', N'99 Đường Nguyễn Thị Minh Khai, Phường 6, Quận 3, TP. Hồ Chí Minh', N'user', 1);
END
GO

-- Tự động khởi tạo Giỏ hàng cho người dùng nếu chưa có
INSERT INTO Carts (UserID)
SELECT UserID FROM Users u
WHERE NOT EXISTS (SELECT 1 FROM Carts c WHERE c.UserID = u.UserID);
GO

-- 3. THÊM 50 CUỐN SÁCH TIÊU BIỂU (10 cuốn mỗi danh mục)
DECLARE @CatVanHoc INT = (SELECT TOP 1 CategoryID FROM Categories WHERE CategoryName = N'Văn Học - Tiểu Thuyết');
DECLARE @CatKinhTe INT = (SELECT TOP 1 CategoryID FROM Categories WHERE CategoryName = N'Kinh Tế - Kinh Doanh');
DECLARE @CatKyNang INT = (SELECT TOP 1 CategoryID FROM Categories WHERE CategoryName = N'Kỹ Năng Sống - Tâm Lý');
DECLARE @CatCongNghe INT = (SELECT TOP 1 CategoryID FROM Categories WHERE CategoryName = N'Công Nghệ - Khoa Học');
DECLARE @CatThieuNhi INT = (SELECT TOP 1 CategoryID FROM Categories WHERE CategoryName = N'Truyện Tranh - Thiếu Nhi');

-- ==================== DANH MỤC 1: VĂN HỌC - TIỂU THUYẾT (10 cuốn) ====================
IF NOT EXISTS (SELECT 1 FROM Books WHERE Title = N'Cho Tôi Xin Một Vé Đi Tuổi Thơ')
    INSERT INTO Books (Title, Author, Price, Quantity, Description, Image, CategoryID, Status)
    VALUES (N'Cho Tôi Xin Một Vé Đi Tuổi Thơ', N'Nguyễn Nhật Ánh', 85000, 80, N'Tác phẩm đưa độc giả trở về những năm tháng ấu thơ trong veo, dí dỏm và đầy ắp kỷ niệm với chú bé cu Mùi cùng bè bạn.', 'https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=500&auto=format&fit=crop&q=80', @CatVanHoc, 1);

IF NOT EXISTS (SELECT 1 FROM Books WHERE Title = N'Mắt Biếc')
    INSERT INTO Books (Title, Author, Price, Quantity, Description, Image, CategoryID, Status)
    VALUES (N'Mắt Biếc', N'Nguyễn Nhật Ánh', 110000, 95, N'Câu chuyện tình đầu da diết, trong sáng nhưng đầy trắc trở giữa Ngạn và cô bạn mắt biếc Hà Lan tại làng Đo Đo.', 'https://images.unsplash.com/photo-1512820790803-83ca734da794?w=500&auto=format&fit=crop&q=80', @CatVanHoc, 1);

IF NOT EXISTS (SELECT 1 FROM Books WHERE Title = N'Rừng Na Uy')
    INSERT INTO Books (Title, Author, Price, Quantity, Description, Image, CategoryID, Status)
    VALUES (N'Rừng Na Uy', N'Haruki Murakami', 135000, 50, N'Tiểu thuyết kinh điển của văn học Nhật Bản hiện đại, khắc họa nỗi cô đơn và tình yêu tuổi trẻ đầy trầm tư.', 'https://images.unsplash.com/photo-1497633762265-9d179a990aa6?w=500&auto=format&fit=crop&q=80', @CatVanHoc, 1);

IF NOT EXISTS (SELECT 1 FROM Books WHERE Title = N'Nhà Giả Kim')
    INSERT INTO Books (Title, Author, Price, Quantity, Description, Image, CategoryID, Status)
    VALUES (N'Nhà Giả Kim', N'Paulo Coelho', 79000, 120, N'Hành trình phiêu lưu tìm kiếm kho báu của chàng chăn cừu Santiago, truyền cảm hứng mạnh mẽ về việc theo đuổi ước mơ.', 'https://images.unsplash.com/photo-1543002588-bfa74002ed7e?w=500&auto=format&fit=crop&q=80', @CatVanHoc, 1);

IF NOT EXISTS (SELECT 1 FROM Books WHERE Title = N'Cây Cam Ngọt Của Tôi')
    INSERT INTO Books (Title, Author, Price, Quantity, Description, Image, CategoryID, Status)
    VALUES (N'Cây Cam Ngọt Của Tôi', N'José Mauro de Vasconcelos', 108000, 110, N'Cuốn tự truyện lay động hàng triệu trái tim về cậu bé Zezé tinh nghịch với tâm hồn nhạy cảm và cây cam biết lắng nghe.', 'https://images.unsplash.com/photo-1532012164546-f432f2e3777f?w=500&auto=format&fit=crop&q=80', @CatVanHoc, 1);

IF NOT EXISTS (SELECT 1 FROM Books WHERE Title = N'Hoàng Tử Bé')
    INSERT INTO Books (Title, Author, Price, Quantity, Description, Image, CategoryID, Status)
    VALUES (N'Hoàng Tử Bé', N'Antoine de Saint-Exupéry', 65000, 85, N'Tác phẩm văn học bất hủ mang triết lý nhân sinh sâu sắc qua đôi mắt ngây thơ của hoàng tử nhỏ đến từ tiểu tinh cầu B612.', 'https://images.unsplash.com/photo-1589829085413-56de8ae18c73?w=500&auto=format&fit=crop&q=80', @CatVanHoc, 1);

IF NOT EXISTS (SELECT 1 FROM Books WHERE Title = N'Hai Số Phận')
    INSERT INTO Books (Title, Author, Price, Quantity, Description, Image, CategoryID, Status)
    VALUES (N'Hai Số Phận', N'Jeffrey Archer', 175000, 40, N'Thiên tiểu thuyết hoành tráng kể về cuộc đời đối đầu sinh tử của hai người đàn ông sinh cùng một ngày tại hai thế giới đối lập.', 'https://images.unsplash.com/photo-1516979187457-637abb4f9353?w=500&auto=format&fit=crop&q=80', @CatVanHoc, 1);

IF NOT EXISTS (SELECT 1 FROM Books WHERE Title = N'Không Gia Đình')
    INSERT INTO Books (Title, Author, Price, Quantity, Description, Image, CategoryID, Status)
    VALUES (N'Không Gia Đình', N'Hector Malot', 145000, 60, N'Hành trình lưu lạc gian nan đầy nghị lực của cậu bé Rémi mồ côi cùng gánh xiếc rong của cụ Vitalis trên khắp nước Pháp.', 'https://images.unsplash.com/photo-1495446815901-a7297e633e8d?w=500&auto=format&fit=crop&q=80', @CatVanHoc, 1);

IF NOT EXISTS (SELECT 1 FROM Books WHERE Title = N'Trăm Năm Cô Đơn')
    INSERT INTO Books (Title, Author, Price, Quantity, Description, Image, CategoryID, Status)
    VALUES (N'Trăm Năm Cô Đơn', N'Gabriel García Márquez', 190000, 35, N'Kiệt tác văn học hiện thực huyền ảo đoạt giải Nobel về dòng họ Buendia qua 7 thế hệ tại ngôi làng hư cấu Macondo.', 'https://images.unsplash.com/photo-1476275466078-4007374efbbe?w=500&auto=format&fit=crop&q=80', @CatVanHoc, 1);

IF NOT EXISTS (SELECT 1 FROM Books WHERE Title = N'Tiếng Chim Hót Trong Bụi Mận Gai')
    INSERT INTO Books (Title, Author, Price, Quantity, Description, Image, CategoryID, Status)
    VALUES (N'Tiếng Chim Hót Trong Bụi Mận Gai', N'Colleen McCullough', 185000, 45, N'Bản tình ca bi tráng và say đắm của nước Úc, xoay quanh mối tình đầy dằn vặt giữa Meggie và cha xứ Ralph de Bricassart.', 'https://images.unsplash.com/photo-1457369804613-52c61a468e7d?w=500&auto=format&fit=crop&q=80', @CatVanHoc, 1);

-- ==================== DANH MỤC 2: KINH TẾ - KINH DOANH (10 cuốn) ====================
IF NOT EXISTS (SELECT 1 FROM Books WHERE Title = N'Cha Giàu Cha Nghèo')
    INSERT INTO Books (Title, Author, Price, Quantity, Description, Image, CategoryID, Status)
    VALUES (N'Cha Giàu Cha Nghèo', N'Robert T. Kiyosaki', 125000, 100, N'Cuốn sách tài chính cá nhân bán chạy nhất mọi thời đại, thay đổi tư duy làm giàu và khái niệm tài sản - tiêu sản.', 'https://images.unsplash.com/photo-1592496431122-2349e0fbc666?w=500&auto=format&fit=crop&q=80', @CatKinhTe, 1);

IF NOT EXISTS (SELECT 1 FROM Books WHERE Title = N'Khởi Nghiệp Tinh Gọn')
    INSERT INTO Books (Title, Author, Price, Quantity, Description, Image, CategoryID, Status)
    VALUES (N'Khởi Nghiệp Tinh Gọn', N'Eric Ries', 145000, 65, N'Phương pháp xây dựng doanh nghiệp đột phá thông qua thử nghiệm nhanh, học hỏi có kiểm chứng và tối ưu hóa chi phí.', 'https://images.unsplash.com/photo-1553729459-efe14ef6055d?w=500&auto=format&fit=crop&q=80', @CatKinhTe, 1);

IF NOT EXISTS (SELECT 1 FROM Books WHERE Title = N'Đắc Nhân Tâm')
    INSERT INTO Books (Title, Author, Price, Quantity, Description, Image, CategoryID, Status)
    VALUES (N'Đắc Nhân Tâm', N'Dale Carnegie', 86000, 150, N'Cẩm nang kinh điển về nghệ thuật giao tiếp, thấu hiểu con người và xây dựng các mối quan hệ bền vững trong cuộc sống.', 'https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=500&auto=format&fit=crop&q=80', @CatKinhTe, 1);

IF NOT EXISTS (SELECT 1 FROM Books WHERE Title = N'Nghĩ Giàu Và Làm Giàu')
    INSERT INTO Books (Title, Author, Price, Quantity, Description, Image, CategoryID, Status)
    VALUES (N'Nghĩ Giàu Và Làm Giàu', N'Napoleon Hill', 110000, 90, N'13 nguyên tắc vàng tạo dựng của cải và thành công được đúc kết từ 500 nhân vật kiệt xuất nhất nước Mỹ.', 'https://images.unsplash.com/photo-1507842229451-79714902636a?w=500&auto=format&fit=crop&q=80', @CatKinhTe, 1);

IF NOT EXISTS (SELECT 1 FROM Books WHERE Title = N'Tâm Lý Học Về Tiền')
    INSERT INTO Books (Title, Author, Price, Quantity, Description, Image, CategoryID, Status)
    VALUES (N'Tâm Lý Học Về Tiền', N'Morgan Housel', 139000, 85, N'Khám phá cách con người suy nghĩ về tiền bạc, sự giàu có và những sai lầm tâm lý thường gặp khi đầu tư tài chính.', 'https://images.unsplash.com/photo-1554224155-8d04cb21cd6c?w=500&auto=format&fit=crop&q=80', @CatKinhTe, 1);

IF NOT EXISTS (SELECT 1 FROM Books WHERE Title = N'Bí Mật Tư Duy Triệu Phú')
    INSERT INTO Books (Title, Author, Price, Quantity, Description, Image, CategoryID, Status)
    VALUES (N'Bí Mật Tư Duy Triệu Phú', N'T. Harv Eker', 98000, 75, N'Lập trình lại kế hoạch tài chính trong tâm thức và áp dụng 17 quy tắc tư duy thịnh vượng của người giàu.', 'https://images.unsplash.com/photo-1579621970563-ebec7560ff3e?w=500&auto=format&fit=crop&q=80', @CatKinhTe, 1);

IF NOT EXISTS (SELECT 1 FROM Books WHERE Title = N'Dốc Hết Trái Tim (Starbucks)')
    INSERT INTO Books (Title, Author, Price, Quantity, Description, Image, CategoryID, Status)
    VALUES (N'Dốc Hết Trái Tim (Starbucks)', N'Howard Schultz', 165000, 40, N'Câu chuyện xây dựng đế chế cà phê Starbucks từ một cửa hàng nhỏ ở Seattle trở thành thương hiệu biểu tượng toàn cầu.', 'https://images.unsplash.com/photo-1501339847302-ac426a4a7cbb?w=500&auto=format&fit=crop&q=80', @CatKinhTe, 1);

IF NOT EXISTS (SELECT 1 FROM Books WHERE Title = N'Không Đến Một')
    INSERT INTO Books (Title, Author, Price, Quantity, Description, Image, CategoryID, Status)
    VALUES (N'Không Đến Một', N'Peter Thiel', 128000, 60, N'Bí quyết sáng tạo tương lai, xây dựng doanh nghiệp độc quyền mang lại giá trị đột phá từ con số 0 đến số 1.', 'https://images.unsplash.com/photo-1460925895917-afdab827c52f?w=500&auto=format&fit=crop&q=80', @CatKinhTe, 1);

IF NOT EXISTS (SELECT 1 FROM Books WHERE Title = N'Quốc Gia Khởi Nghiệp')
    INSERT INTO Books (Title, Author, Price, Quantity, Description, Image, CategoryID, Status)
    VALUES (N'Quốc Gia Khởi Nghiệp', N'Dan Senor, Saul Singer', 135000, 55, N'Câu chuyện thần kỳ về nền kinh tế đổi mới sáng tạo và tinh thần khởi nghiệp vượt nghịch cảnh của đất nước Israel.', 'https://images.unsplash.com/photo-1526304640581-d334cdbbf45e?w=500&auto=format&fit=crop&q=80', @CatKinhTe, 1);

IF NOT EXISTS (SELECT 1 FROM Books WHERE Title = N'Chiến Lược Đại Dương Xanh')
    INSERT INTO Books (Title, Author, Price, Quantity, Description, Image, CategoryID, Status)
    VALUES (N'Chiến Lược Đại Dương Xanh', N'W. Chan Kim, Renée Mauborgne', 160000, 50, N'Cách thức tạo ra khoảng trống thị trường không có cạnh tranh và khiến đối thủ trở nên không còn quan trọng.', 'https://images.unsplash.com/photo-1507679799987-c73779587ccf?w=500&auto=format&fit=crop&q=80', @CatKinhTe, 1);

-- ==================== DANH MỤC 3: KỸ NĂNG SỐNG - TÂM LÝ (10 cuốn) ====================
IF NOT EXISTS (SELECT 1 FROM Books WHERE Title = N'Đọc Vị Bất Kỳ Ai')
    INSERT INTO Books (Title, Author, Price, Quantity, Description, Image, CategoryID, Status)
    VALUES (N'Đọc Vị Bất Kỳ Ai', N'David J. Lieberman', 89000, 110, N'Phương pháp tâm lý học ứng dụng giúp bạn nắm bắt suy nghĩ, cảm xúc và động cơ thật sự của người đối diện.', 'https://images.unsplash.com/photo-1506126613408-eca07ce68773?w=500&auto=format&fit=crop&q=80', @CatKyNang, 1);

IF NOT EXISTS (SELECT 1 FROM Books WHERE Title = N'Sức Mạnh Của Thói Quen')
    INSERT INTO Books (Title, Author, Price, Quantity, Description, Image, CategoryID, Status)
    VALUES (N'Sức Mạnh Của Thói Quen', N'Charles Duhigg', 135000, 70, N'Giải mã cơ chế hình thành thói quen trong não bộ và bí quyết tái lập trình những thói quen tích cực để thành công.', 'https://images.unsplash.com/photo-1499750310107-5fef28a66643?w=500&auto=format&fit=crop&q=80', @CatKyNang, 1);

IF NOT EXISTS (SELECT 1 FROM Books WHERE Title = N'Thao Túng Tâm Lý')
    INSERT INTO Books (Title, Author, Price, Quantity, Description, Image, CategoryID, Status)
    VALUES (N'Thao Túng Tâm Lý', N'Shannon Thomas', 120000, 80, N'Nhận diện các dấu hiệu lạm dụng tâm lý ngầm độc hại và từng bước chữa lành vết thương để lấy lại quyền tự chủ.', 'https://images.unsplash.com/photo-1518495973542-4542c06a5843?w=500&auto=format&fit=crop&q=80', @CatKyNang, 1);

IF NOT EXISTS (SELECT 1 FROM Books WHERE Title = N'Quẳng Gánh Lo Đi Và Vui Sống')
    INSERT INTO Books (Title, Author, Price, Quantity, Description, Image, CategoryID, Status)
    VALUES (N'Quẳng Gánh Lo Đi Và Vui Sống', N'Dale Carnegie', 88000, 95, N'Những lời khuyên thông tuệ và phương pháp thực tế giúp giải phóng tâm trí khỏi căng thẳng, bất an trong cuộc sống.', 'https://images.unsplash.com/photo-1471107340929-a87cd0f5b5f3?w=500&auto=format&fit=crop&q=80', @CatKyNang, 1);

IF NOT EXISTS (SELECT 1 FROM Books WHERE Title = N'Hạt Giống Tâm Hồn')
    INSERT INTO Books (Title, Author, Price, Quantity, Description, Image, CategoryID, Status)
    VALUES (N'Hạt Giống Tâm Hồn', N'Jack Canfield', 75000, 100, N'Tuyển tập những câu chuyện ngắn giàu cảm xúc về lòng nhân ái, tình yêu thương và niềm tin vào cuộc sống.', 'https://images.unsplash.com/photo-1490730141103-6cac27aaab94?w=500&auto=format&fit=crop&q=80', @CatKyNang, 1);

IF NOT EXISTS (SELECT 1 FROM Books WHERE Title = N'Khéo Ăn Nói Sẽ Có Được Thiên Hạ')
    INSERT INTO Books (Title, Author, Price, Quantity, Description, Image, CategoryID, Status)
    VALUES (N'Khéo Ăn Nói Sẽ Có Được Thiên Hạ', N'Trác Nhã', 115000, 85, N'Bí quyết làm chủ lời nói, ứng biến linh hoạt trong mọi tình huống giao tiếp từ công việc đến đời thường.', 'https://images.unsplash.com/photo-1522202176988-66273c2fd55f?w=500&auto=format&fit=crop&q=80', @CatKyNang, 1);

IF NOT EXISTS (SELECT 1 FROM Books WHERE Title = N'Đời Thay Đổi Khi Chúng Ta Thay Đổi')
    INSERT INTO Books (Title, Author, Price, Quantity, Description, Image, CategoryID, Status)
    VALUES (N'Đời Thay Đổi Khi Chúng Ta Thay Đổi', N'Andrew Matthews', 72000, 90, N'Góc nhìn hài hước, lạc quan về cách đối diện với thất bại và tìm kiếm niềm vui giản dị mỗi ngày.', 'https://images.unsplash.com/photo-1492684223066-81342ee5ff30?w=500&auto=format&fit=crop&q=80', @CatKyNang, 1);

IF NOT EXISTS (SELECT 1 FROM Books WHERE Title = N'Nghệ Thuật Tinh Tế Của Việc Đếch Quan Tâm')
    INSERT INTO Books (Title, Author, Price, Quantity, Description, Image, CategoryID, Status)
    VALUES (N'Nghệ Thuật Tinh Tế Của Việc Đếch Quan Tâm', N'Mark Manson', 118000, 105, N'Cuốn sách thách thức các quan niệm truyền thống về hạnh phúc, hướng dẫn cách lựa chọn điều thực sự có ý nghĩa.', 'https://images.unsplash.com/photo-1517841905240-472988babdf9?w=500&auto=format&fit=crop&q=80', @CatKyNang, 1);

IF NOT EXISTS (SELECT 1 FROM Books WHERE Title = N'Dám Bị Ghét')
    INSERT INTO Books (Title, Author, Price, Quantity, Description, Image, CategoryID, Status)
    VALUES (N'Dám Bị Ghét', N'Kishimi Ichiro, Koga Fumitake', 105000, 115, N'Khám phá tâm lý học Alfred Adler thông qua cuộc đối thoại giữa triết gia và chàng thanh niên về tự do và hạnh phúc.', 'https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?w=500&auto=format&fit=crop&q=80', @CatKyNang, 1);

IF NOT EXISTS (SELECT 1 FROM Books WHERE Title = N'Đi Tìm Lẽ Sống')
    INSERT INTO Books (Title, Author, Price, Quantity, Description, Image, CategoryID, Status)
    VALUES (N'Đi Tìm Lẽ Sống', N'Viktor E. Frankl', 95000, 60, N'Trải nghiệm sống còn phi thường tại trại tập trung Đức Quốc Xã và liệu pháp ý nghĩa giúp con người vượt qua nghịch cảnh.', 'https://images.unsplash.com/photo-1456513080510-7bf3a84b82f8?w=500&auto=format&fit=crop&q=80', @CatKyNang, 1);

-- ==================== DANH MỤC 4: CÔNG NGHỆ - KHOA HỌC (10 cuốn) ====================
IF NOT EXISTS (SELECT 1 FROM Books WHERE Title = N'Sapiens: Lược Sử Loài Người')
    INSERT INTO Books (Title, Author, Price, Quantity, Description, Image, CategoryID, Status)
    VALUES (N'Sapiens: Lược Sử Loài Người', N'Yuval Noah Harari', 195000, 75, N'Một cái nhìn bao quát về hành trình tiến hóa vĩ đại của loài người từ vượn cổ đến khi thống trị Trái Đất.', 'https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=500&auto=format&fit=crop&q=80', @CatCongNghe, 1);

IF NOT EXISTS (SELECT 1 FROM Books WHERE Title = N'Vũ Trụ Trong Vỏ Hạt Dẻ')
    INSERT INTO Books (Title, Author, Price, Quantity, Description, Image, CategoryID, Status)
    VALUES (N'Vũ Trụ Trong Vỏ Hạt Dẻ', N'Stephen Hawking', 145000, 50, N'Khám phá những bí ẩn sâu thẳm nhất của vật lý lý thuyết, lỗ đen, thuyết siêu dây và vũ trụ học hiện đại.', 'https://images.unsplash.com/photo-1462331940025-496dfbfc7564?w=500&auto=format&fit=crop&q=80', @CatCongNghe, 1);

IF NOT EXISTS (SELECT 1 FROM Books WHERE Title = N'Clean Code (Mã Sạch)')
    INSERT INTO Books (Title, Author, Price, Quantity, Description, Image, CategoryID, Status)
    VALUES (N'Clean Code (Mã Sạch)', N'Robert C. Martin', 250000, 40, N'Kinh thánh của mọi lập trình viên phần mềm về các nguyên tắc, kỹ thuật viết mã sạch, dễ bảo trì và mở rộng.', 'https://images.unsplash.com/photo-1555066931-4365d14bab8c?w=500&auto=format&fit=crop&q=80', @CatCongNghe, 1);

IF NOT EXISTS (SELECT 1 FROM Books WHERE Title = N'Lược Sử Thời Gian')
    INSERT INTO Books (Title, Author, Price, Quantity, Description, Image, CategoryID, Status)
    VALUES (N'Lược Sử Thời Gian', N'Stephen Hawking', 115000, 65, N'Tác phẩm phổ biến khoa học kinh điển giải thích về nguồn gốc và số phận của vũ trụ từ vụ nổ Big Bang.', 'https://images.unsplash.com/photo-1506703719100-a0f3a48c0f86?w=500&auto=format&fit=crop&q=80', @CatCongNghe, 1);

IF NOT EXISTS (SELECT 1 FROM Books WHERE Title = N'Trí Tuệ Nhân Tạo: Kỷ Nguyên AI')
    INSERT INTO Books (Title, Author, Price, Quantity, Description, Image, CategoryID, Status)
    VALUES (N'Trí Tuệ Nhân Tạo: Kỷ Nguyên AI', N'Kai-Fu Lee', 168000, 55, N'Phân tích sâu sắc về tương lai của AI, làn sóng tự động hóa và trật tự công nghệ thế giới mới.', 'https://images.unsplash.com/photo-1677442136019-21780ecad995?w=500&auto=format&fit=crop&q=80', @CatCongNghe, 1);

IF NOT EXISTS (SELECT 1 FROM Books WHERE Title = N'Cuộc Cách Mạng Công Nghiệp Lần Thứ Tư')
    INSERT INTO Books (Title, Author, Price, Quantity, Description, Image, CategoryID, Status)
    VALUES (N'Cuộc Cách Mạng Công Nghiệp Lần Thứ Tư', N'Klaus Schwab', 130000, 60, N'Bức tranh toàn cảnh về sự hội tụ của công nghệ số, vật lý và sinh học đang tái định hình thế giới.', 'https://images.unsplash.com/photo-1485827404703-89b55fcc595e?w=500&auto=format&fit=crop&q=80', @CatCongNghe, 1);

IF NOT EXISTS (SELECT 1 FROM Books WHERE Title = N'Gen: Lược Sử Và Tương Lai')
    INSERT INTO Books (Title, Author, Price, Quantity, Description, Image, CategoryID, Status)
    VALUES (N'Gen: Lược Sử Và Tương Lai', N'Siddhartha Mukherjee', 225000, 35, N'Câu chuyện sinh động và đầy mê hoặc về mã di truyền, sự sống con người và tương lai công nghệ chỉnh sửa gen.', 'https://images.unsplash.com/photo-1530497610245-94d3c16cda28?w=500&auto=format&fit=crop&q=80', @CatCongNghe, 1);

IF NOT EXISTS (SELECT 1 FROM Books WHERE Title = N'Design Patterns (Mẫu Thiết Kế)')
    INSERT INTO Books (Title, Author, Price, Quantity, Description, Image, CategoryID, Status)
    VALUES (N'Design Patterns (Mẫu Thiết Kế)', N'Erich Gamma, Richard Helm', 240000, 45, N'23 mẫu thiết kế hướng đối tượng kinh điển giúp giải quyết các bài toán kiến trúc phần mềm phức tạp.', 'https://images.unsplash.com/photo-1517694712202-14dd9538aa97?w=500&auto=format&fit=crop&q=80', @CatCongNghe, 1);

IF NOT EXISTS (SELECT 1 FROM Books WHERE Title = N'The Pragmatic Programmer')
    INSERT INTO Books (Title, Author, Price, Quantity, Description, Image, CategoryID, Status)
    VALUES (N'The Pragmatic Programmer', N'Andrew Hunt, David Thomas', 260000, 30, N'Cẩm nang rèn luyện tư duy của một kỹ sư phần mềm thực thụ: từ trách nhiệm, tự động hóa đến kỹ năng nghề nghiệp.', 'https://images.unsplash.com/photo-1526374965328-7f61d4dc18c5?w=500&auto=format&fit=crop&q=80', @CatCongNghe, 1);

IF NOT EXISTS (SELECT 1 FROM Books WHERE Title = N'Bản Thiết Kế Vĩ Đại')
    INSERT INTO Books (Title, Author, Price, Quantity, Description, Image, CategoryID, Status)
    VALUES (N'Bản Thiết Kế Vĩ Đại', N'Stephen Hawking, Leonard Mlodinow', 110000, 50, N'Khám phá những câu hỏi lớn nhất về vũ trụ: Tại sao lại có một vũ trụ? Và tại sao vũ trụ lại được vận hành như thế này?', 'https://images.unsplash.com/photo-1446776811953-b23d57bd21aa?w=500&auto=format&fit=crop&q=80', @CatCongNghe, 1);

-- ==================== DANH MỤC 5: TRUYỆN TRANH - THIẾU NHI (10 cuốn) ====================
IF NOT EXISTS (SELECT 1 FROM Books WHERE Title = N'Doraemon Tuyển Tập Tranh Truyện Màu')
    INSERT INTO Books (Title, Author, Price, Quantity, Description, Image, CategoryID, Status)
    VALUES (N'Doraemon Tuyển Tập Tranh Truyện Màu', N'Fujiko F. Fujio', 45000, 200, N'Tuyển tập những cuộc phiêu lưu kỳ thú với các bảo bối thần kỳ của chú mèo máy thông minh đến từ tương lai.', 'https://images.unsplash.com/photo-1578632767115-351597cf2477?w=500&auto=format&fit=crop&q=80', @CatThieuNhi, 1);

IF NOT EXISTS (SELECT 1 FROM Books WHERE Title = N'Thám Tử Lừng Danh Conan Tập 100')
    INSERT INTO Books (Title, Author, Price, Quantity, Description, Image, CategoryID, Status)
    VALUES (N'Thám Tử Lừng Danh Conan Tập 100', N'Gosho Aoyama', 30000, 250, N'Cột mốc tập thứ 100 đầy gay cấn với những vụ án hóc búa và cuộc đối đầu nghẹt thở cùng Tổ chức Áo đen.', 'https://images.unsplash.com/photo-1607604276583-eef5d076aa5f?w=500&auto=format&fit=crop&q=80', @CatThieuNhi, 1);

IF NOT EXISTS (SELECT 1 FROM Books WHERE Title = N'One Piece Tuyển Tập Đại Hải Trình')
    INSERT INTO Books (Title, Author, Price, Quantity, Description, Image, CategoryID, Status)
    VALUES (N'One Piece Tuyển Tập Đại Hải Trình', N'Eiichiro Oda', 35000, 180, N'Chuyến hải trình tìm kiếm kho báu One Piece huyền thoại của Monkey D. Luffy và băng hải tặc Mũ Rơm.', 'https://images.unsplash.com/photo-1563089145-599997674d42?w=500&auto=format&fit=crop&q=80', @CatThieuNhi, 1);

IF NOT EXISTS (SELECT 1 FROM Books WHERE Title = N'Dế Mèn Phiêu Lưu Ký')
    INSERT INTO Books (Title, Author, Price, Quantity, Description, Image, CategoryID, Status)
    VALUES (N'Dế Mèn Phiêu Lưu Ký', N'Tô Hoài', 55000, 140, N'Tác phẩm thiếu nhi kinh điển của văn học Việt Nam về bài học trưởng thành và lý tưởng hòa bình của chú Dế Mèn.', 'https://images.unsplash.com/photo-1512820790803-83ca734da794?w=500&auto=format&fit=crop&q=80', @CatThieuNhi, 1);

IF NOT EXISTS (SELECT 1 FROM Books WHERE Title = N'Chuyện Con Mèo Dạy Hải Âu Bay')
    INSERT INTO Books (Title, Author, Price, Quantity, Description, Image, CategoryID, Status)
    VALUES (N'Chuyện Con Mèo Dạy Hải Âu Bay', N'Luis Sepúlveda', 68000, 120, N'Câu chuyện ấm áp, cảm động về lời hứa danh dự của chú mèo mập Zorba và tình mẫu tử vượt qua mọi rào cản giống loài.', 'https://images.unsplash.com/photo-1535905557558-afc4877a26fc?w=500&auto=format&fit=crop&q=80', @CatThieuNhi, 1);

IF NOT EXISTS (SELECT 1 FROM Books WHERE Title = N'Totto-chan Bên Cửa Sổ')
    INSERT INTO Books (Title, Author, Price, Quantity, Description, Image, CategoryID, Status)
    VALUES (N'Totto-chan Bên Cửa Sổ', N'Kuroyanagi Tetsuko', 89000, 100, N'Hồi ức tuyệt đẹp về ngôi trường Tomoe tiểu học với lớp học là toa tàu điện và phương pháp giáo dục chan chứa tình yêu.', 'https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=500&auto=format&fit=crop&q=80', @CatThieuNhi, 1);

IF NOT EXISTS (SELECT 1 FROM Books WHERE Title = N'Kính Vạn Hoa Tuyển Tập')
    INSERT INTO Books (Title, Author, Price, Quantity, Description, Image, CategoryID, Status)
    VALUES (N'Kính Vạn Hoa Tuyển Tập', N'Nguyễn Nhật Ánh', 150000, 60, N'Bộ truyện học trò nổi tiếng gắn liền với tuổi thơ thế hệ học sinh Việt Nam qua bộ ba Quý ròm, Tiểu Long và nhỏ Hạnh.', 'https://images.unsplash.com/photo-1516979187457-637abb4f9353?w=500&auto=format&fit=crop&q=80', @CatThieuNhi, 1);

IF NOT EXISTS (SELECT 1 FROM Books WHERE Title = N'Góc Sân Và Khoảng Trời')
    INSERT INTO Books (Title, Author, Price, Quantity, Description, Image, CategoryID, Status)
    VALUES (N'Góc Sân Và Khoảng Trời', N'Trần Đăng Khoa', 65000, 90, N'Những vần thơ trong sáng, mộc mạc vẽ nên bức tranh làng quê Việt Nam thanh bình qua lăng kính tuổi thơ kỳ diệu.', 'https://images.unsplash.com/photo-1506126613408-eca07ce68773?w=500&auto=format&fit=crop&q=80', @CatThieuNhi, 1);

IF NOT EXISTS (SELECT 1 FROM Books WHERE Title = N'Thần Đồng Đất Việt')
    INSERT INTO Books (Title, Author, Price, Quantity, Description, Image, CategoryID, Status)
    VALUES (N'Thần Đồng Đất Việt', N'Lê Linh', 25000, 150, N'Bộ truyện tranh lịch sử - văn hóa dân gian Việt Nam đầy ắp tiếng cười trí tuệ xoay quanh Trạng Tí và nhóm bạn.', 'https://images.unsplash.com/photo-1579783902614-a3fb3927b675?w=500&auto=format&fit=crop&q=80', @CatThieuNhi, 1);

IF NOT EXISTS (SELECT 1 FROM Books WHERE Title = N'Dragon Ball (7 Viên Ngọc Rồng)')
    INSERT INTO Books (Title, Author, Price, Quantity, Description, Image, CategoryID, Status)
    VALUES (N'Dragon Ball (7 Viên Ngọc Rồng)', N'Akira Toriyama', 32000, 160, N'Huyền thoại manga thế giới theo chân Son Goku từ cậu bé đuôi khỉ ngây thơ đến chiến binh bảo vệ vũ trụ kiên cường.', 'https://images.unsplash.com/photo-1618336753974-aae8e04506aa?w=500&auto=format&fit=crop&q=80', @CatThieuNhi, 1);
GO

-- ===================================================================================
-- KIỂM TRA LẠI DỮ LIỆU ĐÃ TẠO
-- ===================================================================================
SELECT 'Users' AS TableName, COUNT(*) AS TotalCount FROM Users
UNION ALL
SELECT 'Categories', COUNT(*) FROM Categories
UNION ALL
SELECT 'Books', COUNT(*) FROM Books;
GO

SELECT UserID, Username, Role, FullName, Email, IsEmailVerified, Status FROM Users;
GO

SELECT CategoryID, CategoryName, Status FROM Categories;
GO

SELECT TOP 10 BookID, Title, Author, Price, Quantity, CategoryID FROM Books;
GO