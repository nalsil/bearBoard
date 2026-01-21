# reCAPTCHA v3 설정 가이드

## 📋 개요

QnA 질문 등록 시 봇(스팸) 방지를 위한 Google reCAPTCHA v3 연동 가이드입니다.

### reCAPTCHA v3 특징
- **사용자 상호작용 없음**: 체크박스나 이미지 선택 없이 백그라운드에서 동작
- **점수 기반 판단**: 0.0(봇) ~ 1.0(사람) 점수로 판단
- **투명한 사용자 경험**: 정상 사용자는 아무런 불편 없음

---

## 🔑 키 발급 방법

### 1단계: Google reCAPTCHA 관리 콘솔 접속

1. https://www.google.com/recaptcha/admin 접속
2. Google 계정으로 로그인

### 2단계: 새 사이트 등록

1. **라벨(Label)**: 프로젝트 식별 이름 입력
   ```
   Bear QnA System
   ```

2. **reCAPTCHA 유형**: `reCAPTCHA v3` 선택
   - v2 (체크박스): 사용자가 직접 체크
   - v3 (권장): 백그라운드 자동 검증

3. **도메인 추가**:
   ```
   localhost          (개발용)
   yourdomain.com     (운영용)
   www.yourdomain.com (운영용)
   ```

4. **reCAPTCHA 서비스 약관 동의** 체크

5. **제출** 버튼 클릭

### 3단계: 키 확인 및 저장

등록 완료 후 두 개의 키가 발급됩니다:

| 키 종류 | 용도 | 예시 |
|---------|------|------|
| **사이트 키 (Site Key)** | 프론트엔드 (HTML/JS) | `6LcXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX` |
| **비밀 키 (Secret Key)** | 서버 검증 | `6LcXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX` |

> ⚠️ **중요**: 비밀 키는 절대 클라이언트에 노출하지 마세요!

---

## ⚙️ 애플리케이션 설정

### 방법 1: 환경 변수 설정 (권장)

**Windows PowerShell:**
```powershell
$env:RECAPTCHA_SITE_KEY="6LcXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"
$env:RECAPTCHA_SECRET_KEY="6LcXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"
```

**Windows CMD:**
```cmd
set RECAPTCHA_SITE_KEY=6LcXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
set RECAPTCHA_SECRET_KEY=6LcXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
```

**Linux / macOS:**
```bash
export RECAPTCHA_SITE_KEY="6LcXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"
export RECAPTCHA_SECRET_KEY="6LcXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"
```

**영구 설정 (Linux/macOS):**
```bash
# ~/.bashrc 또는 ~/.zshrc에 추가
echo 'export RECAPTCHA_SITE_KEY="YOUR_SITE_KEY"' >> ~/.bashrc
echo 'export RECAPTCHA_SECRET_KEY="YOUR_SECRET_KEY"' >> ~/.bashrc
source ~/.bashrc
```

---

### 방법 2: IntelliJ IDEA 실행 설정

1. `Run > Edit Configurations` 메뉴 열기
2. Spring Boot 애플리케이션 설정 선택
3. `Environment variables` 필드에 추가:
   ```
   RECAPTCHA_SITE_KEY=YOUR_SITE_KEY;RECAPTCHA_SECRET_KEY=YOUR_SECRET_KEY
   ```
4. Apply 후 Run

---

### 방법 3: application.yaml 직접 수정 (개발용)

```yaml
app:
  recaptcha:
    site-key: 6LcXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
    secret-key: 6LcXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
    verify-url: https://www.google.com/recaptcha/api/siteverify
    threshold: 0.5
```

> ⚠️ **주의**: 실제 키를 코드에 커밋하지 마세요!

---

## 📊 점수 임계값 설정

`threshold` 값으로 봇 판단 기준을 조절할 수 있습니다.

| 점수 | 의미 | 권장 임계값 |
|------|------|-------------|
| 0.9 | 거의 확실히 사람 | 매우 엄격 |
| 0.7 | 대체로 사람 | 엄격 |
| **0.5** | 보통 (기본값) | **권장** |
| 0.3 | 봇 가능성 있음 | 느슨 |
| 0.1 | 거의 확실히 봇 | 매우 느슨 |

**임계값 변경:**
```yaml
app:
  recaptcha:
    threshold: 0.7  # 더 엄격하게 설정
```

---

## 🔄 동작 흐름

