package com.nalsil.bear.controller.api.admin;

import com.nalsil.bear.dto.response.ApiResponse;
import com.nalsil.bear.dto.response.DashboardStatsResponse;
import com.nalsil.bear.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 대시보드 API 컨트롤러
 */
@Tag(name = "관리자 대시보드 API", description = "대시보드 통계 조회 API")
@Slf4j
@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
@SecurityRequirement(name = "JWT Cookie")
public class DashboardApiController {

    private final CompanyService companyService;
    private final ProductService productService;
    private final FaqService faqService;
    private final QnaService qnaService;
    private final YoutubeVideoService youtubeVideoService;

    /**
     * 대시보드 통계 조회
     */
    @Operation(summary = "대시보드 통계 조회", description = "현재 관리 중인 기업의 대시보드 통계를 조회합니다.")
    @GetMapping
    public Mono<ResponseEntity<ApiResponse<DashboardStatsResponse>>> getDashboardStats(ServerWebExchange exchange) {
        Long companyId = (Long) exchange.getAttributes().get("companyId");
        String role = (String) exchange.getAttributes().get("role");

        if (companyId == null && !"SUPER_ADMIN".equals(role)) {
            return Mono.just(ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("기업이 선택되지 않았습니다.", "NO_COMPANY_SELECTED")));
        }

        // 슈퍼관리자이면서 회사 미선택 시
        if (companyId == null) {
            return Mono.just(ResponseEntity.ok(ApiResponse.success(
                    DashboardStatsResponse.builder()
                            .productCount(0)
                            .postCount(0)
                            .faqCount(0)
                            .qnaCount(0)
                            .pendingQnaCount(0)
                            .youtubeCount(0)
                            .company(null)
                            .build()
            )));
        }

        return companyService.getCompanyById(companyId)
                .flatMap(company -> {
                    Mono<Long> productCountMono = productService.countVisibleProductsByCompanyId(companyId)
                            .onErrorReturn(0L);
                    Mono<Long> faqCountMono = faqService.getAllFaqsByCompanyId(companyId).count()
                            .onErrorReturn(0L);
                    Mono<Long> qnaCountMono = qnaService.getAllQnasByCompanyId(companyId).count()
                            .onErrorReturn(0L);
                    Mono<Long> pendingQnaCountMono = qnaService.getUnansweredQnasByCompanyId(companyId).count()
                            .onErrorReturn(0L);
                    Mono<Long> youtubeCountMono = youtubeVideoService.getAllVideosByCompanyId(companyId).count()
                            .onErrorReturn(0L);

                    return Mono.zip(productCountMono, faqCountMono, qnaCountMono, pendingQnaCountMono, youtubeCountMono)
                            .map(tuple -> {
                                DashboardStatsResponse.CompanyInfo companyInfo = DashboardStatsResponse.CompanyInfo.builder()
                                        .id(company.getId())
                                        .code(company.getCode())
                                        .name(company.getName())
                                        .logoUrl(company.getLogoUrl())
                                        .build();

                                return DashboardStatsResponse.builder()
                                        .productCount(tuple.getT1())
                                        .postCount(0) // 게시글은 게시판 기반이므로 별도 처리 필요
                                        .faqCount(tuple.getT2())
                                        .qnaCount(tuple.getT3())
                                        .pendingQnaCount(tuple.getT4())
                                        .youtubeCount(tuple.getT5())
                                        .company(companyInfo)
                                        .build();
                            });
                })
                .map(stats -> ResponseEntity.ok(ApiResponse.success(stats)))
                .onErrorResume(e -> {
                    log.error("대시보드 통계 조회 실패: companyId={}", companyId, e);
                    return Mono.just(ResponseEntity
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(ApiResponse.error("대시보드 통계 조회에 실패했습니다.")));
                });
    }
}
