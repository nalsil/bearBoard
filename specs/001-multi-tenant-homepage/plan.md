# Implementation Plan: 멀티테넌트 기업용 홈페이지

**Branch**: `001-multi-tenant-homepage` | **Date**: 2025-12-30 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/001-multi-tenant-homepage/spec.md`

**Note**: This template is filled in by the `/speckit.plan` command. See `.specify/templates/commands/plan.md` for the execution workflow.

## Summary

여러 기업이 독립적으로 사용할 수 있는 멀티테넌트 홈페이지 플랫폼을 개발한다. 각 기업은 고유한 기업 코드를 통해 URL 경로로 분리되며(`example.com/{기업코드}`), 게시판, FAQ, QnA, 유튜브 영상 등의 컨텐츠를 관리할 수 있다. Spring Boot 3.5.9, WebFlux, Thymeleaf, PostgreSQL을 사용하여 빠른 응답과 확장성을 보장하며, SEO 최적화와 반응형 디자인을 제공한다.

## Technical Context

**Language/Version**: Java 21
**Primary Dependencies**: Spring Boot 3.5.9 (WebFlux, Security, Thymeleaf, R2DBC PostgreSQL), Lombok, Spring Boot DevTools
**Storage**: PostgreSQL (R2DBC를 통한 리액티브 접근)
**Testing**: JUnit 5, Spring Boot Test, WebTestClient, 목표 코드 커버리지 70%
**Target Platform**: Linux server (JVM 21)
**Project Type**: Web (reactive backend + Thymeleaf frontend)
**Performance Goals**: API 응답 시간 P95 1초 이내, P99 3초 이내, 평균 500ms 이내, 최소 1,000명 동시 사용자 처리
**Constraints**: 단일 쿼리 100ms 이내, UI 렌더링 2초 이내, JVM 힙 메모리 512MB 이하(개발 환경 기준)
**Scale/Scope**: 멀티테넌트 아키텍처로 수백 개 기업 지원, 각 기업당 수천 개 게시글 처리 가능

**추가 기술 요구사항** (Phase 0 Research 완료):
- **지도 서비스 통합**: Kakao Map API (JavaScript SDK)
- **파일 저장소**: 로컬 파일 시스템 (초기), AWS S3 마이그레이션 고려
- **스팸 방지**: Google reCAPTCHA v3 (스코어 기반, 0.5 임계값)
- **세션 관리**: Spring Session + Redis (세션 클러스터링, 30분 타임아웃)

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

### I. 한국어 우선 (Korean-First Development)
- ✅ **PASS**: 모든 문서와 주석은 한국어로 작성
- ✅ **PASS**: 클래스/메서드/변수명은 영어 표준 관례 준수
- ✅ **PASS**: JavaDoc 및 인라인 주석은 한국어로 작성 예정

### II. 간결성과 자기설명 코드 (Simplicity and Self-Documenting Code)
- ✅ **PASS**: 메서드는 단일 책임, 20줄 이하 목표
- ✅ **PASS**: 의미 있는 변수명/메서드명 사용 계획
- ✅ **PASS**: DRY 원칙 준수 계획

### III. 테스트 주도 개발 (Test-Driven Development)
- ✅ **PASS**: 모든 Service 계층 단위 테스트 필수
- ✅ **PASS**: Controller 계층 통합 테스트 필수
- ✅ **PASS**: Repository 계층 계약 테스트 권장
- ✅ **PASS**: 목표 코드 커버리지 70% 설정됨

### IV. 사용자 경험 우선 (User Experience First)
- ✅ **PASS**: API 응답 시간 3초 이내 목표(P99), 1초 이내 목표(P95)
- ✅ **PASS**: UI 렌더링 2초 이내 목표
- ✅ **PASS**: 에러 메시지 사용자 친화적으로 작성 계획
- ✅ **PASS**: 접근성 기본 원칙 준수(시맨틱 HTML, SEO 최적화)

### V. 최소 의존성 (Minimal Dependencies)
- ✅ **PASS**: Spring Boot Starter 의존성 우선 사용
- ✅ **PASS**: 새로운 의존성 추가 시 정당화 필요(지도 API, 파일 저장소, CAPTCHA 등)
- ✅ **PASS**: Spring Boot BOM에서 버전 관리

### VI. 리액티브 프로그래밍 (Reactive Programming)
- ✅ **PASS**: 모든 엔드포인트 `Mono<T>` 또는 `Flux<T>` 반환
- ✅ **PASS**: R2DBC 사용 계획(JPA/JDBC 금지)
- ✅ **PASS**: WebClient 사용 계획(RestTemplate 금지)
- ✅ **PASS**: 백프레셔 처리 고려
- ✅ **PASS**: 에러 처리는 리액티브 연산자 사용 계획

### 성능 표준 (Performance Standards)
- ✅ **PASS**: API 응답 시간 P95 1초, P99 3초, 평균 500ms
- ✅ **PASS**: 단일 쿼리 실행 시간 100ms 이내 목표
- ✅ **PASS**: N+1 쿼리 문제 방지 계획
- ✅ **PASS**: 인덱스 최적화 계획
- ✅ **PASS**: JVM 힙 메모리 512MB 이하(개발 환경)
- ✅ **PASS**: 최소 1,000명 동시 사용자 처리 목표

**Constitution Gate**: ✅ **PASS** - 모든 헌법 원칙 준수

## Project Structure

### Documentation (this feature)

```text
specs/[###-feature]/
├── plan.md              # This file (/speckit.plan command output)
├── research.md          # Phase 0 output (/speckit.plan command)
├── data-model.md        # Phase 1 output (/speckit.plan command)
├── quickstart.md        # Phase 1 output (/speckit.plan command)
├── contracts/           # Phase 1 output (/speckit.plan command)
└── tasks.md             # Phase 2 output (/speckit.tasks command - NOT created by /speckit.plan)
```

### Source Code (repository root)

```text
src/main/java/com/nalsil/bear/
├── domain/                          # 도메인 엔티티 및 Repository
│   ├── company/
│   │   ├── Company.java            # 기업 엔티티
│   │   ├── CompanyRepository.java  # R2DBC Repository
│   │   └── DesignTheme.java        # 디자인 테마 엔티티
│   ├── admin/
│   │   ├── Admin.java              # 관리자 엔티티
│   │   └── AdminRepository.java
│   ├── board/
│   │   ├── Board.java              # 게시판 엔티티
│   │   ├── Post.java               # 게시글 엔티티
│   │   ├── BoardRepository.java
│   │   └── PostRepository.java
│   ├── faq/
│   │   ├── Faq.java
│   │   └── FaqRepository.java
│   ├── qna/
│   │   ├── Qna.java
│   │   └── QnaRepository.java
│   ├── youtube/
│   │   ├── YoutubeVideo.java
│   │   └── YoutubeVideoRepository.java
│   └── product/
│       ├── Product.java
│       └── ProductRepository.java
├── service/                         # 비즈니스 로직 (Service 계층)
│   ├── CompanyService.java
│   ├── AdminService.java
│   ├── BoardService.java
│   ├── PostService.java
│   ├── FaqService.java
│   ├── QnaService.java
│   ├── YoutubeVideoService.java
│   └── ProductService.java
├── controller/                      # WebFlux 컨트롤러
│   ├── public/                     # 일반 사용자 컨트롤러
│   │   ├── HomeController.java     # 기업 홈페이지 메인
│   │   ├── BoardController.java    # 게시판 조회
│   │   ├── FaqController.java
│   │   ├── QnaController.java
│   │   └── YoutubeController.java
│   └── admin/                      # 관리자 컨트롤러
│       ├── AdminLoginController.java
│       ├── AdminDashboardController.java
│       ├── AdminBoardController.java
│       ├── AdminFaqController.java
│       ├── AdminQnaController.java
│       └── AdminYoutubeController.java
├── config/                          # Spring 설정
│   ├── SecurityConfig.java         # Spring Security 설정
│   ├── R2dbcConfig.java            # R2DBC 설정
│   ├── ThymeleafConfig.java        # Thymeleaf 설정
│   └── WebFluxConfig.java          # WebFlux 설정
├── dto/                             # Data Transfer Objects
│   ├── request/
│   │   ├── CreatePostRequest.java
│   │   ├── CreateQnaRequest.java
│   │   └── AdminLoginRequest.java
│   └── response/
│       ├── PostResponse.java
│       ├── QnaResponse.java
│       └── AdminDashboardResponse.java
├── filter/                          # WebFlux 필터
│   └── TenantFilter.java           # 멀티테넌트 컨텍스트 설정
├── exception/                       # 커스텀 예외
│   ├── CompanyNotFoundException.java
│   ├── UnauthorizedAccessException.java
│   └── GlobalExceptionHandler.java # 전역 에러 핸들러
└── util/
    ├── TenantContextHolder.java    # ThreadLocal 기반 테넌트 컨텍스트
    └── FileUploadUtil.java         # 파일 업로드 유틸리티

src/main/resources/
├── templates/                       # Thymeleaf 템플릿
│   ├── public/                     # 일반 사용자 템플릿
│   │   ├── home.html
│   │   ├── board/
│   │   │   ├── list.html
│   │   │   └── detail.html
│   │   ├── faq/
│   │   │   └── list.html
│   │   ├── qna/
│   │   │   ├── list.html
│   │   │   └── form.html
│   │   └── youtube/
│   │       └── list.html
│   └── admin/                      # 관리자 템플릿
│       ├── login.html
│       ├── dashboard.html
│       └── board/
│           ├── list.html
│           └── form.html
├── static/                          # 정적 리소스
│   ├── css/
│   │   ├── common.css
│   │   ├── public.css
│   │   └── admin.css
│   ├── js/
│   │   ├── common.js
│   │   └── admin.js
│   └── images/
│       └── default-logo.png
└── application.yaml                 # Spring Boot 설정

src/test/java/com/nalsil/bear/
├── service/                         # Service 단위 테스트
│   ├── CompanyServiceTest.java
│   ├── BoardServiceTest.java
│   └── QnaServiceTest.java
├── controller/                      # Controller 통합 테스트
│   ├── HomeControllerTest.java
│   └── AdminBoardControllerTest.java
└── repository/                      # Repository 계약 테스트
    ├── CompanyRepositoryTest.java
    └── PostRepositoryTest.java
```

**Structure Decision**: Spring Boot 단일 프로젝트 구조를 채택한다. Thymeleaf를 사용한 서버 사이드 렌더링 웹 애플리케이션이므로 별도의 frontend 프로젝트를 만들지 않고, `src/main/resources/templates`에서 Thymeleaf 템플릿을 관리한다. 일반 사용자(`public/`)와 관리자(`admin/`) 컨트롤러 및 템플릿을 명확히 분리하여 권한 관리를 용이하게 한다.

## Complexity Tracking

> **Fill ONLY if Constitution Check has violations that must be justified**

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| [e.g., 4th project] | [current need] | [why 3 projects insufficient] |
| [e.g., Repository pattern] | [specific problem] | [why direct DB access insufficient] |
