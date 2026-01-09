package com.nalsil.bear.config;

import com.nalsil.bear.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Collections;

/**
 * Spring Security용 관리자 인증 서비스
 *
 * ReactiveUserDetailsService를 구현하여 Spring Security와 통합합니다.
 */
@Service
@RequiredArgsConstructor
public class AdminUserDetailsService implements ReactiveUserDetailsService {

    private final AdminService adminService;

    @Override
    public Mono<UserDetails> findByUsername(String username) {
        return adminService.getAdminByUsername(username)
                .map(admin -> User.builder()
                        .username(admin.getUsername())
                        .password(admin.getPasswordHash())
                        .authorities(Collections.singletonList(
                                new SimpleGrantedAuthority("ROLE_" + admin.getRole())
                        ))
                        .build());
    }
}
