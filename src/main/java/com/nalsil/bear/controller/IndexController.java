package com.nalsil.bear.controller;

import com.nalsil.bear.service.CompanyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Mono;

/**
 * IndexController
 * 루트 URL (/) 처리 - 모든 회사 목록 표시
 */
@Tag(name = "인덱스", description = "루트 페이지 및 기업 목록 API")
@Slf4j
@Controller
@RequiredArgsConstructor
public class IndexController {

    private final CompanyService companyService;

    /**
     * 루트 페이지 - 접속 가능한 모든 회사 목록 표시
     * URL: /
     *
     * @return Rendering (Thymeleaf 템플릿)
     */
    @Operation(summary = "루트 페이지", description = "접속 가능한 모든 활성 기업 목록을 표시합니다.", tags = "인덱스")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "HTML 페이지",
                    content = @Content(
                            mediaType = "text/html"
                    )
            )
    })
    @GetMapping("/")
    public Mono<Rendering> index() {
        log.info("Accessing root page - company list");

        return companyService.getAllActiveCompanies()
                .collectList()
                .map(companies -> Rendering.view("index")
                        .modelAttribute("companies", companies)
                        .build());
    }


    @Operation(summary = "파비콘 조회", description = "웹사이트 파비콘을 반환합니다.", tags = "인덱스")
    @GetMapping(value = "/favicon.ico", produces = "image/x-icon")
    @ResponseBody
    public Mono<Resource> favicon() {
        // src/main/resources/static/favicon.ico 파일을 반환합니다.
        return Mono.just(new ClassPathResource("static/favicon.ico"));
    }
}
