package com.nalsil.bear.controller.public_;

import com.nalsil.bear.service.CompanyService;
import com.nalsil.bear.service.FaqService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Mono;

/**
 * FAQ 컨트롤러
 *
 * FAQ 목록 조회 및 검색 기능을 제공합니다.
 */
@Slf4j
@Controller
@RequestMapping("/{companyCode}/faq")
@RequiredArgsConstructor
public class FaqController {

    private final CompanyService companyService;
    private final FaqService faqService;

    /**
     * FAQ 목록 페이지
     *
     * @param companyCode 기업 코드
     * @param category 카테고리 (선택)
     * @param keyword 검색 키워드 (선택)
     * @param model 모델
     * @return FAQ 목록 템플릿
     */
    @GetMapping
    public Mono<String> list(
            @PathVariable String companyCode,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword,
            Model model) {

        log.info("FAQ 목록 조회: companyCode={}, category={}, keyword={}",
                companyCode, category, keyword);

        return companyService.getActiveCompanyByCode(companyCode)
                .flatMap(company -> {
                    model.addAttribute("company", company);

                    // 검색어가 있으면 검색
                    if (keyword != null && !keyword.trim().isEmpty()) {
                        return faqService.searchFaqs(company.getId(), keyword)
                                .collectList()
                                .doOnNext(faqs -> {
                                    model.addAttribute("faqs", faqs);
                                    model.addAttribute("keyword", keyword);
                                });
                    }
                    // 카테고리가 있으면 카테고리별 조회
                    else if (category != null && !category.trim().isEmpty()) {
                        return faqService.getFaqsByCompanyIdAndCategory(company.getId(), category)
                                .collectList()
                                .doOnNext(faqs -> {
                                    model.addAttribute("faqs", faqs);
                                    model.addAttribute("selectedCategory", category);
                                });
                    }
                    // 전체 FAQ 조회
                    else {
                        return faqService.getFaqsByCompanyId(company.getId())
                                .collectList()
                                .doOnNext(faqs -> model.addAttribute("faqs", faqs));
                    }
                })
                .then(companyService.getActiveCompanyByCode(companyCode))
                .flatMap(company -> faqService.getCategoriesByCompanyId(company.getId())
                        .collectList()
                        .doOnNext(categories -> model.addAttribute("categories", categories))
                )
                .thenReturn("public/faq/list");
    }
}
