# Data Model: 멀티테넌트 기업용 홈페이지

**Date**: 2025-12-30
**Feature**: [spec.md](./spec.md)

## 개요

멀티테넌트 기업용 홈페이지를 위한 데이터베이스 스키마 및 엔티티 모델을 정의한다. PostgreSQL을 사용하며, R2DBC를 통해 리액티브 방식으로 접근한다.

---

## Entity Relationship Diagram (ERD)

```
┌─────────────────┐
│    Company      │
├─────────────────┤
│ id (PK)         │───┐
│ code (UK)       │   │
│ name            │   │
│ logo_url        │   │
│ primary_color   │   │
│ secondary_color │   │
│ description     │   │
│ address         │   │
│ phone           │   │
│ email           │   │
│ latitude        │   │
│ longitude       │   │
│ founded_date    │   │
│ is_active       │   │
│ created_at      │   │
│ updated_at      │   │
└─────────────────┘   │
                      │
         ┌────────────┼────────────────────────────┐
         │            │                            │
         ▼            ▼                            ▼
┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐
│   Admin         │  │   Board         │  │   Product       │
├─────────────────┤  ├─────────────────┤  ├─────────────────┤
│ id (PK)         │  │ id (PK)         │  │ id (PK)         │
│ username (UK)   │  │ company_id (FK) │  │ company_id (FK) │
│ password_hash   │  │ name            │  │ name            │
│ name            │  │ type            │  │ category        │
│ email           │  │ created_at      │  │ description     │
│ role            │  └─────────────────┘  │ price           │
│ company_id (FK) │           │           │ image_url       │
│ last_login_at   │           │           │ display_order   │
│ created_at      │           ▼           │ is_hidden       │
└─────────────────┘  ┌─────────────────┐  │ created_at      │
                     │   Post          │  │ updated_at      │
                     ├─────────────────┤  └─────────────────┘
                     │ id (PK)         │
                     │ board_id (FK)   │           │
                     │ title           │           │
                     │ content         │           ▼
                     │ author          │  ┌─────────────────┐
                     │ view_count      │  │   Faq           │
                     │ file_path       │  ├─────────────────┤
                     │ is_hidden       │  │ id (PK)         │
                     │ created_at      │  │ company_id (FK) │
                     │ updated_at      │  │ category        │
                     └─────────────────┘  │ question        │
                                          │ answer          │
                     ┌─────────────────┐  │ display_order   │
                     │   Qna           │  │ is_hidden       │
                     ├─────────────────┤  │ created_at      │
                     │ id (PK)         │  └─────────────────┘
                     │ company_id (FK) │
                     │ question_title  │  ┌─────────────────┐
                     │ question_body   │  │ YoutubeVideo    │
                     │ asker_email     │  ├─────────────────┤
                     │ answer_body     │  │ id (PK)         │
                     │ answerer_id(FK) │  │ company_id (FK) │
                     │ is_answered     │  │ video_url       │
                     │ is_hidden       │  │ title           │
                     │ created_at      │  │ description     │
                     │ answered_at     │  │ thumbnail_url   │
                     └─────────────────┘  │ display_order   │
                                          │ is_hidden       │
                                          │ created_at      │
                                          └─────────────────┘
```

---

## 엔티티 상세

### 1. Company (기업)

기업 정보 및 브랜딩을 저장한다.

**테이블명**: `company`

| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|----------|------|
| id | BIGSERIAL | PRIMARY KEY | 기업 고유 ID |
| code | VARCHAR(50) | UNIQUE NOT NULL | 기업 코드(URL 경로에 사용, 예: `company-a`) |
| name | VARCHAR(200) | NOT NULL | 기업명 |
| logo_url | VARCHAR(500) | | 로고 이미지 URL |
| primary_color | VARCHAR(7) | | 주 색상(HEX, 예: `#FF5733`) |
| secondary_color | VARCHAR(7) | | 보조 색상(HEX) |
| description | TEXT | | 회사 소개 |
| address | VARCHAR(500) | | 회사 주소 |
| phone | VARCHAR(20) | | 전화번호 |
| email | VARCHAR(100) | | 이메일 |
| latitude | DECIMAL(10,8) | | 위도 |
| longitude | DECIMAL(11,8) | | 경도 |
| founded_date | DATE | | 설립일 |
| is_active | BOOLEAN | DEFAULT TRUE | 활성화 여부 |
| created_at | TIMESTAMP | DEFAULT NOW() | 생성 시각 |
| updated_at | TIMESTAMP | DEFAULT NOW() | 수정 시각 |

