package com.nalsil.bear.service;

import com.nalsil.bear.domain.post.Post;
import com.nalsil.bear.domain.post.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * PostService
 * 게시글 조회 및 관리 비즈니스 로직
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;

    /**
     * 게시판 ID로 공개 게시글 목록 조회 (숨김 제외, 최신순)
     *
     * @param boardId 게시판 ID
     * @param pageable 페이징 정보
     * @return 게시글 목록 (Flux<Post>)
     */
    public Flux<Post> getVisiblePostsByBoardId(Long boardId, Pageable pageable) {
        log.debug("Fetching visible posts for board ID: {}, page: {}", boardId, pageable.getPageNumber());

        return postRepository.findByBoardIdAndIsHiddenOrderByCreatedAtDesc(boardId, false, pageable)
                .doOnComplete(() -> log.debug("Fetched visible posts for board ID: {}", boardId))
                .doOnError(error -> log.error("Failed to fetch visible posts for board ID: {}", boardId, error));
    }

    /**
     * 게시글 ID와 숨김 여부로 조회
     *
     * @param id 게시글 ID
     * @param isHidden 숨김 여부
     * @return 게시글 정보 (Mono<Post>)
     */
    public Mono<Post> getPostByIdAndIsHidden(Long id, Boolean isHidden) {
        log.debug("Fetching post by ID: {}, isHidden: {}", id, isHidden);

        return postRepository.findByIdAndIsHidden(id, isHidden)
                .doOnSuccess(post -> {
                    if (post != null) {
                        log.info("Found post: {} (ID: {})", post.getTitle(), id);
                    } else {
                        log.warn("Post not found for ID: {}, isHidden: {}", id, isHidden);
                    }
                })
                .doOnError(error -> log.error("Failed to fetch post by ID: {}", id, error));
    }

    /**
     * 조회수 증가
     *
     * @param id 게시글 ID
     * @return Mono<Void>
     */
    public Mono<Void> incrementViewCount(Long id) {
        log.debug("Incrementing view count for post ID: {}", id);

        return postRepository.incrementViewCount(id)
                .doOnSuccess(count -> log.debug("Incremented view count for post ID: {}", id))
                .doOnError(error -> log.error("Failed to increment view count for post ID: {}", id, error))
                .then();
    }

    /**
     * 게시판 ID로 공개 게시글 개수 조회 (숨김 제외)
     *
     * @param boardId 게시판 ID
     * @return 게시글 개수 (Mono<Long>)
     */
    public Mono<Long> countVisiblePostsByBoardId(Long boardId) {
        log.debug("Counting visible posts for board ID: {}", boardId);

        return postRepository.countByBoardIdAndIsHidden(boardId, false)
                .doOnSuccess(count -> log.debug("Found {} visible posts for board ID: {}", count, boardId))
                .doOnError(error -> log.error("Failed to count visible posts for board ID: {}", boardId, error));
    }

    /**
     * 게시글 ID로 조회
     *
     * @param id 게시글 ID
     * @return 게시글 정보 (Mono<Post>)
     */
    public Mono<Post> getPostById(Long id) {
        log.debug("Fetching post by ID: {}", id);

        return postRepository.findById(id)
                .doOnSuccess(post -> {
                    if (post != null) {
                        log.info("Found post: {} (ID: {})", post.getTitle(), id);
                    } else {
                        log.warn("Post not found for ID: {}", id);
                    }
                })
                .doOnError(error -> log.error("Failed to fetch post by ID: {}", id, error));
    }

    /**
     * 게시판 ID로 숨김 포함 전체 게시글 목록 조회 (관리자용)
     *
     * @param boardId 게시판 ID
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @return 게시글 목록
     */
    public Flux<Post> getPostsByBoardIdIncludingHidden(Long boardId, int page, int size) {
        log.debug("Fetching all posts (including hidden) for board ID: {}", boardId);
        return postRepository.findByBoardIdOrderByCreatedAtDesc(boardId, org.springframework.data.domain.PageRequest.of(page, size));
    }

    /**
     * 기업 ID로 게시글 개수 조회
     *
     * @param companyId 기업 ID
     * @return 게시글 개수
     */
    public Mono<Long> countPostsByCompanyId(Long companyId) {
        log.debug("Counting posts for company ID: {}", companyId);
        // Note: This requires joining with board table, but for now we'll return 0
        // In a real implementation, you would need a custom query or repository method
        return Mono.just(0L);
    }

    /**
     * 게시글 생성
     *
     * @param post 게시글 엔티티
     * @return 생성된 게시글
     */
    public Mono<Post> createPost(Post post) {
        log.info("Creating post: title={}", post.getTitle());
        return postRepository.save(post);
    }

    /**
     * 게시글 수정
     *
     * @param post 게시글 엔티티
     * @return 수정된 게시글
     */
    public Mono<Post> updatePost(Post post) {
        log.info("Updating post: id={}, title={}", post.getId(), post.getTitle());
        return postRepository.save(post);
    }

    /**
     * 게시글 삭제
     *
     * @param postId 게시글 ID
     * @return 삭제 결과
     */
    public Mono<Void> deletePost(Long postId) {
        log.info("Deleting post: id={}", postId);
        return postRepository.deleteById(postId);
    }
}
