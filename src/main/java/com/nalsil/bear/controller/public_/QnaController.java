package com.nalsil.bear.controller.public_;

import com.nalsil.bear.domain.qna.Qna;
import com.nalsil.bear.dto.request.CreateQnaRequest;
import com.nalsil.bear.mapper.QnaMapper;
import com.nalsil.bear.service.CompanyService;
import com.nalsil.bear.service.QnaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

/**
 * QnA 컨트롤러
 *
 * QnA 목록 조회, 상세 조회, 질문 등록 기능을 제공합니다.
 */
@Slf4j
@Controller
@RequestMapping("/{companyCode}/qna")
@RequiredArgsConstructor
public class QnaController {

    private final CompanyService companyService;
    private final QnaService qnaService;
    private final QnaMapper qnaMapper;

    /**
     * QnA 목록 페이지
     *
     * @param companyCode 기업 코드
     * @param page 페이지 번호 (기본: 0)
     * @param size 페이지 크기 (기본: 10)
     * @param model 모델
     * @return QnA 목록 템플릿
     */
    @GetMapping
    public Mono<String> list(
            @PathVariable String companyCode,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {

        log.info("QnA 목록 조회: companyCode={}, page={}, size={}", companyCode, page, size);

        return companyService.getActiveCompanyByCode(companyCode)
                .flatMap(company -> {
                    model.addAttribute("company", company);

                    // QnA 목록 조회
                    return qnaService.getQnasByCompanyId(company.getId(), page, size)
                            .collectList()
                            .flatMap(qnas -> {
                                model.addAttribute("qnas", qnas);
                                model.addAttribute("currentPage", page);
                                model.addAttribute("pageSize", size);

                                // 전체 개수 조회
                                return qnaService.countQnasByCompanyId(company.getId())
                                        .doOnNext(count -> {
                                            model.addAttribute("totalCount", count);
                                            model.addAttribute("totalPages", (count + size - 1) / size);
                                        });
                            });
                })
                .thenReturn("public/qna/list");
    }

    /**
     * QnA 상세 페이지
     *
     * @param companyCode 기업 코드
     * @param id QnA ID
     * @param model 모델
     * @return QnA 상세 템플릿
     */
    @GetMapping("/{id}")
    public Mono<String> detail(
            @PathVariable String companyCode,
            @PathVariable Long id,
            Model model) {

        log.info("QnA 상세 조회: companyCode={}, id={}", companyCode, id);

        return companyService.getActiveCompanyByCode(companyCode)
                .doOnNext(company -> model.addAttribute("company", company))
                .then(qnaService.getQnaById(id))
                .doOnNext(qna -> model.addAttribute("qna", qna))
                .thenReturn("public/qna/detail");
    }

    /**
     * QnA 질문 등록 폼 페이지
     *
     * @param companyCode 기업 코드
     * @param model 모델
     * @return QnA 질문 등록 폼 템플릿
     */
    @GetMapping("/new")
    public Mono<String> createForm(
            @PathVariable String companyCode,
            Model model) {

        log.info("QnA 질문 등록 폼 요청: companyCode={}", companyCode);

        return companyService.getActiveCompanyByCode(companyCode)
                .doOnNext(company -> model.addAttribute("company", company))
                .thenReturn("public/qna/form");
    }

    /**
     * QnA 질문 등록 처리
     *
     * @param companyCode 기업 코드
     * @param request 질문 등록 요청
     * @return 리다이렉트 URL
     */
    @PostMapping
    public Mono<String> create(
            @PathVariable String companyCode,
            @ModelAttribute CreateQnaRequest request) {

        log.info("QnA 질문 등록 처리: companyCode={}, email={}",
                companyCode, request.getAskerEmail());

        // 이메일 형식 검증
        if (!qnaService.isValidEmail(request.getAskerEmail())) {
            log.warn("잘못된 이메일 형식: {}", request.getAskerEmail());
            return Mono.just("redirect:/" + companyCode + "/qna/new?error=invalid-email");
        }

        // TODO: reCAPTCHA 검증 추가

        return companyService.getActiveCompanyByCode(companyCode)
                .flatMap(company -> {
                    // QnA 엔티티 생성 (MapStruct 사용)
                    Qna qna = qnaMapper.toEntity(request);
                    qna.setCompanyId(company.getId());

                    // QnA 저장
                    return qnaService.createQna(qna);
                })
                .then(Mono.just("redirect:/" + companyCode + "/qna?success=true"));
    }
}
