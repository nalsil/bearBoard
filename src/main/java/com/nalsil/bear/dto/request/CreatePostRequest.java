package com.nalsil.bear.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 게시글 생성/수정 요청 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePostRequest {

    /**
     * 게시판 ID
     */
    private Long boardId;

    /**
     * 제목
     */
    private String title;

    /**
     * 내용
     */
    private String content;

    /**
     * 숨김 여부
     */
    private Boolean isHidden;

    /**
     * 첨부 파일 경로 (선택 사항)
     */
    private String filePath;
}
