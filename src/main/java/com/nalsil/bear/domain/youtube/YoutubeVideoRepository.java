package com.nalsil.bear.domain.youtube;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * YoutubeVideo 리포지토리
 * R2DBC 기반 리액티브 리포지토리
 */
@Repository
public interface YoutubeVideoRepository extends R2dbcRepository<YoutubeVideo, Long> {

    /**
     * 기업 ID로 영상 목록 조회 (숨김 제외, 표시 순서대로)
     *
     * @param companyId 기업 ID
     * @param isHidden 숨김 여부
     * @return 영상 목록 (Flux)
     */
    Flux<YoutubeVideo> findByCompanyIdAndIsHiddenOrderByDisplayOrderAsc(Long companyId, Boolean isHidden);

    /**
     * 기업 ID로 모든 영상 목록 조회 (관리자용, 표시 순서대로)
     *
     * @param companyId 기업 ID
     * @return 영상 목록 (Flux)
     */
    Flux<YoutubeVideo> findByCompanyIdOrderByDisplayOrderAsc(Long companyId);

    /**
     * 기업 ID로 영상 개수 조회 (숨김 제외)
     *
     * @param companyId 기업 ID
     * @param isHidden 숨김 여부
     * @return 영상 개수 (Mono<Long>)
     */
    Mono<Long> countByCompanyIdAndIsHidden(Long companyId, Boolean isHidden);
}