**인덱스**:
- `idx_company_code` ON `code` (고유 인덱스, 빠른 조회)
- `idx_company_is_active` ON `is_active` (활성 기업 필터링)

**비즈니스 규칙**:
- `code`는 소문자, 숫자, 하이픈(`-`)만 허용
- `code`는 최소 3자, 최대 50자
- `email`은 이메일 형식 검증 필요

---

### 2. Admin (관리자)

기업별 관리자 및 슈퍼유저 정보를 저장한다.

**테이블명**: `admin`

| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|----------|------|
| id | BIGSERIAL | PRIMARY KEY | 관리자 고유 ID |
| username | VARCHAR(50) | UNIQUE NOT NULL | 로그인 아이디 |
| password_hash | VARCHAR(255) | NOT NULL | 암호화된 비밀번호(BCrypt) |
| name | VARCHAR(100) | NOT NULL | 관리자 이름 |
| email | VARCHAR(100) | NOT NULL | 이메일 |
| role | VARCHAR(20) | NOT NULL | 역할(`ADMIN`, `SUPER_ADMIN`) |
| company_id | BIGINT | FOREIGN KEY(company.id) | 소속 기업 ID(슈퍼유저는 NULL) |
| last_login_at | TIMESTAMP | | 마지막 로그인 시각 |
| created_at | TIMESTAMP | DEFAULT NOW() | 생성 시각 |

**인덱스**:
- `idx_admin_username` ON `username` (고유 인덱스, 로그인 조회)
- `idx_admin_company_id` ON `company_id` (기업별 관리자 조회)
- `idx_admin_role` ON `role` (역할별 관리자 조회)

**비즈니스 규칙**:
- `role`이 `ADMIN`이면 `company_id`는 필수(NULL 불가)
- `role`이 `SUPER_ADMIN`이면 `company_id`는 NULL
- `password_hash`는 BCrypt로 암호화하여 저장

---

### 3. Board (게시판)

기업별 게시판 정보를 저장한다.

**테이블명**: `board`

| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|----------|------|
| id | BIGSERIAL | PRIMARY KEY | 게시판 고유 ID |
| company_id | BIGINT | FOREIGN KEY(company.id) NOT NULL | 소속 기업 ID |
| name | VARCHAR(100) | NOT NULL | 게시판 이름(예: `공지사항`, `보도자료`) |
| type | VARCHAR(50) | NOT NULL | 게시판 타입(예: `notice`, `press`, `recruit`) |
| created_at | TIMESTAMP | DEFAULT NOW() | 생성 시각 |

**인덱스**:
- `idx_board_company_id` ON `company_id` (기업별 게시판 조회)
- `idx_board_type` ON `type` (타입별 게시판 조회)

**복합 인덱스**:
- `idx_board_company_type` ON `(company_id, type)` (기업 + 타입 조합 조회)

---

### 4. Post (게시글)

게시판에 작성된 게시글을 저장한다.

**테이블명**: `post`

| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|----------|------|
| id | BIGSERIAL | PRIMARY KEY | 게시글 고유 ID |
| board_id | BIGINT | FOREIGN KEY(board.id) NOT NULL | 소속 게시판 ID |
| title | VARCHAR(200) | NOT NULL | 제목 |
| content | TEXT | NOT NULL | 내용 |
| author | VARCHAR(100) | NOT NULL | 작성자(관리자 이름) |
| view_count | INTEGER | DEFAULT 0 | 조회수 |
| file_path | VARCHAR(500) | | 첨부파일 경로 |
| is_hidden | BOOLEAN | DEFAULT FALSE | 숨김 여부 |
| created_at | TIMESTAMP | DEFAULT NOW() | 작성 시각 |
| updated_at | TIMESTAMP | DEFAULT NOW() | 수정 시각 |

**인덱스**:
- `idx_post_board_id` ON `board_id` (게시판별 게시글 조회)
- `idx_post_created_at` ON `created_at DESC` (최신순 정렬)
- `idx_post_view_count` ON `view_count DESC` (조회수순 정렬)
- `idx_post_is_hidden` ON `is_hidden` (숨김 필터링)

