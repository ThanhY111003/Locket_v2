-- ============================================================
-- Locket Finance — PostgreSQL Database Schema
-- Chạy file này trên PostgreSQL trước khi khởi động backend
-- ============================================================

-- Bảng người dùng
CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    full_name VARCHAR(100),
    profile_picture_url VARCHAR(255),
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now()
);

-- Bảng danh mục chi tiêu
CREATE TABLE IF NOT EXISTS categories (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL,
    description TEXT
);

-- Bảng bài đăng (kết hợp ảnh và thông tin giao dịch)
-- Mỗi post sẽ có một transaction đi kèm
CREATE TABLE IF NOT EXISTS posts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id),
    image_url VARCHAR(255) NOT NULL,
    caption TEXT,
    created_at TIMESTAMPTZ DEFAULT now()
);

-- Bảng giao dịch
CREATE TABLE IF NOT EXISTS transactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    post_id UUID UNIQUE NOT NULL REFERENCES posts(id) ON DELETE CASCADE, -- Liên kết 1-1 với posts
    user_id UUID NOT NULL REFERENCES users(id),
    amount DECIMAL(12, 2) NOT NULL,
    category_id INTEGER REFERENCES categories(id),
    transaction_date DATE NOT NULL,
    notes TEXT,
    created_at TIMESTAMPTZ DEFAULT now()
);

-- Bảng quan hệ bạn bè
CREATE TABLE IF NOT EXISTS friendships (
    user_id_1 UUID NOT NULL REFERENCES users(id),
    user_id_2 UUID NOT NULL REFERENCES users(id),
    status VARCHAR(20) NOT NULL CHECK (status IN ('PENDING', 'ACCEPTED', 'BLOCKED')),
    created_at TIMESTAMPTZ DEFAULT now(),
    PRIMARY KEY (user_id_1, user_id_2)
);

-- Indexes để tăng tốc độ truy vấn
CREATE INDEX IF NOT EXISTS idx_posts_user_id ON posts(user_id);
CREATE INDEX IF NOT EXISTS idx_posts_created_at ON posts(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_transactions_user_id ON transactions(user_id);
CREATE INDEX IF NOT EXISTS idx_transactions_category_id ON transactions(category_id);
CREATE INDEX IF NOT EXISTS idx_transactions_date ON transactions(transaction_date);
CREATE INDEX IF NOT EXISTS idx_friendships_user_id_2 ON friendships(user_id_2);

-- Seed một vài danh mục cơ bản
INSERT INTO categories (name, description) VALUES
    ('Ăn uống', 'Chi phí ăn uống, nhà hàng, cà phê'),
    ('Mua sắm', 'Quần áo, đồ dùng, mua sắm online'),
    ('Di chuyển', 'Xăng, xe buýt, taxi, Grab'),
    ('Giải trí', 'Phim, game, du lịch, sở thích'),
    ('Hóa đơn', 'Điện, nước, internet, thuê nhà'),
    ('Khác', 'Các khoản chi tiêu khác')
ON CONFLICT (name) DO NOTHING;