```
┌─────────────────────────────────────────────────────────────────┐
│  [사용자]                                                        │
│     │                                                            │
│     ▼                                                            │
│  QnA 폼 페이지 로드                                              │
│     │                                                            │
│     ▼                                                            │
│  reCAPTCHA v3 스크립트 로드 (Google CDN)                         │
│     │                                                            │
│     ▼                                                            │
│  폼 제출 버튼 클릭                                               │
│     │                                                            │
│     ▼                                                            │
│  grecaptcha.execute() → 토큰 생성                                │
│     │                                                            │
│     ▼                                                            │
│  폼 데이터 + 토큰 → 서버 전송                                    │
│     │                                                            │
│     ▼                                                            │
│  [서버] RecaptchaService.verifyToken()                           │
│     │                                                            │
│     ▼                                                            │
│  Google API 호출 (siteverify)                                    │
│     │                                                            │
│     ▼                                                            │
│  점수 확인 (score >= threshold)                                  │
│     │                                                            │
│     ├── 성공 → QnA 저장 → 목록으로 리다이렉트                    │
│     │                                                            │
│     └── 실패 → 에러 메시지 → 폼으로 리다이렉트                   │
└─────────────────────────────────────────────────────────────────┘
```

---

## 🧪 테스트 방법

### 개발 환경 (키 미설정)

키가 설정되지 않은 경우 자동으로 검증을 건너뜁니다:
```yaml
app:
  recaptcha:
    site-key: test-site-key      # 기본값
    secret-key: test-secret-key  # 기본값
```

### 실제 키로 테스트

1. 환경 변수에 실제 키 설정
2. 애플리케이션 재시작
3. QnA 폼 페이지 접속: `http://localhost:8080/{companyCode}/qna/new`
4. 질문 등록 테스트
5. 로그 확인:
   ```
   INFO  RecaptchaService - reCAPTCHA 검증 결과: success=true, score=0.9
   ```

### 브라우저 개발자 도구 확인

1. F12 → Network 탭
2. 폼 제출 시 POST 요청 확인
3. Form Data에서 `recaptchaToken` 필드 확인

---

## 🛠️ 문제 해결

### 증상: "보안 검증에 실패했습니다" 에러

**원인 1: 키 불일치**
- Site Key와 Secret Key가 같은 프로젝트의 키인지 확인
- 개발용 키와 운영용 키 혼용 여부 확인

**원인 2: 도메인 미등록**
- reCAPTCHA 관리 콘솔에서 현재 도메인이 등록되어 있는지 확인
- `localhost` 도메인 추가 필요

**원인 3: 점수 임계값**
- 로그에서 실제 점수 확인
- 필요시 `threshold` 값 조정

```
WARN  RecaptchaService - reCAPTCHA 검증 실패: score=0.3, errorCodes=[]
```

### 증상: 토큰이 전송되지 않음

**확인 사항:**
1. 브라우저 콘솔에서 JavaScript 에러 확인
2. reCAPTCHA 스크립트 로드 확인
3. 네트워크 차단 (방화벽, 프록시) 확인

### 증상: Google API 호출 실패

**원인: 네트워크 문제**
```
ERROR RecaptchaService - reCAPTCHA 검증 중 오류 발생: Connection refused
```

**해결:**
- 서버 인터넷 연결 확인
- 방화벽에서 `www.google.com` 허용
- 프록시 설정 확인

---

## 📚 관련 파일

### 설정 파일
| 파일 | 설명 |
|------|------|
| `src/main/resources/application.yaml` | reCAPTCHA 설정 값 |
| `src/main/java/.../config/RecaptchaConfig.java` | 설정 바인딩 클래스 |

### 서비스 파일
| 파일 | 설명 |
|------|------|
| `src/main/java/.../service/RecaptchaService.java` | 토큰 검증 서비스 |

### 컨트롤러 파일
| 파일 | 설명 |
|------|------|
| `src/main/java/.../controller/public_/QnaController.java` | QnA 등록 처리 |

### 템플릿 파일
| 파일 | 설명 |
|------|------|
| `src/main/resources/templates/public/qna/form.html` | QnA 질문 등록 폼 |

---

## 🔒 보안 권장사항

### 개발 환경
- ✅ 테스트 키 사용 가능 (`test-site-key`)
- ✅ 낮은 임계값 설정 (`0.3`)
- ✅ 상세 로그 활성화

### 운영 환경
- ⚠️ 반드시 실제 키 사용
- ⚠️ 적절한 임계값 설정 (`0.5` 이상)
- ⚠️ Secret Key 환경 변수로 관리
- ⚠️ 로그에 토큰 값 노출 금지
- ⚠️ HTTPS 필수

---

## 📖 참고 자료

- [Google reCAPTCHA v3 공식 문서](https://developers.google.com/recaptcha/docs/v3)
- [reCAPTCHA 관리 콘솔](https://www.google.com/recaptcha/admin)
- [Spring WebFlux + WebClient 가이드](https://docs.spring.io/spring-framework/reference/web/webflux-webclient.html)