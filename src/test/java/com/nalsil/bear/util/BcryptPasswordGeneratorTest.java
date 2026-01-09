package com.nalsil.bear.util;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * BCrypt 패스워드 해시 생성 및 검증 테스트
 *
 * 사용법:
 * 1. 이 테스트 클래스를 실행
 * 2. 콘솔에 출력된 해시값을 복사하여 데이터베이스에 저장
 */
public class BcryptPasswordGeneratorTest {

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * 기본 패스워드들의 BCrypt 해시 생성
     */
    @Test
    public void generatePasswordHashes() {
        System.out.println("\n=== BCrypt Password Hash Generator ===\n");

        // 테스트할 패스워드 목록
        String[] passwords = {
            "password123",      // 기본 패스워드
            "admin123",         // 관리자 패스워드
            "superadmin",       // 슈퍼관리자 패스워드
            "test1234",         // 테스트 패스워드
            "companyA123",      // Company A 관리자
            "companyB123"       // Company B 관리자
        };

        for (String password : passwords) {
            String hash = passwordEncoder.encode(password);
            System.out.println("Plain text: " + password);
            System.out.println("BCrypt hash: " + hash);
            System.out.println("Length: " + hash.length() + " characters");
            System.out.println();
        }

        System.out.println("=== Hash Generation Complete ===\n");
    }

    /**
     * 패스워드 검증 테스트
     * 생성된 해시가 원본 패스워드와 일치하는지 확인
     */
    @Test
    public void verifyPasswordMatches() {
        System.out.println("\n=== Password Verification Test ===\n");

        String plainPassword = "password123";
        String hashedPassword = passwordEncoder.encode(plainPassword);

        System.out.println("Original password: " + plainPassword);
        System.out.println("Hashed password: " + hashedPassword);
        System.out.println();

        // 패스워드 일치 확인
        boolean matches = passwordEncoder.matches(plainPassword, hashedPassword);
        System.out.println("Password matches: " + matches);

        // 잘못된 패스워드 확인
        boolean wrongMatches = passwordEncoder.matches("wrongpassword", hashedPassword);
        System.out.println("Wrong password matches: " + wrongMatches);

        System.out.println("\n=== Verification Complete ===\n");

        // Assertion
        assert matches : "Password should match!";
        assert !wrongMatches : "Wrong password should not match!";
    }

    /**
     * 같은 패스워드로 여러 번 해시 생성 (매번 다른 해시 생성됨)
     */
    @Test
    public void demonstrateSaltedHashing() {
        System.out.println("\n=== Salted Hashing Demonstration ===\n");
        System.out.println("BCrypt는 매번 다른 salt를 사용하므로 같은 패스워드도 다른 해시를 생성합니다.\n");

        String password = "password123";

        for (int i = 1; i <= 5; i++) {
            String hash = passwordEncoder.encode(password);
            System.out.println("Attempt " + i + ": " + hash);

            // 모든 해시가 원본 패스워드와 일치하는지 확인
            boolean matches = passwordEncoder.matches(password, hash);
            System.out.println("  → Matches: " + matches);
            System.out.println();
        }

        System.out.println("=== Demonstration Complete ===\n");
    }

    /**
     * 커스텀 패스워드 해시 생성 (원하는 패스워드 입력)
     */
    @Test
    public void generateCustomPasswordHash() {
        System.out.println("\n=== Custom Password Hash Generator ===\n");

        // 여기에 원하는 패스워드를 입력하세요
        String customPassword = "mySecurePassword2024!";

        String hash = passwordEncoder.encode(customPassword);

        System.out.println("Your password: " + customPassword);
        System.out.println("BCrypt hash: " + hash);
        System.out.println();
        System.out.println("Copy the hash above and use it in your database:");
        System.out.println("UPDATE admin SET password_hash = '" + hash + "' WHERE username = 'your_username';");

        System.out.println("\n=== Custom Hash Complete ===\n");
    }

    /**
     * SQL INSERT 문 생성
     * 데이터베이스에 바로 사용할 수 있는 INSERT 문 생성
     */
    @Test
    public void generateSQLInsertStatements() {
        System.out.println("\n=== SQL INSERT Statement Generator ===\n");

        // 사용자 정보와 패스워드
        Object[][] users = {
            {"superadmin", "superadmin", "시스템 관리자", "superadmin@bear.com", "SUPER_ADMIN", null},
            {"admin-a", "admin123", "Company A 관리자", "admin-a@techsolution.co.kr", "ADMIN", 1L},
            {"admin-b", "admin123", "Company B 관리자", "admin-b@globaltrade.co.kr", "ADMIN", 2L}
        };

        for (Object[] user : users) {
            String username = (String) user[0];
            String password = (String) user[1];
            String name = (String) user[2];
            String email = (String) user[3];
            String role = (String) user[4];
            Long companyId = (Long) user[5];

            String hash = passwordEncoder.encode(password);

            System.out.println("-- " + name + " (" + username + ")");
            System.out.print("INSERT INTO admin (username, password_hash, name, email, role");
            if (companyId != null) {
                System.out.print(", company_id");
            }
            System.out.println(") VALUES (");
            System.out.print("    '" + username + "', '" + hash + "', '" + name + "', '" + email + "', '" + role + "'");
            if (companyId != null) {
                System.out.print(", " + companyId);
            }
            System.out.println(");");
            System.out.println();
        }

        System.out.println("=== SQL Generation Complete ===\n");
    }

    /**
     * 패스워드 강도 체크 (간단한 예제)
     */
    @Test
    public void checkPasswordStrength() {
        System.out.println("\n=== Password Strength Check ===\n");

        String[] testPasswords = {
            "123456",           // 약함
            "password",         // 약함
            "Password1",        // 중간
            "Password123",      // 중간
            "P@ssw0rd!2024",    // 강함
            "mySecureP@ss123!"  // 강함
        };

        for (String password : testPasswords) {
            String strength = evaluatePasswordStrength(password);
            String hash = passwordEncoder.encode(password);

            System.out.println("Password: " + password);
            System.out.println("Strength: " + strength);
            System.out.println("BCrypt hash: " + hash);
            System.out.println();
        }

        System.out.println("=== Strength Check Complete ===\n");
    }

    /**
     * 패스워드 강도 평가 (간단한 로직)
     */
    private String evaluatePasswordStrength(String password) {
        int score = 0;

        if (password.length() >= 8) score++;
        if (password.length() >= 12) score++;
        if (password.matches(".*[a-z].*")) score++;
        if (password.matches(".*[A-Z].*")) score++;
        if (password.matches(".*\\d.*")) score++;
        if (password.matches(".*[!@#$%^&*(),.?\":{}|<>].*")) score++;

        if (score <= 2) return "약함 (Weak)";
        if (score <= 4) return "중간 (Medium)";
        return "강함 (Strong)";
    }
}
