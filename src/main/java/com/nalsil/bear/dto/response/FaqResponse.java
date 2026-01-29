package com.nalsil.bear.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * FAQ 응답 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FaqResponse {

    private Long id;
    private Long companyId;
    private String category;
    private String question;
    private String answer;
    private Integer displayOrder;
    private Boolean isHidden;
    private LocalDateTime createdAt;
}
