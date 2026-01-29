package com.nalsil.bear.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 통일된 API 응답 래퍼
 * 모든 REST API 응답에 사용되는 표준 응답 형식
 *
 * @param <T> 응답 데이터 타입
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    /**
     * 응답 성공 여부
     */
    private boolean success;

    /**
     * 응답 메시지
     */
    private String message;

    /**
     * 응답 데이터
     */
    private T data;

    /**
     * 에러 코드 (실패 시)
     */
    private String errorCode;

    /**
     * 응답 시간
     */
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    /**
     * 성공 응답 생성 (데이터 포함)
     *
     * @param data 응답 데이터
     * @param <T>  데이터 타입
     * @return 성공 응답
     */
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message("Success")
                .data(data)
                .build();
    }

    /**
     * 성공 응답 생성 (메시지와 데이터 포함)
     *
     * @param message 응답 메시지
     * @param data    응답 데이터
     * @param <T>     데이터 타입
     * @return 성공 응답
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .build();
    }

    /**
     * 성공 응답 생성 (데이터 없음)
     *
     * @return 성공 응답
     */
    public static <T> ApiResponse<T> success() {
        return ApiResponse.<T>builder()
                .success(true)
                .message("Success")
                .build();
    }

    /**
     * 성공 응답 생성 (메시지만)
     *
     * @param message 응답 메시지
     * @return 성공 응답
     */
    public static <T> ApiResponse<T> success(String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .build();
    }

    /**
     * 실패 응답 생성
     *
     * @param message 에러 메시지
     * @param <T>     데이터 타입
     * @return 실패 응답
     */
    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .build();
    }

    /**
     * 실패 응답 생성 (에러 코드 포함)
     *
     * @param message   에러 메시지
     * @param errorCode 에러 코드
     * @param <T>       데이터 타입
     * @return 실패 응답
     */
    public static <T> ApiResponse<T> error(String message, String errorCode) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .errorCode(errorCode)
                .build();
    }
}