**복합 인덱스**:
- `idx_post_board_visible` ON `(board_id, is_hidden, created_at DESC)` (게시판별 공개 게시글 최신순 조회)

---

### 5. Faq (자주 묻는 질문)

기업별 FAQ를 저장한다.

**테이블명**: `faq`

| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|----------|------|
| id | BIGSERIAL | PRIMARY KEY | FAQ 고유 ID |
| company_id | BIGINT | FOREIGN KEY(company.id) NOT NULL | 소속 기업 ID |
| category | VARCHAR(100) | | 카테고리(예: `일반`, `상품`, `배송`) |
| question | VARCHAR(500) | NOT NULL | 질문 |
| answer | TEXT | NOT NULL | 답변 |
| display_order | INTEGER | DEFAULT 0 | 정렬 순서(낮은 숫자가 먼저 표시) |
| is_hidden | BOOLEAN | DEFAULT FALSE | 숨김 여부 |
| created_at | TIMESTAMP | DEFAULT NOW() | 생성 시각 |

**인덱스**:
- `idx_faq_company_id` ON `company_id` (기업별 FAQ 조회)
- `idx_faq_display_order` ON `display_order ASC` (정렬 순서)
- `idx_faq_is_hidden` ON `is_hidden` (숨김 필터링)

**복합 인덱스**:
- `idx_faq_company_visible_order` ON `(company_id, is_hidden, display_order ASC)` (기업별 공개 FAQ 정렬순 조회)

---

### 6. Qna (질문과 답변)

사용자 질문과 관리자 답변을 저장한다.

**테이블명**: `qna`

| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|----------|------|
| id | BIGSERIAL | PRIMARY KEY | QnA 고유 ID |
| company_id | BIGINT | FOREIGN KEY(company.id) NOT NULL | 소속 기업 ID |
| question_title | VARCHAR(200) | NOT NULL | 질문 제목 |
| question_body | TEXT | NOT NULL | 질문 내용 |
| asker_email | VARCHAR(100) | NOT NULL | 질문자 이메일 |
| answer_body | TEXT | | 답변 내용 |
| answerer_id | BIGINT | FOREIGN KEY(admin.id) | 답변 작성자(관리자) ID |
| is_answered | BOOLEAN | DEFAULT FALSE | 답변 완료 여부 |
| is_hidden | BOOLEAN | DEFAULT FALSE | 숨김 여부 |
| created_at | TIMESTAMP | DEFAULT NOW() | 질문 작성 시각 |
| answered_at | TIMESTAMP | | 답변 작성 시각 |

**인덱스**:
- `idx_qna_company_id` ON `company_id` (기업별 QnA 조회)
- `idx_qna_is_answered` ON `is_answered` (답변 여부 필터링)
- `idx_qna_created_at` ON `created_at DESC` (최신순 정렬)
- `idx_qna_asker_email` ON `asker_email` (이메일별 QnA 조회)

**복합 인덱스**:
- `idx_qna_company_visible` ON `(company_id, is_hidden, created_at DESC)` (기업별 공개 QnA 최신순 조회)

**비즈니스 규칙**:
- `asker_email`은 이메일 형식 검증 필요
- `is_answered`가 TRUE이면 `answer_body`와 `answerer_id`는 필수

---

### 7. YoutubeVideo (유튜브 영상)

기업별 유튜브 영상 정보를 저장한다.

**테이블명**: `youtube_video`

| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|----------|------|
| id | BIGSERIAL | PRIMARY KEY | 영상 고유 ID |
| company_id | BIGINT | FOREIGN KEY(company.id) NOT NULL | 소속 기업 ID |
| video_url | VARCHAR(500) | NOT NULL | 유튜브 영상 URL |
| title | VARCHAR(200) | NOT NULL | 영상 제목 |
| description | TEXT | | 영상 설명 |
| thumbnail_url | VARCHAR(500) | | 썸네일 URL |
| display_order | INTEGER | DEFAULT 0 | 정렬 순서 |
| is_hidden | BOOLEAN | DEFAULT FALSE | 숨김 여부 |
| created_at | TIMESTAMP | DEFAULT NOW() | 등록 시각 |

**인덱스**:
- `idx_youtube_company_id` ON `company_id` (기업별 영상 조회)
- `idx_youtube_display_order` ON `display_order ASC` (정렬 순서)
- `idx_youtube_is_hidden` ON `is_hidden` (숨김 필터링)

