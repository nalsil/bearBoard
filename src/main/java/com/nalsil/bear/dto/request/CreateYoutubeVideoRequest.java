package com.nalsil.bear.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 유튜브 영상 생성/수정 요청 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateYoutubeVideoRequest {

    /**
     * 유튜브 영상 URL
     */
    private String videoUrl;

    /**
     * 제목
     */
    private String title;

    /**
     * 설명
     */
    private String description;

    /**
     * 썸네일 URL (선택, 미입력 시 자동 생성)
     */
    private String thumbnailUrl;

    /**
     * 표시 순서
     */
    private Integer displayOrder;

    /**
     * 숨김 여부
     */
    private Boolean isHidden;
}
