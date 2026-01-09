package com.nalsil.bear.service;

import com.nalsil.bear.domain.admin.Admin;
import com.nalsil.bear.domain.admin.AdminRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

/**
 * 관리자 서비스
 *
 * 관리자 인증, 세션 관리, 권한 검증 비즈니스 로직을 처리합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminService {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 관리자 인증 (로그인)
     *
     * @param username 사용자명
     * @param password 비밀번호
     * @return 인증된 관리자 정보 (인증 실패 시 Mono.empty())
     */
    public Mono<Admin> authenticate(String username, String password) {
        log.info("관리자 인증 시도: username={}", username);

        return adminRepository.findByUsername(username)
                .filter(admin -> passwordEncoder.matches(password, admin.getPasswordHash()))
                .doOnNext(admin -> {
                    log.info("관리자 인증 성공: username={}, role={}", username, admin.getRole());
                    // 마지막 로그인 시각 업데이트
                    updateLastLoginAt(admin.getId()).subscribe();
                })
                .doOnSuccess(admin -> {
                    if (admin == null) {
                        log.warn("관리자 인증 실패: username={}", username);
                    }
                });
    }

    /**
     * 마지막 로그인 시각 업데이트
     *
     * @param adminId 관리자 ID
     * @return 업데이트 결과
     */
    public Mono<Integer> updateLastLoginAt(Long adminId) {
        log.debug("마지막 로그인 시각 업데이트: adminId={}", adminId);
        return adminRepository.updateLastLoginAt(adminId, LocalDateTime.now());
    }

    /**
     * 사용자명으로 관리자 조회
     *
     * @param username 사용자명
     * @return 관리자 정보
     */
    public Mono<Admin> getAdminByUsername(String username) {
        log.debug("사용자명으로 관리자 조회: username={}", username);
        return adminRepository.findByUsername(username);
    }

    /**
     * 관리자 ID로 조회
     *
     * @param id 관리자 ID
     * @return 관리자 정보
     */
    public Mono<Admin> getAdminById(Long id) {
        log.debug("관리자 ID로 조회: id={}", id);
        return adminRepository.findById(id);
    }

    /**
     * 관리자가 특정 기업에 대한 권한이 있는지 확인
     *
     * @param admin 관리자 정보
     * @param companyId 기업 ID
     * @return 권한 여부 (SUPER_ADMIN은 항상 true)
     */
    public boolean hasAccessToCompany(Admin admin, Long companyId) {
        if (admin == null || companyId == null) {
            return false;
        }

        // 슈퍼관리자는 모든 기업에 접근 가능
        if ("SUPER_ADMIN".equals(admin.getRole())) {
            return true;
        }

        // 일반 관리자는 자신의 기업만 접근 가능
        return admin.getCompanyId() != null && admin.getCompanyId().equals(companyId);
    }

    /**
     * 슈퍼관리자 여부 확인
     *
     * @param admin 관리자 정보
     * @return 슈퍼관리자 여부
     */
    public boolean isSuperAdmin(Admin admin) {
        return admin != null && "SUPER_ADMIN".equals(admin.getRole());
    }

    /**
     * 관리자 생성
     *
     * @param admin 관리자 정보 (비밀번호는 평문으로 전달, 내부에서 해시 처리)
     * @return 생성된 관리자
     */
    public Mono<Admin> createAdmin(Admin admin) {
        log.info("관리자 생성: username={}, role={}", admin.getUsername(), admin.getRole());

        // 비밀번호 해시 처리
        String hashedPassword = passwordEncoder.encode(admin.getPasswordHash());
        admin.setPasswordHash(hashedPassword);
        admin.setCreatedAt(LocalDateTime.now());

        return adminRepository.save(admin);
    }

    /**
     * 관리자 비밀번호 변경
     *
     * @param adminId 관리자 ID
     * @param currentPassword 현재 비밀번호
     * @param newPassword 새 비밀번호
     * @return 변경 성공 여부
     */
    public Mono<Boolean> changePassword(Long adminId, String currentPassword, String newPassword) {
        log.info("관리자 비밀번호 변경: adminId={}", adminId);

        return adminRepository.findById(adminId)
                .filter(admin -> passwordEncoder.matches(currentPassword, admin.getPasswordHash()))
                .flatMap(admin -> {
                    admin.setPasswordHash(passwordEncoder.encode(newPassword));
                    return adminRepository.save(admin);
                })
                .map(admin -> true)
                .defaultIfEmpty(false);
    }

    /**
     * 사용자명 존재 여부 확인
     *
     * @param username 사용자명
     * @return 존재 여부
     */
    public Mono<Boolean> existsByUsername(String username) {
        log.debug("사용자명 존재 여부 확인: username={}", username);
        return adminRepository.existsByUsername(username);
    }
}
