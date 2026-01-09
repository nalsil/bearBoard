package com.nalsil.bear.domain.post;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

/**
 * Post 엔티티
 * 게시글 정보를 저장하는 엔티티
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("post")
public class Post {

    @Id
    private Long id;

    /**
     * 소속 게시판 ID
     */
    @Column("board_id")
    private Long boardId;

    /**
     * 제목
     */
    @Column("title")
    private String title;

    /**
     * 내용
     */
    @Column("content")
    private String content;

    /**
     * 작성자
     */
    @Column("author")
    private String author;

    /**
     * 조회수
     */
    @Column("view_count")
    private Integer viewCount;

    /**
     * 첨부 파일 경로
     */
    @Column("file_path")
    private String filePath;

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

    /**
     * 수정일시
     */
    @Column("updated_at")
    private LocalDateTime updatedAt;
}
