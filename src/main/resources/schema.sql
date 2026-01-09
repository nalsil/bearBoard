-- Schema creation script for multi-tenant homepage
-- 멀티테넌트 기업용 홈페이지 데이터베이스 스키마

-- 1. Company table
CREATE TABLE IF NOT EXISTS company (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(200) NOT NULL,
    logo_url VARCHAR(500),
    primary_color VARCHAR(7),
    secondary_color VARCHAR(7),
    description TEXT,
    address VARCHAR(500),
    phone VARCHAR(20),
    email VARCHAR(100),
    latitude DECIMAL(10,8),
    longitude DECIMAL(11,8),
    founded_date DATE,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_company_code ON company(code);
CREATE INDEX IF NOT EXISTS idx_company_is_active ON company(is_active);

-- 2. Admin table
CREATE TABLE IF NOT EXISTS admin (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('ADMIN', 'SUPER_ADMIN')),
    company_id BIGINT REFERENCES company(id) ON DELETE CASCADE,
    last_login_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_admin_username ON admin(username);
CREATE INDEX IF NOT EXISTS idx_admin_company_id ON admin(company_id);
CREATE INDEX IF NOT EXISTS idx_admin_role ON admin(role);

-- 3. Board table
CREATE TABLE IF NOT EXISTS board (
    id BIGSERIAL PRIMARY KEY,
    company_id BIGINT NOT NULL REFERENCES company(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_board_company_id ON board(company_id);
CREATE INDEX IF NOT EXISTS idx_board_type ON board(type);
CREATE INDEX IF NOT EXISTS idx_board_company_type ON board(company_id, type);

-- 4. Post table
CREATE TABLE IF NOT EXISTS post (
    id BIGSERIAL PRIMARY KEY,
    board_id BIGINT NOT NULL REFERENCES board(id) ON DELETE CASCADE,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    author VARCHAR(100) NOT NULL,
    view_count INTEGER DEFAULT 0,
    file_path VARCHAR(500),
    is_hidden BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_post_board_id ON post(board_id);
CREATE INDEX IF NOT EXISTS idx_post_created_at ON post(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_post_view_count ON post(view_count DESC);
CREATE INDEX IF NOT EXISTS idx_post_is_hidden ON post(is_hidden);
CREATE INDEX IF NOT EXISTS idx_post_board_visible ON post(board_id, is_hidden, created_at DESC);

-- 5. FAQ table
CREATE TABLE IF NOT EXISTS faq (
    id BIGSERIAL PRIMARY KEY,
    company_id BIGINT NOT NULL REFERENCES company(id) ON DELETE CASCADE,
    category VARCHAR(100),
    question VARCHAR(500) NOT NULL,
    answer TEXT NOT NULL,
    display_order INTEGER DEFAULT 0,
    is_hidden BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_faq_company_id ON faq(company_id);
CREATE INDEX IF NOT EXISTS idx_faq_display_order ON faq(display_order ASC);
CREATE INDEX IF NOT EXISTS idx_faq_is_hidden ON faq(is_hidden);
CREATE INDEX IF NOT EXISTS idx_faq_company_visible_order ON faq(company_id, is_hidden, display_order ASC);

-- 6. QnA table
CREATE TABLE IF NOT EXISTS qna (
    id BIGSERIAL PRIMARY KEY,
    company_id BIGINT NOT NULL REFERENCES company(id) ON DELETE CASCADE,
    question_title VARCHAR(200) NOT NULL,
    question_body TEXT NOT NULL,
    asker_email VARCHAR(100) NOT NULL,
    answer_body TEXT,
    answerer_id BIGINT REFERENCES admin(id) ON DELETE SET NULL,
    is_answered BOOLEAN DEFAULT FALSE,
    is_hidden BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT NOW(),
    answered_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_qna_company_id ON qna(company_id);
CREATE INDEX IF NOT EXISTS idx_qna_is_answered ON qna(is_answered);
CREATE INDEX IF NOT EXISTS idx_qna_created_at ON qna(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_qna_asker_email ON qna(asker_email);
CREATE INDEX IF NOT EXISTS idx_qna_company_visible ON qna(company_id, is_hidden, created_at DESC);

-- 7. YouTube Video table
CREATE TABLE IF NOT EXISTS youtube_video (
    id BIGSERIAL PRIMARY KEY,
    company_id BIGINT NOT NULL REFERENCES company(id) ON DELETE CASCADE,
    video_url VARCHAR(500) NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    thumbnail_url VARCHAR(500),
    display_order INTEGER DEFAULT 0,
    is_hidden BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_youtube_company_id ON youtube_video(company_id);
CREATE INDEX IF NOT EXISTS idx_youtube_display_order ON youtube_video(display_order ASC);
CREATE INDEX IF NOT EXISTS idx_youtube_is_hidden ON youtube_video(is_hidden);
CREATE INDEX IF NOT EXISTS idx_youtube_company_visible_order ON youtube_video(company_id, is_hidden, display_order ASC);

-- 8. Product table
CREATE TABLE IF NOT EXISTS product (
    id BIGSERIAL PRIMARY KEY,
    company_id BIGINT NOT NULL REFERENCES company(id) ON DELETE CASCADE,
    name VARCHAR(200) NOT NULL,
    category VARCHAR(100),
    description TEXT,
    price DECIMAL(12,2),
    image_url VARCHAR(500),
    display_order INTEGER DEFAULT 0,
    is_hidden BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_product_company_id ON product(company_id);
CREATE INDEX IF NOT EXISTS idx_product_category ON product(category);
CREATE INDEX IF NOT EXISTS idx_product_display_order ON product(display_order ASC);
CREATE INDEX IF NOT EXISTS idx_product_is_hidden ON product(is_hidden);
CREATE INDEX IF NOT EXISTS idx_product_company_visible_order ON product(company_id, is_hidden, display_order ASC);
