<!--
Sync Impact Report:
- Version Change: none → 1.0.0 (initial ratification)
- Modified Principles: none (initial creation)
- Added Sections: All sections (initial creation)
  - I. 한국어 우선 (Korean-First Development)
  - II. 간결성과 자기설명 코드 (Simplicity and Self-Documenting Code)
  - III. 테스트 주도 개발 (Test-Driven Development)
  - IV. 사용자 경험 우선 (User Experience First)
  - V. 최소 의존성 (Minimal Dependencies)
  - VI. 리액티브 프로그래밍 (Reactive Programming)
  - Performance Standards
  - Development Workflow
  - Governance
- Removed Sections: none
- Templates Requiring Updates:
  - ✅ plan-template.md - Constitution Check section references this file
  - ✅ spec-template.md - Requirements alignment verified
  - ✅ tasks-template.md - Test coverage requirements aligned
- Follow-up TODOs: none
-->

# Bear 프로젝트 헌법

## 핵심 원칙 (Core Principles)

### I. 한국어 우선 (Korean-First Development)

모든 프로젝트 문서, 코드 주석, 커밋 메시지는 한국어로 작성한다.

**규칙**:
- 클래스, 메서드, 변수명은 영어로 작성 (Java 표준 관례 준수)
- JavaDoc 및 인라인 주석은 한국어로 작성
- README, 기술 문서, API 문서는 한국어로 작성
- Git 커밋 메시지는 한국어로 작성
- 예외: 국제 표준 용어, 프레임워크 용어는 영어 유지 (예: Controller, Service, Repository)

**근거**: 개발팀의 주요 언어가 한국어이며, 코드 이해도와 유지보수성을 높이기 위함

### II. 간결성과 자기설명 코드 (Simplicity and Self-Documenting Code)

코드는 간결하고 읽기 쉬워야 하며, 주석 없이도 의도가 명확해야 한다.

**규칙**:
- 메서드는 단일 책임을 가지며 20줄 이하로 작성
- 의미 있는 변수명과 메서드명 사용 (예: `getUserById` vs `get`)
- 주석은 "왜(why)"를 설명할 때만 사용, "무엇(what)"은 코드로 표현
- 복잡한 비즈니스 로직은 별도 메서드로 추출
- DRY(Don't Repeat Yourself) 원칙 준수

**근거**: 유지보수성 향상 및 코드 리뷰 효율성 증대

### III. 테스트 주도 개발 (Test-Driven Development)

핵심 비즈니스 로직은 반드시 테스트를 작성하며, 최소 70% 코드 커버리지를 유지한다.

**규칙**:
- 모든 Service 계층 로직은 단위 테스트 필수
- Controller 계층은 통합 테스트 필수
- Repository 계층은 계약 테스트 권장
- 테스트는 Given-When-Then 패턴으로 작성
- 테스트 코드도 프로덕션 코드와 동일한 품질 기준 적용
- `./gradlew test` 실행 시 70% 이상 커버리지 달성

**근거**: 안정적인 리팩토링과 회귀 버그 방지

### IV. 사용자 경험 우선 (User Experience First)

사용자 인터페이스는 직관적이고 빠르게 반응해야 한다.

**규칙**:
- 모든 API 응답 시간은 3초 이내 (목표: 1초 이내)
- UI 렌더링은 2초 이내 완료
- 에러 메시지는 사용자 친화적으로 작성 (기술 용어 최소화)
- 로딩 상태는 명확하게 표시 (스피너, 프로그레스 바 등)
- 접근성(Accessibility) 기본 원칙 준수

**근거**: 사용자 만족도 및 재사용률 향상

### V. 최소 의존성 (Minimal Dependencies)

불필요한 외부 라이브러리 사용을 지양하고, 필요한 경우에만 추가한다.

**규칙**:
- 새로운 의존성 추가 시 반드시 정당한 이유 문서화
- Spring Boot Starter 의존성 우선 사용
- 동일 기능을 제공하는 라이브러리가 여러 개일 경우, 가장 경량화된 것 선택
- 사용하지 않는 의존성은 즉시 제거
- 의존성 버전은 Spring Boot BOM(Bill of Materials)에서 관리

**근거**: 빌드 시간 단축, 보안 취약점 최소화, 애플리케이션 크기 감소

### VI. 리액티브 프로그래밍 (Reactive Programming)

Spring WebFlux를 사용한 리액티브 프로그래밍 패턴을 준수한다.

**규칙**:
- 모든 웹 엔드포인트는 `Mono<T>` 또는 `Flux<T>` 반환
- 블로킹 I/O 작업 금지 (RestTemplate 대신 WebClient 사용)
- 데이터베이스 접근은 R2DBC 사용 (JPA/JDBC 금지)
- 백프레셔(backpressure) 처리 고려
- 에러 처리는 `onErrorResume`, `onErrorReturn` 등 리액티브 연산자 사용

**근거**: 높은 동시성 처리 능력 및 시스템 자원 효율성 향상

## 성능 표준 (Performance Standards)

### API 응답 시간
- P95: 1초 이내
- P99: 3초 이내
- 평균: 500ms 이내

### 데이터베이스 쿼리
- 단일 쿼리 실행 시간: 100ms 이내
- N+1 쿼리 문제 발생 금지
- 인덱스 최적화 필수

### 메모리 사용량
- JVM 힙 메모리: 512MB 이하 (개발 환경 기준)
- 메모리 누수 방지를 위한 프로파일링 주기적 실행

### 동시 사용자 처리
- 최소 1,000명의 동시 사용자 처리 가능
- 부하 테스트를 통한 검증 필수

## 개발 워크플로우 (Development Workflow)

### 코드 리뷰
- 모든 Pull Request는 최소 1명의 리뷰어 승인 필요
- 리뷰는 24시간 이내 완료
- 헌법 위반 사항은 즉시 반려

### 테스트 게이트
- PR 병합 전 모든 테스트 통과 필수
- 코드 커버리지 70% 미만 시 병합 불가
- 성능 회귀 테스트 통과 필수

### 배포 승인
- Production 배포는 테스트 환경 검증 후 진행
- 롤백 계획 수립 필수
- Actuator 엔드포인트 보안 설정 확인

### 문서화
- API 변경 시 Swagger/OpenAPI 문서 업데이트 필수
- 주요 기능 추가 시 README 업데이트
- 복잡한 아키텍처 결정은 ADR(Architecture Decision Record) 작성

## 거버넌스 (Governance)

이 헌법은 모든 개발 관행보다 우선하며, 위반 시 코드 리뷰에서 반려된다.

### 개정 절차
- 헌법 개정은 팀 전체의 합의 필요
- 개정 시 버전 증가 (Semantic Versioning 준수)
- 개정 이유 및 마이그레이션 계획 문서화 필수

### 준수 검증
- 모든 PR은 헌법 준수 여부 확인
- 복잡성 증가는 명확한 정당화 필요
- 정기적인 헌법 준수 감사 실시 (분기별)

### 예외 처리
- 헌법 위반이 불가피한 경우, 명시적 예외 요청 및 승인 필요
- 예외는 임시 조치이며, 기술 부채로 등록하여 추후 해결 계획 수립

### 실행 가이드
- 개발 환경 설정 및 실행 가이드는 `CLAUDE.md` 참조
- Specify 워크플로우 실행은 `.specify/templates/commands/*.md` 참조

**Version**: 1.0.0 | **Ratified**: 2025-12-30 | **Last Amended**: 2025-12-30
