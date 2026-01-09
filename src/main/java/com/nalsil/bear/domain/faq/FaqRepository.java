package com.nalsil.bear.domain.faq;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * FAQ 리포지토리
 * R2DBC 기반 리액티브 리포지토리
 */
@Repository
public interface FaqRepository extends R2dbcRepository<Faq, Long> {

    /**
     * 기업 ID로 FAQ 목록 조회 (숨김 제외, 표시 순서대로)
     *
     * @param companyId 기업 ID
     * @param isHidden 숨김 여부
     * @return FAQ 목록 (Flux)
     */
    Flux<Faq> findByCompanyIdAndIsHiddenOrderByDisplayOrderAsc(Long companyId, Boolean isHidden);

    /**
     * 기업 ID와 카테고리로 FAQ 목록 조회 (숨김 제외, 표시 순서대로)
     *
     * @param companyId 기업 ID
     * @param category 카테고리
     * @param isHidden 숨김 여부
     * @return FAQ 목록 (Flux)
     */
    Flux<Faq> findByCompanyIdAndCategoryAndIsHiddenOrderByDisplayOrderAsc(Long companyId, String category, Boolean isHidden);

    /**
     * 기업 ID로 모든 FAQ 목록 조회 (관리자용, 표시 순서대로)
     *
     * @param companyId 기업 ID
     * @return FAQ 목록 (Flux)
     */
    Flux<Faq> findByCompanyIdOrderByDisplayOrderAsc(Long companyId);

    /**
     * 기업 ID로 FAQ 개수 조회 (숨김 제외)
     *
     * @param companyId 기업 ID
     * @param isHidden 숨김 여부
     * @return FAQ 개수 (Mono<Long>)
     */
    Mono<Long> countByCompanyIdAndIsHidden(Long companyId, Boolean isHidden);
}
