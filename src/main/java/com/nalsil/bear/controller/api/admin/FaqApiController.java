package com.nalsil.bear.controller.api.admin;

import com.nalsil.bear.domain.faq.Faq;
import com.nalsil.bear.dto.request.CreateFaqRequest;
import com.nalsil.bear.dto.response.ApiResponse;
import com.nalsil.bear.dto.response.FaqResponse;
import com.nalsil.bear.dto.response.PagedResponse;
import com.nalsil.bear.service.FaqService;
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
 * FAQ 관리 API 컨트롤러
 */
@Tag(name = "FAQ 관리 API", description = "FAQ CRUD API")
@Slf4j
@RestController
@RequestMapping("/api/admin/faqs")
@RequiredArgsConstructor
@SecurityRequirement(name = "JWT Cookie")
public class FaqApiController {

    private final FaqService faqService;

    /**
     * FAQ 목록 조회
     */
    @Operation(summary = "FAQ 목록 조회", description = "현재 기업의 FAQ 목록을 조회합니다.")
    @GetMapping
    public Mono<ResponseEntity<ApiResponse<PagedResponse<FaqResponse>>>> getFaqs(
            @Parameter(description = "페이지 번호 (0부터 시작)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "20") int size,
            ServerWebExchange exchange) {

        Long companyId = (Long) exchange.getAttributes().get("companyId");

        if (companyId == null) {
            return Mono.just(ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("기업이 선택되지 않았습니다.", "NO_COMPANY_SELECTED")));
        }

        return faqService.getAllFaqsByCompanyId(companyId)
                .map(this::toFaqResponse)
                .collectList()
                .map(faqs -> {
                    // 페이지 처리
                    int start = page * size;
                    int end = Math.min(start + size, faqs.size());
                    List<FaqResponse> pagedFaqs = start < faqs.size()
                            ? faqs.subList(start, end)
                            : List.of();

                    PagedResponse<FaqResponse> pagedResponse = PagedResponse.of(
                            pagedFaqs, page, size, faqs.size());
                    return ResponseEntity.ok(ApiResponse.success(pagedResponse));
                })
                .onErrorResume(e -> {
                    log.error("FAQ 목록 조회 실패: companyId={}", companyId, e);
                    return Mono.just(ResponseEntity
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(ApiResponse.error("FAQ 목록 조회에 실패했습니다.")));
                });
    }

    /**
     * FAQ 상세 조회
     */
    @Operation(summary = "FAQ 상세 조회", description = "FAQ 상세 정보를 조회합니다.")
    @GetMapping("/{id}")
    public Mono<ResponseEntity<ApiResponse<FaqResponse>>> getFaq(
            @PathVariable Long id,
            ServerWebExchange exchange) {

        Long companyId = (Long) exchange.getAttributes().get("companyId");

        if (companyId == null) {
            return Mono.just(ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("기업이 선택되지 않았습니다.", "NO_COMPANY_SELECTED")));
        }

