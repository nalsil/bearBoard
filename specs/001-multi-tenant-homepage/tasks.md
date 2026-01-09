# Tasks: 멀티테넌트 기업용 홈페이지

**Input**: Design documents from `/specs/001-multi-tenant-homepage/`
**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/

**Tests**: 이 프로젝트는 헌법에서 테스트 주도 개발(TDD)을 요구하므로 모든 테스트 작업이 포함됩니다.

**Organization**: 작업은 사용자 스토리별로 그룹화되어 각 스토리를 독립적으로 구현하고 테스트할 수 있습니다.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: 병렬 실행 가능 (다른 파일, 의존성 없음)
- **[Story]**: 이 작업이 속한 사용자 스토리 (예: US1, US2, US3)
- 설명에 정확한 파일 경로 포함

## Path Conventions

프로젝트 구조는 Spring Boot 단일 프로젝트 구조를 따릅니다:
- `src/main/java/com/nalsil/bear/` - Java 소스 코드
- `src/main/resources/` - 리소스 파일 (templates, static, application.yaml)
- `src/test/java/com/nalsil/bear/` - 테스트 코드

---

## Phase 1: Setup (공유 인프라)

**목적**: 프로젝트 초기화 및 기본 구조 생성

- [ ] T001 프로젝트 디렉토리 구조 생성 (plan.md 기준)
- [ ] T002 build.gradle에 의존성 추가 (Spring Boot 3.5.9, WebFlux, Security, Thymeleaf, R2DBC, Redis, Lombok)
- [ ] T003 [P] application.yaml 기본 설정 작성 (R2DBC, Redis, 파일 업로드, reCAPTCHA, Kakao Map)
- [ ] T004 [P] PostgreSQL 데이터베이스 스키마 초기화 스크립트 작성 (src/main/resources/schema.sql)
- [ ] T005 [P] 테스트 데이터 삽입 스크립트 작성 (src/main/resources/data.sql.old)
- [ ] T006 [P] Gradle 빌드 및 테스트 검증 (./gradlew build)

---

## Phase 2: Foundational (차단 전제조건)

**목적**: 모든 사용자 스토리가 구현되기 전에 완료되어야 하는 핵심 인프라

**⚠️ CRITICAL**: 이 단계가 완료되기 전까지 사용자 스토리 작업을 시작할 수 없습니다.

### 도메인 엔티티 및 Repository (공통)

- [ ] T007 [P] Company 엔티티 및 CompanyRepository 작성 (src/main/java/com/nalsil/bear/domain/company/)
- [ ] T008 [P] Admin 엔티티 및 AdminRepository 작성 (src/main/java/com/nalsil/bear/domain/admin/)
- [ ] T009 [P] Board 엔티티 및 BoardRepository 작성 (src/main/java/com/nalsil/bear/domain/board/)
- [ ] T010 [P] Post 엔티티 및 PostRepository 작성 (src/main/java/com/nalsil/bear/domain/board/)
- [ ] T011 [P] Faq 엔티티 및 FaqRepository 작성 (src/main/java/com/nalsil/bear/domain/faq/)
- [ ] T012 [P] Qna 엔티티 및 QnaRepository 작성 (src/main/java/com/nalsil/bear/domain/qna/)
- [ ] T013 [P] YoutubeVideo 엔티티 및 YoutubeVideoRepository 작성 (src/main/java/com/nalsil/bear/domain/youtube/)
- [ ] T014 [P] Product 엔티티 및 ProductRepository 작성 (src/main/java/com/nalsil/bear/domain/product/)

### 설정 및 보안

- [ ] T015 R2dbcConfig 작성 (src/main/java/com/nalsil/bear/config/R2dbcConfig.java)
- [ ] T016 SecurityConfig 작성 (Spring Security WebFlux 설정, src/main/java/com/nalsil/bear/config/SecurityConfig.java)
- [ ] T017 WebFluxConfig 작성 (CORS, 필터 설정, src/main/java/com/nalsil/bear/config/WebFluxConfig.java)
- [ ] T018 ThymeleafConfig 작성 (Thymeleaf + Security 통합, src/main/java/com/nalsil/bear/config/ThymeleafConfig.java)

### 멀티테넌트 인프라

