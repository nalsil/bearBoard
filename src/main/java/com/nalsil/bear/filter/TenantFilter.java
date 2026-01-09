package com.nalsil.bear.filter;

import com.nalsil.bear.util.TenantContextHolder;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * TenantFilter
 * URL에서 기업 코드를 추출하여 Reactor Context에 설정하는 필터
 *
 * URL 패턴: /{companyCode}/...
 * 예: /company-a/products, /company-b/about
 */
// @Component - 임시 비활성화 (403 오류 디버깅)
@Order(1)
public class TenantFilter implements WebFilter {

    /**
     * URL 패턴: /{companyCode}/... 형식에서 companyCode 추출
     */
    private static final Pattern TENANT_PATTERN = Pattern.compile("^/([a-z0-9-]+)(/.*)?$");

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        // 정적 리소스 및 관리자 페이지는 필터링 제외
        if (path.startsWith("/css") || path.startsWith("/js") ||
            path.startsWith("/images") || path.startsWith("/admin") ||
            path.startsWith("/superadmin") || path.equals("/favicon.ico")) {
            return chain.filter(exchange);
        }

        Matcher matcher = TENANT_PATTERN.matcher(path);

        if (matcher.matches()) {
            // URL에서 기업 코드 추출
            String companyCode = matcher.group(1);

            // Reactor Context에 기업 코드 설정하여 체인 진행
            return chain.filter(exchange)
                    .contextWrite(ctx -> TenantContextHolder.setCurrentTenant(ctx, companyCode));
        }

        // 기업 코드가 없는 경로는 그대로 진행
        return chain.filter(exchange);
    }
}
