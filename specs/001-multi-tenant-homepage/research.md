# Research: 멀티테넌트 기업용 홈페이지

**Date**: 2025-12-30
**Feature**: [spec.md](./spec.md)

## 연구 개요

Technical Context에서 식별된 4가지 NEEDS CLARIFICATION 항목에 대한 연구 및 결정 사항을 문서화한다.

---

## 1. 지도 서비스 통합

### Decision
**Kakao Map API** 선택

### Rationale
1. **한국 사용자 대상**: 대부분의 기업 고객이 한국에 위치하고, Kakao Map은 한국 지역에 대해 가장 정확한 정보 제공
2. **무료 티어 제공**: 월 300,000건까지 무료로 사용 가능하여 초기 비용 부담 없음
3. **한국어 문서**: 공식 문서가 한국어로 제공되어 개발 및 유지보수 용이
4. **JavaScript SDK**: 웹 페이지에 간단히 통합 가능한 JavaScript SDK 제공
5. **반응형 지원**: 모바일과 데스크톱에서 모두 잘 작동

### Alternatives Considered
- **Google Maps API**:
  - 장점: 글로벌 커버리지, 풍부한 기능
  - 단점: 한국 지역 정확도 낮음, 비용이 높음(월 $200 크레딧 후 과금), 영문 문서

- **Naver Map API**:
  - 장점: 한국 지역 정확도 높음, 무료 티어 제공
  - 단점: Kakao Map 대비 사용자 친화도 낮음, API 제한 사항 많음

### Implementation Details
- Kakao Developers에서 앱 등록 및 JavaScript 키 발급
- Thymeleaf 템플릿에서 Kakao Map JavaScript SDK 로드
- 기업 주소를 좌표로 변환하는 Geocoding API 사용
- 지도 마커 클릭 시 기업 정보 표시

---

## 2. 파일 저장소

### Decision
**로컬 파일 시스템** 선택 (초기), 향후 **AWS S3** 마이그레이션 고려

### Rationale
1. **최소 의존성 원칙**: 헌법 원칙 V에 따라 초기 단계에서는 외부 서비스 의존성 최소화
2. **비용 절감**: 개발 및 초기 운영 단계에서 클라우드 스토리지 비용 없음
3. **간단한 구현**: Spring Boot의 `MultipartFile` 처리만으로 구현 가능
4. **빠른 로컬 액세스**: 로컬 파일 시스템은 네트워크 지연 없이 빠른 읽기/쓰기 가능
5. **마이그레이션 용이성**: 추후 S3로 마이그레이션 시 `FileUploadUtil` 인터페이스만 변경하면 됨

### Alternatives Considered
- **AWS S3**:
  - 장점: 확장성, 내구성, CDN 통합 용이
  - 단점: 초기 비용, 외부 서비스 의존성, 설정 복잡도 증가

- **Azure Blob Storage**:
  - 장점: 확장성, Microsoft 생태계 통합
  - 단점: 초기 비용, 외부 서비스 의존성, 한국 리전 제한적

### Implementation Details
- 파일 저장 경로: `${user.home}/bear-uploads/{company-code}/{type}/{filename}`
  - `type`: `post`, `product` 등
  - `filename`: UUID + 원본 파일 확장자
- 파일 크기 제한: `spring.servlet.multipart.max-file-size=20MB`
- 허용 MIME 타입: `application/pdf`, `application/msword`, `application/vnd.ms-excel`, `image/jpeg`, `image/png`, `image/gif`, `application/zip`
- 파일 업로드 시 MIME 타입 검증 및 바이러스 스캔 고려(ClamAV 통합 검토)

### Future Migration Path
- S3 마이그레이션 시점: 월 10GB 이상 파일 저장 또는 다중 서버 배포 필요 시
- `FileStorageService` 인터페이스 정의 후 구현체 교체 패턴 사용

---

## 3. 스팸 방지

### Decision
**Google reCAPTCHA v3** 선택

### Rationale
1. **사용자 경험**: reCAPTCHA v3는 백그라운드에서 실행되어 사용자가 체크박스를 클릭할 필요 없음
2. **스코어 기반**: 0.0~1.0 점수를 반환하여 관리자가 임계값 조정 가능
3. **무료**: Google 서비스로 무료 제공
4. **Spring Boot 통합 용이**: RestTemplate/WebClient로 간단히 검증 API 호출 가능
5. **낮은 복잡도**: 프론트엔드에 스크립트 추가 및 백엔드에서 검증만 하면 됨

### Alternatives Considered
- **Google reCAPTCHA v2**:
  - 장점: 명확한 봇 차단
  - 단점: 사용자 경험 저하(체크박스 클릭 필요)

