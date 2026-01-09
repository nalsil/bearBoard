# BCrypt Password Hash Generator

Spring Security BCrypt를 사용한 패스워드 해시 생성 및 검증 유틸리티입니다.

## 📋 목차
- [방법 1: JUnit 테스트 실행](#방법-1-junit-테스트-실행)
- [방법 2: 독립 실행 프로그램](#방법-2-독립-실행-프로그램)
- [방법 3: 코드에서 직접 사용](#방법-3-코드에서-직접-사용)
- [생성된 해시 사용법](#생성된-해시-사용법)

---

## 방법 1: JUnit 테스트 실행

### 위치
```
src/test/java/com/nalsil/bear/util/BcryptPasswordGeneratorTest.java
```

### 실행 방법

#### 1-1. 모든 테스트 실행
```bash
./gradlew test --tests BcryptPasswordGeneratorTest
```

#### 1-2. 특정 테스트만 실행

**기본 패스워드 해시 생성:**
```bash
./gradlew test --tests BcryptPasswordGeneratorTest.generatePasswordHashes
```

**패스워드 검증 테스트:**
```bash
./gradlew test --tests BcryptPasswordGeneratorTest.verifyPasswordMatches
```

**Salt 해싱 데모:**
```bash
./gradlew test --tests BcryptPasswordGeneratorTest.demonstrateSaltedHashing
```

**커스텀 패스워드 해시 생성:**
```bash
./gradlew test --tests BcryptPasswordGeneratorTest.generateCustomPasswordHash
```

**SQL INSERT 문 생성:**
```bash
./gradlew test --tests BcryptPasswordGeneratorTest.generateSQLInsertStatements
```

**패스워드 강도 체크:**
```bash
./gradlew test --tests BcryptPasswordGeneratorTest.checkPasswordStrength
```

#### 1-3. IDE에서 실행
1. IntelliJ IDEA 또는 Eclipse에서 `BcryptPasswordGeneratorTest.java` 열기
2. 원하는 테스트 메서드 옆의 ▶️ 버튼 클릭
3. 콘솔 출력에서 생성된 해시 확인

---

## 방법 2: 독립 실행 프로그램

### 위치
```
src/main/java/com/nalsil/bear/util/BcryptPasswordGenerator.java
```

### 실행 방법

#### 2-1. 대화형 모드 (메뉴 선택)
```bash
./gradlew run -Pmain=com.nalsil.bear.util.BcryptPasswordGenerator
```

또는 IDE에서 `BcryptPasswordGenerator.java`의 `main` 메서드 실행

**메뉴:**
```
1. Generate hash for a single password     - 단일 패스워드 해시 생성
2. Generate hash for predefined passwords  - 미리 정의된 패스워드 해시 생성
3. Verify password against hash            - 패스워드 검증
4. Generate SQL INSERT statements          - SQL INSERT 문 생성
5. Check password strength                 - 패스워드 강도 확인
0. Exit                                    - 종료
```

#### 2-2. 커맨드라인 인자 사용
```bash
# 단일 패스워드
./gradlew run -Pmain=com.nalsil.bear.util.BcryptPasswordGenerator --args="password123"

# 여러 패스워드
./gradlew run -Pmain=com.nalsil.bear.util.BcryptPasswordGenerator --args="password123 admin123 test1234"
```

---

## 방법 3: 코드에서 직접 사용

### 패스워드 해시 생성
```java
import com.nalsil.bear.util.BcryptPasswordGenerator;

String plainPassword = "password123";
String hashedPassword = BcryptPasswordGenerator.encode(plainPassword);
System.out.println("Hash: " + hashedPassword);
```

### 패스워드 검증
```java
String plainPassword = "password123";
String hashedPassword = "$2a$10$..."; // 데이터베이스에서 조회한 해시

boolean isValid = BcryptPasswordGenerator.verify(plainPassword, hashedPassword);
System.out.println("Valid: " + isValid);
```

---

## 생성된 해시 사용법

### 1. 데이터베이스 직접 입력

#### SQL UPDATE 예제:
```sql
-- 특정 사용자의 패스워드 업데이트
UPDATE admin
SET password_hash = '$2a$10$abcd1234...'
WHERE username = 'admin';
```

#### SQL INSERT 예제:
```sql
-- 새 관리자 추가
INSERT INTO admin (username, password_hash, name, email, role, company_id)
VALUES (
    'admin-a',
    '$2a$10$abcd1234...',
    'Company A 관리자',
    'admin-a@techsolution.co.kr',
    'ADMIN',
    1
);
```

### 2. 초기 데이터 파일에 사용

`src/main/resources/data.sql` 파일에 생성된 SQL INSERT 문을 복사하여 사용:

```sql
-- 슈퍼관리자
INSERT INTO admin (username, password_hash, name, email, role) VALUES (
    'superadmin',
    '$2a$10$xyz...',
    '시스템 관리자',
    'superadmin@bear.com',
    'SUPER_ADMIN'
);

-- Company A 관리자
INSERT INTO admin (username, password_hash, name, email, role, company_id) VALUES (
    'admin-a',
    '$2a$10$abc...',
    'Company A 관리자',
    'admin-a@techsolution.co.kr',
    'ADMIN',
    1
);
```

---

## 📝 예제 출력

### 패스워드 해시 생성 예제:
```
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
Plain text: password123
Strength:   🟡 중간 (Medium)
BCrypt hash:
$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy
Length:     60 characters
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
```

### SQL INSERT 문 생성 예제:
```sql
-- 시스템 관리자 (superadmin)
INSERT INTO admin (username, password_hash, name, email, role) VALUES (
    'superadmin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '시스템 관리자', 'superadmin@bear.com', 'SUPER_ADMIN');

-- Company A 관리자 (admin-a)
INSERT INTO admin (username, password_hash, name, email, role, company_id) VALUES (
    'admin-a', '$2a$10$rPTrFfMbLaJDJxMvDWMGEeVQRnZhJsPRo0kDbD/LwJnGzXrJPxFjy', 'Company A 관리자', 'admin-a@techsolution.co.kr', 'ADMIN', 1);
```

---

## 🔒 보안 권장사항

### 강력한 패스워드 조건:
- ✅ 최소 12자 이상
- ✅ 대문자 포함 (A-Z)
- ✅ 소문자 포함 (a-z)
- ✅ 숫자 포함 (0-9)
- ✅ 특수문자 포함 (!@#$%^&*)
- ❌ 일반적인 단어 사용 금지 (password, admin, test 등)

### 좋은 패스워드 예제:
```
✅ MyS3cur3P@ss2024!    (Strong)
✅ Tr0ng!P@ssw0rd       (Strong)
✅ C0mpl3x#Secret99     (Strong)
```

### 나쁜 패스워드 예제:
```
❌ password123         (Weak)
❌ admin               (Weak)
❌ 12345678            (Weak)
```

---

## 🎯 사용 팁

1. **개발 환경**: `password123`, `admin123` 등 간단한 패스워드 사용 가능
2. **운영 환경**: 강력한 패스워드 필수 (12자 이상, 복잡한 조합)
3. **패스워드 재사용 금지**: 각 사용자마다 고유한 패스워드 사용
4. **정기적 변경**: 운영 환경에서는 정기적인 패스워드 변경 권장
5. **해시 보관**: 생성된 BCrypt 해시는 안전하게 데이터베이스에 저장

---

## ❓ FAQ

### Q: BCrypt 해시는 왜 매번 다르게 생성되나요?
A: BCrypt는 내부적으로 랜덤 salt를 생성하기 때문입니다. 같은 패스워드라도 매번 다른 해시가 생성되지만, 모두 검증 시 올바르게 일치합니다.

### Q: 해시 길이는 항상 60자인가요?
A: 네, BCrypt 해시는 항상 60자입니다. 데이터베이스 컬럼은 `VARCHAR(255)` 이상으로 설정하세요.

### Q: 기존 해시에서 원본 패스워드를 알 수 있나요?
A: 아니요. BCrypt는 단방향 해시 함수이므로 원본 패스워드를 복구할 수 없습니다. 오직 검증만 가능합니다.

### Q: Spring Security에서 자동으로 검증되나요?
A: 네, Spring Security가 자동으로 BCrypt 해시를 검증합니다. `PasswordEncoder` Bean이 설정되어 있으면 자동 처리됩니다.

---

## 📚 관련 문서

- [Spring Security - Password Encoding](https://docs.spring.io/spring-security/reference/features/authentication/password-storage.html)
- [BCrypt Algorithm](https://en.wikipedia.org/wiki/Bcrypt)
- Bear 프로젝트: `src/main/java/com/nalsil/bear/config/PasswordEncoderConfig.java`
