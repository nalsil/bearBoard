package com.nalsil.bear.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 페이지네이션 응답 DTO
 * 목록 조회 시 페이지 정보와 함께 데이터를 반환
 *
 * @param <T> 목록 아이템 타입
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PagedResponse<T> {

    /**
     * 데이터 목록
     */
    private List<T> content;

    /**
     * 현재 페이지 번호 (0부터 시작)
     */
    private int page;

    /**
     * 페이지 크기
     */
    private int size;

    /**
     * 전체 요소 수
     */
    private long totalElements;

    /**
     * 전체 페이지 수
     */
    private int totalPages;

    /**
     * 첫 페이지 여부
     */
    private boolean first;

    /**
     * 마지막 페이지 여부
     */
    private boolean last;

    /**
     * 현재 페이지 요소 수
     */
    private int numberOfElements;

    /**
     * 빈 페이지 여부
     */
    private boolean empty;

    /**
     * PagedResponse 생성
     *
     * @param content       데이터 목록
     * @param page          현재 페이지 번호
     * @param size          페이지 크기
     * @param totalElements 전체 요소 수
     * @param <T>           데이터 타입
     * @return PagedResponse
     */
    public static <T> PagedResponse<T> of(List<T> content, int page, int size, long totalElements) {
        int totalPages = (int) Math.ceil((double) totalElements / size);

        return PagedResponse.<T>builder()
                .content(content)
                .page(page)
                .size(size)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .first(page == 0)
                .last(page >= totalPages - 1)
                .numberOfElements(content.size())
                .empty(content.isEmpty())
                .build();
    }

    /**
     * 빈 PagedResponse 생성
     *
     * @param page 현재 페이지 번호
     * @param size 페이지 크기
     * @param <T>  데이터 타입
     * @return 빈 PagedResponse
     */
    public static <T> PagedResponse<T> empty(int page, int size) {
        return PagedResponse.<T>builder()
                .content(List.of())
                .page(page)
                .size(size)
                .totalElements(0)
                .totalPages(0)
                .first(true)
                .last(true)
                .numberOfElements(0)
                .empty(true)
                .build();
    }
}
