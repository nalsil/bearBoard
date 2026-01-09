package com.nalsil.bear.util;

import reactor.core.publisher.Mono;
import reactor.util.context.Context;

/**
 * TenantContextHolder
 * ThreadLocal 대신 Reactor Context를 사용한 테넌트 컨텍스트 관리
 * 리액티브 스트림에서 안전하게 테넌트 정보를 관리
 */
public class TenantContextHolder {

    /**
     * Reactor Context 키 (기업 코드 저장용)
     */
    private static final String TENANT_CONTEXT_KEY = "companyCode";

    /**
     * 현재 컨텍스트에서 기업 코드 조회
     *
     * @return 기업 코드 (Mono<String>)
     */
    public static Mono<String> getCurrentTenant() {
        return Mono.deferContextual(ctx ->
                ctx.hasKey(TENANT_CONTEXT_KEY)
                        ? Mono.just(ctx.get(TENANT_CONTEXT_KEY))
                        : Mono.empty()
        );
    }

    /**
     * 컨텍스트에 기업 코드 설정
     *
     * @param context 현재 컨텍스트
     * @param companyCode 기업 코드
     * @return 기업 코드가 설정된 새 컨텍스트
     */
    public static Context setCurrentTenant(Context context, String companyCode) {
        return context.put(TENANT_CONTEXT_KEY, companyCode);
    }

    /**
     * 컨텍스트에서 기업 코드 제거
     *
     * @param context 현재 컨텍스트
     * @return 기업 코드가 제거된 새 컨텍스트
     */
    public static Context clearCurrentTenant(Context context) {
        return context.delete(TENANT_CONTEXT_KEY);
    }

    /**
     * Mono에 기업 코드 컨텍스트 추가
     *
     * @param mono Mono 인스턴스
     * @param companyCode 기업 코드
     * @param <T> Mono 타입
     * @return 컨텍스트가 설정된 Mono
     */
    public static <T> Mono<T> withTenant(Mono<T> mono, String companyCode) {
        return mono.contextWrite(context -> setCurrentTenant(context, companyCode));
    }
}
