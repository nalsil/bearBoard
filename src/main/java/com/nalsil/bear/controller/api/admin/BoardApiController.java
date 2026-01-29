package com.nalsil.bear.controller.api.admin;

import com.nalsil.bear.domain.board.Board;
import com.nalsil.bear.dto.response.ApiResponse;
import com.nalsil.bear.dto.response.BoardResponse;
import com.nalsil.bear.service.BoardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * 게시판 관리 API 컨트롤러
 */
@Tag(name = "게시판 관리 API", description = "게시판 조회 API")
@Slf4j
@RestController
@RequestMapping("/api/admin/boards")
@RequiredArgsConstructor
@SecurityRequirement(name = "JWT Cookie")
public class BoardApiController {

    private final BoardService boardService;

    /**
     * 게시판 목록 조회
     */
    @Operation(summary = "게시판 목록 조회", description = "현재 기업의 게시판 목록을 조회합니다.")
    @GetMapping
    public Mono<ResponseEntity<ApiResponse<List<BoardResponse>>>> getBoards(ServerWebExchange exchange) {
        Long companyId = (Long) exchange.getAttributes().get("companyId");

        if (companyId == null) {
            return Mono.just(ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("기업이 선택되지 않았습니다.", "NO_COMPANY_SELECTED")));
        }

        return boardService.getBoardsByCompanyId(companyId)
                .map(this::toBoardResponse)
                .collectList()
                .map(boards -> ResponseEntity.ok(ApiResponse.success(boards)))
                .onErrorResume(e -> {
                    log.error("게시판 목록 조회 실패: companyId={}", companyId, e);
                    return Mono.just(ResponseEntity
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(ApiResponse.error("게시판 목록 조회에 실패했습니다.")));
                });
    }

    /**
     * 게시판 상세 조회
     */
    @Operation(summary = "게시판 상세 조회", description = "게시판 상세 정보를 조회합니다.")
    @GetMapping("/{id}")
    public Mono<ResponseEntity<ApiResponse<BoardResponse>>> getBoard(
            @PathVariable Long id,
            ServerWebExchange exchange) {

        Long companyId = (Long) exchange.getAttributes().get("companyId");

        if (companyId == null) {
            return Mono.just(ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("기업이 선택되지 않았습니다.", "NO_COMPANY_SELECTED")));
        }

        return boardService.getBoardById(id)
                .filter(board -> board.getCompanyId().equals(companyId))
                .map(this::toBoardResponse)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response)))
                .switchIfEmpty(Mono.just(ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("게시판을 찾을 수 없습니다.", "BOARD_NOT_FOUND"))));
    }

    private BoardResponse toBoardResponse(Board board) {
        return BoardResponse.builder()
                .id(board.getId())
                .companyId(board.getCompanyId())
                .name(board.getName())
                .type(board.getType())
                .createdAt(board.getCreatedAt())
                .build();
    }
}
