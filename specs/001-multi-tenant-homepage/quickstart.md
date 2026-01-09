# Quickstart Guide: 멀티테넌트 기업용 홈페이지

**Date**: 2025-12-30
**Feature**: [spec.md](./spec.md)

이 가이드는 멀티테넌트 기업용 홈페이지 프로젝트를 빠르게 시작하는 방법을 설명합니다.

---

## 사전 준비

### 필수 도구

1. **Java 21**: OpenJDK 21 이상 설치
   ```bash
   java -version
   # openjdk version "21.0.x" 확인
   ```

2. **PostgreSQL 15+**: PostgreSQL 데이터베이스 설치
   ```bash
   # macOS (Homebrew)
   brew install postgresql@15
   brew services start postgresql@15

   # Ubuntu/Debian
   sudo apt install postgresql-15

   # Windows
   # PostgreSQL 공식 웹사이트에서 설치 프로그램 다운로드
   ```

3. **Redis 7+**: 세션 관리를 위한 Redis 설치
   ```bash
   # macOS (Homebrew)
   brew install redis
   brew services start redis

   # Ubuntu/Debian
   sudo apt install redis-server

   # Windows
   # Redis 공식 웹사이트에서 WSL 또는 Docker 사용 권장
   ```

4. **Gradle** (선택사항): 프로젝트에 Gradle Wrapper 포함되어 있음
   ```bash
   ./gradlew -v
   ```

---

## 프로젝트 설정

### 1. 데이터베이스 생성

PostgreSQL에 데이터베이스를 생성합니다.

```bash
# PostgreSQL에 접속
psql -U postgres

# 데이터베이스 생성
CREATE DATABASE bear_db;

# 사용자 생성 (선택사항)
CREATE USER bear_user WITH PASSWORD 'bear_password';

# 권한 부여
GRANT ALL PRIVILEGES ON DATABASE bear_db TO bear_user;

# 종료
\q
```

### 2. 스키마 초기화

데이터베이스 스키마를 초기화합니다.

```bash
# data-model.md에 있는 SQL 스크립트 실행
psql -U postgres -d bear_db -f specs/001-multi-tenant-homepage/data-model.md
```

또는 Spring Boot 애플리케이션 실행 시 자동으로 스키마가 생성되도록 `application.yaml`에 설정:

```yaml
spring:
  r2dbc:
    url: r2dbc:postgresql://localhost:5432/bear_db
    username: bear_user
    password: bear_password
  sql:
    init:
      mode: always
      schema-locations: classpath:schema.sql
```

### 3. 환경 변수 설정

`application.yaml` 또는 `application-local.yaml`을 생성합니다.

```yaml
# src/main/resources/application-local.yaml
spring:
  r2dbc:
    url: r2dbc:postgresql://localhost:5432/bear_db
    username: bear_user
    password: bear_password

  redis:
    host: localhost
    port: 6379

  session:
    store-type: redis
    timeout: 1800s  # 30분

recaptcha:
  secret-key: YOUR_RECAPTCHA_SECRET_KEY  # Google reCAPTCHA v3 비밀 키

kakao:
  map:
    app-key: YOUR_KAKAO_MAP_APP_KEY  # Kakao Map JavaScript 키

file:
  upload:
    base-dir: ${user.home}/bear-uploads  # 파일 업로드 기본 디렉토리
    max-size: 20MB
```

**주의**: 실제 키 값은 보안을 위해 환경 변수나 외부 설정 파일로 관리하세요.

---

## 빌드 및 실행

### 1. 빌드

```bash
# 프로젝트 빌드
./gradlew build

# 테스트 제외하고 빌드
./gradlew build -x test
```

### 2. 애플리케이션 실행

```bash
# Gradle로 실행
./gradlew bootRun

# 또는 JAR 파일 실행
java -jar build/libs/bear-0.0.1-SNAPSHOT.jar

# 프로필 지정 실행 (local 프로필)
./gradlew bootRun --args='--spring.profiles.active=local'
```

애플리케이션이 실행되면 다음 주소로 접속 가능합니다:

- **일반 사용자 홈페이지**: http://localhost:8080/{company-code}
  - 예: http://localhost:8080/company-a
- **관리자 로그인**: http://localhost:8080/admin/login
- **Actuator 엔드포인트**: http://localhost:8080/actuator/health

---

## 테스트 데이터 확인

### 테스트 기업

- **A기업**: `company-a`
  - URL: http://localhost:8080/company-a
  - 관리자 ID: `admin-a`, 비밀번호: `password123`

- **B기업**: `company-b`
  - URL: http://localhost:8080/company-b
  - 관리자 ID: `admin-b`, 비밀번호: `password123`

### 슈퍼유저

- **슈퍼관리자**:
  - 관리자 ID: `superadmin`, 비밀번호: `password123`
  - 모든 기업의 컨텐츠 관리 가능

---

## 개발 워크플로우

### 1. 새로운 기능 개발

