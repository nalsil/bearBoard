package com.nalsil.bear.util;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 빠른 패스워드 해시 생성 테스트
 *
 * 이 테스트를 실행하면 콘솔에 해시가 출력됩니다.
 */
public class QuickPasswordHashTest {

    @Test
    public void generateHashForPassword123() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String password = "password123";
        String hash = encoder.encode(password);

        System.out.println("\n==============================================");
        System.out.println("Password: " + password);
        System.out.println("BCrypt Hash:");
        System.out.println(hash);
        System.out.println("==============================================\n");

        System.out.println("SQL INSERT Example:");
        System.out.println("INSERT INTO admin (username, password_hash, name, email, role) VALUES");
        System.out.println("('admin', '" + hash + "', 'Administrator', 'admin@example.com', 'ADMIN');");
        System.out.println();
    }
}
