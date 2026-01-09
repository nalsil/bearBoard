package com.nalsil.bear.domain.faq;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

/**
 * FAQ 엔티티
 * 자주 묻는 질문 정보를 저장하는 엔티티
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("faq")
public class Faq {

    @Id
    private Long id;

    /**
     * 소속 기업 ID
     */
    @Column("company_id")
    private Long companyId;

    /**
     * 카테고리
     */
    @Column("category")
    private String category;

    /**
     * 질문
     */
    @Column("question")
    private String question;

    /**
     * 답변
     */
    @Column("answer")
    private String answer;

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
