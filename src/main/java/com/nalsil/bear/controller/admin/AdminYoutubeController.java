package com.nalsil.bear.controller.admin;

import com.nalsil.bear.domain.youtube.YoutubeVideo;
import com.nalsil.bear.mapper.YoutubeVideoMapper;
import com.nalsil.bear.service.AdminService;
import com.nalsil.bear.service.CompanyService;
import com.nalsil.bear.service.YoutubeVideoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 관리자 유튜브 영상 컨트롤러
 *
 * 관리자가 유튜브 영상을 등록, 수정, 삭제, 숨김 처리할 수 있습니다.
 */
@Slf4j
@Controller
@RequestMapping("/admin/youtube")
@RequiredArgsConstructor
public class AdminYoutubeController {

    private final YoutubeVideoService youtubeVideoService;
    private final AdminService adminService;
    private final YoutubeVideoMapper youtubeVideoMapper;
    private final CompanyService companyService;

    /**
     * 유튜브 영상 목록
     *
     * @param exchange ServerWebExchange
     * @param model 모델
     * @return 유튜브 영상 목록 템플릿
     */
    @GetMapping
    public Mono<String> list(ServerWebExchange exchange, Model model) {
        Long adminCompanyId = (Long) exchange.getAttributes().get("companyId");
        log.info("관리자 유튜브 영상 목록 조회: companyId={}", adminCompanyId);

        return companyService.getCompanyById(adminCompanyId)
                .flatMap(company -> {
                    model.addAttribute("company", company);

                    return youtubeVideoService.getAllVideosByCompanyId(adminCompanyId)
                            .collectList()
                            .doOnNext(videos -> model.addAttribute("videos", videos));
                })
                .thenReturn("admin/youtube/list");
    }

    /**
     * 유튜브 영상 등록 폼
     *
     * @param exchange ServerWebExchange
     * @param model 모델
     * @return 유튜브 영상 등록 폼 템플릿
     */
    @GetMapping("/new")
    public Mono<String> newVideoForm(ServerWebExchange exchange, Model model) {
        Long adminCompanyId = (Long) exchange.getAttributes().get("companyId");

        return companyService.getCompanyById(adminCompanyId)
                .doOnNext(company -> {
                    model.addAttribute("company", company);
                    model.addAttribute("video", new YoutubeVideo());
                })
                .thenReturn("admin/youtube/form");
    }

    /**
     * 유튜브 영상 등록 처리
     *
     * @param video 유튜브 영상 엔티티
     * @param exchange ServerWebExchange
     * @return 유튜브 영상 목록으로 리다이렉트
     */
    @PostMapping
    public Mono<String> createVideo(@ModelAttribute YoutubeVideo video, ServerWebExchange exchange) {
        Long adminCompanyId = (Long) exchange.getAttributes().get("companyId");
        log.info("유튜브 영상 등록: companyId={}, title={}", adminCompanyId, video.getTitle());

        // URL 유효성 검증
        if (!youtubeVideoService.isValidYoutubeUrl(video.getVideoUrl())) {
            return Mono.just("redirect:/admin/youtube/new?error=invalid_url");
        }

        // MapStruct를 사용하여 엔티티 생성 준비
        YoutubeVideo preparedVideo = youtubeVideoMapper.prepareForCreate(video);
        preparedVideo.setCompanyId(adminCompanyId);

        // 썸네일 URL 자동 생성
        String videoId = youtubeVideoService.extractVideoId(video.getVideoUrl());
        preparedVideo.setThumbnailUrl(youtubeVideoService.getThumbnailUrl(videoId));

        return youtubeVideoService.createVideo(preparedVideo)
                .thenReturn("redirect:/admin/youtube?success=created");
    }

    /**
     * 유튜브 영상 수정 폼
     *
     * @param id 영상 ID
     * @param exchange ServerWebExchange
     * @param model 모델
     * @return 유튜브 영상 수정 폼 템플릿
     */
    @GetMapping("/{id}/edit")
    public Mono<String> editVideoForm(
            @PathVariable Long id,
            ServerWebExchange exchange,
            Model model) {

        Long adminCompanyId = (Long) exchange.getAttributes().get("companyId");
        log.info("유튜브 영상 수정 폼: id={}", id);

        return companyService.getCompanyById(adminCompanyId)
                .flatMap(company -> {
                    model.addAttribute("company", company);

                    return youtubeVideoService.getVideoById(id)
                            .flatMap(video -> {
                                // 권한 확인
                                if (!video.getCompanyId().equals(adminCompanyId)) {
                                    return Mono.error(new IllegalAccessException("접근 권한이 없습니다."));
                                }

                                model.addAttribute("video", video);
                                return Mono.just("admin/youtube/form");
                            });
                })
                .onErrorResume(IllegalAccessException.class, e -> {
                    return Mono.just("redirect:/admin/youtube?error=access_denied");
                });
    }

