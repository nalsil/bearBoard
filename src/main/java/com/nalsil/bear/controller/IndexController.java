package com.nalsil.bear.controller;

import com.nalsil.bear.service.CompanyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Mono;

/**
 * IndexController
 * 루트 URL (/) 처리 - 모든 회사 목록 표시
 */
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
    @GetMapping("/")
    public Mono<Rendering> index() {
        log.info("Accessing root page - company list");

        return companyService.getAllActiveCompanies()
                .collectList()
                .map(companies -> Rendering.view("index")
                        .modelAttribute("companies", companies)
                        .build());
    }
}
