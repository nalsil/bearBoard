package com.nalsil.bear.domain.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Product 엔티티
 * 상품 정보를 저장하는 엔티티
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("product")
public class Product {

    @Id
    private Long id;

    /**
     * 소속 기업 ID
     */
    @Column("company_id")
    private Long companyId;

    /**
     * 상품명
     */
    @Column("name")
    private String name;

    /**
     * 카테고리
     */
    @Column("category")
    private String category;

    /**
     * 설명
     */
    @Column("description")
    private String description;

    /**
     * 가격
     */
    @Column("price")
    private BigDecimal price;

    /**
     * 이미지 URL
     */
    @Column("image_url")
    private String imageUrl;

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

    /**
     * 수정일시
     */
    @Column("updated_at")
    private LocalDateTime updatedAt;
}
