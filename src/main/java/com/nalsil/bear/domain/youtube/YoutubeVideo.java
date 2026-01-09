package com.nalsil.bear.domain.youtube;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

/**
 * YoutubeVideo 엔티티
 * 유튜브 영상 정보를 저장하는 엔티티
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("youtube_video")
public class YoutubeVideo {

    @Id
    private Long id;

    /**
     * 소속 기업 ID
     */
    @Column("company_id")
    private Long companyId;

    /**
     * 영상 URL
     */
    @Column("video_url")
    private String videoUrl;

    /**
     * 제목
     */
    @Column("title")
    private String title;

    /**
     * 설명
     */
    @Column("description")
    private String description;

    /**
     * 썸네일 URL
     */
    @Column("thumbnail_url")
    private String thumbnailUrl;

    /**
     * 표시 순서
     */
    @Column("display_order")
    private Integer displayOrder;

    /**
     * 숨김 여부
     */
    @Column("is_hidden")
    private Boolean isHidden;

    /**
     * 생성일시
     */
    @Column("created_at")
    private LocalDateTime createdAt;
}
