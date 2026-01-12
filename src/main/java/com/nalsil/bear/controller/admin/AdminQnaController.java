package com.nalsil.bear.controller.admin;

import com.nalsil.bear.domain.qna.Qna;
import com.nalsil.bear.mapper.QnaMapper;
import com.nalsil.bear.service.AdminService;
import com.nalsil.bear.service.CompanyService;
import com.nalsil.bear.service.QnaService;
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
 * 관리자 QnA 컨트롤러
 *
 * 관리자가 QnA에 답변하고, 숨김 처리할 수 있습니다.
 */
@Tag(name = "관리자 - QnA 관리", description = "관리자 QnA 답변 등록, 수정, 숨김 처리 API")
@Slf4j
@Controller
@RequestMapping("/admin/qnas")
@RequiredArgsConstructor
@SecurityRequirement(name = "JWT Cookie")
public class AdminQnaController {

    private final QnaService qnaService;
    private final AdminService adminService;
    private final QnaMapper qnaMapper;
    private final CompanyService companyService;

    /**
     * QnA 목록
     *
     * @param exchange ServerWebExchange
     * @param model 모델
     * @return QnA 목록 템플릿
     */
    @Operation(summary = "관리자 QnA 목록 조회", description = "관리자가 자신의 기업 QnA 목록을 조회합니다.", tags = "관리자 - QnA 관리")
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
        log.info("관리자 QnA 목록 조회: companyId={}", adminCompanyId);

        return companyService.getCompanyById(adminCompanyId)
                .flatMap(company -> {
                    model.addAttribute("company", company);

                    return qnaService.getAllQnasByCompanyId(adminCompanyId, 0, 100)
                            .collectList()
                            .doOnNext(qnas -> model.addAttribute("qnas", qnas));
                })
                .thenReturn("admin/qna/list");
    }

    /**
     * QnA 상세 (답변 작성 폼 포함)
     *
     * @param id QnA ID
     * @param exchange ServerWebExchange
     * @param model 모델
     * @return QnA 상세 템플릿
     */
    @Operation(summary = "관리자 QnA 상세 조회", description = "관리자가 QnA 상세 정보를 조회하고 답변을 작성할 수 있습니다.", tags = "관리자 - QnA 관리")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "HTML 페이지",
                    content = @Content(
                            mediaType = "text/html"
                    )
            )
    })
    @GetMapping("/{id}")
    public Mono<String> detail(
            @PathVariable Long id,
            ServerWebExchange exchange,
            Model model) {

        Long adminCompanyId = (Long) exchange.getAttributes().get("companyId");
        log.info("QnA 상세 조회: id={}", id);

        return companyService.getCompanyById(adminCompanyId)
                .flatMap(company -> {
                    model.addAttribute("company", company);

                    return qnaService.getQnaById(id)
                            .flatMap(qna -> {
                                // 권한 확인
                                if (!qna.getCompanyId().equals(adminCompanyId)) {
                                    return Mono.error(new IllegalAccessException("접근 권한이 없습니다."));
                                }

                                model.addAttribute("qna", qna);
                                return Mono.just("admin/qna/detail");
                            });
                })
                .onErrorResume(IllegalAccessException.class, e -> {
                    return Mono.just("redirect:/admin/qnas?error=access_denied");
                });
    }

    /**
     * QnA 답변 작성/수정
     *
     * @param id QnA ID
     * @param answer 답변 내용
     * @param exchange ServerWebExchange
     * @return QnA 목록으로 리다이렉트
     */
    @Operation(summary = "관리자 QnA 답변 작성", description = "관리자가 QnA에 답변을 작성하거나 수정합니다.", tags = "관리자 - QnA 관리")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "HTML 페이지",
                    content = @Content(
                            mediaType = "text/html"
                    )
            )
    })
    @PostMapping("/{id}/answer")
    public Mono<String> answer(
            @PathVariable Long id,
            @RequestParam String answer,
            ServerWebExchange exchange) {

        Long adminCompanyId = (Long) exchange.getAttributes().get("companyId");
        Long adminId = (Long) exchange.getAttributes().get("adminId");
        log.info("QnA 답변 작성: id={}, adminId={}", id, adminId);

        return qnaService.getQnaById(id)
                .flatMap(qna -> {
                    // 권한 확인
                    if (!qna.getCompanyId().equals(adminCompanyId)) {
                        return Mono.error(new IllegalAccessException("접근 권한이 없습니다."));
                    }

                    // 답변 추가 (MapStruct 사용)
                    qnaMapper.updateWithAnswer(answer, adminId, qna);

                    return qnaService.updateQna(qna);
                })
                .thenReturn("redirect:/admin/qnas?success=answered")
                .onErrorResume(IllegalAccessException.class, e -> {
                    return Mono.just("redirect:/admin/qnas?error=access_denied");
                });
    }

    /**
     * QnA 숨김/표시 토글
     *
     * @param id QnA ID
     * @param exchange ServerWebExchange
     * @return QnA 목록으로 리다이렉트
     */
    @Operation(summary = "관리자 QnA 숨김 토글", description = "관리자가 QnA의 숨김 상태를 토글합니다.", tags = "관리자 - QnA 관리")
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
        log.info("QnA 숨김 토글: id={}", id);

        return qnaService.getQnaById(id)
                .flatMap(qna -> {
                    // 권한 확인
                    if (!qna.getCompanyId().equals(adminCompanyId)) {
                        return Mono.error(new IllegalAccessException("접근 권한이 없습니다."));
                    }

                    qna.setIsHidden(!qna.getIsHidden());

                    return qnaService.updateQna(qna);
                })
                .thenReturn("redirect:/admin/qnas")
                .onErrorResume(IllegalAccessException.class, e -> {
                    return Mono.just("redirect:/admin/qnas?error=access_denied");
                });
    }

    /**
     * QnA 삭제
     *
     * @param id QnA ID
     * @param exchange ServerWebExchange
     * @return QnA 목록으로 리다이렉트
     */
    @Operation(summary = "관리자 QnA 삭제", description = "관리자가 QnA를 삭제합니다.", tags = "관리자 - QnA 관리")
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
    public Mono<String> delete(@PathVariable Long id, ServerWebExchange exchange) {
        Long adminCompanyId = (Long) exchange.getAttributes().get("companyId");
        log.info("QnA 삭제: id={}", id);

        return qnaService.getQnaById(id)
                .flatMap(qna -> {
                    // 권한 확인
                    if (!qna.getCompanyId().equals(adminCompanyId)) {
                        return Mono.error(new IllegalAccessException("접근 권한이 없습니다."));
                    }

                    return qnaService.deleteQna(id);
                })
                .thenReturn("redirect:/admin/qnas?success=deleted")
                .onErrorResume(IllegalAccessException.class, e -> {
                    return Mono.just("redirect:/admin/qnas?error=access_denied");
                });
    }
}
