package com.nalsil.bear.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 인증 응답 DTO
 * 로그인 성공 시 반환되는 사용자 정보
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthResponse {

    /**
     * 관리자 ID
     */
    private Long adminId;

    /**
     * 사용자명
     */
    private String username;

    /**
     * 관리자 이름
     */
    private String name;

    /**
     * 이메일
     */
    private String email;

    /**
     * 역할 (ADMIN, SUPER_ADMIN)
     */
    private String role;

    /**
     * 소속 기업 ID (SUPER_ADMIN은 null 가능)
     */
    private Long companyId;

    /**
     * 소속 기업 코드
     */
    private String companyCode;

    /**
     * 소속 기업 이름
     */
    private String companyName;
}
