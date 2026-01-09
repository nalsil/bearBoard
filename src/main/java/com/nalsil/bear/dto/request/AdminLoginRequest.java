package com.nalsil.bear.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 관리자 로그인 요청 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminLoginRequest {

    /**
     * 사용자명
     */
    private String username;

    /**
     * 비밀번호
     */
    private String password;
}
