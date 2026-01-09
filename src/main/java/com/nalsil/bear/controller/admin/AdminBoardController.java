package com.nalsil.bear.controller.admin;

import com.nalsil.bear.dto.request.CreatePostRequest;
import com.nalsil.bear.domain.post.Post;
import com.nalsil.bear.mapper.PostMapper;
import com.nalsil.bear.service.AdminService;
import com.nalsil.bear.service.BoardService;
import com.nalsil.bear.service.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

/**
 * 관리자 게시판 컨트롤러
 *
 * 관리자가 게시글을 등록, 수정, 삭제, 숨김 처리할 수 있습니다.
 */
@Slf4j
@Controller
@RequestMapping("/admin/boards")
@RequiredArgsConstructor
public class AdminBoardController {

    private final BoardService boardService;
    private final PostService postService;
    private final AdminService adminService;
    private final PostMapper postMapper;

    /**
     * 게시판 목록
     *
     * @param boardId 게시판 ID
     * @param exchange ServerWebExchange
     * @param model 모델
     * @return 게시글 목록 템플릿
     */
    @GetMapping("/{boardId}/posts")
    public Mono<String> listPosts(
            @PathVariable Long boardId,
            ServerWebExchange exchange,
            Model model) {

        Long adminCompanyId = (Long) exchange.getAttributes().get("companyId");
        log.info("관리자 게시글 목록 조회: boardId={}, companyId={}", boardId, adminCompanyId);

        return boardService.getBoardById(boardId)
                .flatMap(board -> {
                    // 권한 확인
                    if (!board.getCompanyId().equals(adminCompanyId)) {
                        return Mono.error(new IllegalAccessException("접근 권한이 없습니다."));
                    }

                    model.addAttribute("board", board);

                    // 숨김 포함 전체 게시글 조회 (관리자용)
                    return postService.getPostsByBoardIdIncludingHidden(boardId, 0, 100)
                            .collectList()
                            .doOnNext(posts -> model.addAttribute("posts", posts));
                })
                .thenReturn("admin/board/list")
                .onErrorResume(IllegalAccessException.class, e -> {
                    log.error("게시판 접근 권한 없음: boardId={}, companyId={}", boardId, adminCompanyId);
                    return Mono.just("redirect:/admin/dashboard?error=access_denied");
                });
    }

    /**
     * 게시글 작성 폼
     *
     * @param boardId 게시판 ID
     * @param exchange ServerWebExchange
     * @param model 모델
     * @return 게시글 작성 폼 템플릿
     */
    @GetMapping("/{boardId}/posts/new")
    public Mono<String> newPostForm(
            @PathVariable Long boardId,
            ServerWebExchange exchange,
            Model model) {

        Long adminCompanyId = (Long) exchange.getAttributes().get("companyId");
        log.info("게시글 작성 폼: boardId={}, companyId={}", boardId, adminCompanyId);

        return boardService.getBoardById(boardId)
                .flatMap(board -> {
                    // 권한 확인
                    if (!board.getCompanyId().equals(adminCompanyId)) {
                        return Mono.error(new IllegalAccessException("접근 권한이 없습니다."));
                    }

                    model.addAttribute("board", board);
                    model.addAttribute("post", new CreatePostRequest());
                    return Mono.just("admin/board/form");
                })
                .onErrorResume(IllegalAccessException.class, e -> {
                    return Mono.just("redirect:/admin/dashboard?error=access_denied");
                });
    }

    /**
     * 게시글 작성 처리
     *
     * @param boardId 게시판 ID
     * @param request 게시글 작성 요청
     * @param exchange ServerWebExchange
     * @return 게시글 목록으로 리다이렉트
     */
    @PostMapping("/{boardId}/posts")
    public Mono<String> createPost(
            @PathVariable Long boardId,
            @ModelAttribute CreatePostRequest request,
            ServerWebExchange exchange) {

        Long adminCompanyId = (Long) exchange.getAttributes().get("companyId");
        log.info("게시글 작성: boardId={}, title={}", boardId, request.getTitle());

        return boardService.getBoardById(boardId)
                .flatMap(board -> {
                    // 권한 확인
                    if (!board.getCompanyId().equals(adminCompanyId)) {
                        return Mono.error(new IllegalAccessException("접근 권한이 없습니다."));
                    }

                    // Post 엔티티 생성 (MapStruct 사용)
                    Post post = postMapper.toEntity(request);
                    post.setAuthor("관리자"); // Default author

                    return postService.createPost(post);
                })
                .thenReturn("redirect:/admin/boards/" + boardId + "/posts?success=created")
                .onErrorResume(IllegalAccessException.class, e -> {
                    return Mono.just("redirect:/admin/dashboard?error=access_denied");
                });
    }

