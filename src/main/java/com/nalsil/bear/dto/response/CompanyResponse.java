package com.nalsil.bear.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 회사 응답 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyResponse {

    private Long id;
    private String code;
    private String name;
    private String logoUrl;
    private String primaryColor;
    private String secondaryColor;
    private String description;
    private String address;
    private String phone;
    private String email;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private LocalDate foundedDate;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
