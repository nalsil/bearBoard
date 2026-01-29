package com.nalsil.bear.controller.api.admin;

import com.nalsil.bear.domain.qna.Qna;
import com.nalsil.bear.dto.request.AnswerQnaRequest;
import com.nalsil.bear.dto.response.ApiResponse;
import com.nalsil.bear.dto.response.PagedResponse;
import com.nalsil.bear.dto.response.QnaResponse;
import com.nalsil.bear.service.QnaService;
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
 * QnA 관리 API 컨트롤러
 */
@Tag(name = "QnA 관리 API", description = "QnA 조회 및 답변 API")
@Slf4j
@RestController
@RequestMapping("/api/admin/qnas")
@RequiredArgsConstructor
@SecurityRequirement(name = "JWT Cookie")
public class QnaApiController {

    private final QnaService qnaService;

    /**
     * QnA 목록 조회
     */
    @Operation(summary = "QnA 목록 조회", description = "현재 기업의 QnA 목록을 조회합니다.")
    @GetMapping
    public Mono<ResponseEntity<ApiResponse<PagedResponse<QnaResponse>>>> getQnas(
            @Parameter(description = "페이지 번호 (0부터 시작)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "답변 상태 필터 (all, answered, unanswered)") @RequestParam(defaultValue = "all") String status,
            ServerWebExchange exchange) {

        Long companyId = (Long) exchange.getAttributes().get("companyId");

        if (companyId == null) {
            return Mono.just(ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("기업이 선택되지 않았습니다.", "NO_COMPANY_SELECTED")));
        }

        return qnaService.getAllQnasByCompanyId(companyId, page, size)
                .filter(qna -> {
                    if ("answered".equals(status)) {
                        return Boolean.TRUE.equals(qna.getIsAnswered());
                    } else if ("unanswered".equals(status)) {
                        return Boolean.FALSE.equals(qna.getIsAnswered());
                    }
                    return true;
                })
                .map(this::toQnaResponse)
                .collectList()
                .zipWith(qnaService.countQnasByCompanyId(companyId).defaultIfEmpty(0L))
                .map(tuple -> {
                    List<QnaResponse> qnas = tuple.getT1();
                    long totalElements = tuple.getT2();
                    PagedResponse<QnaResponse> pagedResponse = PagedResponse.of(qnas, page, size, totalElements);
                    return ResponseEntity.ok(ApiResponse.success(pagedResponse));
                })
                .onErrorResume(e -> {
                    log.error("QnA 목록 조회 실패: companyId={}", companyId, e);
                    return Mono.just(ResponseEntity
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(ApiResponse.error("QnA 목록 조회에 실패했습니다.")));
                });
    }

    /**
     * QnA 상세 조회
     */
    @Operation(summary = "QnA 상세 조회", description = "QnA 상세 정보를 조회합니다.")
    @GetMapping("/{id}")
    public Mono<ResponseEntity<ApiResponse<QnaResponse>>> getQna(
            @PathVariable Long id,
            ServerWebExchange exchange) {

        Long companyId = (Long) exchange.getAttributes().get("companyId");

        if (companyId == null) {
            return Mono.just(ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("기업이 선택되지 않았습니다.", "NO_COMPANY_SELECTED")));
        }

