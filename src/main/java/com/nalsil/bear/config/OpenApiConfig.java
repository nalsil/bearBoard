package com.nalsil.bear.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.HandlerMethod;

import java.util.List;

/**
 * OpenAPI (Swagger) 설정
 *
 * Swagger UI는 http://localhost:8080/swagger-ui.html 에서 접근 가능합니다.
 * OpenAPI 문서는 http://localhost:8080/v3/api-docs 에서 확인할 수 있습니다.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Bear Multi-Tenant 홈페이지 API")
                        .version("1.0.0")
                        .description("""
                                멀티 테넌트 기반 기업 홈페이지 관리 시스템 API 문서입니다.

                                ## 주요 기능
                                - 기업별 독립된 홈페이지 관리
                                - 상품, 게시판, FAQ, QnA, 유튜브 영상 관리
                                - 관리자 인증 및 권한 관리
                                - 공개 사용자 페이지

                                ## 인증 방법
                                관리자 API는 JWT 토큰 기반 인증을 사용합니다.
                                1. `/admin/auth/login` 엔드포인트로 로그인
                                2. 응답 쿠키에 포함된 JWT-TOKEN 사용
                                3. 이후 요청 시 쿠키가 자동으로 포함됨
                                """)
                        .contact(new Contact()
                                .name("Bear Development Team")
                                .email("support@bear.com")
                                .url("https://bear.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("로컬 개발 서버"),
                        new Server()
                                .url("https://api.bear.com")
                                .description("운영 서버")
                ))
                .addSecurityItem(new SecurityRequirement().addList("JWT Cookie"))
                .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes("JWT Cookie", new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.COOKIE)
                                .name("JWT-TOKEN")
                                .description("JWT 토큰이 담긴 HTTP-only 쿠키")));
    }

    /**
     * 전체 API
     */
    @Bean
    public GroupedOpenApi allApi() {
        return GroupedOpenApi.builder()
                .group("00. 전체 API")
                .pathsToMatch("/**")
                .build();
    }

    /**
     * 관리자 API
     */
    @Bean
    public GroupedOpenApi adminApi() {
        return GroupedOpenApi.builder()
                .group("01. 관리자 API")
                .pathsToMatch("/admin/**")
                .packagesToScan("com.nalsil.bear.controller.admin")
                .build();
    }

    /**
     * 공개페이지 API
     */
    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("02. 공개 페이지 API")
                .pathsToMatch("/{companyCode}/**", "/", "/favicon.ico")
                .packagesToScan("com.nalsil.bear.controller.public_", "com.nalsil.bear.controller")
                .packagesToExclude("com.nalsil.bear.controller.admin")
                .build();
    }


    /**
     * 관리자 인증 API 그룹
     */
    @Bean
    public GroupedOpenApi adminAuthApi() {
        return GroupedOpenApi.builder()
                .group("11. 관리자 인증")
                .pathsToMatch("/admin/login", "/admin/logout")
                .packagesToScan("com.nalsil.bear.controller.admin")
                .build();
    }

    /**
     * 관리자 대시보드 API 그룹
     */
    @Bean
    public GroupedOpenApi adminDashboardApi() {
        return GroupedOpenApi.builder()
                .group("12. 관리자 대시보드")
                .pathsToMatch("/admin/dashboard", "/admin/select-company", "/admin/switch-company")
                .packagesToScan("com.nalsil.bear.controller.admin")
                .build();
    }

    /**
     * 관리자 상품 관리 API 그룹
     */
    @Bean
    public GroupedOpenApi adminProductApi() {
        return GroupedOpenApi.builder()
                .group("13. 관리자 - 상품 관리")
                .pathsToMatch("/admin/products", "/admin/products/**")
                .packagesToScan("com.nalsil.bear.controller.admin")
                .build();
    }

    /**
     * 관리자 게시판 관리 API 그룹
     */
    @Bean
    public GroupedOpenApi adminBoardApi() {
        return GroupedOpenApi.builder()
                .group("14. 관리자 - 게시판 관리")
                .pathsToMatch("/admin/boards", "/admin/boards/**")
                .packagesToScan("com.nalsil.bear.controller.admin")
                .build();
    }

    /**
     * 관리자 FAQ 관리 API 그룹
     */
    @Bean
    public GroupedOpenApi adminFaqApi() {
        return GroupedOpenApi.builder()
                .group("15. 관리자 - FAQ 관리")
                .pathsToMatch("/admin/faqs", "/admin/faqs/**")
                .packagesToScan("com.nalsil.bear.controller.admin")
                .build();
    }

    /**
     * 관리자 QnA 관리 API 그룹
     */
    @Bean
    public GroupedOpenApi adminQnaApi() {
        return GroupedOpenApi.builder()
                .group("16. 관리자 - QnA 관리")
                .pathsToMatch("/admin/qnas", "/admin/qnas/**")
                .packagesToScan("com.nalsil.bear.controller.admin")
                .build();
    }

    /**
     * 관리자 유튜브 관리 API 그룹
     */
    @Bean
    public GroupedOpenApi adminYoutubeApi() {
        return GroupedOpenApi.builder()
                .group("17. 관리자 - 유튜브 관리")
                .pathsToMatch("/admin/youtube", "/admin/youtube/**")
                .packagesToScan("com.nalsil.bear.controller.admin")
                .build();
    }

    /**
     * 공개 페이지 - 홈 API 그룹
     */
    @Bean
    public GroupedOpenApi publicHomeApi() {
        return GroupedOpenApi.builder()
                .group("18. 공개 페이지 - 홈")
                .pathsToMatch("/{companyCode}", "/{companyCode}/", "/{companyCode}/about")
                .packagesToScan("com.nalsil.bear.controller.public_", "com.nalsil.bear.controller")
                .packagesToExclude("com.nalsil.bear.controller.admin")
                .build();
    }

    /**
     * 공개 페이지 - 상품 API 그룹
     */
    @Bean
    public GroupedOpenApi publicProductApi() {
        return GroupedOpenApi.builder()
                .group("19. 공개 페이지 - 상품")
                .pathsToMatch("/{companyCode}/products", "/{companyCode}/products/**")
                .packagesToScan("com.nalsil.bear.controller.public_")
                .packagesToExclude("com.nalsil.bear.controller.admin")
                .build();
    }

    /**
     * 공개 페이지 - 게시판 API 그룹
     */
    @Bean
    public GroupedOpenApi publicBoardApi() {
        return GroupedOpenApi.builder()
                .group("20. 공개 페이지 - 게시판")
                .pathsToMatch("/{companyCode}/board/**")
                .packagesToScan("com.nalsil.bear.controller.public_")
                .packagesToExclude("com.nalsil.bear.controller.admin")
                .build();
    }

    /**
     * 공개 페이지 - FAQ API 그룹
     */
    @Bean
    public GroupedOpenApi publicFaqApi() {
        return GroupedOpenApi.builder()
                .group("21. 공개 페이지 - FAQ")
                .pathsToMatch("/{companyCode}/faq", "/{companyCode}/faq/**")
                .packagesToScan("com.nalsil.bear.controller.public_")
                .packagesToExclude("com.nalsil.bear.controller.admin")
                .build();
    }

    /**
     * 공개 페이지 - QnA API 그룹
     */
    @Bean
    public GroupedOpenApi publicQnaApi() {
        return GroupedOpenApi.builder()
                .group("22. 공개 페이지 - QnA")
                .pathsToMatch("/{companyCode}/qna", "/{companyCode}/qna/**")
                .packagesToScan("com.nalsil.bear.controller.public_")
                .packagesToExclude("com.nalsil.bear.controller.admin")
                .build();
    }

    /**
     * 공개 페이지 - 유튜브 API 그룹
     */
    @Bean
    public GroupedOpenApi publicYoutubeApi() {
        return GroupedOpenApi.builder()
                .group("23. 공개 페이지 - 유튜브")
                .pathsToMatch("/{companyCode}/youtube", "/{companyCode}/youtube/**")
                .packagesToScan("com.nalsil.bear.controller.public_")
                .packagesToExclude("com.nalsil.bear.controller.admin")
                .build();
    }

    /**
     * 인덱스 (루트) API 그룹
     */
    @Bean
    public GroupedOpenApi indexApi() {
        return GroupedOpenApi.builder()
                .group("24. 인덱스")
                .pathsToMatch("/", "/favicon.ico")
                .packagesToScan("com.nalsil.bear.controller")
                .packagesToExclude("com.nalsil.bear.controller.admin", "com.nalsil.bear.controller.public_")
                .build();
    }


    /**
     * Operation Customizer
     * 모든 핸들러 메서드를 Swagger에 포함시키기 위한 커스터마이저
     */
    @Bean
    public OperationCustomizer operationCustomizer() {
        return (operation, handlerMethod) -> {
            // Mono<String>, Mono<Rendering> 등을 반환하는 메서드도 포함
            // 응답 타입을 text/html로 명시
            if (handlerMethod.getReturnType().getParameterType().getName().contains("Mono")) {
                io.swagger.v3.oas.models.responses.ApiResponses responses = operation.getResponses();
                if (responses == null) {
                    responses = new io.swagger.v3.oas.models.responses.ApiResponses();
                    operation.setResponses(responses);
                }

                responses.addApiResponse("200",
                    new io.swagger.v3.oas.models.responses.ApiResponse()
                        .description("성공 (HTML 페이지 반환)")
                        .content(new io.swagger.v3.oas.models.media.Content()
                            .addMediaType("text/html",
                                new io.swagger.v3.oas.models.media.MediaType())));
            }
            return operation;
        };
    }
}
