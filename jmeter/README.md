# Bear Application JMeter Performance Tests

JMeter를 사용한 Bear 애플리케이션 성능 테스트 스크립트입니다.

## 요구사항

- Apache JMeter 5.6.3 이상
- Java 8 이상
- Bear 애플리케이션이 실행 중이어야 함

## 설치

### Windows
```powershell
# Chocolatey를 통한 설치
choco install jmeter

# 또는 수동 설치
# https://jmeter.apache.org/download_jmeter.cgi 에서 다운로드
```

### macOS
```bash
brew install jmeter
```

### Linux
```bash
# Debian/Ubuntu
sudo apt-get install jmeter

# 또는 수동 설치
wget https://dlcdn.apache.org//jmeter/binaries/apache-jmeter-5.6.3.tgz
tar -xzf apache-jmeter-5.6.3.tgz
```

## 테스트 구성

### 테스트 시나리오

| 시나리오 | 스레드 수 | Ramp-up | 지속 시간 | 설명 |
|---------|----------|---------|----------|------|
| **Load Test** | 10 | 30s | 5분 | 일반적인 부하 테스트 |
| **Stress Test** | 50 | 60s | 10분 | 고부하 스트레스 테스트 |
| **Spike Test** | 200 | 10s | 3분 | 급격한 트래픽 증가 테스트 |

### 테스트 대상 엔드포인트 (100% Coverage)

#### 1. Index Controller
- `GET /` - 루트 페이지 (모든 회사 목록)
- `GET /favicon.ico` - 파비콘

#### 2. Home Controller
- `GET /{companyCode}` - 회사 홈페이지
- `GET /{companyCode}/about` - 소개 페이지

#### 3. Products Controller
- `GET /{companyCode}/products` - 상품 목록
- `GET /{companyCode}/products?category={category}` - 카테고리 필터
- `GET /{companyCode}/products?page={page}&size={size}` - 페이지네이션
- `GET /{companyCode}/products/{id}` - 상품 상세

#### 4. Board Controller
- `GET /{companyCode}/board/{boardType}` - 게시판 목록
- `GET /{companyCode}/board/{boardType}?page={page}&size={size}` - 페이지네이션
- `GET /{companyCode}/board/{boardType}/{postId}` - 게시글 상세

#### 5. FAQ Controller
- `GET /{companyCode}/faq` - FAQ 목록
- `GET /{companyCode}/faq?category={category}` - 카테고리 필터
- `GET /{companyCode}/faq?keyword={keyword}` - 키워드 검색

#### 6. QnA Controller
- `GET /{companyCode}/qna` - QnA 목록
- `GET /{companyCode}/qna?page={page}&size={size}` - 페이지네이션
- `GET /{companyCode}/qna/{id}` - QnA 상세
- `GET /{companyCode}/qna/new` - QnA 작성 폼
- `POST /{companyCode}/qna` - QnA 생성

#### 7. YouTube Controller
- `GET /{companyCode}/youtube` - 유튜브 목록
- `GET /{companyCode}/youtube/{id}` - 유튜브 플레이어

#### 8. Static Resources
- `GET /css/style.css` - CSS 파일
- `GET /js/main.js` - JavaScript 파일
- `GET /favicon.ico` - 파비콘

#### 9. Health Check
- `GET /actuator/health` - 상태 체크

## 사용법

### GUI 모드 (테스트 개발 및 디버깅)

```bash
# JMeter GUI 실행
jmeter -t bear-performance-test.jmx
```

### CLI 모드 (실제 테스트 실행)

#### Load Test 실행
```bash
jmeter -n -t bear-performance-test.jmx \
  -l results/load-test-results.jtl \
  -e -o results/load-test-report \
  -Jserver.host=localhost \
  -Jserver.port=8080 \
  -Jcompany.code=demo-company \
  -Jload.threads=10 \
  -Jload.rampup=30 \
  -Jload.duration=300
```

#### Stress Test 실행
```bash
# 먼저 JMX 파일에서 Stress Test Thread Group을 활성화하세요
jmeter -n -t bear-performance-test.jmx \
  -l results/stress-test-results.jtl \
  -e -o results/stress-test-report \
  -Jserver.host=localhost \
  -Jserver.port=8080 \
  -Jcompany.code=demo-company \
  -Jstress.threads=50 \
  -Jstress.rampup=60 \
  -Jstress.duration=600
```

