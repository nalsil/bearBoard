package com.nalsil.bear.controller.admin;

import com.nalsil.bear.dto.response.AdminDashboardResponse;
import com.nalsil.bear.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

                    // 통계 데이터 수집
                    Mono<Long> totalPosts = postService.countPostsByCompanyId(adminCompanyId);
                    Mono<Long> totalFaqs = faqService.getFaqsByCompanyId(adminCompanyId).count();
                    Mono<Long> unansweredQnas = qnaService.getQnasByAnswerStatus(adminCompanyId, false, 0, Integer.MAX_VALUE).count();
                    Mono<Long> totalYoutubeVideos = youtubeVideoService.getVideosByCompanyId(adminCompanyId).count();
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
     * NOTE: JWT 기반 인증에서는 기업 전환을 위해 새로운 JWT 토큰 발급이 필요합니다.
     * 현재는 기능이 비활성화되어 있으며, 재로그인을 통해 기업을 변경해야 합니다.
     *
     * @param companyId 선택한 기업 ID
     * @param exchange ServerWebExchange
     * @return 대시보드로 리다이렉트
     */
    @PostMapping("/switch-company")
    public Mono<String> switchCompany(@RequestParam Long companyId, ServerWebExchange exchange) {
        String adminRole = (String) exchange.getAttributes().get("role");

        // 슈퍼관리자가 아니면 대시보드로 리다이렉트
        if (!"SUPER_ADMIN".equals(adminRole)) {
            return Mono.just("redirect:/admin/dashboard");
        }

        // JWT 기반 인증에서는 토큰 재발급이 필요하므로 현재는 지원하지 않음
        log.warn("기업 전환은 JWT 기반 인증에서 지원하지 않습니다. 재로그인이 필요합니다.");
        return Mono.just("redirect:/admin/select-company?error=jwt_not_supported");
    }
}
