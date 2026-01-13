# k6 빠른 시작 가이드

Bear 애플리케이션의 성능 테스트를 5분 안에 시작하세요!

## 1단계: k6 설치 (2분)

### macOS
```bash
brew install k6
```

### Windows
```powershell
# Chocolatey
choco install k6

# 또는 Scoop
scoop install k6
```

### Linux
```bash
sudo gpg -k
sudo gpg --no-default-keyring --keyring /usr/share/keyrings/k6-archive-keyring.gpg --keyserver hkp://keyserver.ubuntu.com:80 --recv-keys C5AD17C747E3415A3642D57D77C6C491D6AC1D69
echo "deb [signed-by=/usr/share/keyrings/k6-archive-keyring.gpg] https://dl.k6.io/deb stable main" | sudo tee /etc/apt/sources.list.d/k6.list
sudo apt-get update
sudo apt-get install k6
```

## 2단계: 애플리케이션 실행 (1분)

```bash
# 프로젝트 루트에서
./gradlew bootRun
```

새 터미널을 열어 애플리케이션이 실행 중인지 확인:
```bash
curl http://localhost:8080/actuator/health
```

## 3단계: 테스트 데이터 준비 (필수!)

테스트를 실행하기 전에 다음 데이터가 DB에 있어야 합니다:

1. **기업 (Company)** - `company_code` 확인 필요
2. **제품 (Product)** - 최소 1개
3. **게시판 (Board)** - 'notice' 타입
4. **게시글 (Post)** - 최소 1개
5. **FAQ** - 최소 1개 (선택)
6. **QnA** - 최소 1개 (선택)
7. **유튜브 영상** - 최소 1개 (선택)

### DB에서 기업 코드 확인
```sql
SELECT code, name FROM companies WHERE is_active = true;
```

결과 예시:
```
 code         | name
--------------+------------------
 demo-company | 데모 기업
```

## 4단계: Smoke Test 실행 (1분)

### 방법 A: 헬퍼 스크립트 사용 (권장)

**macOS/Linux:**
```bash
cd k6
./run-tests.sh smoke
```

**Windows:**
```cmd
cd k6
run-tests.bat smoke
```

### 방법 B: 직접 실행

```bash
cd k6

# 기본 설정으로 실행
k6 run scenarios/smoke-test.js

# 또는 환경 변수 지정
k6 run \
  --env BASE_URL=http://localhost:8080 \
  --env COMPANY_CODE=demo-company \
  scenarios/smoke-test.js
```

## 5단계: 결과 확인

### ✅ 성공 시

```
✓ Smoke - Home: status is 200
✓ Smoke - About: status is 200
✓ Smoke - Product List: status is 200
...

✓ checks.........................: 100.00% ✓ 24      ✗ 0
✓ http_req_failed................: 0.00%   ✓ 0       ✗ 12
✓ http_req_duration..............: avg=187.85ms
```

**축하합니다! 🎉** 모든 공개 페이지가 정상 작동합니다.

### ❌ 실패 시

**문제 1: Connection refused**
```
ERRO[0000] GoError: Get "http://localhost:8080/...": dial tcp: connect: connection refused
```
**해결**: 애플리케이션이 실행 중인지 확인
```bash
./gradlew bootRun
```

**문제 2: 404 Not Found**
```
✗ Smoke - Home: status is 200
  ↳  0% — ✓ 0 / ✗ 1
```
**해결**: COMPANY_CODE가 DB에 존재하는지 확인
```bash
k6 run --env COMPANY_CODE=실제코드 scenarios/smoke-test.js
```

## 다음 단계

### 1. Load Test 실행 (정상 부하 테스트)
```bash
./run-tests.sh load
# 또는
k6 run scenarios/load-test.js
```

### 2. 다른 환경에서 테스트
```bash
# 스테이징 환경
k6 run \
  --env BASE_URL=https://staging.example.com \
  --env COMPANY_CODE=real-company \
  scenarios/smoke-test.js
```

### 3. 결과 저장
```bash
# JSON 형식으로 저장
k6 run --out json=results.json scenarios/load-test.js

# CSV 형식으로 저장
k6 run --out csv=results.csv scenarios/load-test.js
```

### 4. 상세 문서 읽기
전체 기능과 옵션은 [README.md](README.md)를 참고하세요.

## 자주 묻는 질문 (FAQ)

### Q: 테스트에 얼마나 시간이 걸리나요?
- **Smoke Test**: 1분
- **Load Test**: 10분
- **Stress Test**: 14분
- **Spike Test**: 7분

### Q: 프로덕션에서 실행해도 되나요?
- **Smoke Test**: ✅ 가능 (부하가 매우 낮음)
- **Load Test**: ⚠️ 피크 시간 외 권장
- **Stress Test**: ❌ 권장하지 않음 (스테이징 환경 사용)
- **Spike Test**: ❌ 권장하지 않음 (스테이징 환경 사용)

### Q: 어떤 순서로 테스트해야 하나요?
1. **Smoke Test** - 기본 기능 확인
2. **Load Test** - 정상 부하 성능 확인
3. **Stress Test** - 한계점 파악 (스테이징에서)
4. **Spike Test** - 급격한 트래픽 대응 확인 (스테이징에서)

### Q: VU(Virtual User) 수를 변경하려면?
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

### Q: 테스트 중 무엇을 모니터링해야 하나요?
- 애플리케이션 로그 (에러 확인)
- CPU, Memory 사용률
- 데이터베이스 연결 풀
- 네트워크 대역폭

## 문제 발생 시

1. **로그 확인**
   ```bash
   tail -f logs/application.log
   ```

2. **Health Check 확인**
   ```bash
   curl http://localhost:8080/actuator/health
   ```

3. **DB 연결 확인**
   ```sql
   SELECT count(*) FROM companies;
   ```

4. **상세 문서 참조**: [README.md](README.md#문제-해결)

## 도움말

- k6 공식 문서: https://k6.io/docs/
- 프로젝트 README: [README.md](README.md)
- 이슈 제보: GitHub Issues