- **hCaptcha**:
  - 장점: 프라이버시 중심, reCAPTCHA 대안
  - 단점: 사용자가 체크박스 클릭 필요, reCAPTCHA 대비 인지도 낮음

### Implementation Details
- reCAPTCHA v3 사이트 키 및 비밀 키 발급
- QnA 질문 등록 폼에 reCAPTCHA v3 JavaScript SDK 추가
- 백엔드에서 `g-recaptcha-response` 토큰을 Google API로 검증
- 스코어 임계값: 0.5 이상이면 정상 사용자로 판단
- 0.5 미만이면 "스팸으로 의심되는 요청입니다" 에러 메시지 표시
- 추가 방어: 동일 IP에서 1분 내 3회 이상 등록 시도 차단(Redis 캐시 사용)

---

## 4. 세션 관리

### Decision
**Spring Session + Redis** 선택

### Rationale
1. **세션 클러스터링**: 다중 서버 배포 시 세션 공유 필요
2. **빠른 세션 액세스**: Redis는 인메모리 데이터베이스로 세션 조회 성능 우수
3. **Spring Boot 통합**: `spring-session-data-redis` 의존성 추가만으로 간단히 통합
4. **세션 타임아웃 관리**: Redis의 TTL 기능으로 세션 자동 만료 처리
5. **헌법 원칙 준수**: 리액티브 프로그래밍 원칙에 부합(Spring WebFlux와 Redis Reactive 통합)

### Alternatives Considered
- **JWT 토큰**:
  - 장점: 서버 상태 없음(Stateless), 확장성 높음
  - 단점: 관리자 로그아웃 시 토큰 무효화 어려움, 토큰 갱신 로직 복잡, 세션 관리 기능 부족

- **Spring Session (기본 설정)**:
  - 장점: 간단한 설정
  - 단점: 단일 서버 환경에서만 동작, 세션 클러스터링 불가

### Implementation Details
- Redis 설치 및 실행(로컬 개발: Docker, 운영: AWS ElastiCache 또는 별도 Redis 서버)
- 의존성 추가: `spring-boot-starter-data-redis-reactive`, `spring-session-data-redis`
- `application.yaml`에서 Redis 연결 정보 설정:
  ```yaml
  spring:
    redis:
      host: localhost
      port: 6379
    session:
      store-type: redis
      timeout: 1800s  # 30분 세션 타임아웃
  ```
- `@EnableRedisWebSession` 어노테이션으로 Redis 세션 활성화
- 관리자 로그인 시 세션에 `adminId`, `companyId`, `isSuperUser` 저장
- 로그아웃 시 세션 무효화

### Session Schema
- Key: `spring:session:sessions:{session-id}`
- Value(Hash):
  - `adminId`: 관리자 ID
  - `companyId`: 소속 기업 ID(슈퍼유저는 null)
  - `isSuperUser`: 슈퍼유저 여부(boolean)
  - `selectedCompanyId`: 슈퍼유저가 선택한 관리 대상 기업 ID
  - `creationTime`: 세션 생성 시각
  - `lastAccessedTime`: 마지막 액세스 시각

---

## 연구 결론

모든 NEEDS CLARIFICATION 항목이 해결되었으며, 다음과 같이 결정되었다:

1. **지도 서비스**: Kakao Map API
2. **파일 저장소**: 로컬 파일 시스템(초기), AWS S3(향후)
3. **스팸 방지**: Google reCAPTCHA v3
4. **세션 관리**: Spring Session + Redis

이 결정들은 모두 프로젝트 헌법의 원칙(한국어 우선, 최소 의존성, 리액티브 프로그래밍, 사용자 경험 우선)을 준수하며, 초기 개발 및 운영 비용을 최소화하면서도 향후 확장성을 고려한 설계이다.

---

## 추가 의존성 목록

위 연구 결과를 바탕으로 `build.gradle`에 추가할 의존성:

```gradle
dependencies {
    // 기존 의존성은 유지

    // Redis for Session Management
    implementation 'org.springframework.boot:spring-boot-starter-data-redis-reactive'
    implementation 'org.springframework.session:spring-session-data-redis'

    // File Upload (Spring Boot에 기본 포함, 명시적 설정만 필요)
    // Kakao Map API (JavaScript SDK, 백엔드 의존성 불필요)
    // reCAPTCHA v3 (WebClient로 검증 API 호출, 별도 의존성 불필요)
}
```

**의존성 정당화**:
- **Redis**: 세션 클러스터링 및 다중 서버 배포를 위해 필요. 헌법 원칙 V(최소 의존성)에 따라 정당한 이유가 있음.