**복합 인덱스**:
- `idx_youtube_company_visible_order` ON `(company_id, is_hidden, display_order ASC)` (기업별 공개 영상 정렬순 조회)

**비즈니스 규칙**:
- `video_url`은 유튜브 URL 형식 검증 필요(예: `https://www.youtube.com/watch?v=...`)

---

### 8. Product (상품)

기업별 상품/서비스 정보를 저장한다.

**테이블명**: `product`

| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|----------|------|
| id | BIGSERIAL | PRIMARY KEY | 상품 고유 ID |
| company_id | BIGINT | FOREIGN KEY(company.id) NOT NULL | 소속 기업 ID |
| name | VARCHAR(200) | NOT NULL | 상품명 |
| category | VARCHAR(100) | | 카테고리 |
| description | TEXT | | 상품 설명 |
| price | DECIMAL(12,2) | | 가격(선택 사항) |
| image_url | VARCHAR(500) | | 상품 이미지 URL |
| display_order | INTEGER | DEFAULT 0 | 정렬 순서 |
| is_hidden | BOOLEAN | DEFAULT FALSE | 숨김 여부 |
| created_at | TIMESTAMP | DEFAULT NOW() | 등록 시각 |
| updated_at | TIMESTAMP | DEFAULT NOW() | 수정 시각 |

**인덱스**:
- `idx_product_company_id` ON `company_id` (기업별 상품 조회)
- `idx_product_category` ON `category` (카테고리별 상품 조회)
- `idx_product_display_order` ON `display_order ASC` (정렬 순서)
- `idx_product_is_hidden` ON `is_hidden` (숨김 필터링)

**복합 인덱스**:
- `idx_product_company_visible_order` ON `(company_id, is_hidden, display_order ASC)` (기업별 공개 상품 정렬순 조회)

---

## 데이터베이스 초기화 스크립트

```sql
-- Schema creation script for multi-tenant homepage

-- 1. Company table
CREATE TABLE company (
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

CREATE INDEX idx_company_code ON company(code);
CREATE INDEX idx_company_is_active ON company(is_active);

-- 2. Admin table
CREATE TABLE admin (
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

CREATE INDEX idx_admin_username ON admin(username);
CREATE INDEX idx_admin_company_id ON admin(company_id);
CREATE INDEX idx_admin_role ON admin(role);

-- 3. Board table
CREATE TABLE board (
    id BIGSERIAL PRIMARY KEY,
    company_id BIGINT NOT NULL REFERENCES company(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_board_company_id ON board(company_id);
CREATE INDEX idx_board_type ON board(type);
CREATE INDEX idx_board_company_type ON board(company_id, type);

-- 4. Post table
CREATE TABLE post (
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

CREATE INDEX idx_post_board_id ON post(board_id);
CREATE INDEX idx_post_created_at ON post(created_at DESC);
CREATE INDEX idx_post_view_count ON post(view_count DESC);
CREATE INDEX idx_post_is_hidden ON post(is_hidden);
CREATE INDEX idx_post_board_visible ON post(board_id, is_hidden, created_at DESC);

-- 5. FAQ table
CREATE TABLE faq (
    id BIGSERIAL PRIMARY KEY,
    company_id BIGINT NOT NULL REFERENCES company(id) ON DELETE CASCADE,
    category VARCHAR(100),
    question VARCHAR(500) NOT NULL,
    answer TEXT NOT NULL,
    display_order INTEGER DEFAULT 0,
    is_hidden BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_faq_company_id ON faq(company_id);
CREATE INDEX idx_faq_display_order ON faq(display_order ASC);
CREATE INDEX idx_faq_is_hidden ON faq(is_hidden);
CREATE INDEX idx_faq_company_visible_order ON faq(company_id, is_hidden, display_order ASC);

-- 6. QnA table
CREATE TABLE qna (
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

CREATE INDEX idx_qna_company_id ON qna(company_id);
CREATE INDEX idx_qna_is_answered ON qna(is_answered);
CREATE INDEX idx_qna_created_at ON qna(created_at DESC);
CREATE INDEX idx_qna_asker_email ON qna(asker_email);
CREATE INDEX idx_qna_company_visible ON qna(company_id, is_hidden, created_at DESC);

-- 7. YouTube Video table
CREATE TABLE youtube_video (
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

CREATE INDEX idx_youtube_company_id ON youtube_video(company_id);
CREATE INDEX idx_youtube_display_order ON youtube_video(display_order ASC);
CREATE INDEX idx_youtube_is_hidden ON youtube_video(is_hidden);
CREATE INDEX idx_youtube_company_visible_order ON youtube_video(company_id, is_hidden, display_order ASC);

-- 8. Product table
CREATE TABLE product (
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

CREATE INDEX idx_product_company_id ON product(company_id);
CREATE INDEX idx_product_category ON product(category);
CREATE INDEX idx_product_display_order ON product(display_order ASC);
CREATE INDEX idx_product_is_hidden ON product(is_hidden);
CREATE INDEX idx_product_company_visible_order ON product(company_id, is_hidden, display_order ASC);
```