- [ ] T019 TenantContextHolder 작성 (ThreadLocal 기반 테넌트 컨텍스트, src/main/java/com/nalsil/bear/util/TenantContextHolder.java)
- [ ] T020 TenantFilter 작성 (URL에서 기업 코드 추출 및 컨텍스트 설정, src/main/java/com/nalsil/bear/filter/TenantFilter.java)

### 예외 처리

- [ ] T021 [P] CompanyNotFoundException 작성 (src/main/java/com/nalsil/bear/exception/CompanyNotFoundException.java)
- [ ] T022 [P] UnauthorizedAccessException 작성 (src/main/java/com/nalsil/bear/exception/UnauthorizedAccessException.java)
- [ ] T023 GlobalExceptionHandler 작성 (전역 에러 핸들러, src/main/java/com/nalsil/bear/exception/GlobalExceptionHandler.java)

### 유틸리티

- [ ] T024 FileUploadUtil 작성 (파일 업로드 검증 및 저장, src/main/java/com/nalsil/bear/util/FileUploadUtil.java)

### 테스트 인프라

- [ ] T025 [P] CompanyRepositoryTest 작성 (계약 테스트, src/test/java/com/nalsil/bear/repository/CompanyRepositoryTest.java)
- [ ] T026 [P] PostRepositoryTest 작성 (계약 테스트, src/test/java/com/nalsil/bear/repository/PostRepositoryTest.java)

**Checkpoint**: 기반 준비 완료 - 이제 사용자 스토리 구현을 병렬로 시작할 수 있습니다.

---

## Phase 3: User Story 1 - 기업 홈페이지 접속 및 정보 조회 (Priority: P1) 🎯 MVP

**Goal**: 일반 사용자가 기업 홈페이지에 접속하여 기업 정보, 상품, 게시판 글을 조회할 수 있습니다.

**Independent Test**: 브라우저에서 `http://localhost:8080/company-a`로 접속하여 회사 소개, 상품 목록, 최신 게시글이 정상적으로 표시되는지 확인합니다.

### 단위 테스트 (US1)

> **NOTE: 이 테스트들을 먼저 작성하고, 구현 전에 실패하는지 확인하세요**

- [ ] T027 [P] [US1] CompanyServiceTest 작성 (기업 조회 단위 테스트, src/test/java/com/nalsil/bear/service/CompanyServiceTest.java)
- [ ] T028 [P] [US1] BoardServiceTest 작성 (게시판 목록 조회 단위 테스트, src/test/java/com/nalsil/bear/service/BoardServiceTest.java)
- [ ] T029 [P] [US1] PostServiceTest 작성 (게시글 조회 단위 테스트, src/test/java/com/nalsil/bear/service/PostServiceTest.java)
- [ ] T030 [P] [US1] ProductServiceTest 작성 (상품 조회 단위 테스트, src/test/java/com/nalsil/bear/service/ProductServiceTest.java)

### 통합 테스트 (US1)

- [ ] T031 [P] [US1] HomeControllerTest 작성 (홈페이지 조회 통합 테스트, src/test/java/com/nalsil/bear/controller/HomeControllerTest.java)
- [ ] T032 [P] [US1] BoardControllerTest 작성 (게시판 조회 통합 테스트, src/test/java/com/nalsil/bear/controller/BoardControllerTest.java)

### 구현 (US1)

#### Service 계층

- [ ] T033 [US1] CompanyService 구현 (기업 조회, src/main/java/com/nalsil/bear/service/CompanyService.java)
- [ ] T034 [US1] BoardService 구현 (게시판 및 게시글 조회, 조회수 증가, src/main/java/com/nalsil/bear/service/BoardService.java)
- [ ] T035 [US1] PostService 구현 (게시글 상세 조회, src/main/java/com/nalsil/bear/service/PostService.java)
- [ ] T036 [US1] ProductService 구현 (상품 조회, src/main/java/com/nalsil/bear/service/ProductService.java)

#### Controller 계층

- [ ] T037 [US1] HomeController 구현 (기업 메인 페이지, 회사 소개, src/main/java/com/nalsil/bear/controller/public_/HomeController.java)
- [ ] T038 [US1] BoardController 구현 (게시판 목록, 게시글 상세, src/main/java/com/nalsil/bear/controller/public_/BoardController.java)

#### Thymeleaf 템플릿

