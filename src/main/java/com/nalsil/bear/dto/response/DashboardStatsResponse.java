package com.nalsil.bear.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 대시보드 통계 응답 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsResponse {

    /**
     * 상품 수
     */
    private long productCount;

    /**
     * 게시글 수
     */
    private long postCount;

    /**
     * FAQ 수
     */
    private long faqCount;

    /**
     * QnA 수
     */
    private long qnaCount;

    /**
     * 답변 대기 QnA 수
     */
    private long pendingQnaCount;

    /**
     * 유튜브 영상 수
     */
    private long youtubeCount;

    /**
     * 기업 정보
     */
    private CompanyInfo company;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CompanyInfo {
        private Long id;
        private String code;
        private String name;
        private String logoUrl;
    }
}