---

## 테스트 데이터

```sql
-- Test data for development

-- Insert test companies
INSERT INTO company (code, name, logo_url, primary_color, secondary_color, description, address, phone, email, latitude, longitude, founded_date, is_active) VALUES
('company-a', 'A기업', '/images/company-a-logo.png', '#FF5733', '#C70039', 'A기업은 혁신적인 IT 솔루션을 제공합니다.', '서울특별시 강남구 테헤란로 123', '02-1234-5678', 'contact@company-a.com', 37.49794, 127.02762, '2010-01-15', TRUE),
('company-b', 'B기업', '/images/company-b-logo.png', '#3498DB', '#2980B9', 'B기업은 고객 중심의 서비스를 제공합니다.', '서울특별시 서초구 서초대로 456', '02-2345-6789', 'info@company-b.com', 37.49393, 127.01010, '2015-03-20', TRUE);

-- Insert test admins (password: 'password123', hashed with BCrypt)
-- Note: Use BCrypt to hash passwords in production
INSERT INTO admin (username, password_hash, name, email, role, company_id) VALUES
('admin-a', '$2a$10$N9qo8uLOickgx2ZMRZoMye7Iy/lSYC6B7Mq4H6.6tFMhh8rrKZP8W', '관리자 A', 'admin-a@company-a.com', 'ADMIN', 1),
('admin-b', '$2a$10$N9qo8uLOickgx2ZMRZoMye7Iy/lSYC6B7Mq4H6.6tFMhh8rrKZP8W', '관리자 B', 'admin-b@company-b.com', 'ADMIN', 2),
('superadmin', '$2a$10$N9qo8uLOickgx2ZMRZoMye7Iy/lSYC6B7Mq4H6.6tFMhh8rrKZP8W', '슈퍼관리자', 'super@admin.com', 'SUPER_ADMIN', NULL);

-- Insert test boards
INSERT INTO board (company_id, name, type) VALUES
(1, '공지사항', 'notice'),
(1, '보도자료', 'press'),
(2, '공지사항', 'notice');

-- Insert test posts
INSERT INTO post (board_id, title, content, author, view_count, is_hidden) VALUES
(1, '첫 번째 공지사항', '이것은 첫 번째 공지사항의 내용입니다.', '관리자 A', 10, FALSE),
(1, '두 번째 공지사항', '이것은 두 번째 공지사항의 내용입니다.', '관리자 A', 5, FALSE),
(2, '보도자료: 신제품 출시', 'A기업에서 혁신적인 신제품을 출시했습니다.', '관리자 A', 20, FALSE);

-- Insert test FAQs
INSERT INTO faq (company_id, category, question, answer, display_order, is_hidden) VALUES
(1, '일반', '영업 시간은 어떻게 되나요?', '평일 오전 9시부터 오후 6시까지입니다.', 1, FALSE),
(1, '일반', '주차 가능한가요?', '네, 건물 지하에 주차장이 있습니다.', 2, FALSE),
(2, '상품', '환불 정책은 어떻게 되나요?', '구매 후 7일 이내에 환불 가능합니다.', 1, FALSE);

-- Insert test QnA
INSERT INTO qna (company_id, question_title, question_body, asker_email, is_answered, is_hidden) VALUES
(1, '상담 문의', '제품 구매 관련하여 상담받고 싶습니다.', 'user@example.com', FALSE, FALSE),
(2, '배송 문의', '배송은 언제 시작되나요?', 'customer@example.com', FALSE, FALSE);

-- Insert test YouTube videos
INSERT INTO youtube_video (company_id, video_url, title, description, thumbnail_url, display_order, is_hidden) VALUES
(1, 'https://www.youtube.com/watch?v=dQw4w9WgXcQ', '회사 소개 영상', 'A기업을 소개하는 영상입니다.', 'https://img.youtube.com/vi/dQw4w9WgXcQ/hqdefault.jpg', 1, FALSE),
(1, 'https://www.youtube.com/watch?v=9bZkp7q19f0', '제품 데모 영상', 'A기업의 주요 제품 데모 영상입니다.', 'https://img.youtube.com/vi/9bZkp7q19f0/hqdefault.jpg', 2, FALSE);

-- Insert test products
INSERT INTO product (company_id, name, category, description, price, image_url, display_order, is_hidden) VALUES
(1, 'IT 컨설팅 서비스', 'IT 서비스', '전문가가 제공하는 IT 컨설팅 서비스입니다.', 5000000.00, '/images/product-consulting.jpg', 1, FALSE),
(1, '클라우드 솔루션', 'IT 서비스', '안전하고 빠른 클라우드 솔루션을 제공합니다.', 3000000.00, '/images/product-cloud.jpg', 2, FALSE),
(2, '프리미엄 패키지', '패키지', '다양한 혜택이 포함된 프리미엄 패키지입니다.', 1000000.00, '/images/product-premium.jpg', 1, FALSE);
```

