package com.nalsil.bear.config;

import com.nalsil.bear.filter.JwtAuthenticationFilter;
import com.nalsil.bear.filter.TenantFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.config.CorsRegistry;
import org.springframework.web.reactive.config.ResourceHandlerRegistry;
import org.springframework.web.reactive.config.ViewResolverRegistry;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.server.WebFilter;
import org.thymeleaf.spring6.view.reactive.ThymeleafReactiveViewResolver;

/**
 * WebFlux 설정 클래스
 * CORS, 필터 등 웹 설정
 *
 * 참고: @EnableWebFlux를 사용하지 않음 - Spring Boot 자동 설정 활용
 * @EnableWebFlux는 자동 설정을 비활성화하므로 정적 리소스 처리에 문제 발생 가능
 */
@Configuration
public class WebFluxConfig implements WebFluxConfigurer {

    private final ThymeleafReactiveViewResolver thymeleafReactiveViewResolver;

    public WebFluxConfig(ThymeleafReactiveViewResolver thymeleafReactiveViewResolver) {
        this.thymeleafReactiveViewResolver = thymeleafReactiveViewResolver;
    }

    /**
     * 뷰 리졸버 설정
     * Thymeleaf 뷰 리졸버 등록
     *
     * @param registry ViewResolverRegistry
     */
    @Override
    public void configureViewResolvers(ViewResolverRegistry registry) {
        registry.viewResolver(thymeleafReactiveViewResolver);
    }

    /**
     * Static 리소스 핸들러 설정
     * CSS, JS, 이미지 등 정적 리소스 제공
     *
     * @param registry ResourceHandlerRegistry
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/css/**")
                .addResourceLocations("classpath:/static/css/");

        registry.addResourceHandler("/js/**")
                .addResourceLocations("classpath:/static/js/");

        registry.addResourceHandler("/images/**")
                .addResourceLocations("classpath:/static/images/");

        registry.addResourceHandler("/favicon.ico")
                .addResourceLocations("classpath:/static/");

        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/");
    }

    /**
     * CORS 설정 - 임시 비활성화 (403 오류 디버깅)
     * 개발 환경용 설정, 운영 환경에서는 허용 도메인을 명시적으로 지정 필요
     *
     * @param registry CorsRegistry
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")  // 모든 Origin 허용 (개발용)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    /**
     * 멀티테넌트 필터 Bean 등록 - 임시 비활성화 (403 오류 디버깅)
     * URL에서 기업 코드를 추출하여 컨텍스트에 설정
     *
     * @return TenantFilter
     */
    // @Bean
    // public TenantFilter tenantFilter() {
    //     return new TenantFilter();
    // }

}