- [ ] T039 [P] [US1] 공통 레이아웃 템플릿 작성 (헤더, 푸터, src/main/resources/templates/layout/layout.html)
- [ ] T040 [P] [US1] 기업 홈 페이지 템플릿 작성 (src/main/resources/templates/public/home.html)
- [ ] T041 [P] [US1] 회사 소개 페이지 템플릿 작성 (src/main/resources/templates/public/about.html)
- [ ] T042 [P] [US1] 게시판 목록 템플릿 작성 (src/main/resources/templates/public/board/list.html)
- [ ] T043 [P] [US1] 게시글 상세 템플릿 작성 (src/main/resources/templates/public/board/detail.html)
- [ ] T044 [P] [US1] 상품 목록 템플릿 작성 (src/main/resources/templates/public/product/list.html)

#### 정적 리소스

- [ ] T045 [P] [US1] 공통 CSS 작성 (반응형 디자인, src/main/resources/static/css/common.css)
- [ ] T046 [P] [US1] 일반 사용자 CSS 작성 (src/main/resources/static/css/public.css)
- [ ] T047 [P] [US1] 공통 JavaScript 작성 (src/main/resources/static/js/common.js)

#### 통합 및 검증

- [ ] T048 [US1] Kakao Map API 통합 (회사 소개 페이지에 지도 표시)
- [ ] T049 [US1] SEO 최적화 (H1, H2, H3 계층, meta 태그)
- [ ] T050 [US1] 에러 처리 추가 (404, 500 에러 페이지)
- [ ] T051 [US1] 로깅 추가 (기업 홈페이지 접속 로그)

**Checkpoint**: User Story 1이 완전히 작동하고 독립적으로 테스트 가능해야 합니다.

---

## Phase 4: User Story 2 - FAQ 및 QnA 조회 (Priority: P2)

**Goal**: 일반 사용자가 FAQ를 확인하거나 QnA 게시판을 통해 질문을 등록하고 답변을 조회할 수 있습니다.

**Independent Test**: A기업 홈페이지에서 FAQ 페이지로 이동하여 질문 목록이 표시되고, 각 질문을 클릭하면 답변이 펼쳐지는지 확인합니다. QnA 페이지에서 이메일 주소로 질문을 등록할 수 있는지 확인합니다.

### 단위 테스트 (US2)

- [ ] T052 [P] [US2] FaqServiceTest 작성 (FAQ 조회 단위 테스트, src/test/java/com/nalsil/bear/service/FaqServiceTest.java)
- [ ] T053 [P] [US2] QnaServiceTest 작성 (QnA 조회 및 등록 단위 테스트, src/test/java/com/nalsil/bear/service/QnaServiceTest.java)

### 통합 테스트 (US2)

- [ ] T054 [P] [US2] FaqControllerTest 작성 (FAQ 조회 통합 테스트, src/test/java/com/nalsil/bear/controller/FaqControllerTest.java)
- [ ] T055 [P] [US2] QnaControllerTest 작성 (QnA 조회 및 등록 통합 테스트, src/test/java/com/nalsil/bear/controller/QnaControllerTest.java)

### 구현 (US2)

#### DTO

- [ ] T056 [P] [US2] CreateQnaRequest DTO 작성 (src/main/java/com/nalsil/bear/dto/request/CreateQnaRequest.java)
- [ ] T057 [P] [US2] QnaResponse DTO 작성 (src/main/java/com/nalsil/bear/dto/response/QnaResponse.java)

#### Service 계층

- [ ] T058 [US2] FaqService 구현 (FAQ 조회, 검색, src/main/java/com/nalsil/bear/service/FaqService.java)
- [ ] T059 [US2] QnaService 구현 (QnA 조회, 질문 등록, 이메일 검증, reCAPTCHA 검증, src/main/java/com/nalsil/bear/service/QnaService.java)

#### Controller 계층

- [ ] T060 [US2] FaqController 구현 (FAQ 목록 조회, 검색, src/main/java/com/nalsil/bear/controller/public_/FaqController.java)
- [ ] T061 [US2] QnaController 구현 (QnA 목록 조회, 질문 등록 폼, 질문 등록 처리, src/main/java/com/nalsil/bear/controller/public_/QnaController.java)

#### Thymeleaf 템플릿