#### Spike Test 실행
```bash
# 먼저 JMX 파일에서 Spike Test Thread Group을 활성화하세요
jmeter -n -t bear-performance-test.jmx \
  -l results/spike-test-results.jtl \
  -e -o results/spike-test-report \
  -Jserver.host=localhost \
  -Jserver.port=8080 \
  -Jcompany.code=demo-company \
  -Jspike.threads=200 \
  -Jspike.rampup=10 \
  -Jspike.duration=180
```

### Windows PowerShell 실행 스크립트

```powershell
# Load Test
.\run-tests.ps1 -TestType load

# Stress Test
.\run-tests.ps1 -TestType stress

# Spike Test
.\run-tests.ps1 -TestType spike
```

## 설정 파라미터

### 서버 설정
| 파라미터 | 기본값 | 설명 |
|---------|-------|------|
| `server.host` | localhost | 서버 호스트 |
| `server.port` | 8080 | 서버 포트 |
| `protocol` | http | 프로토콜 (http/https) |

### 테스트 데이터
| 파라미터 | 기본값 | 설명 |
|---------|-------|------|
| `company.code` | demo-company | 테스트할 회사 코드 |
| `sample.product.id` | 1 | 샘플 상품 ID |
| `sample.post.id` | 1 | 샘플 게시글 ID |
| `sample.qna.id` | 1 | 샘플 QnA ID |
| `sample.youtube.id` | 1 | 샘플 유튜브 ID |
| `board.type` | notice | 게시판 타입 |
| `product.category` | electronics | 상품 카테고리 |
| `faq.category` | general | FAQ 카테고리 |
| `search.keyword` | test | 검색 키워드 |

### Load Test 설정
| 파라미터 | 기본값 | 설명 |
|---------|-------|------|
| `load.threads` | 10 | 동시 사용자 수 |
| `load.rampup` | 30 | Ramp-up 시간 (초) |
| `load.duration` | 300 | 테스트 지속 시간 (초) |

### Stress Test 설정
| 파라미터 | 기본값 | 설명 |
|---------|-------|------|
| `stress.threads` | 50 | 동시 사용자 수 |
| `stress.rampup` | 60 | Ramp-up 시간 (초) |
| `stress.duration` | 600 | 테스트 지속 시간 (초) |

### Spike Test 설정
| 파라미터 | 기본값 | 설명 |
|---------|-------|------|
| `spike.threads` | 200 | 동시 사용자 수 |
| `spike.rampup` | 10 | Ramp-up 시간 (초) |
| `spike.duration` | 180 | 테스트 지속 시간 (초) |

## 결과 확인

### HTML 리포트
테스트 완료 후 `results/` 디렉토리에 HTML 리포트가 생성됩니다.

```bash
# 리포트 열기
open results/load-test-report/index.html  # macOS
start results\load-test-report\index.html  # Windows
```

### CSV 결과
`results/bear-test-results.csv` 파일에서 상세 결과를 확인할 수 있습니다.

## 성능 기준

| 지표 | 목표값 | 설명 |
|-----|-------|------|
| **응답 시간 (p95)** | < 500ms | 95% 요청이 500ms 이내 응답 |
| **응답 시간 (p99)** | < 1000ms | 99% 요청이 1초 이내 응답 |
| **오류율** | < 1% | HTTP 오류 발생률 1% 미만 |
| **처리량** | > 100 req/s | 초당 100개 이상 요청 처리 |

## 문제 해결

### OutOfMemoryError
JMeter 힙 메모리를 늘려주세요:

```bash
# jmeter.bat 또는 jmeter.sh 수정
HEAP="-Xms1g -Xmx4g"
```

### Connection Refused
- 애플리케이션이 실행 중인지 확인
- 방화벽 설정 확인
- 올바른 포트 사용 확인

### 404 Not Found
- 테스트 데이터(회사, 상품, 게시글 등)가 DB에 존재하는지 확인
- `company.code` 파라미터가 올바른지 확인

## 파일 구조

```
jmeter/
├── bear-performance-test.jmx    # 메인 테스트 파일
├── README.md                    # 이 파일
├── jmeter.properties            # JMeter 설정
├── run-tests.ps1               # Windows 실행 스크립트
├── run-tests.sh                # Unix 실행 스크립트
└── results/                    # 테스트 결과 (gitignore)
    ├── *.jtl                   # JMeter 결과 파일
    ├── *.csv                   # CSV 결과 파일
    └── *-report/               # HTML 리포트
```

## 라이선스

이 프로젝트는 Bear 애플리케이션의 일부입니다.
