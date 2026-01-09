# 🚀 BCrypt Password Generator - Quick Start

## 가장 빠른 방법 (1분 완료)

### 1. JUnit 테스트로 즉시 생성

#### IntelliJ IDEA:
1. `src/test/java/com/nalsil/bear/util/QuickPasswordHashTest.java` 파일 열기
2. 클래스 이름 옆의 녹색 ▶️ 아이콘 클릭
3. 콘솔에서 생성된 해시 복사

#### 터미널:
```bash
./gradlew test --tests QuickPasswordHashTest --info
```

---

### 2. 주요 패스워드 해시 (미리 생성됨)

아래는 테스트용으로 미리 생성된 BCrypt 해시입니다:

#### 📌 password123
```
$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy
```

**SQL 예제:**
```sql
UPDATE admin SET password_hash = '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy' WHERE username = 'admin';
```

---

#### 📌 admin123
```
$2a$10$rPTrFfMbLaJDJxMvDWMGEeVQRnZhJsPRo0kDbD/LwJnGzXrJPxFjy
```

**SQL 예제:**
```sql
INSERT INTO admin (username, password_hash, name, email, role) VALUES
('admin', '$2a$10$rPTrFfMbLaJDJxMvDWMGEeVQRnZhJsPRo0kDbD/LwJnGzXrJPxFjy', 'Administrator', 'admin@example.com', 'ADMIN');
```

---

#### 📌 superadmin
```
$2a$10$qFJZjyJMVbZXr8I/KBBfaeT5vbLvGLrJGHa0VhiLHmxvYYQiP8h5e
```

**SQL 예제:**
```sql
INSERT INTO admin (username, password_hash, name, email, role) VALUES
('superadmin', '$2a$10$qFJZjyJMVbZXr8I/KBBfaeT5vbLvGLrJGHa0VhiLHmxvYYQiP8h5e', 'Super Admin', 'superadmin@bear.com', 'SUPER_ADMIN');
```

---

### 3. 커스텀 패스워드 해시 생성

#### 방법 A: 테스트 파일 수정
1. `QuickPasswordHashTest.java` 열기
2. `String password = "password123";` 부분을 원하는 패스워드로 변경
3. 테스트 실행

#### 방법 B: 대화형 프로그램 실행
```bash
# IntelliJ에서 실행:
src/main/java/com/nalsil/bear/util/BcryptPasswordGenerator.java
→ main 메서드 옆의 ▶️ 클릭

# 또는 터미널에서:
./gradlew run -Pmain=com.nalsil.bear.util.BcryptPasswordGenerator
```

선택:
- `1` → 단일 패스워드 입력
- 원하는 패스워드 입력
- 생성된 해시 복사

---

## 💡 사용 예제

### 데이터베이스 초기 데이터 설정

`src/main/resources/data.sql`:
```sql
-- 슈퍼관리자 (username: superadmin, password: superadmin)
INSERT INTO admin (username, password_hash, name, email, role)
VALUES ('superadmin', '$2a$10$qFJZjyJMVbZXr8I/KBBfaeT5vbLvGLrJGHa0VhiLHmxvYYQiP8h5e', '시스템 관리자', 'superadmin@bear.com', 'SUPER_ADMIN');

-- Company A 관리자 (username: admin-a, password: admin123)
INSERT INTO admin (username, password_hash, name, email, role, company_id)
VALUES ('admin-a', '$2a$10$rPTrFfMbLaJDJxMvDWMGEeVQRnZhJsPRo0kDbD/LwJnGzXrJPxFjy', 'Company A 관리자', 'admin-a@techsolution.co.kr', 'ADMIN', 1);

-- Company B 관리자 (username: admin-b, password: admin123)
INSERT INTO admin (username, password_hash, name, email, role, company_id)
VALUES ('admin-b', '$2a$10$rPTrFfMbLaJDJxMvDWMGEeVQRnZhJsPRo0kDbD/LwJnGzXrJPxFjy', 'Company B 관리자', 'admin-b@globaltrade.co.kr', 'ADMIN', 2);
```

### 로그인 테스트

애플리케이션 실행 후:

```bash
# Company A 관리자로 로그인
POST http://localhost:8080/admin/login
Content-Type: application/json

{
  "username": "admin-a",
  "password": "admin123"
}
```

---

## ⚠️ 중요 사항

1. **개발 환경**: 위의 간단한 패스워드 사용 가능
2. **운영 환경**: 반드시 강력한 패스워드 사용 필요
   - 최소 12자 이상
   - 대소문자, 숫자, 특수문자 조합
   - 예: `MyS3cur3P@ss2024!`

3. **패스워드 재사용 금지**: 각 계정마다 다른 패스워드 사용

4. **해시 특성**:
   - 같은 패스워드라도 매번 다른 해시 생성됨 (랜덤 salt)
   - 이는 정상이며, 모두 검증 시 올바르게 작동함

---

## 📚 더 자세한 정보

전체 사용 설명서: `PASSWORD_GENERATOR_README.md` 참고
