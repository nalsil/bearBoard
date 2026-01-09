package com.nalsil.bear.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 비밀번호 인코더 설정 클래스
 * 순환 참조 방지를 위해 SecurityConfig에서 분리
 */
@Configuration
public class PasswordEncoderConfig {

    /**
     * 비밀번호 인코더 설정 (BCrypt)
     *
     * @return PasswordEncoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
