# 관리자 로그인 가이드

## 📋 기본 계정 정보

애플리케이션에 미리 등록된 관리자 계정입니다.

### 1. Company A 관리자
```
Username: admin-a
Password: password123
Role: ADMIN
Company: 테크솔루션 주식회사 (company-a)
```

**접근 경로:**
- 로그인: `http://localhost:8080/admin/login`
- 대시보드: `http://localhost:8080/admin/dashboard`

---

### 2. Company B 관리자
```
Username: admin-b
Password: password123
Role: ADMIN
Company: 글로벌무역 주식회사 (company-b)
```

**접근 경로:**
- 로그인: `http://localhost:8080/admin/login`
- 대시보드: `http://localhost:8080/admin/dashboard`

---

### 3. 슈퍼관리자
```
Username: superadmin
Password: password123
Role: SUPER_ADMIN
Company: (전체 시스템 관리)
```

**접근 경로:**
- 로그인: `http://localhost:8080/admin/login`
- 대시보드: `http://localhost:8080/admin/dashboard`
- 슈퍼관리자 페이지: `http://localhost:8080/superadmin/*`

---

## 🔐 인증 방식

### JWT (JSON Web Token) 기반 Sessionless 인증

1. **로그인 처리:**
   - POST `/admin/login`
   - 아이디/비밀번호 검증
   - JWT 토큰 생성
   - HTTP-Only 쿠키에 저장

2. **인증 유지:**
   - 쿠키: `JWT-TOKEN`
   - 유효기간: 24시간
   - 자동 갱신: 없음 (재로그인 필요)

3. **로그아웃:**
   - GET `/admin/logout`
   - JWT 쿠키 삭제

---

## 🚀 로그인 테스트

### 방법 1: 브라우저에서 로그인
1. `http://localhost:8080/admin/login` 접속
2. 아이디/비밀번호 입력
3. 로그인 버튼 클릭
4. 대시보드로 자동 이동

### 방법 2: cURL로 로그인
```bash
# 로그인 (쿠키 저장)
curl -i -X POST http://localhost:8080/admin/login \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "username=admin-a&password=password123" \
  -c cookies.txt

# 대시보드 접근 (저장된 쿠키 사용)
curl -i http://localhost:8080/admin/dashboard -b cookies.txt
```

---

## 🛠️ 문제 해결

### 403 Forbidden 오류 발생 시

**원인:** JWT 토큰의 권한(role)에 "ROLE_" 접두사가 없어서 발생

**해결:** 이미 수정 완료 (JwtAuthenticationFilter.java:57)
```java
// Spring Security는 hasRole("ADMIN")을 "ROLE_ADMIN"으로 변환
String authorityName = role.startsWith("ROLE_") ? role : "ROLE_" + role;
```

### 로그인 실패 시

**증상:** "아이디 또는 비밀번호가 올바르지 않습니다."

**확인 사항:**
1. 아이디/비밀번호 정확성
2. 데이터베이스 연결 상태
3. 패스워드 해시 일치 여부

**기본 계정:**
- admin-a / password123
- admin-b / password123
- superadmin / password123

### 데이터베이스 초기화

초기 데이터가 없는 경우:
```sql
-- src/main/resources/data.sql 확인
-- 애플리케이션 재시작 시 자동 실행됨
```

---

## 🔑 새 관리자 계정 생성

### 1. 패스워드 해시 생성
```bash
# JUnit 테스트 실행
./gradlew test --tests QuickPasswordHashTest

# 또는 대화형 프로그램 실행
./gradlew run -Pmain=com.nalsil.bear.util.BcryptPasswordGenerator
```

### 2. 데이터베이스에 계정 추가
```sql
INSERT INTO admin (username, password_hash, name, email, role, company_id)
VALUES (
    'new-admin',
    '$2a$10$...',  -- BCrypt 해시
    '새 관리자',
    'new@example.com',
    'ADMIN',
    1  -- Company ID (NULL for SUPER_ADMIN)
);
```

---

## 📊 권한 체계

### ADMIN
- 자신이 소속된 회사의 데이터만 관리
- 게시판, FAQ, QnA, 유튜브 영상, 상품 관리
- 회사 정보 수정 불가

### SUPER_ADMIN
- 모든 회사의 데이터 관리
- 새 회사 추가/삭제
- 관리자 계정 관리
- 시스템 전체 설정

---

## 🔒 보안 권장사항

### 개발 환경
- ✅ 간단한 패스워드 사용 가능 (password123)
- ✅ HTTPS 비활성화 (secure: false)
- ✅ 디버그 로그 활성화

### 운영 환경
- ⚠️ 강력한 패스워드 필수 (12자 이상, 복잡한 조합)
- ⚠️ HTTPS 필수 (secure: true)
- ⚠️ 디버그 로그 비활성화
- ⚠️ JWT Secret 변경
- ⚠️ 쿠키 SameSite=Strict 설정

### 패스워드 정책
```
최소 12자 이상
대문자 + 소문자 + 숫자 + 특수문자 조합
예: MyS3cur3P@ss2024!
```

---

## 📚 관련 파일

**컨트롤러:**
- `src/main/java/com/nalsil/bear/controller/admin/AdminLoginController.java`

**보안 설정:**
- `src/main/java/com/nalsil/bear/config/SecurityConfig.java`
- `src/main/java/com/nalsil/bear/filter/JwtAuthenticationFilter.java`

**JWT 유틸:**
- `src/main/java/com/nalsil/bear/util/JwtUtil.java`

**템플릿:**
- `src/main/resources/templates/admin/login.html`
- `src/main/resources/templates/admin/dashboard.html`

**초기 데이터:**
- `src/main/resources/data.sql`
