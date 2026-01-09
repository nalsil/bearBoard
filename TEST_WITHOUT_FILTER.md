# 필터 없이 테스트

만약 여전히 403이 발생한다면, 다음 단계를 시도하세요:

## 1단계: SecurityConfig에서 필터 제거

SecurityConfig.java의 98-99번째 줄을 주석 처리:

```java
// JWT 인증 필터 추가 - Bean 주입받은 것 사용
// .addFilterBefore(jwtAuthenticationFilter, SecurityWebFiltersOrder.AUTHENTICATION)
```

## 2단계: 재시작 및 테스트

애플리케이션을 재시작하고 `/admin/login`에서 로그인을 시도합니다.

- **성공하면**: 문제는 JwtAuthenticationFilter에 있습니다
- **여전히 403이면**: 문제는 다른 Security 설정에 있습니다

## 3단계: 결과 공유

테스트 결과와 서버 로그를 공유해주세요.
