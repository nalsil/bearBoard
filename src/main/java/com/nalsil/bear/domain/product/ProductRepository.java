package com.nalsil.bear.domain.product;

import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Product 리포지토리
 * R2DBC 기반 리액티브 리포지토리
 */
@Repository
public interface ProductRepository extends R2dbcRepository<Product, Long> {

    /**
     * 기업 ID로 상품 목록 조회 (숨김 제외, 표시 순서대로)
     *
     * @param companyId 기업 ID
     * @param isHidden 숨김 여부
     * @param pageable 페이징 정보
     * @return 상품 목록 (Flux)
     */
    Flux<Product> findByCompanyIdAndIsHiddenOrderByDisplayOrderAsc(Long companyId, Boolean isHidden, Pageable pageable);

    /**
     * 기업 ID와 카테고리로 상품 목록 조회 (숨김 제외, 표시 순서대로)
     *
     * @param companyId 기업 ID
     * @param category 카테고리
     * @param isHidden 숨김 여부
     * @param pageable 페이징 정보
     * @return 상품 목록 (Flux)
     */
    Flux<Product> findByCompanyIdAndCategoryAndIsHiddenOrderByDisplayOrderAsc(Long companyId, String category, Boolean isHidden, Pageable pageable);

    /**
     * 기업 ID로 모든 상품 목록 조회 (관리자용, 표시 순서대로)
     *
     * @param companyId 기업 ID
     * @param pageable 페이징 정보
     * @return 상품 목록 (Flux)
     */
    Flux<Product> findByCompanyIdOrderByDisplayOrderAsc(Long companyId, Pageable pageable);

    /**
     * 기업 ID로 상품 개수 조회 (숨김 제외)
     *
     * @param companyId 기업 ID
     * @param isHidden 숨김 여부
     * @return 상품 개수 (Mono<Long>)
     */
    Mono<Long> countByCompanyIdAndIsHidden(Long companyId, Boolean isHidden);

    /**
     * 기업 ID와 카테고리로 상품 개수 조회 (숨김 제외)
     *
     * @param companyId 기업 ID
     * @param category 카테고리
     * @param isHidden 숨김 여부
     * @return 상품 개수 (Mono<Long>)
     */
    Mono<Long> countByCompanyIdAndCategoryAndIsHidden(Long companyId, String category, Boolean isHidden);

    /**
     * 기업 ID로 카테고리 목록 조회 (중복 제거)
     *
     * @param companyId 기업 ID
     * @return 카테고리 목록 (Flux<String>)
     */
    @Query("SELECT DISTINCT category FROM product WHERE company_id = :companyId AND category IS NOT NULL ORDER BY category")
    Flux<String> findDistinctCategoriesByCompanyId(Long companyId);

    /**
     * 상품 ID와 숨김 여부로 조회
     *
     * @param id 상품 ID
     * @param isHidden 숨김 여부
     * @return 상품 정보 (Mono<Product>)
     */
    Mono<Product> findByIdAndIsHidden(Long id, Boolean isHidden);
}
