package com.nalsil.bear.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * QnA 응답 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QnaResponse {

    private Long id;
    private Long companyId;
    private String questionTitle;
    private String questionBody;
    private String askerEmail;
    private String answerBody;
    private Long answererId;
    private Boolean isAnswered;
    private Boolean isHidden;
    private LocalDateTime createdAt;
    private LocalDateTime answeredAt;
}
