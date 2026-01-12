package com.nalsil.bear.controller.public_;

import com.nalsil.bear.service.CompanyService;
import com.nalsil.bear.service.YoutubeVideoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import reactor.core.publisher.Mono;

/**
 * 유튜브 영상 컨트롤러
 *
 * 유튜브 영상 목록 조회 및 재생 기능을 제공합니다.
 */
@Tag(name = "공개 페이지 - 유튜브", description = "기업 공개 유튜브 영상 목록 및 상세 API")
@Slf4j
@Controller
@RequestMapping("/{companyCode}/youtube")
@RequiredArgsConstructor
public class YoutubeController {

    private final CompanyService companyService;
    private final YoutubeVideoService youtubeVideoService;

    /**
     * 유튜브 영상 목록 페이지
     *
     * @param companyCode 기업 코드
     * @param model 모델
     * @return 유튜브 영상 목록 템플릿
     */
    @Operation(summary = "유튜브 영상 목록 조회", description = "기업의 유튜브 영상 목록을 조회합니다.", tags = "공개 페이지 - 유튜브")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "HTML 페이지",
                    content = @Content(
                            mediaType = "text/html"
                    )
            )
    })
    @GetMapping
    public Mono<String> list(
            @PathVariable String companyCode,
            Model model) {

        log.info("유튜브 영상 목록 조회: companyCode={}", companyCode);

        return companyService.getActiveCompanyByCode(companyCode)
                .flatMap(company -> {
                    model.addAttribute("company", company);

                    // 유튜브 영상 목록 조회
                    return youtubeVideoService.getVideosByCompanyId(company.getId())
                            .collectList()
                            .doOnNext(videos -> model.addAttribute("videos", videos));
                })
                .thenReturn("public/youtube/list");
    }

    /**
     * 유튜브 영상 재생 페이지
     *
     * @param companyCode 기업 코드
     * @param id 영상 ID
     * @param model 모델
     * @return 유튜브 영상 재생 템플릿
     */
    @Operation(summary = "유튜브 영상 재생", description = "특정 유튜브 영상을 재생합니다.", tags = "공개 페이지 - 유튜브")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "HTML 페이지",
                    content = @Content(
                            mediaType = "text/html"
                    )
            )
    })
    @GetMapping("/{id}")
    public Mono<String> player(
            @PathVariable String companyCode,
            @PathVariable Long id,
            Model model) {

        log.info("유튜브 영상 재생: companyCode={}, id={}", companyCode, id);

        return companyService.getActiveCompanyByCode(companyCode)
                .doOnNext(company -> model.addAttribute("company", company))
                .then(youtubeVideoService.getVideoById(id))
                .doOnNext(video -> {
                    model.addAttribute("video", video);
                    // 비디오 ID 추출
                    String videoId = youtubeVideoService.extractVideoId(video.getVideoUrl());
                    model.addAttribute("videoId", videoId);
                })
                .thenReturn("public/youtube/player");
    }
}