```bash
# 기능 브랜치 생성
git checkout -b feature/new-feature

# 코드 작성 및 테스트
./gradlew test

# 커밋 (한국어 커밋 메시지)
git add .
git commit -m "기능: 새로운 기능 추가

상세 설명...

🤖 Generated with Claude Code
Co-Authored-By: Claude Sonnet 4.5 <noreply@anthropic.com>"

# Push
git push origin feature/new-feature
```

### 2. 테스트 실행

```bash
# 모든 테스트 실행
./gradlew test

# 특정 테스트 클래스 실행
./gradlew test --tests "com.nalsil.bear.service.CompanyServiceTest"

# 코드 커버리지 확인 (JaCoCo)
./gradlew jacocoTestReport
# 리포트: build/reports/jacoco/test/html/index.html
```

### 3. 코드 품질 검사

```bash
# Checkstyle 실행 (선택사항)
./gradlew checkstyleMain

# SpotBugs 실행 (선택사항)
./gradlew spotbugsMain
```

---

## API 테스트

### 일반 사용자 API

#### 기업 홈페이지 조회
```bash
curl http://localhost:8080/company-a
```

#### 게시판 목록 조회
```bash
curl http://localhost:8080/company-a/boards/notice?page=0&size=10
```

#### FAQ 목록 조회
```bash
curl http://localhost:8080/company-a/faq
```

#### QnA 질문 등록 (POST)
```bash
curl -X POST http://localhost:8080/company-a/qna \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "questionTitle=문의사항" \
  -d "questionBody=제품 구매 관련 문의드립니다." \
  -d "askerEmail=user@example.com" \
  -d "recaptchaToken=test-token"
```

### 관리자 API

#### 로그인
```bash
curl -X POST http://localhost:8080/admin/login \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "username=admin-a" \
  -d "password=password123" \
  -c cookies.txt  # 쿠키 저장
```

#### 게시글 등록 (인증 필요)
```bash
curl -X POST http://localhost:8080/admin/boards/1/posts \
  -H "Content-Type: multipart/form-data" \
  -b cookies.txt \
  -F "title=새로운 공지사항" \
  -F "content=공지사항 내용입니다." \
  -F "isHidden=false"
```

---

## 문제 해결

### 문제: PostgreSQL 연결 실패

**증상**: `Connection refused` 또는 `Connection timeout` 에러

**해결 방법**:
1. PostgreSQL 서비스 실행 여부 확인:
   ```bash
   # macOS
   brew services list | grep postgresql

   # Ubuntu/Debian
   sudo systemctl status postgresql
   ```

2. `application.yaml`에서 데이터베이스 URL, 사용자명, 비밀번호 확인

3. 방화벽 설정 확인 (PostgreSQL 기본 포트: 5432)

### 문제: Redis 연결 실패

**증상**: `Unable to connect to Redis` 에러

**해결 방법**:
1. Redis 서비스 실행 여부 확인:
   ```bash
   # macOS
   brew services list | grep redis

   # Ubuntu/Debian
   sudo systemctl status redis-server
   ```

2. Redis 연결 테스트:
   ```bash
   redis-cli ping
   # 응답: PONG
   ```

### 문제: 파일 업로드 실패

**증상**: `File size exceeds maximum allowed size` 에러

**해결 방법**:
1. `application.yaml`에서 파일 크기 제한 확인:
   ```yaml
   spring:
     servlet:
       multipart:
         max-file-size: 20MB
         max-request-size: 25MB
   ```

2. 파일 저장 디렉토리 권한 확인:
   ```bash
   mkdir -p ~/bear-uploads
   chmod 755 ~/bear-uploads
   ```

### 문제: 세션 만료

**증상**: 로그인 후 일정 시간이 지나면 자동 로그아웃

**해결 방법**:
1. `application.yaml`에서 세션 타임아웃 설정 확인:
   ```yaml
   spring:
     session:
       timeout: 1800s  # 30분
   ```

2. Redis에 세션 데이터가 저장되는지 확인:
   ```bash
   redis-cli
   > KEYS spring:session:*
   ```

---

## 추가 리소스

- **프로젝트 헌법**: [.specify/memory/constitution.md](../../.specify/memory/constitution.md)
- **명세서**: [spec.md](./spec.md)
- **데이터 모델**: [data-model.md](./data-model.md)
- **API 계약**: [contracts/](./contracts/)
- **Spring Boot 공식 문서**: https://docs.spring.io/spring-boot/docs/current/reference/html/
- **Spring WebFlux 가이드**: https://docs.spring.io/spring-framework/reference/web/webflux.html
- **R2DBC 공식 문서**: https://r2dbc.io/

---

## 다음 단계

1. **작업 계획 생성**: `/speckit.tasks` 명령으로 구현 작업을 생성
2. **코드 구현**: 생성된 작업 목록에 따라 순차적으로 구현
3. **테스트 작성**: 각 기능 구현 후 단위 테스트 및 통합 테스트 작성
4. **코드 리뷰**: Pull Request 생성 및 팀원 리뷰
5. **배포**: 테스트 환경에 배포 후 프로덕션 배포

---

**Happy Coding!** 🚀
