package com.nalsil.bear.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 게시글 응답 DTO
 * MapStruct를 사용하여 Post 엔티티와 매핑됩니다.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostResponse {

    private Long id;
    private Long boardId;
    private String title;
    private String content;
    private Integer viewCount;
    private Boolean isHidden;
    private String filePath;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