- [ ] T062 [P] [US2] FAQ 목록 템플릿 작성 (아코디언 UI, src/main/resources/templates/public/faq/list.html)
- [ ] T063 [P] [US2] QnA 목록 템플릿 작성 (src/main/resources/templates/public/qna/list.html)
- [ ] T064 [P] [US2] QnA 질문 등록 폼 템플릿 작성 (reCAPTCHA v3 포함, src/main/resources/templates/public/qna/form.html)
- [ ] T065 [P] [US2] QnA 상세 템플릿 작성 (질문 + 답변 표시, src/main/resources/templates/public/qna/detail.html)

#### 통합 및 검증

- [ ] T066 [US2] Google reCAPTCHA v3 통합 (QnA 질문 등록 시 스팸 방지)
- [ ] T067 [US2] 이메일 형식 검증 추가 (백엔드 및 프론트엔드)
- [ ] T068 [US2] 스팸 질문 등록 제한 (동일 IP 1분 내 3회 제한, Redis 캐시 사용)
- [ ] T069 [US2] 에러 처리 추가 (잘못된 이메일, reCAPTCHA 실패)

**Checkpoint**: User Story 1과 2가 모두 독립적으로 작동해야 합니다.

---

## Phase 5: User Story 3 - 유튜브 영상 조회 (Priority: P3)

**Goal**: 일반 사용자가 기업에서 등록한 유튜브 영상 목록을 조회하고 재생할 수 있습니다.

**Independent Test**: A기업 홈페이지에서 "영상 갤러리" 메뉴로 이동하여 유튜브 영상 썸네일 목록이 표시되고, 각 영상을 클릭하면 재생되는지 확인합니다.

### 단위 테스트 (US3)

- [ ] T070 [P] [US3] YoutubeVideoServiceTest 작성 (유튜브 영상 조회 단위 테스트, src/test/java/com/nalsil/bear/service/YoutubeVideoServiceTest.java)

### 통합 테스트 (US3)

- [ ] T071 [P] [US3] YoutubeControllerTest 작성 (유튜브 영상 조회 통합 테스트, src/test/java/com/nalsil/bear/controller/YoutubeControllerTest.java)

### 구현 (US3)

#### Service 계층

- [ ] T072 [US3] YoutubeVideoService 구현 (유튜브 영상 조회, src/main/java/com/nalsil/bear/service/YoutubeVideoService.java)

#### Controller 계층

- [ ] T073 [US3] YoutubeController 구현 (유튜브 영상 목록 조회, src/main/java/com/nalsil/bear/controller/public_/YoutubeController.java)

#### Thymeleaf 템플릿

- [ ] T074 [P] [US3] 유튜브 영상 목록 템플릿 작성 (썸네일 그리드, src/main/resources/templates/public/youtube/list.html)
- [ ] T075 [P] [US3] 유튜브 영상 재생 템플릿 작성 (iframe 임베드, src/main/resources/templates/public/youtube/player.html)

#### 통합 및 검증

- [ ] T076 [US3] 유튜브 URL 검증 추가 (잘못된 URL 또는 삭제된 영상 처리)
- [ ] T077 [US3] 에러 처리 추가 (영상을 불러올 수 없는 경우)

**Checkpoint**: User Story 1, 2, 3가 모두 독립적으로 작동해야 합니다.

---

## Phase 6: User Story 4 - 관리자 로그인 및 컨텐츠 관리 (Priority: P4)

**Goal**: 기업별 관리자가 로그인하여 자신의 기업 홈페이지 컨텐츠(게시판, FAQ, QnA, 유튜브 영상)를 등록, 수정, 삭제, 숨김 처리할 수 있습니다.

**Independent Test**: 관리자 계정으로 로그인하여 게시글 등록 폼이 표시되고, 게시글을 작성한 후 저장하면 해당 기업의 홈페이지에 즉시 반영되는지 확인합니다.

### 단위 테스트 (US4)

- [ ] T078 [P] [US4] AdminServiceTest 작성 (관리자 인증 단위 테스트, src/test/java/com/nalsil/bear/service/AdminServiceTest.java)

### 통합 테스트 (US4)

- [ ] T079 [P] [US4] AdminLoginControllerTest 작성 (관리자 로그인 통합 테스트, src/test/java/com/nalsil/bear/controller/admin/AdminLoginControllerTest.java)
- [ ] T080 [P] [US4] AdminBoardControllerTest 작성 (관리자 게시판 관리 통합 테스트, src/test/java/com/nalsil/bear/controller/admin/AdminBoardControllerTest.java)

