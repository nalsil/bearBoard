package com.nalsil.bear.controller.api.admin;

import com.nalsil.bear.domain.company.Company;
import com.nalsil.bear.dto.response.ApiResponse;
import com.nalsil.bear.dto.response.CompanyResponse;
import com.nalsil.bear.service.CompanyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * 회사 관리 API 컨트롤러
 */
@Tag(name = "회사 관리 API", description = "회사 조회 API (슈퍼관리자)")
@Slf4j
@RestController
@RequestMapping("/api/admin/companies")
@RequiredArgsConstructor
@SecurityRequirement(name = "JWT Cookie")
public class CompanyApiController {

    private final CompanyService companyService;

    /**
     * 회사 목록 조회 (슈퍼관리자 전용)
     */
    @Operation(summary = "회사 목록 조회", description = "모든 회사 목록을 조회합니다. (슈퍼관리자 전용)")
    @GetMapping
    public Mono<ResponseEntity<ApiResponse<List<CompanyResponse>>>> getCompanies(ServerWebExchange exchange) {
        String role = (String) exchange.getAttributes().get("role");

        if (!"SUPER_ADMIN".equals(role)) {
            return Mono.just(ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("슈퍼관리자만 접근할 수 있습니다.", "ACCESS_DENIED")));
        }

        return companyService.getAllCompanies()
                .map(this::toCompanyResponse)
                .collectList()
                .map(companies -> ResponseEntity.ok(ApiResponse.success(companies)))
                .onErrorResume(e -> {
                    log.error("회사 목록 조회 실패", e);
                    return Mono.just(ResponseEntity
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(ApiResponse.error("회사 목록 조회에 실패했습니다.")));
                });
    }

    /**
     * 활성화된 회사 목록 조회 (슈퍼관리자 전용)
     */
    @Operation(summary = "활성화된 회사 목록 조회", description = "활성화된 회사 목록을 조회합니다. (슈퍼관리자 전용)")
    @GetMapping("/active")
    public Mono<ResponseEntity<ApiResponse<List<CompanyResponse>>>> getActiveCompanies(ServerWebExchange exchange) {
        String role = (String) exchange.getAttributes().get("role");

        if (!"SUPER_ADMIN".equals(role)) {
            return Mono.just(ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("슈퍼관리자만 접근할 수 있습니다.", "ACCESS_DENIED")));
        }

        return companyService.getAllActiveCompanies()
                .map(this::toCompanyResponse)
                .collectList()
                .map(companies -> ResponseEntity.ok(ApiResponse.success(companies)))
                .onErrorResume(e -> {
                    log.error("활성화된 회사 목록 조회 실패", e);
                    return Mono.just(ResponseEntity
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(ApiResponse.error("회사 목록 조회에 실패했습니다.")));
                });
    }

    /**
     * 회사 상세 조회
     */
    @Operation(summary = "회사 상세 조회", description = "회사 상세 정보를 조회합니다.")
    @GetMapping("/{id}")
    public Mono<ResponseEntity<ApiResponse<CompanyResponse>>> getCompany(
            @PathVariable Long id,
            ServerWebExchange exchange) {

        String role = (String) exchange.getAttributes().get("role");
        Long companyId = (Long) exchange.getAttributes().get("companyId");

        // 슈퍼관리자가 아니면 자신의 회사만 조회 가능
        if (!"SUPER_ADMIN".equals(role) && !id.equals(companyId)) {
            return Mono.just(ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("자신의 회사 정보만 조회할 수 있습니다.", "ACCESS_DENIED")));
        }

        return companyService.getCompanyById(id)
                .map(this::toCompanyResponse)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response)))
                .switchIfEmpty(Mono.just(ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("회사를 찾을 수 없습니다.", "COMPANY_NOT_FOUND"))))
                .onErrorResume(e -> {
                    log.error("회사 상세 조회 실패: id={}", id, e);
                    return Mono.just(ResponseEntity
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(ApiResponse.error("회사 조회에 실패했습니다.")));
                });
    }

    /**
     * 현재 회사 정보 조회
     */
    @Operation(summary = "현재 회사 정보 조회", description = "현재 선택된 회사 정보를 조회합니다.")
    @GetMapping("/current")
    public Mono<ResponseEntity<ApiResponse<CompanyResponse>>> getCurrentCompany(ServerWebExchange exchange) {
        Long companyId = (Long) exchange.getAttributes().get("companyId");

        if (companyId == null) {
            return Mono.just(ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("기업이 선택되지 않았습니다.", "NO_COMPANY_SELECTED")));
        }

        return companyService.getCompanyById(companyId)
                .map(this::toCompanyResponse)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response)))
                .switchIfEmpty(Mono.just(ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("회사를 찾을 수 없습니다.", "COMPANY_NOT_FOUND"))));
    }

    private CompanyResponse toCompanyResponse(Company company) {
        return CompanyResponse.builder()
                .id(company.getId())
                .code(company.getCode())
                .name(company.getName())
                .logoUrl(company.getLogoUrl())
                .primaryColor(company.getPrimaryColor())
                .secondaryColor(company.getSecondaryColor())
                .description(company.getDescription())
                .address(company.getAddress())
                .phone(company.getPhone())
                .email(company.getEmail())
                .latitude(company.getLatitude())
                .longitude(company.getLongitude())
                .foundedDate(company.getFoundedDate())
                .isActive(company.getIsActive())
                .createdAt(company.getCreatedAt())
                .updatedAt(company.getUpdatedAt())
                .build();
    }
}
