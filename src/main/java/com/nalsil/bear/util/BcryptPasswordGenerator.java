package com.nalsil.bear.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Scanner;

/**
 * BCrypt 패스워드 해시 생성 유틸리티
 *
 * 독립 실행 가능한 패스워드 해시 생성기
 * IDE에서 직접 실행하거나 커맨드라인에서 실행 가능
 *
 * 사용법:
 * 1. IDE에서 이 클래스의 main 메서드 실행
 * 2. 또는 터미널에서: ./gradlew run --args="password123"
 */
public class BcryptPasswordGenerator {

    private static final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public static void main(String[] args) {
        System.out.println("\n╔═══════════════════════════════════════════════════════════╗");
        System.out.println("║      BCrypt Password Hash Generator - Bear Platform      ║");
        System.out.println("╚═══════════════════════════════════════════════════════════╝\n");

        if (args.length > 0) {
            // 커맨드라인 인자로 패스워드 받음
            for (String password : args) {
                generateAndPrint(password);
            }
        } else {
            // 대화형 모드
            interactiveMode();
        }

        System.out.println("\n╔═══════════════════════════════════════════════════════════╗");
        System.out.println("║                   Generation Complete                     ║");
        System.out.println("╚═══════════════════════════════════════════════════════════╝\n");
    }

    /**
     * 대화형 모드 - 사용자 입력 받기
     */
    private static void interactiveMode() {
        Scanner scanner = new Scanner(System.in);
        boolean continueGenerating = true;

        while (continueGenerating) {
            System.out.println("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            System.out.println("Choose an option:");
            System.out.println("  1. Generate hash for a single password");
            System.out.println("  2. Generate hash for predefined passwords");
            System.out.println("  3. Verify password against hash");
            System.out.println("  4. Generate SQL INSERT statements");
            System.out.println("  5. Check password strength");
            System.out.println("  0. Exit");
            System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            System.out.print("\nEnter your choice: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    generateSinglePassword(scanner);
                    break;
                case "2":
                    generatePredefinedPasswords();
                    break;
                case "3":
                    verifyPassword(scanner);
                    break;
                case "4":
                    generateSQLStatements();
                    break;
                case "5":
                    checkPasswordStrength(scanner);
                    break;
                case "0":
                    continueGenerating = false;
                    System.out.println("\nGoodbye!");
                    break;
                default:
                    System.out.println("\n⚠️  Invalid choice. Please try again.");
            }
        }

        scanner.close();
    }

    /**
     * 단일 패스워드 해시 생성
     */
    private static void generateSinglePassword(Scanner scanner) {
        System.out.print("\nEnter password: ");
        String password = scanner.nextLine();

        if (password.isEmpty()) {
            System.out.println("⚠️  Password cannot be empty!");
            return;
        }

        generateAndPrint(password);
    }

    /**
     * 미리 정의된 패스워드 해시 생성
     */
    private static void generatePredefinedPasswords() {
        System.out.println("\n📋 Generating hashes for predefined passwords...\n");

        String[] passwords = {
            "password123",
            "admin123",
            "superadmin",
            "test1234",
            "companyA123",
            "companyB123"
        };

        for (String password : passwords) {
            generateAndPrint(password);
        }
    }

    /**
     * 패스워드 검증
     */
    private static void verifyPassword(Scanner scanner) {
        System.out.print("\nEnter plain text password: ");
        String plainPassword = scanner.nextLine();

        System.out.print("Enter BCrypt hash: ");
        String hash = scanner.nextLine();

        boolean matches = passwordEncoder.matches(plainPassword, hash);

        System.out.println("\n" + (matches ? "✅" : "❌") + " Password " + (matches ? "MATCHES" : "DOES NOT MATCH"));
    }

    /**
     * SQL INSERT 문 생성
     */
    private static void generateSQLStatements() {
        System.out.println("\n📄 Generating SQL INSERT statements...\n");

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
    }

    /**
     * 패스워드 강도 체크
     */
    private static void checkPasswordStrength(Scanner scanner) {
        System.out.print("\nEnter password to check: ");
        String password = scanner.nextLine();

        if (password.isEmpty()) {
            System.out.println("⚠️  Password cannot be empty!");
            return;
        }

        String strength = evaluatePasswordStrength(password);
        System.out.println("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("Password: " + password);
        System.out.println("Strength: " + strength);
        System.out.println("Length: " + password.length() + " characters");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        // 패스워드 개선 제안
        suggestImprovements(password);

        // 해시 생성
        System.out.print("\nGenerate BCrypt hash for this password? (y/n): ");
        Scanner confirmScanner = new Scanner(System.in);
        String confirm = confirmScanner.nextLine().trim().toLowerCase();

        if (confirm.equals("y") || confirm.equals("yes")) {
            generateAndPrint(password);
        }
    }

    /**
     * 패스워드 해시 생성 및 출력
     */
    private static void generateAndPrint(String password) {
        String hash = passwordEncoder.encode(password);
        String strength = evaluatePasswordStrength(password);

        System.out.println("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("Plain text: " + password);
        System.out.println("Strength:   " + strength);
        System.out.println("BCrypt hash:");
        System.out.println(hash);
        System.out.println("Length:     " + hash.length() + " characters");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
    }

    /**
     * 패스워드 강도 평가
     */
    private static String evaluatePasswordStrength(String password) {
        int score = 0;

        if (password.length() >= 8) score++;
        if (password.length() >= 12) score++;
        if (password.matches(".*[a-z].*")) score++;     // 소문자
        if (password.matches(".*[A-Z].*")) score++;     // 대문자
        if (password.matches(".*\\d.*")) score++;        // 숫자
        if (password.matches(".*[!@#$%^&*(),.?\":{}|<>].*")) score++;  // 특수문자

        if (score <= 2) return "🔴 약함 (Weak)";
        if (score <= 4) return "🟡 중간 (Medium)";
        return "🟢 강함 (Strong)";
    }

    /**
     * 패스워드 개선 제안
     */
    private static void suggestImprovements(String password) {
        System.out.println("\n💡 Suggestions for improvement:");

        if (password.length() < 8) {
            System.out.println("  • Use at least 8 characters (12+ recommended)");
        }
        if (!password.matches(".*[a-z].*")) {
            System.out.println("  • Add lowercase letters (a-z)");
        }
        if (!password.matches(".*[A-Z].*")) {
            System.out.println("  • Add uppercase letters (A-Z)");
        }
        if (!password.matches(".*\\d.*")) {
            System.out.println("  • Add numbers (0-9)");
        }
        if (!password.matches(".*[!@#$%^&*(),.?\":{}|<>].*")) {
            System.out.println("  • Add special characters (!@#$%^&*)");
        }

        // 일반적인 취약한 패스워드 체크
        String[] commonPasswords = {"password", "123456", "admin", "test", "qwerty"};
        for (String common : commonPasswords) {
            if (password.toLowerCase().contains(common)) {
                System.out.println("  ⚠️  Avoid common words like '" + common + "'");
                break;
            }
        }
    }

    /**
     * 패스워드 해시 생성 (프로그래밍 방식으로 사용)
     *
     * @param password 평문 패스워드
     * @return BCrypt 해시
     */
    public static String encode(String password) {
        return passwordEncoder.encode(password);
    }

    /**
     * 패스워드 검증 (프로그래밍 방식으로 사용)
     *
     * @param plainPassword 평문 패스워드
     * @param hashedPassword BCrypt 해시
     * @return 일치 여부
     */
    public static boolean verify(String plainPassword, String hashedPassword) {
        return passwordEncoder.matches(plainPassword, hashedPassword);
    }
}
