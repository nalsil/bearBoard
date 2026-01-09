package com.nalsil.bear.config;

import com.nalsil.bear.filter.JwtAuthenticationFilter;
import com.nalsil.bear.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;

/**
 * Spring Security WebFlux 설정 클래스 - JWT 기반 인증
 * /admin/login 경로만 허용, 나머지는 JWT 필터로 인증
 */
@Slf4j
@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtUtil jwtUtil;

    /**
     * JWT 기반 Security 설정
     *
     * @param http ServerHttpSecurity
     * @return SecurityWebFilterChain
     */
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter) {
        log.info("========== JWT SecurityWebFilterChain 빈 생성 ==========");

        return http
                .cors(ServerHttpSecurity.CorsSpec::disable)  // CORS 비활성화 (WebFluxConfig에서 처리)
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .logout(ServerHttpSecurity.LogoutSpec::disable)
                .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
                // JWT 필터를 인증 필터 전에 추가
                .addFilterBefore(jwtAuthenticationFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .authorizeExchange(exchanges -> exchanges
                        // 로그인 페이지는 인증 없이 접근 가능
                        .pathMatchers(HttpMethod.GET, "/admin/login").permitAll()
                        .pathMatchers(HttpMethod.POST, "/admin/login").permitAll()
                        // 정적 리소스는 인증 없이 접근 가능
                        .pathMatchers("/css/**", "/js/**", "/images/**", "/favicon.ico").permitAll()
                        .pathMatchers("/actuator/health").permitAll()
                        // 기타 모든 admin 경로는 인증 필요
                        .pathMatchers("/admin/**").authenticated()
                        // 나머지 경로는 허용
                        .anyExchange().permitAll()
                )
                .build();
    }

    /**
     * JWT 인증 필터 Bean
     *
     * @return JwtAuthenticationFilter
     */
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtUtil);
    }

}
