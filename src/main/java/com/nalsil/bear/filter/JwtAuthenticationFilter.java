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
        String method = request.getMethod().name();

        log.info("========== JwtAuthenticationFilter 실행 ==========");
        log.info("Method: {}, Path: {}", method, path);
        log.info("Cookies: {}", request.getCookies().keySet());

        // 정적 리소스와 공개 경로는 필터 건너뛰기
        if (path.startsWith("/css/") || path.startsWith("/js/") ||
            path.startsWith("/images/") || path.equals("/favicon.ico") ||
            path.equals("/admin/login") || // 로그인 페이지는 필터 건너뛰기
            !path.startsWith("/admin/")) {
            log.info("JwtAuthenticationFilter: 필터 건너뛰기 - Path: {}", path);
            return chain.filter(exchange);
        }

        log.info("JwtAuthenticationFilter: JWT 검증 시작 - Path: {}", path);

        // Authorization 헤더 또는 Cookie에서 JWT 토큰 추출
        String token = extractToken(request);

        if (token != null && jwtUtil.validateToken(token)) {
            try {
                String username = jwtUtil.getUsernameFromToken(token);
                String role = jwtUtil.getRoleFromToken(token);
                Long adminId = jwtUtil.getAdminIdFromToken(token);
                Long companyId = jwtUtil.getCompanyIdFromToken(token);

                log.info("========== JWT 인증 성공 ==========");
                log.info("Path: {}, Username: {}, Role: {}, AdminId: {}, CompanyId: {}",
                    path, username, role, adminId, companyId);

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
                exchange.getAttributes().put("companyId", companyId);
                exchange.getAttributes().put("username", username);
                exchange.getAttributes().put("role", role);

                // SecurityContext에 인증 정보 설정
                return chain.filter(exchange)
                        .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication));

            } catch (Exception e) {
                log.error("JWT 인증 처리 중 오류 발생", e);
            }
        } else {
            if (token == null) {
                log.warn("JWT 토큰을 찾을 수 없음 - Path: {}", path);
            } else {
                log.warn("JWT 토큰 검증 실패 - Path: {}", path);
            }
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
        log.info("Authorization 헤더: {}", bearerToken);
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            log.info("Bearer 토큰 발견");
            return bearerToken.substring(7);
        }

        // 2. Cookie에서 JWT 토큰 추출
        log.info("쿠키 검색 시작 - 전체 쿠키: {}", request.getCookies());
        if (request.getCookies().containsKey("JWT-TOKEN")) {
            var cookie = request.getCookies().getFirst("JWT-TOKEN");
            if (cookie != null) {
                log.info("JWT-TOKEN 쿠키 발견: {}", cookie.getValue().substring(0, Math.min(20, cookie.getValue().length())) + "...");
                return cookie.getValue();
            }
        } else {
            log.warn("JWT-TOKEN 쿠키를 찾을 수 없음");
        }

        return null;
    }
}
