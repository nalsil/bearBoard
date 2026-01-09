package com.nalsil.bear.service;

import com.nalsil.bear.domain.product.Product;
import com.nalsil.bear.domain.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * ProductService
 * 상품 조회 비즈니스 로직
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    /**
     * 기업 ID로 공개 상품 목록 조회 (숨김 제외, 표시 순서대로)
     *
     * @param companyId 기업 ID
     * @param pageable 페이징 정보
     * @return 상품 목록 (Flux<Product>)
     */
    public Flux<Product> getVisibleProductsByCompanyId(Long companyId, Pageable pageable) {
        log.debug("Fetching visible products for company ID: {}, page: {}", companyId, pageable.getPageNumber());

        return productRepository.findByCompanyIdAndIsHiddenOrderByDisplayOrderAsc(companyId, false, pageable)
                .doOnComplete(() -> log.debug("Fetched visible products for company ID: {}", companyId))
                .doOnError(error -> log.error("Failed to fetch visible products for company ID: {}", companyId, error));
    }

    /**
     * 기업 ID와 카테고리로 공개 상품 목록 조회 (숨김 제외, 표시 순서대로)
     *
     * @param companyId 기업 ID
     * @param category 카테고리
     * @param pageable 페이징 정보
     * @return 상품 목록 (Flux<Product>)
     */
    public Flux<Product> getVisibleProductsByCompanyIdAndCategory(Long companyId, String category, Pageable pageable) {
        log.debug("Fetching visible products for company ID: {}, category: {}, page: {}",
                companyId, category, pageable.getPageNumber());

        return productRepository.findByCompanyIdAndCategoryAndIsHiddenOrderByDisplayOrderAsc(
                        companyId, category, false, pageable)
                .doOnComplete(() -> log.debug("Fetched visible products for company ID: {}, category: {}",
                        companyId, category))
                .doOnError(error -> log.error("Failed to fetch visible products for company ID: {}, category: {}",
                        companyId, category, error));
    }

    /**
     * 기업 ID로 공개 상품 개수 조회 (숨김 제외)
     *
     * @param companyId 기업 ID
     * @return 상품 개수 (Mono<Long>)
     */
    public Mono<Long> countVisibleProductsByCompanyId(Long companyId) {
        log.debug("Counting visible products for company ID: {}", companyId);

        return productRepository.countByCompanyIdAndIsHidden(companyId, false)
                .doOnSuccess(count -> log.debug("Found {} visible products for company ID: {}", count, companyId))
                .doOnError(error -> log.error("Failed to count visible products for company ID: {}", companyId, error));
    }

    /**
     * 기업 ID와 카테고리로 공개 상품 개수 조회 (숨김 제외)
     *
     * @param companyId 기업 ID
     * @param category 카테고리
     * @return 상품 개수 (Mono<Long>)
     */
    public Mono<Long> countVisibleProductsByCompanyIdAndCategory(Long companyId, String category) {
        log.debug("Counting visible products for company ID: {}, category: {}", companyId, category);

        return productRepository.countByCompanyIdAndCategoryAndIsHidden(companyId, category, false)
                .doOnSuccess(count -> log.debug("Found {} visible products for company ID: {}, category: {}",
                        count, companyId, category))
                .doOnError(error -> log.error("Failed to count visible products for company ID: {}, category: {}",
                        companyId, category, error));
    }

    /**
     * 기업 ID로 카테고리 목록 조회
     *
     * @param companyId 기업 ID
     * @return 카테고리 목록 (Flux<String>)
     */
    public Flux<String> getCategoriesByCompanyId(Long companyId) {
        log.debug("Fetching categories for company ID: {}", companyId);

        return productRepository.findDistinctCategoriesByCompanyId(companyId)
                .doOnComplete(() -> log.debug("Fetched categories for company ID: {}", companyId))
                .doOnError(error -> log.error("Failed to fetch categories for company ID: {}", companyId, error));
    }

    /**
     * 상품 ID와 숨김 여부로 조회
     *
     * @param id 상품 ID
     * @param isHidden 숨김 여부
     * @return 상품 정보 (Mono<Product>)
     */
    public Mono<Product> getProductByIdAndIsHidden(Long id, Boolean isHidden) {
        log.debug("Fetching product by ID: {}, isHidden: {}", id, isHidden);

        return productRepository.findByIdAndIsHidden(id, isHidden)
                .doOnSuccess(product -> {
                    if (product != null) {
                        log.info("Found product: {} (ID: {})", product.getName(), id);
                    } else {
                        log.warn("Product not found for ID: {}, isHidden: {}", id, isHidden);
                    }
                })
                .doOnError(error -> log.error("Failed to fetch product by ID: {}, isHidden: {}", id, isHidden, error));
    }

    /**
     * 상품 ID로 조회
     *
     * @param id 상품 ID
     * @return 상품 정보 (Mono<Product>)
     */
    public Mono<Product> getProductById(Long id) {
        log.debug("Fetching product by ID: {}", id);

        return productRepository.findById(id)
                .doOnSuccess(product -> {
                    if (product != null) {
                        log.info("Found product: {} (ID: {})", product.getName(), id);
                    } else {
                        log.warn("Product not found for ID: {}", id);
                    }
                })
                .doOnError(error -> log.error("Failed to fetch product by ID: {}", id, error));
    }

    /**
     * 기업 ID로 전체 상품 목록 조회 (관리자용, 숨김 포함)
     *
     * @param companyId 기업 ID
     * @return 상품 목록
     */
    public Flux<Product> getProductsByCompanyId(Long companyId) {
        log.debug("Fetching all products for company ID: {}", companyId);
        return productRepository.findByCompanyIdOrderByDisplayOrderAsc(companyId, Pageable.unpaged());
    }

    /**
     * 상품 생성
     *
     * @param product 상품 엔티티
     * @return 생성된 상품
     */
    public Mono<Product> createProduct(Product product) {
        log.info("Creating product: name={}", product.getName());
        return productRepository.save(product);
    }

    /**
     * 상품 수정
     *
     * @param product 상품 엔티티
     * @return 수정된 상품
     */
    public Mono<Product> updateProduct(Product product) {
        log.info("Updating product: id={}, name={}", product.getId(), product.getName());
        return productRepository.save(product);
    }

    /**
     * 상품 삭제
     *
     * @param productId 상품 ID
     * @return 삭제 결과
     */
    public Mono<Void> deleteProduct(Long productId) {
        log.info("Deleting product: id={}", productId);
        return productRepository.deleteById(productId);
    }
}