---

## R2DBC 엔티티 매핑 예시

```java
package com.nalsil.bear.domain.company;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 기업 엔티티
 *
 * 멀티테넌트 구조의 핵심 엔티티로, 각 기업의 정보와 브랜딩을 저장한다.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("company")
public class Company {

    @Id
    private Long id;

    @Column("code")
    private String code;

    @Column("name")
    private String name;

    @Column("logo_url")
    private String logoUrl;

    @Column("primary_color")
    private String primaryColor;

    @Column("secondary_color")
    private String secondaryColor;

    @Column("description")
    private String description;

    @Column("address")
    private String address;

    @Column("phone")
    private String phone;

    @Column("email")
    private String email;

    @Column("latitude")
    private BigDecimal latitude;

    @Column("longitude")
    private BigDecimal longitude;

    @Column("founded_date")
    private LocalDate foundedDate;

    @Column("is_active")
    private Boolean isActive;

    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("updated_at")
    private LocalDateTime updatedAt;
}
```

---

## 성능 최적화 고려사항

1. **인덱스 전략**: 모든 `company_id` 외래 키에 인덱스를 생성하여 멀티테넌트 조회 성능 최적화
2. **복합 인덱스**: 자주 사용되는 조회 패턴(예: `company_id + is_hidden + created_at`)에 대해 복합 인덱스 생성
3. **N+1 쿼리 방지**: R2DBC의 `@Query` 어노테이션을 사용하여 JOIN 쿼리 작성
4. **페이징 최적화**: `LIMIT`과 `OFFSET` 대신 커서 기반 페이징 고려(특히 대량 데이터 조회 시)
5. **캐싱 전략**: 자주 변경되지 않는 데이터(Company, FAQ)는 Redis 캐싱 적용 고려

---

## 데이터 검증 규칙

- **Company.code**: 정규식 `^[a-z0-9-]{3,50}$` (소문자, 숫자, 하이픈만 허용)
- **Company.email**, **Admin.email**, **Qna.askerEmail**: 이메일 형식 검증
- **Admin.passwordHash**: BCrypt 해시(강도 10 이상)
- **YoutubeVideo.videoUrl**: 유튜브 URL 형식 검증
- **Post.fileSize**: 최대 20MB 제한
- **Post.fileMimeType**: 허용된 MIME 타입만 허용

---

## 향후 확장 계획

- **파일 메타데이터 테이블**: 첨부파일 정보를 별도 테이블로 분리하여 다중 파일 첨부 지원
- **댓글 테이블**: 게시글에 대한 댓글 기능 추가
- **통계 테이블**: 기업별 방문자 통계, 게시글 조회 통계 등 집계 데이터 저장
- **알림 테이블**: QnA 답변 시 이메일 알림 발송 이력 저장
- **감사 로그 테이블**: 관리자의 모든 CRUD 작업 이력 저장
