package com.nalsil.bear.domain.board;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

/**
 * Board 엔티티
 * 게시판 정보를 저장하는 엔티티
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("board")
public class Board {

    @Id
    private Long id;

    /**
     * 소속 기업 ID
     */
    @Column("company_id")
    private Long companyId;

    /**
     * 게시판 이름
     */
    @Column("name")
    private String name;

    /**
     * 게시판 타입 (notice, press, recruit 등)
     */
    @Column("type")
    private String type;

    /**
     * 생성일시
     */
    @Column("created_at")
    private LocalDateTime createdAt;
}
