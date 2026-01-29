package com.nalsil.bear.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 상품 생성 요청 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateProductRequest {

    /**
     * 상품명
     */
    private String name;

    /**
     * 카테고리
     */
    private String category;

    /**
     * 설명
     */
    private String description;

    /**
     * 가격
     */
    private BigDecimal price;

    /**
     * 이미지 URL
     */
    private String imageUrl;

    /**
     * 표시 순서
     */
    private Integer displayOrder;

    /**
     * 숨김 여부
     */
    private Boolean isHidden;
}
