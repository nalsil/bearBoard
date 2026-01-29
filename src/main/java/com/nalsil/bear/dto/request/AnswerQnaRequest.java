package com.nalsil.bear.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * QnA 답변 요청 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnswerQnaRequest {

    /**
     * 답변 내용
     */
    private String answerBody;
}
