package com.nalsil.bear.controller.public_;

import com.nalsil.bear.domain.company.Company;
import com.nalsil.bear.service.CompanyService;
import com.nalsil.bear.service.ProductService;
import com.nalsil.bear.util.TenantContextHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Mono;

/**
 * HomeController
 * 기업 홈페이지 메인 페이지 및 회사 소개 페이지 처리
 */
@Slf4j
@Controller
@RequestMapping("/{companyCode}")
@RequiredArgsConstructor
public class HomeController {

    private final CompanyService companyService;
    private final ProductService productService;

    /**
     * 기업 홈페이지 메인 페이지
     * URL: /{companyCode}
     *
     * @param companyCode 기업 코드
     * @return Rendering (Thymeleaf 템플릿)
     */
    @GetMapping
    public Mono<Rendering> home(@PathVariable String companyCode) {
        log.info("Accessing home page for company: {}", companyCode);

        // 기업 정보 조회
        Mono<Company> companyMono = companyService.getActiveCompanyByCode(companyCode);

        // 상품 목록 조회 (최대 6개)
        Mono<Company> companyForProducts = companyService.getActiveCompanyByCode(companyCode);

        return companyMono.flatMap(company ->
                companyForProducts.flatMap(c ->
                        productService.getVisibleProductsByCompanyId(c.getId(), PageRequest.of(0, 6))
                                .collectList()
                                .map(products -> Rendering.view("public/home")
                                        .modelAttribute("company", company)
                                        .modelAttribute("products", products)
                                        .build())
                )
        ).contextWrite(ctx -> TenantContextHolder.setCurrentTenant(ctx, companyCode));
    }

    /**
     * 회사 소개 페이지
     * URL: /{companyCode}/about
     *
     * @param companyCode 기업 코드
     * @return Rendering (Thymeleaf 템플릿)
     */
    @GetMapping("/about")
    public Mono<Rendering> about(@PathVariable String companyCode) {
        log.info("Accessing about page for company: {}", companyCode);

        return companyService.getActiveCompanyByCode(companyCode)
                .map(company -> Rendering.view("public/about")
                        .modelAttribute("company", company)
                        .build())
                .contextWrite(ctx -> TenantContextHolder.setCurrentTenant(ctx, companyCode));
    }

}
