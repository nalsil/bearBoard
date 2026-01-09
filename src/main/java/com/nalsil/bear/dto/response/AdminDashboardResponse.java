package com.nalsil.bear.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 관리자 대시보드 응답 DTO
 *
 * 대시보드에 표시될 통계 정보를 담습니다.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardResponse {

    /**
     * 기업명
     */
    private String companyName;

    /**
     * 전체 게시글 수
     */
    private Long totalPosts;

    /**
     * 전체 FAQ 수
     */
    private Long totalFaqs;

    /**
     * 미답변 QnA 수
     */
    private Long unansweredQnas;

    /**
     * 전체 유튜브 영상 수
     */
    private Long totalYoutubeVideos;

    /**
     * 전체 상품 수
     */
    private Long totalProducts;

    /**
     * 이번 달 신규 게시글 수
     */
    private Long newPostsThisMonth;

    /**
     * 이번 달 신규 QnA 수
     */
    private Long newQnasThisMonth;
}
