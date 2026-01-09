package com.nalsil.bear.repository;

import com.nalsil.bear.domain.post.Post;
import com.nalsil.bear.domain.post.PostRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * PostRepository 계약 테스트
 * R2DBC 리포지토리 기본 동작 검증
 */
@DataR2dbcTest
@ActiveProfiles("test")
class PostRepositoryTest {

    @Autowired
    private PostRepository postRepository;

    @Test
    @DisplayName("게시판 ID로 게시글 목록 조회 (숨김 제외) - 성공")
    void testFindByBoardIdAndIsHiddenOrderByCreatedAtDesc_Success() {
        // Given: 데이터베이스에 게시글 존재 (data.sql)
        Long boardId = 1L; // Company A 공지사항 게시판
        Pageable pageable = PageRequest.of(0, 10);

        // When: 게시판 ID로 숨기지 않은 게시글 조회
        Flux<Post> postsFlux = postRepository.findByBoardIdAndIsHiddenOrderByCreatedAtDesc(
                boardId, false, pageable);

        // Then: 게시글 목록이 조회되고 최신순 정렬됨
        StepVerifier.create(postsFlux)
                .expectNextMatches(post ->
                        post.getBoardId().equals(boardId) &&
                        post.getIsHidden().equals(false)
                )
                .expectNextCount(2) // 최소 3개 이상 게시글이 있어야 함
                .verifyComplete();
    }

    @Test
    @DisplayName("게시판 ID로 게시글 개수 조회 (숨김 제외) - 성공")
    void testCountByBoardIdAndIsHidden_Success() {
        // Given: 데이터베이스에 게시글 존재
        Long boardId = 1L; // Company A 공지사항 게시판

        // When: 게시판 ID로 숨기지 않은 게시글 개수 조회
        Mono<Long> countMono = postRepository.countByBoardIdAndIsHidden(boardId, false);

        // Then: 개수가 0보다 큼
        StepVerifier.create(countMono)
                .expectNextMatches(count -> count >= 3) // data.sql에서 3개 삽입
                .verifyComplete();
    }

    @Test
    @DisplayName("게시글 ID와 숨김 여부로 조회 - 성공")
    void testFindByIdAndIsHidden_Success() {
        // Given: 먼저 게시글 ID 확인
        Long boardId = 1L;
        Pageable pageable = PageRequest.of(0, 1);

        Mono<Long> postIdMono = postRepository
                .findByBoardIdAndIsHiddenOrderByCreatedAtDesc(boardId, false, pageable)
                .next()
                .map(Post::getId);

        // When & Then: ID로 조회 후 검증
        StepVerifier.create(
                postIdMono.flatMap(id -> postRepository.findByIdAndIsHidden(id, false))
        )
                .expectNextMatches(post ->
                        post.getBoardId().equals(boardId) &&
                        post.getIsHidden().equals(false)
                )
                .verifyComplete();
    }

    @Test
    @DisplayName("조회수 증가 - 성공")
    void testIncrementViewCount_Success() {
        // Given: 게시글 조회 및 초기 조회수 확인
        Long boardId = 1L;
        Pageable pageable = PageRequest.of(0, 1);

        Mono<Post> initialPostMono = postRepository
                .findByBoardIdAndIsHiddenOrderByCreatedAtDesc(boardId, false, pageable)
                .next();

        // When: 조회수 증가 실행
        Mono<Integer> incrementMono = initialPostMono
                .flatMap(post -> postRepository.incrementViewCount(post.getId())
                        .then(postRepository.findById(post.getId()))
                        .map(updatedPost -> updatedPost.getViewCount() - post.getViewCount())
                );

        // Then: 조회수가 1 증가함
        StepVerifier.create(incrementMono)
                .expectNext(1)
                .verifyComplete();
    }

    @Test
    @DisplayName("게시글 저장 - 성공")
    void testSave_Success() {
        // Given: 새 게시글 정보
        Post newPost = Post.builder()
                .boardId(1L)
                .title("테스트 게시글")
                .content("테스트 내용입니다.")
                .author("테스터")
                .viewCount(0)
                .isHidden(false)
                .build();

        // When: 게시글 저장
        Mono<Post> savedMono = postRepository.save(newPost);

        // Then: 저장된 게시글 정보 확인
        StepVerifier.create(savedMono)
                .expectNextMatches(post ->
                        post.getId() != null &&
                        post.getTitle().equals("테스트 게시글") &&
                        post.getAuthor().equals("테스터") &&
                        post.getViewCount().equals(0)
                )
                .verifyComplete();
    }

    @Test
    @DisplayName("게시판 ID로 전체 게시글 조회 (숨김 포함) - 성공")
    void testFindByBoardIdOrderByCreatedAtDesc_Success() {
        // Given: 데이터베이스에 게시글 존재
        Long boardId = 1L;
        Pageable pageable = PageRequest.of(0, 10);

        // When: 게시판 ID로 모든 게시글 조회 (숨김 포함)
        Flux<Post> postsFlux = postRepository.findByBoardIdOrderByCreatedAtDesc(boardId, pageable);

        // Then: 게시글 목록이 조회됨 (숨김 포함)
        StepVerifier.create(postsFlux)
                .expectNextMatches(post -> post.getBoardId().equals(boardId))
                .expectNextCount(2) // 최소 3개 이상
                .verifyComplete();
    }
}
