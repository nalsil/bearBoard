package com.nalsil.bear.controller.api.admin;

import com.nalsil.bear.domain.product.Product;
import com.nalsil.bear.dto.request.CreateProductRequest;
import com.nalsil.bear.dto.response.ApiResponse;
import com.nalsil.bear.dto.response.PagedResponse;
import com.nalsil.bear.dto.response.ProductResponse;
import com.nalsil.bear.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 상품 관리 API 컨트롤러
 */
@Tag(name = "상품 관리 API", description = "상품 CRUD API")
@Slf4j
@RestController
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
@SecurityRequirement(name = "JWT Cookie")
public class ProductApiController {

    private final ProductService productService;

    /**
     * 상품 목록 조회
     */
    @Operation(summary = "상품 목록 조회", description = "현재 기업의 상품 목록을 조회합니다.")
    @GetMapping
    public Mono<ResponseEntity<ApiResponse<PagedResponse<ProductResponse>>>> getProducts(
            @Parameter(description = "페이지 번호 (0부터 시작)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "20") int size,
            ServerWebExchange exchange) {

        Long companyId = (Long) exchange.getAttributes().get("companyId");

        if (companyId == null) {
            return Mono.just(ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("기업이 선택되지 않았습니다.", "NO_COMPANY_SELECTED")));
        }

        return productService.getProductsByCompanyId(companyId)
                .map(this::toProductResponse)
                .collectList()
                .zipWith(productService.countVisibleProductsByCompanyId(companyId).defaultIfEmpty(0L))
                .map(tuple -> {
                    List<ProductResponse> products = tuple.getT1();
                    // 페이지 처리
                    int start = page * size;
                    int end = Math.min(start + size, products.size());
                    List<ProductResponse> pagedProducts = start < products.size()
                            ? products.subList(start, end)
                            : List.of();

                    PagedResponse<ProductResponse> pagedResponse = PagedResponse.of(
                            pagedProducts, page, size, products.size());
                    return ResponseEntity.ok(ApiResponse.success(pagedResponse));
                })
                .onErrorResume(e -> {
                    log.error("상품 목록 조회 실패: companyId={}", companyId, e);
                    return Mono.just(ResponseEntity
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(ApiResponse.error("상품 목록 조회에 실패했습니다.")));
                });
    }

    /**
     * 상품 상세 조회
     */
    @Operation(summary = "상품 상세 조회", description = "상품 상세 정보를 조회합니다.")
    @GetMapping("/{id}")
    public Mono<ResponseEntity<ApiResponse<ProductResponse>>> getProduct(
            @PathVariable Long id,
            ServerWebExchange exchange) {

        Long companyId = (Long) exchange.getAttributes().get("companyId");

        if (companyId == null) {
            return Mono.just(ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("기업이 선택되지 않았습니다.", "NO_COMPANY_SELECTED")));
        }

