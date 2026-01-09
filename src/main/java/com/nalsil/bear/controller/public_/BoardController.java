package com.nalsil.bear.controller.public_;

import com.nalsil.bear.domain.company.Company;
import com.nalsil.bear.service.BoardService;
import com.nalsil.bear.service.CompanyService;
import com.nalsil.bear.service.PostService;
import com.nalsil.bear.util.TenantContextHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Mono;

/**
 * BoardController
 * 게시판 목록 및 게시글 상세 페이지 처리
 */
@Slf4j
@Controller
@RequestMapping("/{companyCode}/board")
@RequiredArgsConstructor
public class BoardController {

    private final CompanyService companyService;
    private final BoardService boardService;
    private final PostService postService;

    /**
     * 게시판 목록 페이지
     * URL: /{companyCode}/board/{boardType}
     *
     * @param companyCode 기업 코드
     * @param boardType 게시판 타입 (notice, press, recruit 등)
     * @param page 페이지 번호 (기본값: 0)
     * @param size 페이지 크기 (기본값: 10)
     * @return Rendering (Thymeleaf 템플릿)
     */
    @GetMapping("/{boardType}")
    public Mono<Rendering> boardList(
            @PathVariable String companyCode,
            @PathVariable String boardType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        log.info("Accessing board list for company: {}, type: {}, page: {}", companyCode, boardType, page);

        // 기업 정보 조회
        Mono<Company> companyMono = companyService.getActiveCompanyByCode(companyCode);

        return companyMono.flatMap(company ->
                // 게시판 정보 조회
                boardService.getBoardByCompanyIdAndType(company.getId(), boardType)
                        .flatMap(board ->
                                // 게시글 목록 조회
                                postService.getVisiblePostsByBoardId(board.getId(), PageRequest.of(page, size))
                                        .collectList()
                                        .zipWith(postService.countVisiblePostsByBoardId(board.getId()))
                                        .map(tuple -> {
                                            long totalPosts = tuple.getT2();
                                            int totalPages = (int) Math.ceil((double) totalPosts / size);

                                            return Rendering.view("public/board/list")
                                                    .modelAttribute("company", company)
                                                    .modelAttribute("board", board)
                                                    .modelAttribute("posts", tuple.getT1())
                                                    .modelAttribute("currentPage", page)
                                                    .modelAttribute("totalPages", totalPages)
                                                    .modelAttribute("totalPosts", totalPosts)
                                                    .build();
                                        })
                        )
                        .switchIfEmpty(Mono.just(Rendering.view("public/board/list")
                                .modelAttribute("company", company)
                                .modelAttribute("board", null)
                                .modelAttribute("posts", java.util.Collections.emptyList())
                                .modelAttribute("message", "해당 게시판을 찾을 수 없습니다.")
                                .build()))
        ).contextWrite(ctx -> TenantContextHolder.setCurrentTenant(ctx, companyCode));
    }

    /**
     * 게시글 상세 페이지
     * URL: /{companyCode}/board/{boardType}/{postId}
     *
     * @param companyCode 기업 코드
     * @param boardType 게시판 타입
     * @param postId 게시글 ID
     * @return Rendering (Thymeleaf 템플릿)
     */
    @GetMapping("/{boardType}/{postId}")
    public Mono<Rendering> postDetail(
            @PathVariable String companyCode,
            @PathVariable String boardType,
            @PathVariable Long postId) {

        log.info("Accessing post detail for company: {}, type: {}, postId: {}", companyCode, boardType, postId);

        // 기업 정보 조회
        Mono<Company> companyMono = companyService.getActiveCompanyByCode(companyCode);

        return companyMono.flatMap(company ->
                // 게시판 정보 조회
                boardService.getBoardByCompanyIdAndType(company.getId(), boardType)
                        .flatMap(board ->
                                // 게시글 조회 (숨김 제외)
                                postService.getPostByIdAndIsHidden(postId, false)
                                        .flatMap(post -> {
                                            // 조회수 증가
                                            return postService.incrementViewCount(postId)
                                                    .then(Mono.just(Rendering.view("public/board/detail")
                                                            .modelAttribute("company", company)
                                                            .modelAttribute("board", board)
                                                            .modelAttribute("post", post)
                                                            .build()));
                                        })
                                        .switchIfEmpty(Mono.just(Rendering.view("public/board/detail")
                                                .modelAttribute("company", company)
                                                .modelAttribute("board", board)
                                                .modelAttribute("post", null)
                                                .modelAttribute("message", "해당 게시글을 찾을 수 없습니다.")
                                                .build()))
                        )
        ).contextWrite(ctx -> TenantContextHolder.setCurrentTenant(ctx, companyCode));
    }
}
