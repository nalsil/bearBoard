package com.nalsil.bear.controller.admin;

import com.nalsil.bear.domain.qna.Qna;
import com.nalsil.bear.mapper.QnaMapper;
import com.nalsil.bear.service.AdminService;
import com.nalsil.bear.service.QnaService;
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
@Slf4j
@Controller
@RequestMapping("/admin/qnas")
@RequiredArgsConstructor
public class AdminQnaController {

    private final QnaService qnaService;
    private final AdminService adminService;
    private final QnaMapper qnaMapper;

    /**
     * QnA 목록
     *
     * @param exchange ServerWebExchange
     * @param model 모델
     * @return QnA 목록 템플릿
     */
    @GetMapping
    public Mono<String> list(ServerWebExchange exchange, Model model) {
        Long adminCompanyId = (Long) exchange.getAttributes().get("companyId");
        log.info("관리자 QnA 목록 조회: companyId={}", adminCompanyId);

        return qnaService.getQnasByCompanyId(adminCompanyId, 0, 100)
                .collectList()
                .doOnNext(qnas -> model.addAttribute("qnas", qnas))
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
    @GetMapping("/{id}")
    public Mono<String> detail(
            @PathVariable Long id,
            ServerWebExchange exchange,
            Model model) {

        Long adminCompanyId = (Long) exchange.getAttributes().get("companyId");
        log.info("QnA 상세 조회: id={}", id);

        return qnaService.getQnaById(id)
                .flatMap(qna -> {
                    // 권한 확인
                    if (!qna.getCompanyId().equals(adminCompanyId)) {
                        return Mono.error(new IllegalAccessException("접근 권한이 없습니다."));
                    }

                    model.addAttribute("qna", qna);
                    return Mono.just("admin/qna/detail");
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