        return productService.getProductById(id)
                .filter(product -> product.getCompanyId().equals(companyId))
                .map(this::toProductResponse)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response)))
                .switchIfEmpty(Mono.just(ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("상품을 찾을 수 없습니다.", "PRODUCT_NOT_FOUND"))));
    }

    /**
     * 상품 생성
     */
    @Operation(summary = "상품 생성", description = "새 상품을 생성합니다.")
    @PostMapping
    public Mono<ResponseEntity<ApiResponse<ProductResponse>>> createProduct(
            @RequestBody CreateProductRequest request,
            ServerWebExchange exchange) {

        Long companyId = (Long) exchange.getAttributes().get("companyId");

        if (companyId == null) {
            return Mono.just(ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("기업이 선택되지 않았습니다.", "NO_COMPANY_SELECTED")));
        }

        Product product = Product.builder()
                .companyId(companyId)
                .name(request.getName())
                .category(request.getCategory())
                .description(request.getDescription())
                .price(request.getPrice())
                .imageUrl(request.getImageUrl())
                .displayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0)
                .isHidden(request.getIsHidden() != null ? request.getIsHidden() : false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        return productService.createProduct(product)
                .map(this::toProductResponse)
                .map(response -> ResponseEntity
                        .status(HttpStatus.CREATED)
                        .body(ApiResponse.success("상품이 생성되었습니다.", response)))
                .onErrorResume(e -> {
                    log.error("상품 생성 실패: companyId={}", companyId, e);
                    return Mono.just(ResponseEntity
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(ApiResponse.error("상품 생성에 실패했습니다.")));
                });
    }

    /**
     * 상품 수정
     */
    @Operation(summary = "상품 수정", description = "상품 정보를 수정합니다.")
    @PutMapping("/{id}")
    public Mono<ResponseEntity<ApiResponse<ProductResponse>>> updateProduct(
            @PathVariable Long id,
            @RequestBody CreateProductRequest request,
            ServerWebExchange exchange) {

        Long companyId = (Long) exchange.getAttributes().get("companyId");

        if (companyId == null) {
            return Mono.just(ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("기업이 선택되지 않았습니다.", "NO_COMPANY_SELECTED")));
        }

        return productService.getProductById(id)
                .filter(product -> product.getCompanyId().equals(companyId))
                .flatMap(existingProduct -> {
                    existingProduct.setName(request.getName());
                    existingProduct.setCategory(request.getCategory());
                    existingProduct.setDescription(request.getDescription());
                    existingProduct.setPrice(request.getPrice());
                    existingProduct.setImageUrl(request.getImageUrl());
                    if (request.getDisplayOrder() != null) {
                        existingProduct.setDisplayOrder(request.getDisplayOrder());
                    }
                    if (request.getIsHidden() != null) {
                        existingProduct.setIsHidden(request.getIsHidden());
                    }
                    existingProduct.setUpdatedAt(LocalDateTime.now());

                    return productService.updateProduct(existingProduct);
                })
                .map(this::toProductResponse)
                .map(response -> ResponseEntity.ok(ApiResponse.success("상품이 수정되었습니다.", response)))
                .switchIfEmpty(Mono.just(ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("상품을 찾을 수 없습니다.", "PRODUCT_NOT_FOUND"))))
                .onErrorResume(e -> {
                    log.error("상품 수정 실패: id={}", id, e);
                    return Mono.just(ResponseEntity
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(ApiResponse.error("상품 수정에 실패했습니다.")));
                });
    }

    /**
     * 상품 삭제
     */
    @Operation(summary = "상품 삭제", description = "상품을 삭제합니다.")
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<ApiResponse<Void>>> deleteProduct(
            @PathVariable Long id,
            ServerWebExchange exchange) {

        Long companyId = (Long) exchange.getAttributes().get("companyId");

        if (companyId == null) {
            return Mono.just(ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("기업이 선택되지 않았습니다.", "NO_COMPANY_SELECTED")));
        }

        return productService.getProductById(id)
                .filter(product -> product.getCompanyId().equals(companyId))
                .flatMap(product -> productService.deleteProduct(id).thenReturn(product))
                .map(product -> ResponseEntity.ok(ApiResponse.<Void>success("상품이 삭제되었습니다.")))
                .switchIfEmpty(Mono.just(ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("상품을 찾을 수 없습니다.", "PRODUCT_NOT_FOUND"))))
                .onErrorResume(e -> {
                    log.error("상품 삭제 실패: id={}", id, e);
                    return Mono.just(ResponseEntity
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(ApiResponse.error("상품 삭제에 실패했습니다.")));
                });
    }

    /**
     * 카테고리 목록 조회
     */
    @Operation(summary = "카테고리 목록 조회", description = "현재 기업의 상품 카테고리 목록을 조회합니다.")
    @GetMapping("/categories")
    public Mono<ResponseEntity<ApiResponse<List<String>>>> getCategories(ServerWebExchange exchange) {
        Long companyId = (Long) exchange.getAttributes().get("companyId");

        if (companyId == null) {
            return Mono.just(ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("기업이 선택되지 않았습니다.", "NO_COMPANY_SELECTED")));
        }

        return productService.getCategoriesByCompanyId(companyId)
                .collectList()
                .map(categories -> ResponseEntity.ok(ApiResponse.success(categories)));
    }

    private ProductResponse toProductResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .companyId(product.getCompanyId())
                .name(product.getName())
                .category(product.getCategory())
                .description(product.getDescription())
                .price(product.getPrice())
                .imageUrl(product.getImageUrl())
                .displayOrder(product.getDisplayOrder())
                .isHidden(product.getIsHidden())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}
