package com.nalsil.bear.filter;

import com.nalsil.bear.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Collections;

/**
 * JWT 인증 필터
 * HTTP 요청의 Authorization 헤더 또는 Cookie에서 JWT 토큰을 추출하고 검증
 *
 * SecurityConfig에서 Bean으로 생성되어 Spring Security 필터 체인에 추가됨
 */
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements WebFilter {

    private final JwtUtil jwtUtil;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // 정적 리소스와 공개 경로는 필터 건너뛰기
        if (path.startsWith("/css/") || path.startsWith("/js/") ||
            path.startsWith("/images/") || path.equals("/favicon.ico") ||
            path.equals("/admin/login") || // 로그인 페이지는 필터 건너뛰기
            path.equals("/admin/logout") || // 로그아웃 페이지는 필터 건너뛰기
            path.equals("/api/auth/login") || // API 로그인은 필터 건너뛰기
            path.equals("/api/auth/logout")) { // API 로그아웃은 필터 건너뛰기
            return chain.filter(exchange);
        }

        // /admin/ 또는 /api/auth/ 또는 /api/admin/ 경로만 JWT 검증
        if (!path.startsWith("/admin/") && !path.startsWith("/api/auth/") && !path.startsWith("/api/admin/")) {
            return chain.filter(exchange);
        }

        log.debug("JWT 검증: {}", path);

        // Authorization 헤더 또는 Cookie에서 JWT 토큰 추출
        String token = extractToken(request);

        if (token != null && jwtUtil.validateToken(token)) {
            try {
                String username = jwtUtil.getUsernameFromToken(token);
                String role = jwtUtil.getRoleFromToken(token);
                Long adminId = jwtUtil.getAdminIdFromToken(token);
                Long companyId = jwtUtil.getCompanyIdFromToken(token);

                log.debug("JWT 인증 성공: user={}, role={}", username, role);

                // Spring Security Authentication 생성
                // Spring Security는 hasRole("ADMIN")을 "ROLE_ADMIN"으로 변환하므로 접두사 추가 필요
                String authorityName = role.startsWith("ROLE_") ? role : "ROLE_" + role;
                UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                        username,
                        null,
                        Collections.singletonList(new SimpleGrantedAuthority(authorityName))
                    );

                // JWT 정보를 request attribute에 저장 (컨트롤러에서 사용)
                exchange.getAttributes().put("adminId", adminId);
                // companyId가 null일 수 있으므로 null 체크 후 저장
                if (companyId != null) {
                    exchange.getAttributes().put("companyId", companyId);
                }
                exchange.getAttributes().put("username", username);
                exchange.getAttributes().put("role", role);

                // SecurityContext에 인증 정보 설정
                return chain.filter(exchange)
                        .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication));

            } catch (Exception e) {
                log.error("JWT 인증 처리 중 오류 발생", e);
            }
        } else {
            log.debug("JWT 인증 실패: path={}, token={}", path, token != null ? "invalid" : "missing");
        }

        return chain.filter(exchange);
    }

    /**
     * Authorization 헤더 또는 Cookie에서 JWT 토큰 추출
     *
     * @param request ServerHttpRequest
     * @return JWT 토큰
     */
    private String extractToken(ServerHttpRequest request) {
        // 1. Authorization 헤더에서 Bearer 토큰 추출
        String bearerToken = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        // 2. Cookie에서 JWT 토큰 추출
        if (request.getCookies().containsKey("JWT-TOKEN")) {
            var cookie = request.getCookies().getFirst("JWT-TOKEN");
            if (cookie != null) {
                return cookie.getValue();
            }
        }

        return null;
    }
}
