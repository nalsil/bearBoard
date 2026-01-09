package com.nalsil.bear.domain.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

/**
 * Admin 엔티티
 * 관리자 및 슈퍼관리자 정보를 저장하는 엔티티
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("admin")
public class Admin {

    @Id
    private Long id;

    /**
     * 사용자명 (로그인 ID, 유니크)
     */
    @Column("username")
    private String username;

    /**
     * 비밀번호 해시 (BCrypt)
     */
    @Column("password_hash")
    private String passwordHash;

    /**
     * 관리자 이름
     */
    @Column("name")
    private String name;

    /**
     * 이메일
     */
    @Column("email")
    private String email;

    /**
     * 역할 (ADMIN, SUPER_ADMIN)
     */
    @Column("role")
    private String role;

    /**
     * 소속 기업 ID (SUPER_ADMIN은 null)
     */
    @Column("company_id")
    private Long companyId;

    /**
     * 마지막 로그인 일시
     */
    @Column("last_login_at")
    private LocalDateTime lastLoginAt;

    /**
     * 생성일시
     */
    @Column("created_at")
    private LocalDateTime createdAt;
}
