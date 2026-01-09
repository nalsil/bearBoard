package com.nalsil.bear.domain.qna;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

/**
 * QnA 엔티티
 * 질문과 답변 정보를 저장하는 엔티티
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("qna")
public class Qna {

    @Id
    private Long id;

    /**
     * 소속 기업 ID
     */
    @Column("company_id")
    private Long companyId;

    /**
     * 질문 제목
     */
    @Column("question_title")
    private String questionTitle;

    /**
     * 질문 내용
     */
    @Column("question_body")
    private String questionBody;

    /**
     * 질문자 이메일
     */
    @Column("asker_email")
    private String askerEmail;

    /**
     * 답변 내용
     */
    @Column("answer_body")
    private String answerBody;

    /**
     * 답변자 ID (Admin)
     */
    @Column("answerer_id")
    private Long answererId;

    /**
     * 답변 완료 여부
     */
    @Column("is_answered")
    private Boolean isAnswered;

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
     * 답변일시
     */
    @Column("answered_at")
    private LocalDateTime answeredAt;
}
