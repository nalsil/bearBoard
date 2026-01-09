package com.nalsil.bear.controller.admin;

import com.nalsil.bear.domain.product.Product;
import com.nalsil.bear.mapper.ProductMapper;
import com.nalsil.bear.service.AdminService;
import com.nalsil.bear.service.CompanyService;
import com.nalsil.bear.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 관리자 상품 컨트롤러
 *
 * 관리자가 상품을 등록, 수정, 삭제, 숨김 처리할 수 있습니다.
 */
@Slf4j
@Controller
@RequestMapping("/admin/products")
@RequiredArgsConstructor
public class AdminProductController {

    private final ProductService productService;
    private final AdminService adminService;
    private final ProductMapper productMapper;
    private final CompanyService companyService;

    /**
     * 상품 목록
     *
     * @param exchange ServerWebExchange
     * @param model 모델
     * @return 상품 목록 템플릿
     */
    @GetMapping
    public Mono<String> list(ServerWebExchange exchange, Model model) {
        Long adminCompanyId = (Long) exchange.getAttributes().get("companyId");
        log.info("관리자 상품 목록 조회: companyId={}", adminCompanyId);

        return companyService.getCompanyById(adminCompanyId)
                .flatMap(company -> {
                    model.addAttribute("company", company);

                    return productService.getProductsByCompanyId(adminCompanyId)
                            .collectList()
                            .doOnNext(products -> model.addAttribute("products", products));
                })
                .thenReturn("admin/product/list");
    }

    /**
     * 상품 등록 폼
     *
     * @param exchange ServerWebExchange
     * @param model 모델
     * @return 상품 등록 폼 템플릿
     */
    @GetMapping("/new")
    public Mono<String> newProductForm(ServerWebExchange exchange, Model model) {
        Long adminCompanyId = (Long) exchange.getAttributes().get("companyId");

        return companyService.getCompanyById(adminCompanyId)
                .doOnNext(company -> {
                    model.addAttribute("company", company);
                    model.addAttribute("product", new Product());
                })
                .thenReturn("admin/product/form");
    }

    /**
     * 상품 등록 처리
     *
     * @param product 상품 엔티티
     * @param exchange ServerWebExchange
     * @return 상품 목록으로 리다이렉트
     */
    @PostMapping
    public Mono<String> createProduct(@ModelAttribute Product product, ServerWebExchange exchange) {
        Long adminCompanyId = (Long) exchange.getAttributes().get("companyId");
        log.info("상품 등록: companyId={}, name={}", adminCompanyId, product.getName());

        // MapStruct를 사용하여 엔티티 생성 준비
        Product preparedProduct = productMapper.prepareForCreate(product);
        preparedProduct.setCompanyId(adminCompanyId);

        return productService.createProduct(preparedProduct)
                .thenReturn("redirect:/admin/products?success=created");
    }

    /**
     * 상품 수정 폼
     *
     * @param id 상품 ID
     * @param exchange ServerWebExchange
     * @param model 모델
     * @return 상품 수정 폼 템플릿
     */
    @GetMapping("/{id}/edit")
    public Mono<String> editProductForm(
            @PathVariable Long id,
            ServerWebExchange exchange,
            Model model) {

        Long adminCompanyId = (Long) exchange.getAttributes().get("companyId");
        log.info("상품 수정 폼: id={}", id);

        return companyService.getCompanyById(adminCompanyId)
                .flatMap(company -> {
                    model.addAttribute("company", company);

                    return productService.getProductById(id)
                            .flatMap(product -> {
                                // 권한 확인
                                if (!product.getCompanyId().equals(adminCompanyId)) {
                                    return Mono.error(new IllegalAccessException("접근 권한이 없습니다."));
                                }

                                model.addAttribute("product", product);
                                return Mono.just("admin/product/form");
                            });
                })
                .onErrorResume(IllegalAccessException.class, e -> {
                    return Mono.just("redirect:/admin/products?error=access_denied");
                });
    }

    /**
     * 상품 수정 처리
     *
     * @param id 상품 ID
     * @param product 상품 엔티티
     * @param exchange ServerWebExchange
     * @return 상품 목록으로 리다이렉트
     */
    @PostMapping("/{id}")
    public Mono<String> updateProduct(
            @PathVariable Long id,
            @ModelAttribute Product product,
            ServerWebExchange exchange) {

        Long adminCompanyId = (Long) exchange.getAttributes().get("companyId");
        log.info("상품 수정: id={}", id);

        return productService.getProductById(id)
                .flatMap(existingProduct -> {
                    // 권한 확인
                    if (!existingProduct.getCompanyId().equals(adminCompanyId)) {
                        return Mono.error(new IllegalAccessException("접근 권한이 없습니다."));
                    }

                    // MapStruct를 사용하여 엔티티 업데이트
                    productMapper.updateProduct(product, existingProduct);

                    return productService.updateProduct(existingProduct);
                })
                .thenReturn("redirect:/admin/products?success=updated")
                .onErrorResume(IllegalAccessException.class, e -> {
                    return Mono.just("redirect:/admin/products?error=access_denied");
                });
    }

    /**
     * 상품 삭제
     *
     * @param id 상품 ID
     * @param exchange ServerWebExchange
     * @return 상품 목록으로 리다이렉트
     */
    @PostMapping("/{id}/delete")
    public Mono<String> deleteProduct(@PathVariable Long id, ServerWebExchange exchange) {
        Long adminCompanyId = (Long) exchange.getAttributes().get("companyId");
        log.info("상품 삭제: id={}", id);

        return productService.getProductById(id)
                .flatMap(product -> {
                    // 권한 확인
                    if (!product.getCompanyId().equals(adminCompanyId)) {
                        return Mono.error(new IllegalAccessException("접근 권한이 없습니다."));
                    }

                    return productService.deleteProduct(id);
                })
                .thenReturn("redirect:/admin/products?success=deleted")
                .onErrorResume(IllegalAccessException.class, e -> {
                    return Mono.just("redirect:/admin/products?error=access_denied");
                });
    }

    /**
     * 상품 숨김/표시 토글
     *
     * @param id 상품 ID
     * @param exchange ServerWebExchange
     * @return 상품 목록으로 리다이렉트
     */
    @PostMapping("/{id}/toggle-hidden")
    public Mono<String> toggleHidden(@PathVariable Long id, ServerWebExchange exchange) {
        Long adminCompanyId = (Long) exchange.getAttributes().get("companyId");
        log.info("상품 숨김 토글: id={}", id);

        return productService.getProductById(id)
                .flatMap(product -> {
                    // 권한 확인
                    if (!product.getCompanyId().equals(adminCompanyId)) {
                        return Mono.error(new IllegalAccessException("접근 권한이 없습니다."));
                    }

                    // 숨김 상태 토글
                    Product updateData = Product.builder()
                            .isHidden(!product.getIsHidden())
                            .build();
                    productMapper.updateProduct(updateData, product);

                    return productService.updateProduct(product);
                })
                .thenReturn("redirect:/admin/products")
                .onErrorResume(IllegalAccessException.class, e -> {
                    return Mono.just("redirect:/admin/products?error=access_denied");
                });
    }
}
