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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Mono;

/**
 * ProductController
 * 제품 목록 및 상세 페이지 처리
 */
@Slf4j
@Controller
@RequestMapping("/{companyCode}/products")
@RequiredArgsConstructor
public class ProductController {

    private final CompanyService companyService;
    private final ProductService productService;

    /**
     * 제품 목록 페이지
     * URL: /{companyCode}/products
     *
     * @param companyCode 기업 코드
     * @param category 카테고리 (선택)
     * @param page 페이지 번호 (기본값: 0)
     * @param size 페이지 크기 (기본값: 12)
     * @return Rendering (Thymeleaf 템플릿)
     */
    @GetMapping
    public Mono<Rendering> productList(
            @PathVariable String companyCode,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {

        log.info("Accessing product list for company: {}, category: {}, page: {}", companyCode, category, page);

        // 기업 정보 조회
        Mono<Company> companyMono = companyService.getActiveCompanyByCode(companyCode);

        return companyMono.flatMap(company -> {
            // 카테고리별 또는 전체 제품 조회
            Mono<Long> totalCountMono;
            reactor.core.publisher.Flux<com.nalsil.bear.domain.product.Product> productFlux;

            if (category != null && !category.trim().isEmpty()) {
                // 카테고리별 제품 조회
                productFlux = productService.getVisibleProductsByCompanyIdAndCategory(
                        company.getId(), category, PageRequest.of(page, size));
                totalCountMono = productService.countVisibleProductsByCompanyIdAndCategory(company.getId(), category);
            } else {
                // 전체 제품 조회
                productFlux = productService.getVisibleProductsByCompanyId(
                        company.getId(), PageRequest.of(page, size));
                totalCountMono = productService.countVisibleProductsByCompanyId(company.getId());
            }

            return productFlux.collectList()
                    .zipWith(totalCountMono)
                    .zipWith(productService.getCategoriesByCompanyId(company.getId()).collectList())
                    .map(tuple -> {
                        long totalProducts = tuple.getT1().getT2();
                        int totalPages = (int) Math.ceil((double) totalProducts / size);

                        return Rendering.view("public/product/list")
                                .modelAttribute("company", company)
                                .modelAttribute("products", tuple.getT1().getT1())
                                .modelAttribute("categories", tuple.getT2())
                                .modelAttribute("selectedCategory", category)
                                .modelAttribute("currentPage", page)
                                .modelAttribute("totalPages", totalPages)
                                .modelAttribute("totalProducts", totalProducts)
                                .build();
                    });
        }).contextWrite(ctx -> TenantContextHolder.setCurrentTenant(ctx, companyCode));
    }

    /**
     * 제품 상세 페이지
     * URL: /{companyCode}/products/{productId}
     *
     * @param companyCode 기업 코드
     * @param productId 제품 ID
     * @return Rendering (Thymeleaf 템플릿)
     */
    @GetMapping("/{productId}")
    public Mono<Rendering> productDetail(
            @PathVariable String companyCode,
            @PathVariable Long productId) {

        log.info("Accessing product detail for company: {}, productId: {}", companyCode, productId);

        // 기업 정보 조회
        Mono<Company> companyMono = companyService.getActiveCompanyByCode(companyCode);

        return companyMono.flatMap(company ->
                // 제품 조회 (숨김 제외)
                productService.getProductByIdAndIsHidden(productId, false)
                        .map(product -> Rendering.view("public/product/detail")
                                .modelAttribute("company", company)
                                .modelAttribute("product", product)
                                .build())
                        .switchIfEmpty(Mono.just(Rendering.view("public/product/detail")
                                .modelAttribute("company", company)
                                .modelAttribute("product", null)
                                .modelAttribute("message", "해당 제품을 찾을 수 없습니다.")
                                .build()))
        ).contextWrite(ctx -> TenantContextHolder.setCurrentTenant(ctx, companyCode));
    }
}
