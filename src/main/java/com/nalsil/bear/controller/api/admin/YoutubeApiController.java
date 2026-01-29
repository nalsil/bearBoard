package com.nalsil.bear.controller.api.admin;

import com.nalsil.bear.domain.youtube.YoutubeVideo;
import com.nalsil.bear.dto.request.CreateYoutubeVideoRequest;
import com.nalsil.bear.dto.response.ApiResponse;
import com.nalsil.bear.dto.response.PagedResponse;
import com.nalsil.bear.dto.response.YoutubeVideoResponse;
import com.nalsil.bear.service.YoutubeVideoService;
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
 * 유튜브 영상 관리 API 컨트롤러
 */
@Tag(name = "유튜브 관리 API", description = "유튜브 영상 CRUD API")
@Slf4j
@RestController
@RequestMapping("/api/admin/youtube")
@RequiredArgsConstructor
@SecurityRequirement(name = "JWT Cookie")
public class YoutubeApiController {

    private final YoutubeVideoService youtubeVideoService;

    /**
     * 유튜브 영상 목록 조회
     */
    @Operation(summary = "유튜브 영상 목록 조회", description = "현재 기업의 유튜브 영상 목록을 조회합니다.")
    @GetMapping
    public Mono<ResponseEntity<ApiResponse<PagedResponse<YoutubeVideoResponse>>>> getVideos(
            @Parameter(description = "페이지 번호 (0부터 시작)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "20") int size,
            ServerWebExchange exchange) {

        Long companyId = (Long) exchange.getAttributes().get("companyId");

        if (companyId == null) {
            return Mono.just(ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("기업이 선택되지 않았습니다.", "NO_COMPANY_SELECTED")));
        }

        return youtubeVideoService.getAllVideosByCompanyId(companyId)
                .map(this::toYoutubeVideoResponse)
                .collectList()
                .map(videos -> {
                    // 페이지 처리
                    int start = page * size;
                    int end = Math.min(start + size, videos.size());
                    List<YoutubeVideoResponse> pagedVideos = start < videos.size()
                            ? videos.subList(start, end)
                            : List.of();

                    PagedResponse<YoutubeVideoResponse> pagedResponse = PagedResponse.of(
                            pagedVideos, page, size, videos.size());
                    return ResponseEntity.ok(ApiResponse.success(pagedResponse));
                })
                .onErrorResume(e -> {
                    log.error("유튜브 영상 목록 조회 실패: companyId={}", companyId, e);
                    return Mono.just(ResponseEntity
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(ApiResponse.error("유튜브 영상 목록 조회에 실패했습니다.")));
                });
    }

    /**
     * 유튜브 영상 상세 조회
     */
    @Operation(summary = "유튜브 영상 상세 조회", description = "유튜브 영상 상세 정보를 조회합니다.")
    @GetMapping("/{id}")
    public Mono<ResponseEntity<ApiResponse<YoutubeVideoResponse>>> getVideo(
            @PathVariable Long id,
            ServerWebExchange exchange) {

        Long companyId = (Long) exchange.getAttributes().get("companyId");

        if (companyId == null) {
            return Mono.just(ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("기업이 선택되지 않았습니다.", "NO_COMPANY_SELECTED")));
        }

