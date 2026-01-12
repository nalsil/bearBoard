package com.nalsil.bear.controller.admin;

import com.nalsil.bear.domain.faq.Faq;
import com.nalsil.bear.mapper.FaqMapper;
import com.nalsil.bear.service.AdminService;
import com.nalsil.bear.service.CompanyService;
import com.nalsil.bear.service.FaqService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 관리자 FAQ 컨트롤러
 *
 * 관리자가 FAQ를 등록, 수정, 삭제, 숨김 처리할 수 있습니다.
 */
@Tag(name = "관리자 - FAQ 관리", description = "관리자 FAQ 등록, 수정, 삭제, 숨김 처리 API")
@Slf4j
@Controller
@RequestMapping("/admin/faqs")
@RequiredArgsConstructor
@SecurityRequirement(name = "JWT Cookie")
public class AdminFaqController {

    private final FaqService faqService;
    private final AdminService adminService;
    private final FaqMapper faqMapper;
    private final CompanyService companyService;

    /**
     * FAQ 목록
     *
     * @param exchange ServerWebExchange
     * @param model 모델
     * @return FAQ 목록 템플릿
     */
    @Operation(summary = "관리자 FAQ 목록 조회", description = "관리자가 자신의 기업 FAQ 목록을 조회합니다.", tags = "관리자 - FAQ 관리")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "HTML 페이지",
                    content = @Content(
                            mediaType = "text/html"
                    )
            )
    })
    @GetMapping
    public Mono<String> list(ServerWebExchange exchange, Model model) {
        Long adminCompanyId = (Long) exchange.getAttributes().get("companyId");
        log.info("관리자 FAQ 목록 조회: companyId={}", adminCompanyId);

        return companyService.getCompanyById(adminCompanyId)
                .flatMap(company -> {
                    model.addAttribute("company", company);

                    return faqService.getAllFaqsByCompanyId(adminCompanyId)
                            .collectList()
                            .doOnNext(faqs -> {
                                model.addAttribute("faqs", faqs);
                                // 카테고리 목록도 전달
                                faqService.getCategoriesByCompanyId(adminCompanyId)
                                        .collectList()
                                        .doOnNext(categories -> model.addAttribute("categories", categories))
                                        .subscribe();
                            });
                })
                .thenReturn("admin/faq/list");
    }

    /**
     * FAQ 작성 폼
     *
     * @param exchange ServerWebExchange
     * @param model 모델
     * @return FAQ 작성 폼 템플릿
     */
    @Operation(summary = "관리자 FAQ 등록 폼", description = "관리자가 새로운 FAQ를 등록하기 위한 폼을 표시합니다.", tags = "관리자 - FAQ 관리")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "HTML 페이지",
                    content = @Content(
                            mediaType = "text/html"
                    )
            )
    })
    @GetMapping("/new")
    public Mono<String> newFaqForm(ServerWebExchange exchange, Model model) {
        Long adminCompanyId = (Long) exchange.getAttributes().get("companyId");

        return companyService.getCompanyById(adminCompanyId)
                .doOnNext(company -> {
                    model.addAttribute("company", company);
                    model.addAttribute("faq", new Faq());
                })
                .thenReturn("admin/faq/form");
    }

    /**
     * FAQ 작성 처리
     *
     * @param faq FAQ 엔티티
     * @param exchange ServerWebExchange
     * @return FAQ 목록으로 리다이렉트
     */
    @Operation(summary = "관리자 FAQ 등록", description = "관리자가 새로운 FAQ를 등록합니다.", tags = "관리자 - FAQ 관리")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "HTML 페이지",
                    content = @Content(
                            mediaType = "text/html"
                    )
            )
    })
    @PostMapping
    public Mono<String> createFaq(@ModelAttribute Faq faq, ServerWebExchange exchange) {
        Long adminCompanyId = (Long) exchange.getAttributes().get("companyId");
        log.info("FAQ 작성: companyId={}, question={}", adminCompanyId, faq.getQuestion());

        // MapStruct를 사용하여 엔티티 생성 준비
        Faq preparedFaq = faqMapper.prepareForCreate(faq);
        preparedFaq.setCompanyId(adminCompanyId);

        return faqService.createFaq(preparedFaq)
                .thenReturn("redirect:/admin/faqs?success=created");
    }

    /**
     * FAQ 수정 폼
     *
     * @param id FAQ ID
     * @param exchange ServerWebExchange
     * @param model 모델
     * @return FAQ 수정 폼 템플릿
     */
    @Operation(summary = "관리자 FAQ 수정 폼", description = "관리자가 기존 FAQ를 수정하기 위한 폼을 표시합니다.", tags = "관리자 - FAQ 관리")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "HTML 페이지",
                    content = @Content(
                            mediaType = "text/html"
                    )
            )
    })
    @GetMapping("/{id}/edit")
    public Mono<String> editFaqForm(
            @PathVariable Long id,
            ServerWebExchange exchange,
            Model model) {

        Long adminCompanyId = (Long) exchange.getAttributes().get("companyId");
        log.info("FAQ 수정 폼: id={}", id);

        return companyService.getCompanyById(adminCompanyId)
                .flatMap(company -> {
                    model.addAttribute("company", company);

                    return faqService.getFaqById(id)
                            .flatMap(faq -> {
                                // 권한 확인
                                if (!faq.getCompanyId().equals(adminCompanyId)) {
                                    return Mono.error(new IllegalAccessException("접근 권한이 없습니다."));
                                }

                                model.addAttribute("faq", faq);
                                return Mono.just("admin/faq/form");
                            });
                })
                .onErrorResume(IllegalAccessException.class, e -> {
                    return Mono.just("redirect:/admin/faqs?error=access_denied");
                });
    }

    /**
     * FAQ 수정 처리
     *
     * @param id FAQ ID
     * @param faq FAQ 엔티티
     * @param exchange ServerWebExchange
     * @return FAQ 목록으로 리다이렉트
     */
    @Operation(summary = "관리자 FAQ 수정", description = "관리자가 기존 FAQ를 수정합니다.", tags = "관리자 - FAQ 관리")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "HTML 페이지",
                    content = @Content(
                            mediaType = "text/html"
                    )
            )
    })
    @PostMapping("/{id}")
    public Mono<String> updateFaq(
            @PathVariable Long id,
            @ModelAttribute Faq faq,
            ServerWebExchange exchange) {

        Long adminCompanyId = (Long) exchange.getAttributes().get("companyId");
        log.info("FAQ 수정: id={}", id);

        return faqService.getFaqById(id)
                .flatMap(existingFaq -> {
                    // 권한 확인
                    if (!existingFaq.getCompanyId().equals(adminCompanyId)) {
                        return Mono.error(new IllegalAccessException("접근 권한이 없습니다."));
                    }

                    // MapStruct를 사용하여 엔티티 업데이트
                    faqMapper.updateFaq(faq, existingFaq);

                    return faqService.updateFaq(existingFaq);
                })
                .thenReturn("redirect:/admin/faqs?success=updated")
                .onErrorResume(IllegalAccessException.class, e -> {
                    return Mono.just("redirect:/admin/faqs?error=access_denied");
                });
    }

    /**
     * FAQ 삭제
     *
     * @param id FAQ ID
     * @param exchange ServerWebExchange
     * @return FAQ 목록으로 리다이렉트
     */
    @Operation(summary = "관리자 FAQ 삭제", description = "관리자가 FAQ를 삭제합니다.", tags = "관리자 - FAQ 관리")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "HTML 페이지",
                    content = @Content(
                            mediaType = "text/html"
                    )
            )
    })
    @PostMapping("/{id}/delete")
    public Mono<String> deleteFaq(@PathVariable Long id, ServerWebExchange exchange) {
        Long adminCompanyId = (Long) exchange.getAttributes().get("companyId");
        log.info("FAQ 삭제: id={}", id);

        return faqService.getFaqById(id)
                .flatMap(faq -> {
                    // 권한 확인
                    if (!faq.getCompanyId().equals(adminCompanyId)) {
                        return Mono.error(new IllegalAccessException("접근 권한이 없습니다."));
                    }

                    return faqService.deleteFaq(id);
                })
                .thenReturn("redirect:/admin/faqs?success=deleted")
                .onErrorResume(IllegalAccessException.class, e -> {
                    return Mono.just("redirect:/admin/faqs?error=access_denied");
                });
    }

    /**
     * FAQ 숨김/표시 토글
     *
     * @param id FAQ ID
     * @param exchange ServerWebExchange
     * @return FAQ 목록으로 리다이렉트
     */
    @Operation(summary = "관리자 FAQ 숨김 토글", description = "관리자가 FAQ의 숨김 상태를 토글합니다.", tags = "관리자 - FAQ 관리")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "HTML 페이지",
                    content = @Content(
                            mediaType = "text/html"
                    )
            )
    })
    @PostMapping("/{id}/toggle-hidden")
    public Mono<String> toggleHidden(@PathVariable Long id, ServerWebExchange exchange) {
        Long adminCompanyId = (Long) exchange.getAttributes().get("companyId");
        log.info("FAQ 숨김 토글: id={}", id);

        return faqService.getFaqById(id)
                .flatMap(faq -> {
                    // 권한 확인
                    if (!faq.getCompanyId().equals(adminCompanyId)) {
                        return Mono.error(new IllegalAccessException("접근 권한이 없습니다."));
                    }

                    // 숨김 상태 토글
                    Faq updateData = Faq.builder()
                            .isHidden(!faq.getIsHidden())
                            .build();
                    faqMapper.updateFaq(updateData, faq);

                    return faqService.updateFaq(faq);
                })
                .thenReturn("redirect:/admin/faqs")
                .onErrorResume(IllegalAccessException.class, e -> {
                    return Mono.just("redirect:/admin/faqs?error=access_denied");
                });
    }
}
