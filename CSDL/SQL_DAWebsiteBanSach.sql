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
CreatedAt DATETIME DEFAULT GETDATE()
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

-- Thêm cột Status cho Users
ALTER TABLE Users
ADD Status BIT DEFAULT 1;
go


-- Tạo bảng Categories
CREATE TABLE Categories (
CategoryID INT IDENTITY PRIMARY KEY,
CategoryName NVARCHAR(100) NOT NULL
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

SELECT * FROM Users
go