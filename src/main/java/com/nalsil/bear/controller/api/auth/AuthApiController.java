package com.nalsil.bear.controller.api.auth;

import com.nalsil.bear.domain.admin.Admin;
import com.nalsil.bear.dto.request.AdminLoginRequest;
import com.nalsil.bear.dto.request.SwitchCompanyRequest;
import com.nalsil.bear.dto.response.ApiResponse;
import com.nalsil.bear.dto.response.AuthResponse;
import com.nalsil.bear.service.AdminService;
import com.nalsil.bear.service.CompanyService;
import com.nalsil.bear.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 인증 API 컨트롤러
 * React 관리자 앱을 위한 JWT 인증 API
 */
@Tag(name = "인증 API", description = "관리자 로그인, 로그아웃, 세션 관리 API")
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthApiController {

    private final AdminService adminService;
    private final CompanyService companyService;
    private final JwtUtil jwtUtil;

    /**
     * 관리자 로그인
     */
    @Operation(
            summary = "관리자 로그인",
            description = "사용자명과 비밀번호로 로그인하여 JWT 토큰을 발급받습니다. " +
                    "토큰은 HTTP-only 쿠키에 저장됩니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "로그인 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @PostMapping("/login")
    public Mono<ResponseEntity<ApiResponse<AuthResponse>>> login(
            @RequestBody AdminLoginRequest request,
            ServerWebExchange exchange) {

        log.info("API 로그인 요청: username={}", request.getUsername());

        return adminService.authenticate(request.getUsername(), request.getPassword())
                .flatMap(admin -> createAuthResponse(admin, exchange))
                .switchIfEmpty(Mono.defer(() -> {
                    log.warn("API 로그인 실패: username={}", request.getUsername());
                    return Mono.just(ResponseEntity
                            .status(HttpStatus.UNAUTHORIZED)
                            .body(ApiResponse.error("아이디 또는 비밀번호가 올바르지 않습니다.", "AUTH_FAILED")));
                }))
                .doOnError(e -> log.error("API 로그인 처리 중 오류: username={}", request.getUsername(), e));
    }

    /**
     * 관리자 로그아웃
     */
    @Operation(
            summary = "관리자 로그아웃",
            description = "JWT 토큰 쿠키를 삭제하여 로그아웃합니다."
    )
    @PostMapping("/logout")
    public Mono<ResponseEntity<ApiResponse<Void>>> logout(ServerWebExchange exchange) {
        log.info("API 로그아웃 요청");

        // JWT 토큰 쿠키 삭제
        ResponseCookie cookie = ResponseCookie.from("JWT-TOKEN", "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();

        exchange.getResponse().addCookie(cookie);

        return Mono.just(ResponseEntity.ok(ApiResponse.success("로그아웃되었습니다.")));
    }

    /**
     * 현재 로그인한 사용자 정보 조회
     */
    @Operation(
            summary = "현재 사용자 정보 조회",
            description = "JWT 토큰에서 현재 로그인한 사용자 정보를 조회합니다."
    )
    @SecurityRequirement(name = "JWT Cookie")
    @GetMapping("/me")
    public Mono<ResponseEntity<ApiResponse<AuthResponse>>> getCurrentUser(ServerWebExchange exchange) {
        Long adminId = (Long) exchange.getAttributes().get("adminId");
        Long companyId = (Long) exchange.getAttributes().get("companyId");
        String role = (String) exchange.getAttributes().get("role");
        String username = (String) exchange.getAttributes().get("username");

        if (adminId == null) {
            return Mono.just(ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("인증되지 않은 사용자입니다.", "NOT_AUTHENTICATED")));
        }

        return adminService.getAdminById(adminId)
                .flatMap(admin -> {
                    if (companyId != null) {
                        return companyService.getCompanyById(companyId)
                                .map(company -> buildAuthResponse(admin, company.getCode(), company.getName()))
                                .onErrorResume(e -> Mono.just(buildAuthResponse(admin, null, null)));
                    }
                    return Mono.just(buildAuthResponse(admin, null, null));
                })
                .map(authResponse -> ResponseEntity.ok(ApiResponse.success(authResponse)))
                .switchIfEmpty(Mono.just(ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("사용자 정보를 찾을 수 없습니다.", "USER_NOT_FOUND"))));
    }

    /**
     * 슈퍼관리자 회사 전환
     */
    @Operation(
            summary = "회사 전환 (슈퍼관리자)",
            description = "슈퍼관리자가 관리할 회사를 전환합니다. 새로운 JWT 토큰이 발급됩니다."
    )
    @SecurityRequirement(name = "JWT Cookie")
    @PostMapping("/switch-company")
    public Mono<ResponseEntity<ApiResponse<AuthResponse>>> switchCompany(
            @RequestBody SwitchCompanyRequest request,
            ServerWebExchange exchange) {

        Long adminId = (Long) exchange.getAttributes().get("adminId");
        String role = (String) exchange.getAttributes().get("role");

        if (adminId == null) {
            return Mono.just(ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("인증되지 않은 사용자입니다.", "NOT_AUTHENTICATED")));
        }

        if (!"SUPER_ADMIN".equals(role)) {
            return Mono.just(ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("슈퍼관리자만 회사를 전환할 수 있습니다.", "ACCESS_DENIED")));
        }

        return adminService.getAdminById(adminId)
                .flatMap(admin -> companyService.getCompanyById(request.getCompanyId())
                        .flatMap(company -> {
                            // 새 JWT 토큰 생성 (companyId 포함)
                            String token = jwtUtil.generateToken(
                                    admin.getUsername(),
                                    admin.getId(),
                                    company.getId(),
                                    admin.getRole()
                            );

                            // JWT 토큰 쿠키 설정
                            ResponseCookie cookie = ResponseCookie.from("JWT-TOKEN", token)
                                    .httpOnly(true)
                                    .secure(false)
                                    .path("/")
                                    .maxAge(24 * 60 * 60)
                                    .sameSite("Lax")
                                    .build();

                            exchange.getResponse().addCookie(cookie);

                            log.info("슈퍼관리자 회사 전환: adminId={}, newCompanyId={}", adminId, company.getId());

                            AuthResponse authResponse = buildAuthResponse(admin, company.getCode(), company.getName());
                            authResponse.setCompanyId(company.getId());

                            return Mono.just(ResponseEntity.ok(ApiResponse.success("회사가 전환되었습니다.", authResponse)));
                        }))
                .switchIfEmpty(Mono.just(ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("사용자 또는 회사를 찾을 수 없습니다.", "NOT_FOUND"))));
    }

    /**
     * 인증 응답 생성 (로그인 성공 시)
     */
    private Mono<ResponseEntity<ApiResponse<AuthResponse>>> createAuthResponse(Admin admin, ServerWebExchange exchange) {
        // JWT 토큰 생성
        String token = jwtUtil.generateToken(
                admin.getUsername(),
                admin.getId(),
                admin.getCompanyId(),
                admin.getRole()
        );

        // JWT 토큰 쿠키 설정
        ResponseCookie cookie = ResponseCookie.from("JWT-TOKEN", token)
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(24 * 60 * 60)
                .sameSite("Lax")
                .build();

        exchange.getResponse().addCookie(cookie);

        log.info("API 로그인 성공: username={}, role={}", admin.getUsername(), admin.getRole());

        // 회사 정보 조회 후 응답
        if (admin.getCompanyId() != null) {
            return companyService.getCompanyById(admin.getCompanyId())
                    .map(company -> ResponseEntity.ok(ApiResponse.success(
                            "로그인되었습니다.",
                            buildAuthResponse(admin, company.getCode(), company.getName())
                    )))
                    .onErrorResume(e -> Mono.just(ResponseEntity.ok(ApiResponse.success(
                            "로그인되었습니다.",
                            buildAuthResponse(admin, null, null)
                    ))));
        }

        return Mono.just(ResponseEntity.ok(ApiResponse.success(
                "로그인되었습니다.",
                buildAuthResponse(admin, null, null)
        )));
    }

    /**
     * AuthResponse 빌드
     */
    private AuthResponse buildAuthResponse(Admin admin, String companyCode, String companyName) {
        return AuthResponse.builder()
                .adminId(admin.getId())
                .username(admin.getUsername())
                .name(admin.getName())
                .email(admin.getEmail())
                .role(admin.getRole())
                .companyId(admin.getCompanyId())
                .companyCode(companyCode)
                .companyName(companyName)
                .build();
    }
}
