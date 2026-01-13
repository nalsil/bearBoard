# k6 Load & Stress Testing

Bear 애플리케이션의 공용 페이지에 대한 k6 부하 테스트 및 스트레스 테스트 스크립트 모음입니다.

## 📋 목차

- [개요](#개요)
- [사전 준비](#사전-준비)
- [빠른 시작](#빠른-시작)
- [테스트 시나리오](#테스트-시나리오)
- [환경 설정](#환경-설정)
- [실행 방법](#실행-방법)
- [결과 분석](#결과-분석)
- [모범 사례](#모범-사례)
- [문제 해결](#문제-해결)

## 개요

이 프로젝트는 Bear 애플리케이션의 공개 페이지에 대한 성능 테스트를 위한 k6 스크립트를 제공합니다.

### 테스트 대상 엔드포인트

- **홈페이지**: `/{companyCode}`, `/{companyCode}/about`
- **제품**: `/{companyCode}/products`, `/{companyCode}/products/{id}`
- **게시판**: `/{companyCode}/board/{type}`, `/{companyCode}/board/{type}/{id}`
- **FAQ**: `/{companyCode}/faq`
- **QnA**: `/{companyCode}/qna`, `/{companyCode}/qna/{id}`
- **유튜브**: `/{companyCode}/youtube`, `/{companyCode}/youtube/{id}`

### 테스트 유형

| 테스트 유형 | 목적 | VU 수 | 기간 | 파일 |
|------------|------|-------|------|------|
| **Smoke Test** | 기본 기능 확인 | 1-2 | 1분 | `scenarios/smoke-test.js` |
| **Load Test** | 정상 부하 성능 측정 | 10-50 | 10분 | `scenarios/load-test.js` |
| **Stress Test** | 한계점 및 복구 능력 확인 | 50-200 | 14분 | `scenarios/stress-test.js` |
| **Spike Test** | 급격한 트래픽 증가 대응 | 10-500 | 7분 | `scenarios/spike-test.js` |

## 사전 준비

### 1. k6 설치

**macOS (Homebrew)**
```bash
brew install k6
```

**Windows (Chocolatey)**
```bash
choco install k6
```

**Windows (Scoop)**
```bash
scoop install k6
```

**Linux**
```bash
sudo gpg -k
sudo gpg --no-default-keyring --keyring /usr/share/keyrings/k6-archive-keyring.gpg --keyserver hkp://keyserver.ubuntu.com:80 --recv-keys C5AD17C747E3415A3642D57D77C6C491D6AC1D69
echo "deb [signed-by=/usr/share/keyrings/k6-archive-keyring.gpg] https://dl.k6.io/deb stable main" | sudo tee /etc/apt/sources.list.d/k6.list
sudo apt-get update
sudo apt-get install k6
```

**Docker**
```bash
docker pull grafana/k6:latest
```

### 2. 테스트 데이터 준비

데이터베이스에 테스트용 데이터가 필요합니다:

1. **기업 (Company)**: 테스트할 기업 코드 생성
2. **제품 (Product)**: 1개 이상의 제품 등록
3. **게시판 (Board)**: notice, press 등 게시판 타입 생성
4. **게시글 (Post)**: 게시판에 1개 이상의 게시글 등록
5. **FAQ**: 1개 이상의 FAQ 등록
6. **QnA**: 1개 이상의 QnA 등록
7. **유튜브 영상**: 1개 이상의 유튜브 영상 등록

### 3. 애플리케이션 실행

```bash
./gradlew bootRun
```

또는 Docker로 실행:
```bash
docker-compose up -d
```

## 빠른 시작

### 1. 환경 변수 설정

```bash
cd k6

# .env.example을 참고하여 환경 변수 설정
export BASE_URL=http://localhost:8080
export COMPANY_CODE=demo-company
export SAMPLE_PRODUCT_ID=1
export SAMPLE_POST_ID=1
```

### 2. Smoke Test 실행 (기본 기능 확인)

```bash
k6 run scenarios/smoke-test.js
```

### 3. Load Test 실행 (성능 테스트)

```bash
k6 run scenarios/load-test.js
```

## 테스트 시나리오

### 1️⃣ Smoke Test - 기본 기능 확인

**목적**: 모든 엔드포인트가 정상적으로 작동하는지 확인

**특징**:
- 최소 부하 (1 VU)
- 모든 공개 엔드포인트를 한 번씩 호출
- 빠른 실행 (1분)

**실행**:
```bash
k6 run scenarios/smoke-test.js
```

**성공 기준**:
- ✅ HTTP 실패율 < 1%
- ✅ 95% 요청이 1초 이내 응답
- ✅ 모든 엔드포인트가 200 응답 반환

---

### 2️⃣ Load Test - 일반 부하 테스트

**목적**: 정상 트래픽 상황에서 시스템 성능 측정

**특징**:
- 점진적 부하 증가 (0 → 50 VU)
- 실제 사용자 행동 패턴 시뮬레이션
- 중간 기간 (10분)

**사용자 시나리오**:
- 30% 홈페이지 방문자
- 30% 제품 탐색자
- 20% 게시판 독자
- 10% FAQ 검색자
- 10% QnA 조회자

**실행**:
```bash
k6 run scenarios/load-test.js
```

**성공 기준**:
- ✅ HTTP 실패율 < 1%
- ✅ 95% 요청이 500ms 이내 응답
- ✅ 평균 응답 시간 < 300ms
- ✅ 처리율 > 100 req/s

---

### 3️⃣ Stress Test - 한계 테스트

**목적**: 시스템의 한계점과 장애 복구 능력 확인

**특징**:
- 높은 부하 (최대 200 VU)
- 단계적 증가 및 복구 확인
- 긴 기간 (14분)

**부하 단계**:
1. Ramp-up: 0 → 50 VU (2분)
2. Normal: 50 VU 유지 (3분)
3. Stress: 50 → 100 VU (2분)
4. High Stress: 100 → 200 VU (2분)
5. Peak: 200 VU 유지 (3분)
6. Recovery: 200 → 0 VU (2분)

**실행**:
```bash
k6 run scenarios/stress-test.js
```

**분석 포인트**:
- 🔍 Breaking Point (시스템 한계점)
- 🔍 에러 발생 시점 및 원인
- 🔍 복구 시간
- 🔍 리소스 병목 지점

**⚠️ 주의**: 프로덕션 환경에서는 신중하게 실행!

---

### 4️⃣ Spike Test - 급격한 트래픽 증가

**목적**: 갑작스런 트래픽 급증 상황 대응 확인

**특징**:
- 순간적으로 매우 높은 부하 (최대 500 VU)
- 정상 → 스파이크 → 정상 패턴 반복
- 중간 기간 (7분)

**시나리오**:
- SNS 바이럴
- 뉴스 보도
- 마케팅 이벤트

**스파이크 패턴**:
1. Normal: 10 VU (1분)
2. Spike 1: 10 → 300 VU (30초)
3. Hold: 300 VU (1분)
4. Recovery: 300 → 10 VU (30초)
5. Normal: 10 VU (1분)
6. Spike 2: 10 → 500 VU (30초)
7. Hold: 500 VU (1분)
8. Recovery: 500 → 0 VU (1분)

**실행**:
```bash
k6 run scenarios/spike-test.js
```

**분석 포인트**:
- 🔍 Auto-scaling 반응 시간
- 🔍 큐잉 시스템 동작
- 🔍 캐싱 효과
- 🔍 에러율 및 타임아웃

## 환경 설정

### 기본 환경 변수

| 변수 | 설명 | 기본값 | 필수 |
|------|------|--------|------|
| `BASE_URL` | 애플리케이션 기본 URL | `http://localhost:8080` | ✅ |
| `COMPANY_CODE` | 테스트할 기업 코드 | `demo-company` | ✅ |
| `SAMPLE_PRODUCT_ID` | 샘플 제품 ID | `1` | ✅ |
| `SAMPLE_POST_ID` | 샘플 게시글 ID | `1` | ✅ |
| `SAMPLE_BOARD_TYPE` | 샘플 게시판 타입 | `notice` | ✅ |
| `SAMPLE_FAQ_ID` | 샘플 FAQ ID | `1` | - |
| `SAMPLE_QNA_ID` | 샘플 QnA ID | `1` | - |
| `SAMPLE_YOUTUBE_ID` | 샘플 유튜브 ID | `1` | - |
| `TEST_TYPE` | 테스트 유형 태그 | `load` | - |
| `ENVIRONMENT` | 환경 태그 | `local` | - |

### 환경별 실행 예시

**로컬 개발 환경**
```bash
k6 run \
  --env BASE_URL=http://localhost:8080 \
  --env COMPANY_CODE=demo-company \
  --env SAMPLE_PRODUCT_ID=1 \
  scenarios/smoke-test.js
```

**스테이징 환경**
```bash
k6 run \
  --env BASE_URL=https://staging.example.com \
  --env COMPANY_CODE=real-company \
  --env ENVIRONMENT=staging \
  scenarios/load-test.js
```

**프로덕션 환경 (주의!)**
```bash
k6 run \
  --env BASE_URL=https://www.example.com \
  --env COMPANY_CODE=production-company \
  --env ENVIRONMENT=production \
  scenarios/smoke-test.js
```

## 실행 방법

### 기본 실행

```bash
# 현재 디렉토리에서
k6 run scenarios/smoke-test.js
```

### 환경 변수와 함께 실행

```bash
k6 run \
  --env BASE_URL=http://localhost:8080 \
  --env COMPANY_CODE=demo \
  scenarios/load-test.js
```

### 결과를 파일로 저장

```bash
# JSON 형식으로 저장
k6 run --out json=results.json scenarios/load-test.js

# CSV 형식으로 저장
k6 run --out csv=results.csv scenarios/load-test.js
```

### k6 Cloud로 결과 전송

```bash
# k6 Cloud에 로그인
k6 login cloud

# 테스트 실행 및 결과 전송
k6 run --out cloud scenarios/load-test.js
```

### Docker로 실행

```bash
# 기본 실행
docker run --rm -i \
  -v $(pwd):/scripts \
  grafana/k6 run /scripts/scenarios/smoke-test.js

# 환경 변수와 함께 실행
docker run --rm -i \
  -e BASE_URL=http://host.docker.internal:8080 \
  -e COMPANY_CODE=demo \
  -v $(pwd):/scripts \
  grafana/k6 run /scripts/scenarios/load-test.js
```

### VU 수 커스터마이징

```bash
# Smoke Test를 10 VU로 실행
k6 run --vus 10 --duration 2m scenarios/smoke-test.js

# Load Test의 최대 VU를 100으로 증가
k6 run \
  --stage 2m:20 \
  --stage 5m:100 \
  --stage 2m:100 \
  --stage 1m:0 \
  scenarios/load-test.js
```

## 결과 분석

### 콘솔 출력 이해하기

k6 실행 시 다음과 같은 메트릭이 표시됩니다:

```
✓ Smoke - Home: status is 200
✓ Smoke - Home: response time < 1s

scenarios: (100.00%) 1 scenario, 1 max VUs, 1m30s max duration
default: 1 iterations for each of 1 VUs (maxDuration: 1m0s)

running (00m15.3s), 0/1 VUs, 1 complete and 0 interrupted iterations
default ✓ [======================================] 1 VUs  00m15.3s/1m0s  1/1 iters, 1 per VU

✓ checks.........................: 100.00% ✓ 24      ✗ 0
  data_received..................: 156 kB  10 kB/s
  data_sent......................: 3.2 kB  210 B/s
  http_req_blocked...............: avg=12.45ms  min=1µs     med=3µs     max=298.81ms p(95)=298.81ms p(99)=298.81ms
  http_req_connecting............: avg=3.58ms   min=0s      med=0s      max=85.99ms  p(95)=85.99ms  p(99)=85.99ms
✓ http_req_duration..............: avg=187.85ms min=22.87ms med=93.35ms max=1.02s    p(95)=498.84ms p(99)=1.02s
    { expected_response:true }...: avg=187.85ms min=22.87ms med=93.35ms max=1.02s    p(95)=498.84ms p(99)=1.02s
✓ http_req_failed................: 0.00%   ✓ 0       ✗ 12
  http_req_receiving.............: avg=175.66µs min=30µs    med=83µs    max=1.11ms   p(95)=498.1µs  p(99)=1.11ms
  http_req_sending...............: avg=24.75µs  min=5µs     med=17.5µs  max=118µs    p(95)=49.74µs  p(99)=118µs
  http_req_tls_handshaking.......: avg=8.78ms   min=0s      med=0s      max=210.65ms p(95)=210.65ms p(99)=210.65ms
  http_req_waiting...............: avg=187.65ms min=22.74ms med=93.12ms max=1.02s    p(95)=498.75ms p(99)=1.02s
  http_reqs......................: 12      0.783634/s
  iteration_duration.............: avg=15.3s    min=15.3s   med=15.3s   max=15.3s    p(95)=15.3s    p(99)=15.3s
  iterations.....................: 1       0.065303/s
  vus............................: 1       min=1     max=1
  vus_max........................: 1       min=1     max=1
```

### 주요 메트릭 설명

| 메트릭 | 의미 | 목표값 |
|--------|------|--------|
| `checks` | 체크 성공률 | 100% |
| `http_req_failed` | HTTP 요청 실패율 | < 1% |
| `http_req_duration` | 응답 시간 (p95) | < 500ms |
| `http_req_duration` | 응답 시간 (p99) | < 1000ms |
| `http_reqs` | 초당 요청 수 (RPS) | 상황에 따라 다름 |
| `iteration_duration` | 전체 시나리오 완료 시간 | 상황에 따라 다름 |

### Threshold 실패 시

임계값을 초과하면 다음과 같이 표시됩니다:

```
✗ http_req_duration..............: avg=2.5s min=500ms med=2s max=5s p(95)=4s p(99)=5s
  ✗ p(95) < 500ms
```

이 경우 성능 개선이 필요합니다.

### 결과 시각화

**Grafana + InfluxDB**
```bash
# InfluxDB로 결과 전송
k6 run --out influxdb=http://localhost:8086/k6 scenarios/load-test.js
```

**k6 Cloud (유료)**
```bash
k6 run --out cloud scenarios/load-test.js
```

**HTML 리포트 생성 (확장 기능)**
```bash
# k6-reporter 설치
npm install -g k6-to-junit

# HTML 리포트 생성
k6 run --out json=results.json scenarios/load-test.js
k6-to-junit results.json
```

## 모범 사례

### 1. 테스트 순서

```bash
# 1. Smoke Test로 기본 기능 확인
k6 run scenarios/smoke-test.js

# 2. Load Test로 정상 부하 성능 확인
k6 run scenarios/load-test.js

# 3. Stress Test로 한계점 파악 (스테이징 환경 권장)
k6 run scenarios/stress-test.js

# 4. Spike Test로 급격한 트래픽 대응 확인 (스테이징 환경 권장)
k6 run scenarios/spike-test.js
```

### 2. 환경별 권장 사항

**로컬 개발 환경**
- ✅ Smoke Test
- ✅ Load Test (VU 수 줄여서)
- ❌ Stress Test (리소스 부족)
- ❌ Spike Test (리소스 부족)

**스테이징 환경**
- ✅ Smoke Test
- ✅ Load Test
- ✅ Stress Test
- ✅ Spike Test

**프로덕션 환경**
- ✅ Smoke Test (새 배포 후)
- ⚠️ Load Test (피크 시간 외)
- ❌ Stress Test (권장하지 않음)
- ❌ Spike Test (권장하지 않음)

### 3. 테스트 데이터 관리

```bash
# 테스트 전 데이터 준비
./scripts/prepare-test-data.sh

# 테스트 실행
k6 run scenarios/load-test.js

# 테스트 후 데이터 정리
./scripts/cleanup-test-data.sh
```

### 4. CI/CD 통합

**GitHub Actions 예시**
```yaml
name: Performance Test

on:
  pull_request:
    branches: [ main ]

jobs:
  performance-test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2

      - name: Run application
        run: docker-compose up -d

      - name: Wait for app
        run: sleep 30

      - name: Run k6 smoke test
        uses: grafana/k6-action@v0.3.0
        with:
          filename: k6/scenarios/smoke-test.js
        env:
          BASE_URL: http://localhost:8080
          COMPANY_CODE: test-company

      - name: Shutdown
        run: docker-compose down
```

### 5. 모니터링과 함께 실행

테스트 실행 중 다음을 모니터링하세요:

- **애플리케이션 로그**: 에러 발생 여부
- **시스템 리소스**: CPU, Memory, Disk I/O
- **데이터베이스**: 연결 풀, 쿼리 성능
- **네트워크**: 대역폭, 레이턴시

```bash
# 별도 터미널에서 모니터링
# 1. 애플리케이션 로그
tail -f logs/application.log

# 2. 시스템 리소스 (htop)
htop

# 3. 데이터베이스 연결 (PostgreSQL)
watch -n 1 'psql -c "SELECT count(*) FROM pg_stat_activity;"'
```

## 문제 해결

### 문제 1: "Connection refused" 에러

**원인**: 애플리케이션이 실행되지 않았거나 BASE_URL이 잘못됨

**해결**:
```bash
# 애플리케이션 상태 확인
curl http://localhost:8080/actuator/health

# BASE_URL 확인
echo $BASE_URL
```

### 문제 2: "404 Not Found" 에러

**원인**: COMPANY_CODE가 DB에 존재하지 않음

**해결**:
```bash
# DB에서 기업 코드 확인
psql -d bear_db -c "SELECT code, name FROM companies WHERE is_active = true;"

# 올바른 COMPANY_CODE로 실행
k6 run --env COMPANY_CODE=실제코드 scenarios/smoke-test.js
```

### 문제 3: 높은 에러율

**원인**: 데이터베이스 연결 부족, 타임아웃 등

**해결**:
1. `application.yaml`에서 DB 연결 풀 크기 증가
2. 타임아웃 설정 확인
3. 인덱스 추가 여부 확인

```yaml
# application.yaml
spring:
  r2dbc:
    pool:
      initial-size: 20  # 증가
      max-size: 50      # 증가
```

### 문제 4: 느린 응답 시간

**원인**: 쿼리 최적화 필요, 캐싱 부족

**해결**:
1. 느린 쿼리 식별
2. 인덱스 추가
3. 캐싱 전략 적용

```bash
# PostgreSQL 느린 쿼리 로그 활성화
# postgresql.conf
log_min_duration_statement = 100  # 100ms 이상 쿼리 로깅
```

### 문제 5: k6 설치 실패

**해결**:

**macOS**
```bash
brew update
brew install k6
```

**Windows** (관리자 권한)
```powershell
choco install k6 -y
```

**Linux**
```bash
# 공식 스크립트 사용
wget https://github.com/grafana/k6/releases/download/v0.47.0/k6-v0.47.0-linux-amd64.tar.gz
tar -xzf k6-v0.47.0-linux-amd64.tar.gz
sudo mv k6-v0.47.0-linux-amd64/k6 /usr/local/bin/
```

## 추가 자료

- [k6 공식 문서](https://k6.io/docs/)
- [k6 Best Practices](https://k6.io/docs/testing-guides/test-types/)
- [성능 테스트 가이드](https://k6.io/docs/test-types/load-testing/)
- [k6 Extensions](https://k6.io/docs/extensions/)

## 기여

테스트 시나리오 개선이나 버그 수정은 Pull Request를 통해 기여해주세요.

## 라이센스

이 프로젝트는 Bear 애플리케이션의 일부입니다.
