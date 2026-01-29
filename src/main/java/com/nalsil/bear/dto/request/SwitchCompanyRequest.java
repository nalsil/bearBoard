package com.nalsil.bear.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 기업 전환 요청 DTO
 * 슈퍼관리자가 다른 기업으로 전환할 때 사용
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SwitchCompanyRequest {

    /**
     * 전환할 기업 ID
     */
    private Long companyId;
}
