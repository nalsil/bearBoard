package com.nalsil.bear.service;

import com.nalsil.bear.domain.post.Post;
import com.nalsil.bear.domain.post.PostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * PostService 단위 테스트
 * 게시글 조회 비즈니스 로직 검증
 */
@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @InjectMocks
    private PostService postService;

    private Post testPost1;
    private Post testPost2;

    @BeforeEach
    void setUp() {
        // 테스트용 게시글 데이터 준비
        testPost1 = Post.builder()
                .id(1L)
                .boardId(1L)
                .title("테스트 게시글 1")
                .content("테스트 내용 1")
                .author("작성자1")
                .viewCount(10)
                .isHidden(false)
                .build();

        testPost2 = Post.builder()
                .id(2L)
                .boardId(1L)
                .title("테스트 게시글 2")
                .content("테스트 내용 2")
                .author("작성자2")
                .viewCount(20)
                .isHidden(false)
                .build();
    }

    @Test
    @DisplayName("게시판 ID로 게시글 목록 조회 (숨김 제외) - 성공")
    void testGetVisiblePostsByBoardId_Success() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        when(postRepository.findByBoardIdAndIsHiddenOrderByCreatedAtDesc(1L, false, pageable))
                .thenReturn(Flux.just(testPost1, testPost2));

        // When
        Flux<Post> result = postService.getVisiblePostsByBoardId(1L, pageable);

        // Then
        StepVerifier.create(result)
                .expectNextMatches(post ->
                        post.getId().equals(1L) &&
                        post.getTitle().equals("테스트 게시글 1") &&
                        post.getIsHidden().equals(false)
                )
                .expectNextMatches(post ->
                        post.getId().equals(2L) &&
                        post.getTitle().equals("테스트 게시글 2")
                )
                .verifyComplete();
    }

    @Test
    @DisplayName("게시판 ID로 게시글 목록 조회 - 빈 결과")
    void testGetVisiblePostsByBoardId_Empty() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        when(postRepository.findByBoardIdAndIsHiddenOrderByCreatedAtDesc(anyLong(), anyBoolean(), any(Pageable.class)))
                .thenReturn(Flux.empty());

        // When
        Flux<Post> result = postService.getVisiblePostsByBoardId(999L, pageable);

        // Then
        StepVerifier.create(result)
                .verifyComplete();
    }

    @Test
    @DisplayName("게시글 ID와 숨김 여부로 조회 - 성공")
    void testGetPostByIdAndIsHidden_Success() {
        // Given
        when(postRepository.findByIdAndIsHidden(1L, false))
                .thenReturn(Mono.just(testPost1));

        // When
        Mono<Post> result = postService.getPostByIdAndIsHidden(1L, false);

        // Then
        StepVerifier.create(result)
                .expectNextMatches(post ->
                        post.getId().equals(1L) &&
                        post.getTitle().equals("테스트 게시글 1")
                )
                .verifyComplete();
    }

    @Test
    @DisplayName("게시글 ID와 숨김 여부로 조회 - 존재하지 않음")
    void testGetPostByIdAndIsHidden_NotFound() {
        // Given
        when(postRepository.findByIdAndIsHidden(anyLong(), anyBoolean()))
                .thenReturn(Mono.empty());

        // When
        Mono<Post> result = postService.getPostByIdAndIsHidden(999L, false);

        // Then
        StepVerifier.create(result)
                .verifyComplete();
    }

    @Test
    @DisplayName("조회수 증가 - 성공")
    void testIncrementViewCount_Success() {
        // Given
        when(postRepository.incrementViewCount(1L))
                .thenReturn(Mono.just(1));

        // When
        Mono<Void> result = postService.incrementViewCount(1L);

        // Then
        StepVerifier.create(result)
                .verifyComplete();

        verify(postRepository).incrementViewCount(1L);
    }

    @Test
    @DisplayName("게시판 ID로 게시글 개수 조회 (숨김 제외) - 성공")
    void testCountVisiblePostsByBoardId_Success() {
        // Given
        when(postRepository.countByBoardIdAndIsHidden(1L, false))
                .thenReturn(Mono.just(2L));

        // When
        Mono<Long> result = postService.countVisiblePostsByBoardId(1L);

        // Then
        StepVerifier.create(result)
                .expectNext(2L)
                .verifyComplete();
    }

    @Test
    @DisplayName("게시판 ID로 게시글 개수 조회 - 0개")
    void testCountVisiblePostsByBoardId_Zero() {
        // Given
        when(postRepository.countByBoardIdAndIsHidden(anyLong(), anyBoolean()))
                .thenReturn(Mono.just(0L));

        // When
        Mono<Long> result = postService.countVisiblePostsByBoardId(999L);

        // Then
        StepVerifier.create(result)
                .expectNext(0L)
                .verifyComplete();
    }
}
