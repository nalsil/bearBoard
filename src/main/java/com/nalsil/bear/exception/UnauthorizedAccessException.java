package com.nalsil.bear.exception;

/**
 * UnauthorizedAccessException
 * 권한이 없는 리소스에 접근할 때 발생하는 예외
 */
public class UnauthorizedAccessException extends RuntimeException {

    /**
     * 기본 생성자
     */
    public UnauthorizedAccessException() {
        super("접근 권한이 없습니다.");
    }

    /**
     * 메시지를 포함한 생성자
     *
     * @param message 예외 메시지
     */
    public UnauthorizedAccessException(String message) {
        super(message);
    }

    /**
     * 메시지와 원인을 포함한 생성자
     *
     * @param message 예외 메시지
     * @param cause 원인
     */
    public UnauthorizedAccessException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 리소스 타입과 ID로 예외 생성
     *
     * @param resourceType 리소스 타입 (예: "게시글", "상품")
     * @param resourceId 리소스 ID
     * @return UnauthorizedAccessException
     */
    public static UnauthorizedAccessException forResource(String resourceType, Long resourceId) {
        return new UnauthorizedAccessException(
                String.format("%s(ID: %d)에 대한 접근 권한이 없습니다.", resourceType, resourceId)
        );
    }
}
