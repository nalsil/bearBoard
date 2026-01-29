package com.nalsil.bear.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RequestPredicate;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;

/**
 * React Admin App 라우터 설정
 * /admin-app/** 경로 처리를 위한 RouterFunction 설정
 */
@Configuration
public class AdminAppRouter {

    private static final Resource INDEX_HTML = new ClassPathResource("static/admin-app/index.html");

    /**
     * Admin App 통합 라우터
     * 1. /admin-app/assets/** - 정적 리소스 제공
     * 2. /admin-app/** (그 외) - SPA index.html로 라우팅
     */
    @Bean
    public RouterFunction<ServerResponse> adminAppRouterFunction() {
        // 정적 리소스 라우터 (assets)
        RouterFunction<ServerResponse> assetsRouter = RouterFunctions.resources(
                "/admin-app/assets/**",
                new ClassPathResource("static/admin-app/assets/")
        );

        // SPA Fallback 라우터
        RouterFunction<ServerResponse> spaRouter = RouterFunctions.route()
                .GET("/admin-app", this::serveIndexHtml)
                .GET("/admin-app/", this::serveIndexHtml)
                .route(GET("/admin-app/**").and(isNotAssetPath()), this::serveIndexHtml)
                .build();

        // assets 라우터가 먼저 처리되도록 조합
        return assetsRouter.and(spaRouter);
    }

    /**
     * index.html 반환
     */
    private Mono<ServerResponse> serveIndexHtml(ServerRequest request) {
        return ServerResponse.ok()
                .contentType(MediaType.TEXT_HTML)
                .bodyValue(INDEX_HTML);
    }

    /**
     * assets 경로가 아닌지 확인하는 RequestPredicate
     */
    private RequestPredicate isNotAssetPath() {
        return request -> {
            String path = request.path();
            return !path.startsWith("/admin-app/assets/");
        };
    }
}
