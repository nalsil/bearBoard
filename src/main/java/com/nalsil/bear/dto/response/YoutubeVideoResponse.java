package com.nalsil.bear.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 유튜브 영상 응답 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class YoutubeVideoResponse {

    private Long id;
    private Long companyId;
    private String videoUrl;
    private String title;
    private String description;
    private String thumbnailUrl;
    private Integer displayOrder;
    private Boolean isHidden;
    private LocalDateTime createdAt;
}
