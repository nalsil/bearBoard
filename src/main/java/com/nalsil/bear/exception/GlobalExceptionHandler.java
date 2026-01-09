package com.nalsil.bear.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * GlobalExceptionHandler
 * 전역 예외 처리 핸들러
 *
 * 임시로 비활성화: @RestControllerAdvice가 @Controller와 충돌하는지 확인
 */
@Slf4j
// @RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * CompanyNotFoundException 처리
     *
     * @param ex CompanyNotFoundException
     * @param exchange ServerWebExchange
     * @return 에러 응답 (Mono<ResponseEntity<Map>>)
     */
    @ExceptionHandler(CompanyNotFoundException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleCompanyNotFoundException(
            CompanyNotFoundException ex,
            ServerWebExchange exchange) {

        log.error("Company not found: {}", ex.getMessage());

        Map<String, Object> errorResponse = createErrorResponse(
                HttpStatus.NOT_FOUND,
                ex.getMessage(),
                exchange.getRequest().getPath().value()
        );

        return Mono.just(ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(errorResponse));
    }

    /**
     * UnauthorizedAccessException 처리
     *
     * @param ex UnauthorizedAccessException
     * @param exchange ServerWebExchange
     * @return 에러 응답 (Mono<ResponseEntity<Map>>)
     */
    @ExceptionHandler(UnauthorizedAccessException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleUnauthorizedAccessException(
            UnauthorizedAccessException ex,
            ServerWebExchange exchange) {

        log.error("Unauthorized access: {}", ex.getMessage());

        Map<String, Object> errorResponse = createErrorResponse(
                HttpStatus.FORBIDDEN,
                ex.getMessage(),
                exchange.getRequest().getPath().value()
        );

        return Mono.just(ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(errorResponse));
    }

    /**
     * IllegalArgumentException 처리
     *
     * @param ex IllegalArgumentException
     * @param exchange ServerWebExchange
     * @return 에러 응답 (Mono<ResponseEntity<Map>>)
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleIllegalArgumentException(
            IllegalArgumentException ex,
            ServerWebExchange exchange) {

        log.error("Illegal argument: {}", ex.getMessage());

        Map<String, Object> errorResponse = createErrorResponse(
                HttpStatus.BAD_REQUEST,
                ex.getMessage(),
                exchange.getRequest().getPath().value()
        );

        return Mono.just(ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse));
    }

    /**
     * ResponseStatusException 처리
     * 주로 404 Not Found 등의 상태 예외 처리
     *
     * @param ex ResponseStatusException
     * @param exchange ServerWebExchange
     * @return 에러 응답 (Mono<ResponseEntity<Map>>)
     */
    @ExceptionHandler(ResponseStatusException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleResponseStatusException(
            ResponseStatusException ex,
            ServerWebExchange exchange) {

        String path = exchange.getRequest().getPath().value();

        // Static 리소스에 대한 404는 WARN 레벨로 로깅 (덜 심각함)
        if (ex.getStatusCode() == HttpStatus.NOT_FOUND && isStaticResource(path)) {
            log.warn("Static resource not found: {}", path);
        } else {
            log.error("Response status exception: {} - {}", ex.getStatusCode(), path);
        }

        Map<String, Object> errorResponse = createErrorResponse(
                HttpStatus.valueOf(ex.getStatusCode().value()),
                ex.getReason() != null ? ex.getReason() : "요청을 처리할 수 없습니다.",
                path
        );

        return Mono.just(ResponseEntity
                .status(ex.getStatusCode())
                .body(errorResponse));
    }

    /**
     * 일반 예외 처리
     *
     * @param ex Exception
     * @param exchange ServerWebExchange
     * @return 에러 응답 (Mono<ResponseEntity<Map>>)
     */
    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleGeneralException(
            Exception ex,
            ServerWebExchange exchange) {

        log.error("Unexpected error occurred", ex);

        Map<String, Object> errorResponse = createErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "서버 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.",
                exchange.getRequest().getPath().value()
        );

        return Mono.just(ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorResponse));
    }

    /**
     * Static 리소스 경로인지 확인
     *
     * @param path 요청 경로
     * @return static 리소스 여부
     */
    private boolean isStaticResource(String path) {
        return path.startsWith("/css/") ||
               path.startsWith("/js/") ||
               path.startsWith("/images/") ||
               path.startsWith("/fonts/") ||
               path.endsWith(".ico") ||
               path.endsWith(".css") ||
               path.endsWith(".js") ||
               path.endsWith(".png") ||
               path.endsWith(".jpg") ||
               path.endsWith(".jpeg") ||
               path.endsWith(".gif") ||
               path.endsWith(".svg") ||
               path.endsWith(".woff") ||
               path.endsWith(".woff2") ||
               path.endsWith(".ttf");
    }

    /**
     * 에러 응답 생성
     *
     * @param status HTTP 상태
     * @param message 에러 메시지
     * @param path 요청 경로
     * @return 에러 응답 맵
     */
    private Map<String, Object> createErrorResponse(HttpStatus status, String message, String path) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now().toString());
        errorResponse.put("status", status.value());
        errorResponse.put("error", status.getReasonPhrase());
        errorResponse.put("message", message);
        errorResponse.put("path", path);
        return errorResponse;
    }
}
