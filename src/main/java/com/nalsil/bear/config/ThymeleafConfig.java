package com.nalsil.bear.config;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.spring6.SpringWebFluxTemplateEngine;
import org.thymeleaf.spring6.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.spring6.view.reactive.ThymeleafReactiveViewResolver;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.extras.springsecurity6.dialect.SpringSecurityDialect;

/**
 * Thymeleaf 설정 클래스
 * Thymeleaf 템플릿 엔진 및 Spring Security 통합 설정
 */
@Configuration
public class ThymeleafConfig {

    /**
     * Thymeleaf 템플릿 리졸버 설정
     *
     * @param applicationContext Spring ApplicationContext
     * @return SpringResourceTemplateResolver
     */
    @Bean
    public SpringResourceTemplateResolver templateResolver(ApplicationContext applicationContext) {
        SpringResourceTemplateResolver resolver = new SpringResourceTemplateResolver();
        resolver.setApplicationContext(applicationContext);
        resolver.setPrefix("classpath:/templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setCharacterEncoding("UTF-8");
        resolver.setCacheable(false); // 개발 환경용, 운영 환경에서는 true로 변경
        resolver.setCheckExistence(true);
        return resolver;
    }

    /**
     * Thymeleaf WebFlux 템플릿 엔진 설정
     *
     * @param applicationContext Spring ApplicationContext
     * @return SpringWebFluxTemplateEngine
     */
    @Bean
    public SpringWebFluxTemplateEngine templateEngine(ApplicationContext applicationContext) {
        SpringWebFluxTemplateEngine engine = new SpringWebFluxTemplateEngine();
        engine.setTemplateResolver(templateResolver(applicationContext));
        engine.setEnableSpringELCompiler(true);
        // Spring Security 다이얼렉트 추가
        engine.addDialect(new SpringSecurityDialect());
        return engine;
    }

    /**
     * Thymeleaf 리액티브 뷰 리졸버 설정
     *
     * @param applicationContext Spring ApplicationContext
     * @return ThymeleafReactiveViewResolver
     */
    @Bean
    public ThymeleafReactiveViewResolver thymeleafReactiveViewResolver(ApplicationContext applicationContext) {
        ThymeleafReactiveViewResolver viewResolver = new ThymeleafReactiveViewResolver();
        viewResolver.setTemplateEngine(templateEngine(applicationContext));
        viewResolver.setResponseMaxChunkSizeBytes(8192);
        viewResolver.setDefaultCharset(java.nio.charset.StandardCharsets.UTF_8);
        viewResolver.setOrder(1); // 뷰 리졸버 우선순위 설정
        return viewResolver;
    }
}
