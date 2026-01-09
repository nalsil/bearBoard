package com.nalsil.bear.domain.admin;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Admin 리포지토리
 * R2DBC 기반 리액티브 리포지토리
 */
@Repository
public interface AdminRepository extends R2dbcRepository<Admin, Long> {

    /**
     * 사용자명으로 관리자 조회
     *
     * @param username 사용자명
     * @return 관리자 정보 (Mono)
     */
    Mono<Admin> findByUsername(String username);

    /**
     * 기업 ID로 관리자 목록 조회
     *
     * @param companyId 기업 ID
     * @return 관리자 목록 (Flux)
     */
    Flux<Admin> findByCompanyId(Long companyId);

    /**
     * 역할로 관리자 목록 조회
     *
     * @param role 역할 (ADMIN, SUPER_ADMIN)
     * @return 관리자 목록 (Flux)
     */
    Flux<Admin> findByRole(String role);

    /**
     * 사용자명 존재 여부 확인
     *
     * @param username 사용자명
     * @return 존재 여부 (Mono<Boolean>)
     */
    Mono<Boolean> existsByUsername(String username);

    /**
     * 마지막 로그인 일시 업데이트
     *
     * @param id 관리자 ID
     * @param lastLoginAt 마지막 로그인 일시
     * @return 업데이트된 행 수 (Mono<Integer>)
     */
    @Query("UPDATE admin SET last_login_at = :lastLoginAt WHERE id = :id")
    Mono<Integer> updateLastLoginAt(Long id, java.time.LocalDateTime lastLoginAt);
}
