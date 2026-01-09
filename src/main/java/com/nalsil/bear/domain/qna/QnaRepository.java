package com.nalsil.bear.domain.qna;

import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * QnA 리포지토리
 * R2DBC 기반 리액티브 리포지토리
 */
@Repository
public interface QnaRepository extends R2dbcRepository<Qna, Long> {

    /**
     * 기업 ID로 QnA 목록 조회 (숨김 제외, 최신순)
     *
     * @param companyId 기업 ID
     * @param isHidden 숨김 여부
     * @param pageable 페이징 정보
     * @return QnA 목록 (Flux)
     */
    Flux<Qna> findByCompanyIdAndIsHiddenOrderByCreatedAtDesc(Long companyId, Boolean isHidden, Pageable pageable);

    /**
     * 기업 ID와 답변 상태로 QnA 목록 조회 (최신순)
     *
     * @param companyId 기업 ID
     * @param isAnswered 답변 완료 여부
     * @param pageable 페이징 정보
     * @return QnA 목록 (Flux)
     */
    Flux<Qna> findByCompanyIdAndIsAnsweredOrderByCreatedAtDesc(Long companyId, Boolean isAnswered, Pageable pageable);

    /**
     * 질문자 이메일로 QnA 목록 조회 (최신순)
     *
     * @param askerEmail 질문자 이메일
     * @param pageable 페이징 정보
     * @return QnA 목록 (Flux)
     */
    Flux<Qna> findByAskerEmailOrderByCreatedAtDesc(String askerEmail, Pageable pageable);

    /**
     * 기업 ID로 QnA 개수 조회 (숨김 제외)
     *
     * @param companyId 기업 ID
     * @param isHidden 숨김 여부
     * @return QnA 개수 (Mono<Long>)
     */
    Mono<Long> countByCompanyIdAndIsHidden(Long companyId, Boolean isHidden);

    /**
     * 기업 ID와 답변 상태로 QnA 개수 조회
     *
     * @param companyId 기업 ID
     * @param isAnswered 답변 완료 여부
     * @return QnA 개수 (Mono<Long>)
     */
    Mono<Long> countByCompanyIdAndIsAnswered(Long companyId, Boolean isAnswered);
}
