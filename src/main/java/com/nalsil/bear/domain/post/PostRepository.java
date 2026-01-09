package com.nalsil.bear.domain.post;

import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Post 리포지토리
 * R2DBC 기반 리액티브 리포지토리
 */
@Repository
public interface PostRepository extends R2dbcRepository<Post, Long> {

    /**
     * 게시판 ID로 게시글 목록 조회 (숨김 제외, 최신순)
     *
     * @param boardId 게시판 ID
     * @param isHidden 숨김 여부
     * @param pageable 페이징 정보
     * @return 게시글 목록 (Flux)
     */
    Flux<Post> findByBoardIdAndIsHiddenOrderByCreatedAtDesc(Long boardId, Boolean isHidden, Pageable pageable);

    /**
     * 게시판 ID로 게시글 목록 조회 (모든 글 포함, 최신순)
     *
     * @param boardId 게시판 ID
     * @param pageable 페이징 정보
     * @return 게시글 목록 (Flux)
     */
    Flux<Post> findByBoardIdOrderByCreatedAtDesc(Long boardId, Pageable pageable);

    /**
     * 게시판 ID로 게시글 개수 조회 (숨김 제외)
     *
     * @param boardId 게시판 ID
     * @param isHidden 숨김 여부
     * @return 게시글 개수 (Mono<Long>)
     */
    Mono<Long> countByBoardIdAndIsHidden(Long boardId, Boolean isHidden);

    /**
     * 조회수 증가
     *
     * @param id 게시글 ID
     * @return 업데이트된 행 수 (Mono<Integer>)
     */
    @Query("UPDATE post SET view_count = view_count + 1 WHERE id = :id")
    Mono<Integer> incrementViewCount(Long id);

    /**
     * 게시글 ID와 숨김 여부로 게시글 조회
     *
     * @param id 게시글 ID
     * @param isHidden 숨김 여부
     * @return 게시글 정보 (Mono)
     */
    Mono<Post> findByIdAndIsHidden(Long id, Boolean isHidden);
}