### 구현 (US4)

#### DTO

- [ ] T081 [P] [US4] AdminLoginRequest DTO 작성 (src/main/java/com/nalsil/bear/dto/request/AdminLoginRequest.java)
- [ ] T082 [P] [US4] CreatePostRequest DTO 작성 (src/main/java/com/nalsil/bear/dto/request/CreatePostRequest.java)
- [ ] T083 [P] [US4] PostResponse DTO 작성 (src/main/java/com/nalsil/bear/dto/response/PostResponse.java)
- [ ] T084 [P] [US4] AdminDashboardResponse DTO 작성 (src/main/java/com/nalsil/bear/dto/response/AdminDashboardResponse.java)

#### Service 계층

- [ ] T085 [US4] AdminService 구현 (관리자 인증, 세션 관리, src/main/java/com/nalsil/bear/service/AdminService.java)

#### Controller 계층 (관리자)

- [ ] T086 [US4] AdminLoginController 구현 (로그인 페이지, 로그인 처리, 로그아웃, src/main/java/com/nalsil/bear/controller/admin/AdminLoginController.java)
- [ ] T087 [US4] AdminDashboardController 구현 (대시보드, src/main/java/com/nalsil/bear/controller/admin/AdminDashboardController.java)
- [ ] T088 [US4] AdminBoardController 구현 (게시판 관리 CRUD, src/main/java/com/nalsil/bear/controller/admin/AdminBoardController.java)
- [ ] T089 [US4] AdminFaqController 구현 (FAQ 관리 CRUD, src/main/java/com/nalsil/bear/controller/admin/AdminFaqController.java)
- [ ] T090 [US4] AdminQnaController 구현 (QnA 답변 관리, src/main/java/com/nalsil/bear/controller/admin/AdminQnaController.java)
- [ ] T091 [US4] AdminYoutubeController 구현 (유튜브 영상 관리 CRUD, src/main/java/com/nalsil/bear/controller/admin/AdminYoutubeController.java)
- [ ] T092 [US4] AdminProductController 구현 (상품 관리 CRUD, src/main/java/com/nalsil/bear/controller/admin/AdminProductController.java)

#### Thymeleaf 템플릿 (관리자)

- [ ] T093 [P] [US4] 관리자 로그인 템플릿 작성 (src/main/resources/templates/admin/login.html)
- [ ] T094 [P] [US4] 관리자 대시보드 템플릿 작성 (src/main/resources/templates/admin/dashboard.html)
- [ ] T095 [P] [US4] 게시판 목록 관리 템플릿 작성 (src/main/resources/templates/admin/board/list.html)
- [ ] T096 [P] [US4] 게시글 작성/수정 폼 템플릿 작성 (파일 업로드 포함, src/main/resources/templates/admin/board/form.html)
- [ ] T097 [P] [US4] FAQ 관리 템플릿 작성 (src/main/resources/templates/admin/faq/list.html)
- [ ] T098 [P] [US4] QnA 관리 템플릿 작성 (답변 작성 폼 포함, src/main/resources/templates/admin/qna/list.html)
- [ ] T099 [P] [US4] 유튜브 영상 관리 템플릿 작성 (src/main/resources/templates/admin/youtube/list.html)
- [ ] T100 [P] [US4] 상품 관리 템플릿 작성 (src/main/resources/templates/admin/product/list.html)

#### 정적 리소스 (관리자)

- [ ] T101 [P] [US4] 관리자 CSS 작성 (src/main/resources/static/css/admin.css)
- [ ] T102 [P] [US4] 관리자 JavaScript 작성 (파일 업로드 검증, src/main/resources/static/js/admin.js)

#### 통합 및 검증

- [ ] T103 [US4] Spring Security 설정 업데이트 (관리자 경로 보호, 세션 관리)
- [ ] T104 [US4] 파일 업로드 기능 통합 (20MB 제한, MIME 타입 검증)
- [ ] T105 [US4] 숨김 처리 기능 구현 (게시글, FAQ, QnA, 유튜브, 상품)
- [ ] T106 [US4] 권한 검증 추가 (다른 기업의 컨텐츠 수정 시도 차단)
- [ ] T107 [US4] 에러 처리 추가 (인증 실패, 권한 부족, 파일 업로드 실패)

