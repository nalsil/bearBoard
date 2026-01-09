# Bear - 멀티테넌트 기업 홈페이지

Spring Boot 3.5.9 기반의 반응형 멀티테넌트 기업 홈페이지 시스템입니다.

## 주요 기능

### 멀티테넌트 아키텍처
- URL 기반 테넌트 식별 (`/{companyCode}/...`)
- 기업별 독립적인 컨텐츠 관리
- 기업별 커스터마이징 (로고, 색상, 정보)

### 사용자 기능
1. **홈페이지** - 기업 정보 표시
2. **게시판** - 공지사항, 뉴스 등
3. **FAQ** - 자주 묻는 질문
4. **QnA** - 문의하기 (reCAPTCHA v3 연동 예정)
5. **유튜브 영상** - 영상 갤러리
6. **상품** - 상품 목록 및 상세

### 관리자 기능
- **로그인/로그아웃** - 세션 기반 인증
- **대시보드** - 통계 현황 (게시글, FAQ, QnA, 영상, 상품 수)
- **컨텐츠 관리** - 생성, 수정, 삭제, 숨김 처리
- **권한 관리** - 기업별 접근 제어
- **슈퍼유저** - 모든 기업 관리, 기업 전환 기능

## 기술 스택

- **Framework**: Spring Boot 3.5.9
- **Java**: 21
- **Build Tool**: Gradle
- **Database**: PostgreSQL (R2DBC)
- **Web**: Spring WebFlux (Reactive)
- **Security**: Spring Security (WebFlux)
- **Template Engine**: Thymeleaf
- **Utils**: Lombok, DevTools

## 프로젝트 구조

```
src/main/java/com/nalsil/bear/
├── config/                 # 설정 클래스
│   ├── AdminUserDetailsService.java
│   └── SecurityConfig.java
├── controller/             # 컨트롤러
│   ├── admin/             # 관리자 컨트롤러
│   └── public/            # 공개 컨트롤러
├── domain/                # 도메인 엔티티 및 리포지토리
│   ├── admin/
│   ├── board/
│   ├── company/
│   ├── faq/
│   ├── post/
│   ├── product/
│   ├── qna/
│   └── youtube/
├── dto/                   # DTO (요청/응답)
│   ├── request/
│   └── response/
├── exception/             # 커스텀 예외
├── filter/                # 필터 (테넌트 식별)
├── service/               # 서비스 계층
└── util/                  # 유틸리티

src/main/resources/
├── templates/             # Thymeleaf 템플릿
│   ├── admin/            # 관리자 페이지
│   ├── error/            # 에러 페이지 (403, 404, 500)
│   └── public/           # 공개 페이지
├── static/               # 정적 리소스
│   ├── css/
│   └── js/
├── schema.sql            # 데이터베이스 스키마
├── data.sql.old              # 초기 데이터
└── application.yaml      # 애플리케이션 설정
```

## 시작하기

### 사전 요구사항

- Java 21
- PostgreSQL 12+
- Gradle 8.0+

### 데이터베이스 설정

1. PostgreSQL 데이터베이스 생성:
```sql
CREATE DATABASE bear;
```

2. `application.yaml` 파일에서 데이터베이스 연결 정보 수정:
```yaml
spring:
  r2dbc:
    url: r2dbc:postgresql://localhost:5432/bear
    username: your_username
    password: your_password
```

### 애플리케이션 실행

```bash
# 빌드
./gradlew build

# 실행
./gradlew bootRun
```

애플리케이션은 기본적으로 http://localhost:8080 에서 실행됩니다.

### 초기 데이터

`data.sql.old`에 정의된 초기 데이터:
- 기업 2개 (company-a, company-b)
- 관리자 계정 3개
  - 슈퍼유저: `superadmin` / `password123`
  - A기업 관리자: `admin_a` / `password123`
  - B기업 관리자: `admin_b` / `password123`

### 접속 URL

- **공개 홈페이지**: `http://localhost:8080/{companyCode}`
  - 예: `http://localhost:8080/company-a`
- **관리자 로그인**: `http://localhost:8080/admin/login`
- **관리자 대시보드**: `http://localhost:8080/admin/dashboard`

## 개발 가이드

### 빌드 명령어

```bash
# 전체 빌드
./gradlew build

# 테스트 제외 빌드
./gradlew build -x test

# 테스트만 실행
./gradlew test

# 클린 빌드
./gradlew clean build

# JAR 파일 생성
./gradlew bootJar
```

### 코드 스타일

- Lombok 사용 (`@Data`, `@Builder`, `@RequiredArgsConstructor`)
- 반응형 프로그래밍 (`Mono<T>`, `Flux<T>`)
- RESTful 라우팅 규칙 준수
- 한글 주석 및 JavaDoc

## 주요 특징

### 반응형 프로그래밍
- Spring WebFlux를 사용한 논블로킹 I/O
- R2DBC를 사용한 반응형 데이터베이스 액세스
- `Mono<T>` (단일 결과), `Flux<T>` (다중 결과) 활용

### 보안
- Spring Security WebFlux 통합
- BCrypt 암호화
- 세션 기반 인증
- 경로별 권한 제어 (`/admin/**`, `/superadmin/**`)

### 멀티테넌트
- `TenantFilter`를 통한 테넌트 식별
- 기업별 데이터 격리
- 슈퍼유저의 크로스 테넌트 관리

## 라이선스

이 프로젝트는 개인 학습 및 포트폴리오 목적으로 제작되었습니다.

## 문의

프로젝트에 대한 문의사항이 있으시면 이슈를 등록해 주세요.
