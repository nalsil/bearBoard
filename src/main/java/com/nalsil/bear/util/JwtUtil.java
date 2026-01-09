package com.nalsil.bear.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 유틸리티 클래스
 * JWT 토큰 생성, 검증, 파싱 기능 제공
 */
@Slf4j
@Component
public class JwtUtil {

    private final SecretKey secretKey;
    private final long expiration;

    public JwtUtil(@Value("${app.jwt.secret}") String secret,
                   @Value("${app.jwt.expiration}") long expiration) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiration = expiration;
    }

    /**
     * JWT 토큰 생성
     *
     * @param username 사용자명
     * @param adminId 관리자 ID
     * @param companyId 소속 기업 ID
     * @param role 역할
     * @return JWT 토큰
     */
    public String generateToken(String username, Long adminId, Long companyId, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("adminId", adminId);
        claims.put("companyId", companyId);
        claims.put("role", role);

        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(secretKey)
                .compact();
    }

    /**
     * JWT 토큰에서 사용자명 추출
     *
     * @param token JWT 토큰
     * @return 사용자명
     */
    public String getUsernameFromToken(String token) {
        return getClaimsFromToken(token).getSubject();
    }

    /**
     * JWT 토큰에서 관리자 ID 추출
     *
     * @param token JWT 토큰
     * @return 관리자 ID
     */
    public Long getAdminIdFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        Object adminId = claims.get("adminId");
        if (adminId instanceof Integer) {
            return ((Integer) adminId).longValue();
        }
        return (Long) adminId;
    }

    /**
     * JWT 토큰에서 기업 ID 추출
     *
     * @param token JWT 토큰
     * @return 기업 ID (nullable)
     */
    public Long getCompanyIdFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        Object companyId = claims.get("companyId");
        if (companyId == null) {
            return null;
        }
        if (companyId instanceof Integer) {
            return ((Integer) companyId).longValue();
        }
        return (Long) companyId;
    }

    /**
     * JWT 토큰에서 역할 추출
     *
     * @param token JWT 토큰
     * @return 역할
     */
    public String getRoleFromToken(String token) {
        return (String) getClaimsFromToken(token).get("role");
    }

    /**
     * JWT 토큰 유효성 검증
     *
     * @param token JWT 토큰
     * @return 유효 여부
     */
    public boolean validateToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            return !claims.getExpiration().before(new Date());
        } catch (Exception e) {
            log.error("JWT 토큰 검증 실패: {}", e.getMessage());
            return false;
        }
    }

    /**
     * JWT 토큰에서 Claims 추출
     *
     * @param token JWT 토큰
     * @return Claims
     */
    private Claims getClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
