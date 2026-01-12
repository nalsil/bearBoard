package com.nalsil.bear.controller.admin;

import com.nalsil.bear.dto.response.AdminDashboardResponse;
import com.nalsil.bear.service.*;
import com.nalsil.bear.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 관리자 대시보드 컨트롤러
 *
 * 관리자 대시보드 페이지 및 통계 정보를 제공합니다.
 */
@Slf4j
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final CompanyService companyService;
    private final PostService postService;
    private final FaqService faqService;
    private final QnaService qnaService;
    private final YoutubeVideoService youtubeVideoService;
    private final ProductService productService;
    private final JwtUtil jwtUtil;

    /**
     * 관리자 대시보드
     *
     * @param exchange ServerWebExchange
     * @param model 모델
     * @return 대시보드 템플릿
     */
    @GetMapping("/dashboard")
    public Mono<String> dashboard(ServerWebExchange exchange, Model model) {
        Long adminCompanyId = (Long) exchange.getAttributes().get("companyId");
        String adminRole = (String) exchange.getAttributes().get("role");
        boolean isSuperAdmin = "SUPER_ADMIN".equals(adminRole);

        log.info("관리자 대시보드 접근: companyId={}, role={}", adminCompanyId, adminRole);

        // 슈퍼관리자인 경우 기업 선택이 필요
        if (isSuperAdmin && adminCompanyId == null) {
            return Mono.just("redirect:/admin/select-company");
        }

        // 슈퍼유저인 경우 모든 기업 목록 조회
        Mono<Void> companiesTask = isSuperAdmin
                ? companyService.getAllCompanies()
                        .collectList()
                        .doOnNext(companies -> model.addAttribute("companies", companies))
                        .then()
                : Mono.empty();

        // 대시보드 통계 조회
        return companiesTask
                .then(companyService.getCompanyById(adminCompanyId))
                .flatMap(company -> {
                    model.addAttribute("company", company);
                    model.addAttribute("isSuperAdmin", isSuperAdmin);

                    // 통계 데이터 수집 (관리자용 - 숨김 포함)
                    Mono<Long> totalPosts = postService.countPostsByCompanyId(adminCompanyId);
                    Mono<Long> totalFaqs = faqService.getAllFaqsByCompanyId(adminCompanyId).count();
                    Mono<Long> unansweredQnas = qnaService.getQnasByAnswerStatus(adminCompanyId, false, 0, Integer.MAX_VALUE).count();
                    Mono<Long> totalYoutubeVideos = youtubeVideoService.getAllVideosByCompanyId(adminCompanyId).count();
                    Mono<Long> totalProducts = productService.getProductsByCompanyId(adminCompanyId).count();

                    return Mono.zip(totalPosts, totalFaqs, unansweredQnas, totalYoutubeVideos, totalProducts)
                            .map(tuple -> AdminDashboardResponse.builder()
                                    .companyName(company.getName())
                                    .totalPosts(tuple.getT1())
                                    .totalFaqs(tuple.getT2())
                                    .unansweredQnas(tuple.getT3())
                                    .totalYoutubeVideos(tuple.getT4())
                                    .totalProducts(tuple.getT5())
                                    .build())
                            .doOnNext(stats -> model.addAttribute("stats", stats));
                })
                .thenReturn("admin/dashboard");
    }

    /**
     * 슈퍼유저 기업 선택 페이지
     *
     * @param exchange ServerWebExchange
     * @param model 모델
     * @return 기업 선택 템플릿
     */
    @GetMapping("/select-company")
    public Mono<String> selectCompany(ServerWebExchange exchange, Model model) {
        String adminRole = (String) exchange.getAttributes().get("role");

        // 슈퍼관리자가 아니면 대시보드로 리다이렉트
        if (!"SUPER_ADMIN".equals(adminRole)) {
            return Mono.just("redirect:/admin/dashboard");
        }

        // 모든 기업 목록 조회
        return companyService.getAllCompanies()
                .collectList()
                .doOnNext(companies -> model.addAttribute("companies", companies))
                .thenReturn("admin/select-company");
    }

    /**
     * 기업 전환 처리
     *
     * 슈퍼관리자가 기업을 선택하면 새로운 JWT 토큰을 발급합니다.
     *
     * @param exchange ServerWebExchange
     * @return 대시보드로 리다이렉트
     */
    @PostMapping("/switch-company")
    public Mono<String> switchCompany(ServerWebExchange exchange) {
        return exchange.getFormData()
            .flatMap(formData -> {
                String companyIdStr = formData.getFirst("companyId");
                if (companyIdStr == null || companyIdStr.isEmpty()) {
                    log.error("companyId 파라미터가 누락됨");
                    return Mono.just("redirect:/admin/select-company?error=invalid_company");
                }

                Long companyId;
                try {
                    companyId = Long.parseLong(companyIdStr);
                } catch (NumberFormatException e) {
                    log.error("유효하지 않은 companyId 형식: {}", companyIdStr);
                    return Mono.just("redirect:/admin/select-company?error=invalid_company");
                }

                String adminRole = (String) exchange.getAttributes().get("role");
                Long adminId = (Long) exchange.getAttributes().get("adminId");
                String username = (String) exchange.getAttributes().get("username");

                log.info("기업 전환 요청: adminId={}, username={}, role={}, companyId={}",
                        adminId, username, adminRole, companyId);

                // 슈퍼관리자가 아니면 대시보드로 리다이렉트
                if (!"SUPER_ADMIN".equals(adminRole)) {
                    log.warn("슈퍼관리자가 아닌 사용자의 기업 전환 시도");
                    return Mono.just("redirect:/admin/dashboard");
                }

                // 선택한 기업이 유효한지 확인
                return companyService.getCompanyById(companyId)
                        .flatMap(company -> {
                            if (!company.getIsActive()) {
                                log.warn("비활성화된 기업 선택 시도: companyId={}", companyId);
                                return Mono.just("redirect:/admin/select-company?error=inactive_company");
                            }

                            // 새로운 JWT 토큰 발급 (선택한 companyId 포함)
                            String newToken = jwtUtil.generateToken(username, adminId, companyId, adminRole);

                            log.info("슈퍼관리자 기업 전환 성공: username={}, companyId={}, companyName={}",
                                    username, companyId, company.getName());

                            // JWT 토큰을 HTTP-Only Cookie에 저장
                            ResponseCookie cookie = ResponseCookie.from("JWT-TOKEN", newToken)
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
                            log.error("유효하지 않은 기업 ID: companyId={}", companyId);
                            return Mono.just("redirect:/admin/select-company?error=invalid_company");
                        }))
                        .onErrorResume(e -> {
                            log.error("기업 전환 처리 중 오류 발생", e);
                            return Mono.just("redirect:/admin/select-company?error=system_error");
                        });
            });
    }
}
