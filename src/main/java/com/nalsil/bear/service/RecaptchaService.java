package com.nalsil.bear.service;

import com.nalsil.bear.config.RecaptchaConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * reCAPTCHA v3 검증 서비스
 *
 * Google reCAPTCHA v3 토큰을 검증하고 봇 여부를 판단합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RecaptchaService {

    private final RecaptchaConfig recaptchaConfig;
    private final WebClient.Builder webClientBuilder;

    /**
     * reCAPTCHA 토큰 검증
     *
     * @param token reCAPTCHA 토큰 (프론트엔드에서 전달)
     * @return 검증 성공 여부 (true: 사람, false: 봇 의심)
     */
    public Mono<Boolean> verifyToken(String token) {
        if (token == null || token.isEmpty()) {
            log.warn("reCAPTCHA 토큰이 비어있습니다.");
            return Mono.just(false);
        }

        // 테스트 환경에서는 검증 건너뛰기
        if ("test-secret-key".equals(recaptchaConfig.getSecretKey())) {
            log.info("테스트 환경: reCAPTCHAreCAPTCHA 검증 건너뜀");
            return Mono.just(true);
        }

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("secret", recaptchaConfig.getSecretKey());
        formData.add("response", token);

        return webClientBuilder.build()
                .post()
                .uri(recaptchaConfig.getVerifyUrl())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(formData))
                .retrieve()
                .bodyToMono(RecaptchaResponse.class)
                .map(response -> {
                    boolean success = response.isSuccess()
                            && response.getScore() >= recaptchaConfig.getThreshold();

                    log.info("reCAPTCHA 검증 결과: success={}, score={}, threshold={}, action={}",
                            response.isSuccess(), response.getScore(),
                            recaptchaConfig.getThreshold(), response.getAction());

                    if (!success) {
                        log.warn("reCAPTCHA 검증 실패: score={}, errorCodes={}",
                                response.getScore(), response.getErrorCodes());
                    }

                    return success;
                })
                .onErrorResume(e -> {
                    log.error("reCAPTCHA 검증 중 오류 발생: {}", e.getMessage());
                    return Mono.just(false);
                });
    }

    /**
     * reCAPTCHA API 응답 DTO
     */
    @lombok.Data
    public static class RecaptchaResponse {
        private boolean success;
        private double score;
        private String action;
        private String challenge_ts;
        private String hostname;
        @com.fasterxml.jackson.annotation.JsonProperty("error-codes")
        private java.util.List<String> errorCodes;
    }
}