        return qnaService.getQnaById(id)
                .filter(qna -> qna.getCompanyId().equals(companyId))
                .map(this::toQnaResponse)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response)))
                .switchIfEmpty(Mono.just(ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("QnA를 찾을 수 없습니다.", "QNA_NOT_FOUND"))));
    }

    /**
     * QnA 답변 등록/수정
     */
    @Operation(summary = "QnA 답변", description = "QnA에 답변을 등록하거나 수정합니다.")
    @PutMapping("/{id}/answer")
    public Mono<ResponseEntity<ApiResponse<QnaResponse>>> answerQna(
            @PathVariable Long id,
            @RequestBody AnswerQnaRequest request,
            ServerWebExchange exchange) {

        Long companyId = (Long) exchange.getAttributes().get("companyId");
        Long adminId = (Long) exchange.getAttributes().get("adminId");

        if (companyId == null) {
            return Mono.just(ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("기업이 선택되지 않았습니다.", "NO_COMPANY_SELECTED")));
        }

        return qnaService.getQnaById(id)
                .filter(qna -> qna.getCompanyId().equals(companyId))
                .flatMap(existingQna -> {
                    existingQna.setAnswerBody(request.getAnswerBody());
                    existingQna.setAnswererId(adminId);
                    existingQna.setIsAnswered(true);
                    existingQna.setAnsweredAt(LocalDateTime.now());

                    return qnaService.updateQna(existingQna);
                })
                .map(this::toQnaResponse)
                .map(response -> ResponseEntity.ok(ApiResponse.success("답변이 등록되었습니다.", response)))
                .switchIfEmpty(Mono.just(ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("QnA를 찾을 수 없습니다.", "QNA_NOT_FOUND"))))
                .onErrorResume(e -> {
                    log.error("QnA 답변 실패: id={}", id, e);
                    return Mono.just(ResponseEntity
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(ApiResponse.error("QnA 답변에 실패했습니다.")));
                });
    }

    /**
     * QnA 숨김 처리
     */
    @Operation(summary = "QnA 숨김 처리", description = "QnA를 숨김 처리합니다.")
    @PutMapping("/{id}/hide")
    public Mono<ResponseEntity<ApiResponse<QnaResponse>>> hideQna(
            @PathVariable Long id,
            @RequestParam boolean hidden,
            ServerWebExchange exchange) {

        Long companyId = (Long) exchange.getAttributes().get("companyId");

        if (companyId == null) {
            return Mono.just(ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("기업이 선택되지 않았습니다.", "NO_COMPANY_SELECTED")));
        }

        return qnaService.getQnaById(id)
                .filter(qna -> qna.getCompanyId().equals(companyId))
                .flatMap(existingQna -> {
                    existingQna.setIsHidden(hidden);
                    return qnaService.updateQna(existingQna);
                })
                .map(this::toQnaResponse)
                .map(response -> ResponseEntity.ok(ApiResponse.success(
                        hidden ? "QnA가 숨김 처리되었습니다." : "QnA가 공개되었습니다.", response)))
                .switchIfEmpty(Mono.just(ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("QnA를 찾을 수 없습니다.", "QNA_NOT_FOUND"))))
                .onErrorResume(e -> {
                    log.error("QnA 숨김 처리 실패: id={}", id, e);
                    return Mono.just(ResponseEntity
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(ApiResponse.error("QnA 숨김 처리에 실패했습니다.")));
                });
    }

    /**
     * QnA 삭제
     */
    @Operation(summary = "QnA 삭제", description = "QnA를 삭제합니다.")
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<ApiResponse<Void>>> deleteQna(
            @PathVariable Long id,
            ServerWebExchange exchange) {

        Long companyId = (Long) exchange.getAttributes().get("companyId");

        if (companyId == null) {
            return Mono.just(ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("기업이 선택되지 않았습니다.", "NO_COMPANY_SELECTED")));
        }

        return qnaService.getQnaById(id)
                .filter(qna -> qna.getCompanyId().equals(companyId))
                .flatMap(qna -> qnaService.deleteQna(id).thenReturn(qna))
                .map(qna -> ResponseEntity.ok(ApiResponse.<Void>success("QnA가 삭제되었습니다.")))
                .switchIfEmpty(Mono.just(ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("QnA를 찾을 수 없습니다.", "QNA_NOT_FOUND"))))
                .onErrorResume(e -> {
                    log.error("QnA 삭제 실패: id={}", id, e);
                    return Mono.just(ResponseEntity
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(ApiResponse.error("QnA 삭제에 실패했습니다.")));
                });
    }

    private QnaResponse toQnaResponse(Qna qna) {
        return QnaResponse.builder()
                .id(qna.getId())
                .companyId(qna.getCompanyId())
                .questionTitle(qna.getQuestionTitle())
                .questionBody(qna.getQuestionBody())
                .askerEmail(qna.getAskerEmail())
                .answerBody(qna.getAnswerBody())
                .answererId(qna.getAnswererId())
                .isAnswered(qna.getIsAnswered())
                .isHidden(qna.getIsHidden())
                .createdAt(qna.getCreatedAt())
                .answeredAt(qna.getAnsweredAt())
                .build();
    }
}
