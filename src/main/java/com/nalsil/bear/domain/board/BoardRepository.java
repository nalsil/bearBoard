package com.nalsil.bear.domain.board;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Board 리포지토리
 * R2DBC 기반 리액티브 리포지토리
 */
@Repository
public interface BoardRepository extends R2dbcRepository<Board, Long> {

    /**
     * 기업 ID로 게시판 목록 조회
     *
     * @param companyId 기업 ID
     * @return 게시판 목록 (Flux)
     */
    Flux<Board> findByCompanyId(Long companyId);

    /**
     * 기업 ID와 게시판 타입으로 게시판 조회
     *
     * @param companyId 기업 ID
     * @param type 게시판 타입
     * @return 게시판 정보 (Mono)
     */
    Mono<Board> findByCompanyIdAndType(Long companyId, String type);

    /**
     * 기업 ID와 게시판 이름으로 게시판 조회
     *
     * @param companyId 기업 ID
     * @param name 게시판 이름
     * @return 게시판 정보 (Mono)
     */
    Mono<Board> findByCompanyIdAndName(Long companyId, String name);
}
