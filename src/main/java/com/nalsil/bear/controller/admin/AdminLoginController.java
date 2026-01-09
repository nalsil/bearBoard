package com.nalsil.bear.controller.admin;

import com.nalsil.bear.service.AdminService;
import com.nalsil.bear.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 관리자 로그인 컨트롤러 - JWT 기반
 *
 * 관리자 로그인, 로그아웃 기능을 제공합니다.
 */
@Slf4j
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminLoginController {

    private final AdminService adminService;
    private final JwtUtil jwtUtil;

    /**
     * 관리자 로그인 페이지
     *
     * @param model 모델
     * @return 로그인 템플릿
     */
    @GetMapping("/login")
    public Mono<String> loginPage(Model model) {
        log.info("관리자 로그인 페이지 접근");
        return Mono.just("admin/login");
    }

    /**
     * 관리자 로그인 처리 - JWT 토큰 발급
     *
     * @param request 로그인 요청
     * @param exchange ServerWebExchange
     * @param model 모델
     * @return 로그인 성공 시 대시보드로 리다이렉트, 실패 시 로그인 페이지
     */
    @PostMapping("/login")
    public Mono<String> login(ServerWebExchange exchange) {
        log.error("========== !!! 관리자 로그인 POST 요청 도달!!! ==========");
        log.error("Request Method: {}", exchange.getRequest().getMethod());
        log.error("Request URI: {}", exchange.getRequest().getURI());
        log.error("Content-Type: {}", exchange.getRequest().getHeaders().getContentType());

        return exchange.getFormData()
                .doOnNext(formData -> {
                    log.info("Form Data: {}", formData);
                    log.info("Username: {}", formData.getFirst("username"));
                    log.info("Password: {}", formData.getFirst("password"));
                })
                .flatMap(formData -> {
                    String username = formData.getFirst("username");
                    String password = formData.getFirst("password");

                    if (username == null || password == null) {
                        log.error("Username or password is null");
                        return Mono.just("admin/login");
                    }

                    log.info("인증 시도: username={}", username);

                    return adminService.authenticate(username, password)
                            .flatMap(admin -> {
                                // JWT 토큰 생성
                                String token = jwtUtil.generateToken(
                                        admin.getUsername(),
                                        admin.getId(),
                                        admin.getCompanyId(),
                                        admin.getRole()
                                );

                                log.info("관리자 로그인 성공: username={}, role={}, token generated",
                                        admin.getUsername(), admin.getRole());

                                // JWT 토큰을 HTTP-Only Cookie에 저장
                                ResponseCookie cookie = ResponseCookie.from("JWT-TOKEN", token)
                                        .httpOnly(true)
                                        .secure(false) // HTTPS에서는 true로 설정
                                        .path("/")
                                        .maxAge(24 * 60 * 60) // 24시간
                                        .sameSite("Lax")
                                        .build();

                                exchange.getResponse().addCookie(cookie);

                                return Mono.just("redirect:/admin/dashboard");
                            })
                            .switchIfEmpty(Mono.defer(() -> {
                                log.warn("인증 실패: username={}", username);
                                return Mono.just("admin/login");
                            }));
                })
                .onErrorResume(e -> {
                    log.error("로그인 처리 중 오류 발생", e);
                    return Mono.just("admin/login");
                });
    }

    /**
     * 관리자 로그아웃 - JWT 토큰 쿠키 삭제
     *
     * @param exchange ServerWebExchange
     * @return 로그인 페이지로 리다이렉트
     */
    @GetMapping("/logout")
    public Mono<String> logout(ServerWebExchange exchange) {
        log.info("관리자 로그아웃");

        // JWT 토큰 쿠키 삭제
        ResponseCookie cookie = ResponseCookie.from("JWT-TOKEN", "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0) // 즉시 만료
                .build();

        exchange.getResponse().addCookie(cookie);

        return Mono.just("redirect:/admin/login?logout");
    }
}
