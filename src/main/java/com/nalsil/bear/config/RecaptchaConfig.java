package com.nalsil.bear.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * reCAPTCHA v3 설정
 *
 * Google reCAPTCHA v3 연동을 위한 설정 값을 관리합니다.
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "app.recaptcha")
public class RecaptchaConfig {

    /**
     * reCAPTCHA 사이트 키 (프론트엔드용)
     */
    private String siteKey;

    /**
     * reCAPTCHA 비밀 키 (서버 검증용)
     */
    private String secretKey;

    /**
     * reCAPTCHA 검증 URL
     */
    private String verifyUrl = "https://www.google.com/recaptcha/api/siteverify";

    /**
     * 점수 임계값 (0.0 ~ 1.0, 기본값: 0.5)
     * 이 값 이상이면 사람으로 판단
     */
    private double threshold = 0.5;
}
