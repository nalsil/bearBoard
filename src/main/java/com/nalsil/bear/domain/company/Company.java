package com.nalsil.bear.domain.company;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Company 엔티티
 * 멀티테넌트 기업 정보를 저장하는 핵심 엔티티
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("company")
public class Company {

    @Id
    private Long id;

    /**
     * 기업 코드 (URL path에 사용, 유니크)
     * 예: company-a, company-b
     */
    @Column("code")
    private String code;

    /**
     * 기업명
     */
    @Column("name")
    private String name;

    /**
     * 로고 URL
     */
    @Column("logo_url")
    private String logoUrl;

    /**
     * 기본 색상 (Primary Color, HEX 코드)
     */
    @Column("primary_color")
    private String primaryColor;

    /**
     * 보조 색상 (Secondary Color, HEX 코드)
     */
    @Column("secondary_color")
    private String secondaryColor;

    /**
     * 기업 설명
     */
    @Column("description")
    private String description;

    /**
     * 주소
     */
    @Column("address")
    private String address;

    /**
     * 전화번호
     */
    @Column("phone")
    private String phone;

    /**
     * 이메일
     */
    @Column("email")
    private String email;

    /**
     * 위도 (지도 표시용)
     */
    @Column("latitude")
    private BigDecimal latitude;

    /**
     * 경도 (지도 표시용)
     */
    @Column("longitude")
    private BigDecimal longitude;

    /**
     * 설립일
     */
    @Column("founded_date")
    private LocalDate foundedDate;

    /**
     * 활성화 여부
     */
    @Column("is_active")
    private Boolean isActive;

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
