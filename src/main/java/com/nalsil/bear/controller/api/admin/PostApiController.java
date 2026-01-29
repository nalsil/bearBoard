package com.nalsil.bear.controller.api.admin;

import com.nalsil.bear.domain.post.Post;
import com.nalsil.bear.dto.request.CreatePostRequest;
import com.nalsil.bear.dto.response.ApiResponse;
import com.nalsil.bear.dto.response.PagedResponse;
import com.nalsil.bear.dto.response.PostResponse;
import com.nalsil.bear.service.BoardService;
import com.nalsil.bear.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 게시글 관리 API 컨트롤러
 */
@Tag(name = "게시글 관리 API", description = "게시글 CRUD API")
@Slf4j
@RestController
@RequestMapping("/api/admin/posts")
@RequiredArgsConstructor
@SecurityRequirement(name = "JWT Cookie")
public class PostApiController {

    private final PostService postService;
    private final BoardService boardService;

    /**
     * 게시글 목록 조회 (게시판별)
     */
    @Operation(summary = "게시글 목록 조회", description = "특정 게시판의 게시글 목록을 조회합니다.")
    @GetMapping
    public Mono<ResponseEntity<ApiResponse<PagedResponse<PostResponse>>>> getPosts(
            @Parameter(description = "게시판 ID", required = true) @RequestParam Long boardId,
            @Parameter(description = "페이지 번호 (0부터 시작)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "20") int size,
            ServerWebExchange exchange) {

        Long companyId = (Long) exchange.getAttributes().get("companyId");

        if (companyId == null) {
            return Mono.just(ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("기업이 선택되지 않았습니다.", "NO_COMPANY_SELECTED")));
        }

        // 게시판 소유권 확인
        return boardService.getBoardById(boardId)
                .filter(board -> board.getCompanyId().equals(companyId))
                .flatMap(board -> postService.getPostsByBoardIdIncludingHidden(boardId, page, size)
                        .map(this::toPostResponse)
                        .collectList()
                        .zipWith(postService.countVisiblePostsByBoardId(boardId).defaultIfEmpty(0L))
                        .map(tuple -> {
                            List<PostResponse> posts = tuple.getT1();
                            long totalElements = tuple.getT2();
                            PagedResponse<PostResponse> pagedResponse = PagedResponse.of(posts, page, size, totalElements);
                            return ResponseEntity.ok(ApiResponse.success(pagedResponse));
                        }))
                .switchIfEmpty(Mono.just(ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("게시판을 찾을 수 없습니다.", "BOARD_NOT_FOUND"))))
                .onErrorResume(e -> {
                    log.error("게시글 목록 조회 실패: boardId={}", boardId, e);
                    return Mono.just(ResponseEntity
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(ApiResponse.error("게시글 목록 조회에 실패했습니다.")));
                });
    }

    /**
     * 게시글 상세 조회
     */
    @Operation(summary = "게시글 상세 조회", description = "게시글 상세 정보를 조회합니다.")
    @GetMapping("/{id}")
    public Mono<ResponseEntity<ApiResponse<PostResponse>>> getPost(
            @PathVariable Long id,
            ServerWebExchange exchange) {

        Long companyId = (Long) exchange.getAttributes().get("companyId");

        if (companyId == null) {
            return Mono.just(ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("기업이 선택되지 않았습니다.", "NO_COMPANY_SELECTED")));
        }

        return postService.getPostById(id)
                .flatMap(post -> boardService.getBoardById(post.getBoardId())
                        .filter(board -> board.getCompanyId().equals(companyId))
                        .map(board -> post))
                .map(this::toPostResponse)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response)))
                .switchIfEmpty(Mono.just(ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("게시글을 찾을 수 없습니다.", "POST_NOT_FOUND"))));
    }

    /**
     * 게시글 생성
     */
    @Operation(summary = "게시글 생성", description = "새 게시글을 생성합니다.")
    @PostMapping
    public Mono<ResponseEntity<ApiResponse<PostResponse>>> createPost(
            @RequestBody CreatePostRequest request,
            ServerWebExchange exchange) {

        Long companyId = (Long) exchange.getAttributes().get("companyId");
        String username = (String) exchange.getAttributes().get("username");

        if (companyId == null) {
            return Mono.just(ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("기업이 선택되지 않았습니다.", "NO_COMPANY_SELECTED")));
        }

        // 게시판 소유권 확인
        return boardService.getBoardById(request.getBoardId())
                .filter(board -> board.getCompanyId().equals(companyId))
                .flatMap(board -> {
                    Post post = Post.builder()
                            .boardId(request.getBoardId())
                            .title(request.getTitle())
                            .content(request.getContent())
                            .author(username)
                            .viewCount(0)
                            .isHidden(request.getIsHidden() != null ? request.getIsHidden() : false)
                            .filePath(request.getFilePath())
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build();

                    return postService.createPost(post);
                })
                .map(this::toPostResponse)
                .map(response -> ResponseEntity
                        .status(HttpStatus.CREATED)
                        .body(ApiResponse.success("게시글이 생성되었습니다.", response)))
                .switchIfEmpty(Mono.just(ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("게시판을 찾을 수 없습니다.", "BOARD_NOT_FOUND"))))
                .onErrorResume(e -> {
                    log.error("게시글 생성 실패", e);
                    return Mono.just(ResponseEntity
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(ApiResponse.error("게시글 생성에 실패했습니다.")));
                });
    }

    /**
     * 게시글 수정
     */
    @Operation(summary = "게시글 수정", description = "게시글 정보를 수정합니다.")
    @PutMapping("/{id}")
    public Mono<ResponseEntity<ApiResponse<PostResponse>>> updatePost(
            @PathVariable Long id,
            @RequestBody CreatePostRequest request,
            ServerWebExchange exchange) {

        Long companyId = (Long) exchange.getAttributes().get("companyId");

        if (companyId == null) {
            return Mono.just(ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("기업이 선택되지 않았습니다.", "NO_COMPANY_SELECTED")));
        }

        return postService.getPostById(id)
                .flatMap(post -> boardService.getBoardById(post.getBoardId())
                        .filter(board -> board.getCompanyId().equals(companyId))
                        .map(board -> post))
                .flatMap(existingPost -> {
                    existingPost.setTitle(request.getTitle());
                    existingPost.setContent(request.getContent());
                    if (request.getIsHidden() != null) {
                        existingPost.setIsHidden(request.getIsHidden());
                    }
                    existingPost.setFilePath(request.getFilePath());
                    existingPost.setUpdatedAt(LocalDateTime.now());

                    return postService.updatePost(existingPost);
                })
                .map(this::toPostResponse)
                .map(response -> ResponseEntity.ok(ApiResponse.success("게시글이 수정되었습니다.", response)))
                .switchIfEmpty(Mono.just(ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("게시글을 찾을 수 없습니다.", "POST_NOT_FOUND"))))
                .onErrorResume(e -> {
                    log.error("게시글 수정 실패: id={}", id, e);
                    return Mono.just(ResponseEntity
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(ApiResponse.error("게시글 수정에 실패했습니다.")));
                });
    }

    /**
     * 게시글 삭제
     */
    @Operation(summary = "게시글 삭제", description = "게시글을 삭제합니다.")
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<ApiResponse<Void>>> deletePost(
            @PathVariable Long id,
            ServerWebExchange exchange) {

        Long companyId = (Long) exchange.getAttributes().get("companyId");

        if (companyId == null) {
            return Mono.just(ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("기업이 선택되지 않았습니다.", "NO_COMPANY_SELECTED")));
        }

        return postService.getPostById(id)
                .flatMap(post -> boardService.getBoardById(post.getBoardId())
                        .filter(board -> board.getCompanyId().equals(companyId))
                        .map(board -> post))
                .flatMap(post -> postService.deletePost(id).thenReturn(post))
                .map(post -> ResponseEntity.ok(ApiResponse.<Void>success("게시글이 삭제되었습니다.")))
                .switchIfEmpty(Mono.just(ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("게시글을 찾을 수 없습니다.", "POST_NOT_FOUND"))))
                .onErrorResume(e -> {
                    log.error("게시글 삭제 실패: id={}", id, e);
                    return Mono.just(ResponseEntity
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(ApiResponse.error("게시글 삭제에 실패했습니다.")));
                });
    }

    private PostResponse toPostResponse(Post post) {
        return PostResponse.builder()
                .id(post.getId())
                .boardId(post.getBoardId())
                .title(post.getTitle())
                .content(post.getContent())
                .viewCount(post.getViewCount())
                .isHidden(post.getIsHidden())
                .filePath(post.getFilePath())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }
}
