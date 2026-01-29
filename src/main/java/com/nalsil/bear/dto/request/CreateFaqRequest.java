package com.nalsil.bear.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * FAQ 생성/수정 요청 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateFaqRequest {

    /**
     * 카테고리
     */
    private String category;

    /**
     * 질문
     */
    private String question;

    /**
     * 답변
     */
    private String answer;

    /**
     * 표시 순서
     */
    private Integer displayOrder;

    /**
     * 숨김 여부
     */
    private Boolean isHidden;
}