        return faqService.getFaqById(id)
                .filter(faq -> faq.getCompanyId().equals(companyId))
                .map(this::toFaqResponse)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response)))
                .switchIfEmpty(Mono.just(ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("FAQ를 찾을 수 없습니다.", "FAQ_NOT_FOUND"))));
    }

    /**
     * FAQ 생성
     */
    @Operation(summary = "FAQ 생성", description = "새 FAQ를 생성합니다.")
    @PostMapping
    public Mono<ResponseEntity<ApiResponse<FaqResponse>>> createFaq(
            @RequestBody CreateFaqRequest request,
            ServerWebExchange exchange) {

        Long companyId = (Long) exchange.getAttributes().get("companyId");

        if (companyId == null) {
            return Mono.just(ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("기업이 선택되지 않았습니다.", "NO_COMPANY_SELECTED")));
        }

        Faq faq = Faq.builder()
                .companyId(companyId)
                .category(request.getCategory())
                .question(request.getQuestion())
                .answer(request.getAnswer())
                .displayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0)
                .isHidden(request.getIsHidden() != null ? request.getIsHidden() : false)
                .createdAt(LocalDateTime.now())
                .build();

        return faqService.createFaq(faq)
                .map(this::toFaqResponse)
                .map(response -> ResponseEntity
                        .status(HttpStatus.CREATED)
                        .body(ApiResponse.success("FAQ가 생성되었습니다.", response)))
                .onErrorResume(e -> {
                    log.error("FAQ 생성 실패: companyId={}", companyId, e);
                    return Mono.just(ResponseEntity
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(ApiResponse.error("FAQ 생성에 실패했습니다.")));
                });
    }

    /**
     * FAQ 수정
     */
    @Operation(summary = "FAQ 수정", description = "FAQ 정보를 수정합니다.")
    @PutMapping("/{id}")
    public Mono<ResponseEntity<ApiResponse<FaqResponse>>> updateFaq(
            @PathVariable Long id,
            @RequestBody CreateFaqRequest request,
            ServerWebExchange exchange) {

        Long companyId = (Long) exchange.getAttributes().get("companyId");

        if (companyId == null) {
            return Mono.just(ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("기업이 선택되지 않았습니다.", "NO_COMPANY_SELECTED")));
        }

        return faqService.getFaqById(id)
                .filter(faq -> faq.getCompanyId().equals(companyId))
                .flatMap(existingFaq -> {
                    existingFaq.setCategory(request.getCategory());
                    existingFaq.setQuestion(request.getQuestion());
                    existingFaq.setAnswer(request.getAnswer());
                    if (request.getDisplayOrder() != null) {
                        existingFaq.setDisplayOrder(request.getDisplayOrder());
                    }
                    if (request.getIsHidden() != null) {
                        existingFaq.setIsHidden(request.getIsHidden());
                    }

                    return faqService.updateFaq(existingFaq);
                })
                .map(this::toFaqResponse)
                .map(response -> ResponseEntity.ok(ApiResponse.success("FAQ가 수정되었습니다.", response)))
                .switchIfEmpty(Mono.just(ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("FAQ를 찾을 수 없습니다.", "FAQ_NOT_FOUND"))))
                .onErrorResume(e -> {
                    log.error("FAQ 수정 실패: id={}", id, e);
                    return Mono.just(ResponseEntity
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(ApiResponse.error("FAQ 수정에 실패했습니다.")));
                });
    }

    /**
     * FAQ 삭제
     */
    @Operation(summary = "FAQ 삭제", description = "FAQ를 삭제합니다.")
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<ApiResponse<Void>>> deleteFaq(
            @PathVariable Long id,
            ServerWebExchange exchange) {

        Long companyId = (Long) exchange.getAttributes().get("companyId");

        if (companyId == null) {
            return Mono.just(ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("기업이 선택되지 않았습니다.", "NO_COMPANY_SELECTED")));
        }

        return faqService.getFaqById(id)
                .filter(faq -> faq.getCompanyId().equals(companyId))
                .flatMap(faq -> faqService.deleteFaq(id).thenReturn(faq))
                .map(faq -> ResponseEntity.ok(ApiResponse.<Void>success("FAQ가 삭제되었습니다.")))
                .switchIfEmpty(Mono.just(ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("FAQ를 찾을 수 없습니다.", "FAQ_NOT_FOUND"))))
                .onErrorResume(e -> {
                    log.error("FAQ 삭제 실패: id={}", id, e);
                    return Mono.just(ResponseEntity
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(ApiResponse.error("FAQ 삭제에 실패했습니다.")));
                });
    }

    /**
     * 카테고리 목록 조회
     */
    @Operation(summary = "FAQ 카테고리 목록 조회", description = "현재 기업의 FAQ 카테고리 목록을 조회합니다.")
    @GetMapping("/categories")
    public Mono<ResponseEntity<ApiResponse<List<String>>>> getCategories(ServerWebExchange exchange) {
        Long companyId = (Long) exchange.getAttributes().get("companyId");

        if (companyId == null) {
            return Mono.just(ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("기업이 선택되지 않았습니다.", "NO_COMPANY_SELECTED")));
        }

        return faqService.getCategoriesByCompanyId(companyId)
                .collectList()
                .map(categories -> ResponseEntity.ok(ApiResponse.success(categories)));
    }

    private FaqResponse toFaqResponse(Faq faq) {
        return FaqResponse.builder()
                .id(faq.getId())
                .companyId(faq.getCompanyId())
                .category(faq.getCategory())
                .question(faq.getQuestion())
                .answer(faq.getAnswer())
                .displayOrder(faq.getDisplayOrder())
                .isHidden(faq.getIsHidden())
                .createdAt(faq.getCreatedAt())
                .build();
    }
}