    /**
     * 유튜브 영상 수정 처리
     *
     * @param id 영상 ID
     * @param video 유튜브 영상 엔티티
     * @param exchange ServerWebExchange
     * @return 유튜브 영상 목록으로 리다이렉트
     */
    @PostMapping("/{id}")
    public Mono<String> updateVideo(
            @PathVariable Long id,
            @ModelAttribute YoutubeVideo video,
            ServerWebExchange exchange) {

        Long adminCompanyId = (Long) exchange.getAttributes().get("companyId");
        log.info("유튜브 영상 수정: id={}", id);

        // URL 유효성 검증
        if (!youtubeVideoService.isValidYoutubeUrl(video.getVideoUrl())) {
            return Mono.just("redirect:/admin/youtube/" + id + "/edit?error=invalid_url");
        }

        return youtubeVideoService.getVideoById(id)
                .flatMap(existingVideo -> {
                    // 권한 확인
                    if (!existingVideo.getCompanyId().equals(adminCompanyId)) {
                        return Mono.error(new IllegalAccessException("접근 권한이 없습니다."));
                    }

                    // MapStruct를 사용하여 엔티티 업데이트
                    youtubeVideoMapper.updateVideo(video, existingVideo);

                    // 썸네일 URL 갱신
                    String videoId = youtubeVideoService.extractVideoId(video.getVideoUrl());
                    existingVideo.setThumbnailUrl(youtubeVideoService.getThumbnailUrl(videoId));

                    return youtubeVideoService.updateVideo(existingVideo);
                })
                .thenReturn("redirect:/admin/youtube?success=updated")
                .onErrorResume(IllegalAccessException.class, e -> {
                    return Mono.just("redirect:/admin/youtube?error=access_denied");
                });
    }

    /**
     * 유튜브 영상 삭제
     *
     * @param id 영상 ID
     * @param exchange ServerWebExchange
     * @return 유튜브 영상 목록으로 리다이렉트
     */
    @PostMapping("/{id}/delete")
    public Mono<String> deleteVideo(@PathVariable Long id, ServerWebExchange exchange) {
        Long adminCompanyId = (Long) exchange.getAttributes().get("companyId");
        log.info("유튜브 영상 삭제: id={}", id);

        return youtubeVideoService.getVideoById(id)
                .flatMap(video -> {
                    // 권한 확인
                    if (!video.getCompanyId().equals(adminCompanyId)) {
                        return Mono.error(new IllegalAccessException("접근 권한이 없습니다."));
                    }

                    return youtubeVideoService.deleteVideo(id);
                })
                .thenReturn("redirect:/admin/youtube?success=deleted")
                .onErrorResume(IllegalAccessException.class, e -> {
                    return Mono.just("redirect:/admin/youtube?error=access_denied");
                });
    }

    /**
     * 유튜브 영상 숨김/표시 토글
     *
     * @param id 영상 ID
     * @param exchange ServerWebExchange
     * @return 유튜브 영상 목록으로 리다이렉트
     */
    @PostMapping("/{id}/toggle-hidden")
    public Mono<String> toggleHidden(@PathVariable Long id, ServerWebExchange exchange) {
        Long adminCompanyId = (Long) exchange.getAttributes().get("companyId");
        log.info("유튜브 영상 숨김 토글: id={}", id);

        return youtubeVideoService.getVideoById(id)
                .flatMap(video -> {
                    // 권한 확인
                    if (!video.getCompanyId().equals(adminCompanyId)) {
                        return Mono.error(new IllegalAccessException("접근 권한이 없습니다."));
                    }

                    // 숨김 상태 토글
                    YoutubeVideo updateData = YoutubeVideo.builder()
                            .isHidden(!video.getIsHidden())
                            .build();
                    youtubeVideoMapper.updateVideo(updateData, video);

                    return youtubeVideoService.updateVideo(video);
                })
                .thenReturn("redirect:/admin/youtube")
                .onErrorResume(IllegalAccessException.class, e -> {
                    return Mono.just("redirect:/admin/youtube?error=access_denied");
                });
    }
}
