package com.nalsil.bear.exception;

import com.nalsil.bear.dto.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * API 전용 예외 처리 핸들러
 * /api/** 경로에 대한 예외를 ApiResponse 형식으로 처리
 */
@Slf4j
@RestControllerAdvice(basePackages = "com.nalsil.bear.controller.api")
public class ApiExceptionHandler {

    /**
     * CompanyNotFoundException 처리
     */
    @ExceptionHandler(CompanyNotFoundException.class)
    public Mono<ResponseEntity<ApiResponse<Void>>> handleCompanyNotFoundException(
            CompanyNotFoundException ex,
            ServerWebExchange exchange) {

        log.error("Company not found: path={}, message={}",
                exchange.getRequest().getPath().value(), ex.getMessage());

        return Mono.just(ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage(), "COMPANY_NOT_FOUND")));
    }

    /**
     * UnauthorizedAccessException 처리
     */
    @ExceptionHandler(UnauthorizedAccessException.class)
    public Mono<ResponseEntity<ApiResponse<Void>>> handleUnauthorizedAccessException(
            UnauthorizedAccessException ex,
            ServerWebExchange exchange) {

        log.error("Unauthorized access: path={}, message={}",
                exchange.getRequest().getPath().value(), ex.getMessage());

        return Mono.just(ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error(ex.getMessage(), "ACCESS_DENIED")));
    }

    /**
     * IllegalArgumentException 처리
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public Mono<ResponseEntity<ApiResponse<Void>>> handleIllegalArgumentException(
            IllegalArgumentException ex,
            ServerWebExchange exchange) {

        log.error("Illegal argument: path={}, message={}",
                exchange.getRequest().getPath().value(), ex.getMessage());

        return Mono.just(ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getMessage(), "INVALID_ARGUMENT")));
    }

    /**
     * WebExchangeBindException 처리 (Validation 실패)
     */
    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<ApiResponse<Void>>> handleValidationException(
            WebExchangeBindException ex,
            ServerWebExchange exchange) {

        String message = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .orElse("유효성 검증에 실패했습니다.");

        log.error("Validation failed: path={}, message={}",
                exchange.getRequest().getPath().value(), message);

        return Mono.just(ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(message, "VALIDATION_FAILED")));
    }

    /**
     * ResponseStatusException 처리
     */
    @ExceptionHandler(ResponseStatusException.class)
    public Mono<ResponseEntity<ApiResponse<Void>>> handleResponseStatusException(
            ResponseStatusException ex,
            ServerWebExchange exchange) {

        log.error("Response status exception: path={}, status={}, reason={}",
                exchange.getRequest().getPath().value(),
                ex.getStatusCode(),
                ex.getReason());

        String message = ex.getReason() != null ? ex.getReason() : "요청을 처리할 수 없습니다.";
        String errorCode = "HTTP_" + ex.getStatusCode().value();

        return Mono.just(ResponseEntity
                .status(ex.getStatusCode())
                .body(ApiResponse.error(message, errorCode)));
    }

    /**
     * 일반 예외 처리
     */
    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ApiResponse<Void>>> handleGeneralException(
            Exception ex,
            ServerWebExchange exchange) {

        log.error("Unexpected error occurred: path={}",
                exchange.getRequest().getPath().value(), ex);

        return Mono.just(ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("서버 오류가 발생했습니다.", "INTERNAL_ERROR")));
    }
}
