package com.nalsil.bear.domain.company;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

/**
 * Company 리포지토리
 * R2DBC 기반 리액티브 리포지토리
 */
@Repository
public interface CompanyRepository extends R2dbcRepository<Company, Long> {

    /**
     * 기업 코드로 기업 조회
     *
     * @param code 기업 코드
     * @return 기업 정보 (Mono)
     */
    Mono<Company> findByCode(String code);

    /**
     * 기업 코드로 활성화된 기업 조회
     *
     * @param code 기업 코드
     * @param isActive 활성화 여부
     * @return 기업 정보 (Mono)
     */
    Mono<Company> findByCodeAndIsActive(String code, Boolean isActive);

    /**
     * 기업 코드 존재 여부 확인
     *
     * @param code 기업 코드
     * @return 존재 여부 (Mono<Boolean>)
     */
    Mono<Boolean> existsByCode(String code);
}