**Checkpoint**: User Story 1~4가 모두 독립적으로 작동해야 합니다.

---

## Phase 7: User Story 5 - 슈퍼유저의 전체 기업 관리 (Priority: P5)

**Goal**: 슈퍼유저가 로그인하여 모든 기업의 컨텐츠를 관리하고, 대상 기업을 선택하여 해당 기업의 관리자 권한으로 작업할 수 있습니다.

**Independent Test**: 슈퍼유저 계정으로 로그인하여 기업 선택 드롭다운에서 A기업을 선택한 후, A기업의 게시글을 수정하고 다시 B기업을 선택하여 B기업의 컨텐츠를 관리할 수 있는지 확인합니다.

### 단위 테스트 (US5)

- [ ] T108 [P] [US5] AdminService 슈퍼유저 기능 테스트 (기업 선택, src/test/java/com/nalsil/bear/service/AdminServiceTest.java)

### 통합 테스트 (US5)

- [ ] T109 [P] [US5] AdminDashboardController 슈퍼유저 테스트 (기업 선택 통합 테스트, src/test/java/com/nalsil/bear/controller/admin/AdminDashboardControllerTest.java)

### 구현 (US5)

#### Service 계층

- [ ] T110 [US5] AdminService 슈퍼유저 기능 추가 (모든 기업 조회, 기업 선택)

#### Controller 계층

- [ ] T111 [US5] AdminDashboardController 슈퍼유저 기능 추가 (기업 선택 드롭다운, 기업 전환 처리)

#### Thymeleaf 템플릿

- [ ] T112 [P] [US5] 대시보드 템플릿 업데이트 (슈퍼유저용 기업 선택 드롭다운 추가)
- [ ] T113 [P] [US5] 전체 기업 통계 템플릿 작성 (src/main/resources/templates/admin/statistics.html)

#### 통합 및 검증

- [ ] T114 [US5] 세션 관리 업데이트 (슈퍼유저의 선택된 기업 ID 저장)
- [ ] T115 [US5] 권한 검증 업데이트 (슈퍼유저는 모든 기업 접근 가능)
- [ ] T116 [US5] 기업 전환 기능 테스트 (2초 이내 전환 검증)

**Checkpoint**: 모든 사용자 스토리가 독립적으로 작동해야 합니다.

---

## Phase 8: Polish & Cross-Cutting Concerns

**목적**: 여러 사용자 스토리에 영향을 미치는 개선 사항

- [ ] T117 [P] 404 에러 페이지 템플릿 작성 (src/main/resources/templates/error/404.html)
- [ ] T118 [P] 500 에러 페이지 템플릿 작성 (src/main/resources/templates/error/500.html)
- [ ] T119 [P] 403 에러 페이지 템플릿 작성 (src/main/resources/templates/error/403.html)
- [ ] T120 [P] README.md 업데이트 (프로젝트 개요, 실행 방법)
- [ ] T121 코드 리팩토링 (중복 코드 제거, DRY 원칙 적용)
- [ ] T122 성능 최적화 (N+1 쿼리 방지, 인덱스 최적화, 캐싱)
- [ ] T123 보안 강화 (SQL 인젝션 방지, XSS 방지, CSRF 토큰)
- [ ] T124 [P] 단위 테스트 커버리지 70% 달성 확인 (./gradlew jacocoTestReport)
- [ ] T125 [P] quickstart.md 검증 실행 (개발 환경 설정 가이드 테스트)
- [ ] T126 Actuator 엔드포인트 보안 설정 (health, metrics만 공개)
- [ ] T127 로깅 레벨 및 형식 최적화 (운영 환경 대비)

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: 의존성 없음 - 즉시 시작 가능
- **Foundational (Phase 2)**: Setup 완료에 의존 - 모든 사용자 스토리를 차단
- **User Stories (Phase 3~7)**: 모두 Foundational 단계 완료에 의존
  - 사용자 스토리는 병렬로 진행 가능 (인력이 있는 경우)
  - 또는 우선순위 순서대로 순차 진행 (P1 → P2 → P3 → P4 → P5)
- **Polish (Phase 8)**: 원하는 모든 사용자 스토리 완료에 의존