        return youtubeVideoService.getVideoById(id)
                .filter(video -> video.getCompanyId().equals(companyId))
                .map(this::toYoutubeVideoResponse)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response)))
                .switchIfEmpty(Mono.just(ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("유튜브 영상을 찾을 수 없습니다.", "VIDEO_NOT_FOUND"))));
    }

    /**
     * 유튜브 영상 생성
     */
    @Operation(summary = "유튜브 영상 생성", description = "새 유튜브 영상을 등록합니다.")
    @PostMapping
    public Mono<ResponseEntity<ApiResponse<YoutubeVideoResponse>>> createVideo(
            @RequestBody CreateYoutubeVideoRequest request,
            ServerWebExchange exchange) {

        Long companyId = (Long) exchange.getAttributes().get("companyId");

        if (companyId == null) {
            return Mono.just(ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("기업이 선택되지 않았습니다.", "NO_COMPANY_SELECTED")));
        }

        // URL 유효성 검증
        if (!youtubeVideoService.isValidYoutubeUrl(request.getVideoUrl())) {
            return Mono.just(ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("유효하지 않은 유튜브 URL입니다.", "INVALID_URL")));
        }

        // 썸네일 URL 자동 생성
        String thumbnailUrl = request.getThumbnailUrl();
        if (thumbnailUrl == null || thumbnailUrl.isEmpty()) {
            String videoId = youtubeVideoService.extractVideoId(request.getVideoUrl());
            thumbnailUrl = youtubeVideoService.getThumbnailUrl(videoId);
        }

        YoutubeVideo video = YoutubeVideo.builder()
                .companyId(companyId)
                .videoUrl(request.getVideoUrl())
                .title(request.getTitle())
                .description(request.getDescription())
                .thumbnailUrl(thumbnailUrl)
                .displayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0)
                .isHidden(request.getIsHidden() != null ? request.getIsHidden() : false)
                .createdAt(LocalDateTime.now())
                .build();

        return youtubeVideoService.createVideo(video)
                .map(this::toYoutubeVideoResponse)
                .map(response -> ResponseEntity
                        .status(HttpStatus.CREATED)
                        .body(ApiResponse.success("유튜브 영상이 등록되었습니다.", response)))
                .onErrorResume(e -> {
                    log.error("유튜브 영상 생성 실패: companyId={}", companyId, e);
                    return Mono.just(ResponseEntity
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(ApiResponse.error("유튜브 영상 등록에 실패했습니다.")));
                });
    }

    /**
     * 유튜브 영상 수정
     */
    @Operation(summary = "유튜브 영상 수정", description = "유튜브 영상 정보를 수정합니다.")
    @PutMapping("/{id}")
    public Mono<ResponseEntity<ApiResponse<YoutubeVideoResponse>>> updateVideo(
            @PathVariable Long id,
            @RequestBody CreateYoutubeVideoRequest request,
            ServerWebExchange exchange) {

        Long companyId = (Long) exchange.getAttributes().get("companyId");

        if (companyId == null) {
            return Mono.just(ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("기업이 선택되지 않았습니다.", "NO_COMPANY_SELECTED")));
        }

        // URL 유효성 검증
        if (request.getVideoUrl() != null && !youtubeVideoService.isValidYoutubeUrl(request.getVideoUrl())) {
            return Mono.just(ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("유효하지 않은 유튜브 URL입니다.", "INVALID_URL")));
        }

        return youtubeVideoService.getVideoById(id)
                .filter(video -> video.getCompanyId().equals(companyId))
                .flatMap(existingVideo -> {
                    if (request.getVideoUrl() != null) {
                        existingVideo.setVideoUrl(request.getVideoUrl());
                        // 썸네일 URL 자동 업데이트
                        if (request.getThumbnailUrl() == null || request.getThumbnailUrl().isEmpty()) {
                            String videoId = youtubeVideoService.extractVideoId(request.getVideoUrl());
                            existingVideo.setThumbnailUrl(youtubeVideoService.getThumbnailUrl(videoId));
                        }
                    }
                    if (request.getTitle() != null) {
                        existingVideo.setTitle(request.getTitle());
                    }
                    if (request.getDescription() != null) {
                        existingVideo.setDescription(request.getDescription());
                    }
                    if (request.getThumbnailUrl() != null && !request.getThumbnailUrl().isEmpty()) {
                        existingVideo.setThumbnailUrl(request.getThumbnailUrl());
                    }
                    if (request.getDisplayOrder() != null) {
                        existingVideo.setDisplayOrder(request.getDisplayOrder());
                    }
                    if (request.getIsHidden() != null) {
                        existingVideo.setIsHidden(request.getIsHidden());
                    }

                    return youtubeVideoService.updateVideo(existingVideo);
                })
                .map(this::toYoutubeVideoResponse)
                .map(response -> ResponseEntity.ok(ApiResponse.success("유튜브 영상이 수정되었습니다.", response)))
                .switchIfEmpty(Mono.just(ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("유튜브 영상을 찾을 수 없습니다.", "VIDEO_NOT_FOUND"))))
                .onErrorResume(e -> {
                    log.error("유튜브 영상 수정 실패: id={}", id, e);
                    return Mono.just(ResponseEntity
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(ApiResponse.error("유튜브 영상 수정에 실패했습니다.")));
                });
    }

    /**
     * 유튜브 영상 삭제
     */
    @Operation(summary = "유튜브 영상 삭제", description = "유튜브 영상을 삭제합니다.")
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<ApiResponse<Void>>> deleteVideo(
            @PathVariable Long id,
            ServerWebExchange exchange) {

        Long companyId = (Long) exchange.getAttributes().get("companyId");

        if (companyId == null) {
            return Mono.just(ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("기업이 선택되지 않았습니다.", "NO_COMPANY_SELECTED")));
        }

        return youtubeVideoService.getVideoById(id)
                .filter(video -> video.getCompanyId().equals(companyId))
                .flatMap(video -> youtubeVideoService.deleteVideo(id).thenReturn(video))
                .map(video -> ResponseEntity.ok(ApiResponse.<Void>success("유튜브 영상이 삭제되었습니다.")))
                .switchIfEmpty(Mono.just(ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("유튜브 영상을 찾을 수 없습니다.", "VIDEO_NOT_FOUND"))))
                .onErrorResume(e -> {
                    log.error("유튜브 영상 삭제 실패: id={}", id, e);
                    return Mono.just(ResponseEntity
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(ApiResponse.error("유튜브 영상 삭제에 실패했습니다.")));
                });
    }

    private YoutubeVideoResponse toYoutubeVideoResponse(YoutubeVideo video) {
        return YoutubeVideoResponse.builder()
                .id(video.getId())
                .companyId(video.getCompanyId())
                .videoUrl(video.getVideoUrl())
                .title(video.getTitle())
                .description(video.getDescription())
                .thumbnailUrl(video.getThumbnailUrl())
                .displayOrder(video.getDisplayOrder())
                .isHidden(video.getIsHidden())
                .createdAt(video.getCreatedAt())
                .build();
    }
}
