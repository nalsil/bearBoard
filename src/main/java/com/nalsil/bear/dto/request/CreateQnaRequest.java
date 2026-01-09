package com.nalsil.bear.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * QnA 질문 등록 요청 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateQnaRequest {

    /**
     * 질문 제목
     */
    private String questionTitle;

    /**
     * 질문 내용
     */
    private String questionBody;

    /**
     * 질문자 이메일
     */
    private String askerEmail;

    /**
     * reCAPTCHA 토큰
     */
    private String recaptchaToken;
}