### User Story Dependencies

- **User Story 1 (P1)**: Foundational (Phase 2) 이후 시작 가능 - 다른 스토리에 의존성 없음
- **User Story 2 (P2)**: Foundational (Phase 2) 이후 시작 가능 - US1과 통합되지만 독립적으로 테스트 가능
- **User Story 3 (P3)**: Foundational (Phase 2) 이후 시작 가능 - US1/US2와 통합되지만 독립적으로 테스트 가능
- **User Story 4 (P4)**: Foundational (Phase 2) 이후 시작 가능 - US1~3과 통합되지만 독립적으로 테스트 가능
- **User Story 5 (P5)**: Foundational (Phase 2) 이후 시작 가능 - US4에 의존하지만 독립적으로 테스트 가능

### Within Each User Story

- 테스트를 먼저 작성하고, 구현 전에 실패하는지 확인
- 모델 → 서비스 → 컨트롤러 → 템플릿 순서
- 핵심 구현 → 통합 → 검증 순서
- 스토리 완료 후 다음 우선순위로 이동

### Parallel Opportunities

- Setup의 모든 [P] 작업은 병렬 실행 가능
- Foundational의 모든 [P] 작업은 병렬 실행 가능 (Phase 2 내에서)
- Foundational 단계 완료 후, 모든 사용자 스토리를 병렬로 시작 가능 (팀 역량이 허용하는 경우)
- 사용자 스토리 내 모든 테스트 [P] 작업은 병렬 실행 가능
- 사용자 스토리 내 모델 [P] 작업은 병렬 실행 가능
- 서로 다른 사용자 스토리는 다른 팀원이 병렬로 작업 가능

---

## Parallel Example: User Story 1

```bash
# User Story 1의 모든 단위 테스트를 함께 실행:
Task: "CompanyServiceTest 작성 (기업 조회 단위 테스트)"
Task: "BoardServiceTest 작성 (게시판 목록 조회 단위 테스트)"
Task: "PostServiceTest 작성 (게시글 조회 단위 테스트)"
Task: "ProductServiceTest 작성 (상품 조회 단위 테스트)"

# User Story 1의 모든 템플릿을 함께 작성:
Task: "공통 레이아웃 템플릿 작성"
Task: "기업 홈 페이지 템플릿 작성"
Task: "회사 소개 페이지 템플릿 작성"
Task: "게시판 목록 템플릿 작성"
Task: "게시글 상세 템플릿 작성"
Task: "상품 목록 템플릿 작성"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Phase 1 완료: Setup
2. Phase 2 완료: Foundational (CRITICAL - 모든 스토리 차단)
3. Phase 3 완료: User Story 1
4. **STOP and VALIDATE**: User Story 1을 독립적으로 테스트
5. 준비되면 배포/데모

### Incremental Delivery

1. Setup + Foundational 완료 → 기반 준비 완료
2. User Story 1 추가 → 독립적으로 테스트 → 배포/데모 (MVP!)
3. User Story 2 추가 → 독립적으로 테스트 → 배포/데모
4. User Story 3 추가 → 독립적으로 테스트 → 배포/데모
5. User Story 4 추가 → 독립적으로 테스트 → 배포/데모
6. User Story 5 추가 → 독립적으로 테스트 → 배포/데모
7. 각 스토리는 이전 스토리를 손상시키지 않고 가치를 추가

### Parallel Team Strategy

여러 개발자가 있는 경우:

1. 팀이 함께 Setup + Foundational 완료
2. Foundational 완료 후:
   - Developer A: User Story 1
   - Developer B: User Story 2
   - Developer C: User Story 3
   - Developer D: User Story 4
   - Developer E: User Story 5
3. 스토리가 독립적으로 완료되고 통합됨

---

## Notes

- [P] 작업 = 다른 파일, 의존성 없음
- [Story] 라벨은 작업을 특정 사용자 스토리에 매핑하여 추적 가능
- 각 사용자 스토리는 독립적으로 완료 및 테스트 가능해야 함
- 구현 전에 테스트가 실패하는지 확인
- 각 작업 또는 논리적 그룹 후 커밋
- 체크포인트에서 중지하여 스토리를 독립적으로 검증
- 피해야 할 것: 모호한 작업, 동일 파일 충돌, 독립성을 깨는 스토리 간 의존성