    /**
     * 게시글 수정 폼
     *
     * @param boardId 게시판 ID
     * @param postId 게시글 ID
     * @param exchange ServerWebExchange
     * @param model 모델
     * @return 게시글 수정 폼 템플릿
     */
    @GetMapping("/{boardId}/posts/{postId}/edit")
    public Mono<String> editPostForm(
            @PathVariable Long boardId,
            @PathVariable Long postId,
            ServerWebExchange exchange,
            Model model) {

        Long adminCompanyId = (Long) exchange.getAttributes().get("companyId");
        log.info("게시글 수정 폼: boardId={}, postId={}", boardId, postId);

        return boardService.getBoardById(boardId)
                .zipWith(postService.getPostById(postId))
                .flatMap(tuple -> {
                    var board = tuple.getT1();
                    var post = tuple.getT2();

                    // 권한 확인
                    if (!board.getCompanyId().equals(adminCompanyId)) {
                        return Mono.error(new IllegalAccessException("접근 권한이 없습니다."));
                    }

                    model.addAttribute("board", board);
                    model.addAttribute("post", post);
                    return Mono.just("admin/board/form");
                })
                .onErrorResume(IllegalAccessException.class, e -> {
                    return Mono.just("redirect:/admin/dashboard?error=access_denied");
                });
    }

    /**
     * 게시글 수정 처리
     *
     * @param boardId 게시판 ID
     * @param postId 게시글 ID
     * @param request 게시글 수정 요청
     * @param exchange ServerWebExchange
     * @return 게시글 목록으로 리다이렉트
     */
    @PostMapping("/{boardId}/posts/{postId}")
    public Mono<String> updatePost(
            @PathVariable Long boardId,
            @PathVariable Long postId,
            @ModelAttribute CreatePostRequest request,
            ServerWebExchange exchange) {

        Long adminCompanyId = (Long) exchange.getAttributes().get("companyId");
        log.info("게시글 수정: boardId={}, postId={}", boardId, postId);

        return boardService.getBoardById(boardId)
                .zipWith(postService.getPostById(postId))
                .flatMap(tuple -> {
                    var board = tuple.getT1();
                    var post = tuple.getT2();

                    // 권한 확인
                    if (!board.getCompanyId().equals(adminCompanyId)) {
                        return Mono.error(new IllegalAccessException("접근 권한이 없습니다."));
                    }

                    // 게시글 업데이트 (MapStruct 사용)
                    postMapper.updateEntityFromRequest(request, post);

                    return postService.updatePost(post);
                })
                .thenReturn("redirect:/admin/boards/" + boardId + "/posts?success=updated")
                .onErrorResume(IllegalAccessException.class, e -> {
                    return Mono.just("redirect:/admin/dashboard?error=access_denied");
                });
    }

    /**
     * 게시글 삭제
     *
     * @param boardId 게시판 ID
     * @param postId 게시글 ID
     * @param exchange ServerWebExchange
     * @return 게시글 목록으로 리다이렉트
     */
    @PostMapping("/{boardId}/posts/{postId}/delete")
    public Mono<String> deletePost(
            @PathVariable Long boardId,
            @PathVariable Long postId,
            ServerWebExchange exchange) {

        Long adminCompanyId = (Long) exchange.getAttributes().get("companyId");
        log.info("게시글 삭제: boardId={}, postId={}", boardId, postId);

        return boardService.getBoardById(boardId)
                .flatMap(board -> {
                    // 권한 확인
                    if (!board.getCompanyId().equals(adminCompanyId)) {
                        return Mono.error(new IllegalAccessException("접근 권한이 없습니다."));
                    }

                    return postService.deletePost(postId);
                })
                .thenReturn("redirect:/admin/boards/" + boardId + "/posts?success=deleted")
                .onErrorResume(IllegalAccessException.class, e -> {
                    return Mono.just("redirect:/admin/dashboard?error=access_denied");
                });
    }

    /**
     * 게시글 숨김/표시 토글
     *
     * @param boardId 게시판 ID
     * @param postId 게시글 ID
     * @param exchange ServerWebExchange
     * @return 게시글 목록으로 리다이렉트
     */
    @PostMapping("/{boardId}/posts/{postId}/toggle-hidden")
    public Mono<String> toggleHidden(
            @PathVariable Long boardId,
            @PathVariable Long postId,
            ServerWebExchange exchange) {

        Long adminCompanyId = (Long) exchange.getAttributes().get("companyId");
        log.info("게시글 숨김 토글: boardId={}, postId={}", boardId, postId);

        return boardService.getBoardById(boardId)
                .zipWith(postService.getPostById(postId))
                .flatMap(tuple -> {
                    var board = tuple.getT1();
                    var post = tuple.getT2();

                    // 권한 확인
                    if (!board.getCompanyId().equals(adminCompanyId)) {
                        return Mono.error(new IllegalAccessException("접근 권한이 없습니다."));
                    }

                    // 숨김 상태 토글
                    post.setIsHidden(!post.getIsHidden());
                    post.setUpdatedAt(LocalDateTime.now());

                    return postService.updatePost(post);
                })
                .thenReturn("redirect:/admin/boards/" + boardId + "/posts")
                .onErrorResume(IllegalAccessException.class, e -> {
                    return Mono.just("redirect:/admin/dashboard?error=access_denied");
                });
    }
}
