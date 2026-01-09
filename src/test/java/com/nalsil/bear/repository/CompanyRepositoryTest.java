package com.nalsil.bear.repository;

import com.nalsil.bear.domain.company.Company;
import com.nalsil.bear.domain.company.CompanyRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * CompanyRepository 계약 테스트
 * R2DBC 리포지토리 기본 동작 검증
 */
@DataR2dbcTest
@ActiveProfiles("test")
class CompanyRepositoryTest {

    @Autowired
    private CompanyRepository companyRepository;

    @Test
    @DisplayName("기업 코드로 기업 조회 - 성공")
    void testFindByCode_Success() {
        // Given: 데이터베이스에 company-a 존재 (data.sql에서 삽입)

        // When: company-a 조회
        Mono<Company> companyMono = companyRepository.findByCode("company-a");

        // Then: 기업 정보가 정상 조회됨
        StepVerifier.create(companyMono)
                .expectNextMatches(company ->
                        company.getCode().equals("company-a") &&
                        company.getName().equals("테크솔루션 주식회사") &&
                        company.getIsActive().equals(true)
                )
                .verifyComplete();
    }

    @Test
    @DisplayName("기업 코드로 기업 조회 - 존재하지 않는 코드")
    void testFindByCode_NotFound() {
        // Given: 존재하지 않는 기업 코드

        // When: 존재하지 않는 코드로 조회
        Mono<Company> companyMono = companyRepository.findByCode("non-existent-company");

        // Then: 결과가 비어있음
        StepVerifier.create(companyMono)
                .verifyComplete();
    }

    @Test
    @DisplayName("기업 코드와 활성화 상태로 기업 조회 - 성공")
    void testFindByCodeAndIsActive_Success() {
        // Given: 데이터베이스에 활성화된 company-a 존재

        // When: company-a를 활성화 상태로 조회
        Mono<Company> companyMono = companyRepository.findByCodeAndIsActive("company-a", true);

        // Then: 기업 정보가 정상 조회됨
        StepVerifier.create(companyMono)
                .expectNextMatches(company ->
                        company.getCode().equals("company-a") &&
                        company.getIsActive().equals(true)
                )
                .verifyComplete();
    }

    @Test
    @DisplayName("기업 코드 존재 여부 확인 - 존재함")
    void testExistsByCode_Exists() {
        // Given: 데이터베이스에 company-a 존재

        // When: company-a 존재 여부 확인
        Mono<Boolean> existsMono = companyRepository.existsByCode("company-a");

        // Then: true 반환
        StepVerifier.create(existsMono)
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    @DisplayName("기업 코드 존재 여부 확인 - 존재하지 않음")
    void testExistsByCode_NotExists() {
        // Given: 존재하지 않는 기업 코드

        // When: 존재하지 않는 코드 확인
        Mono<Boolean> existsMono = companyRepository.existsByCode("non-existent-company");

        // Then: false 반환
        StepVerifier.create(existsMono)
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    @DisplayName("기업 저장 - 성공")
    void testSave_Success() {
        // Given: 새 기업 정보
        Company newCompany = Company.builder()
                .code("company-c")
                .name("테스트 기업")
                .logoUrl("https://example.com/logo.png")
                .primaryColor("#FF5722")
                .secondaryColor("#FFC107")
                .description("테스트용 기업입니다.")
                .address("서울특별시 강남구 테스트로 123")
                .phone("02-1111-2222")
                .email("test@example.com")
                .latitude(new BigDecimal("37.123456"))
                .longitude(new BigDecimal("127.123456"))
                .foundedDate(LocalDate.of(2020, 1, 1))
                .isActive(true)
                .build();

        // When: 기업 저장
        Mono<Company> savedMono = companyRepository.save(newCompany);

        // Then: 저장된 기업 정보 확인
        StepVerifier.create(savedMono)
                .expectNextMatches(company ->
                        company.getId() != null &&
                        company.getCode().equals("company-c") &&
                        company.getName().equals("테스트 기업")
                )
                .verifyComplete();
    }

    @Test
    @DisplayName("기업 ID로 조회 - 성공")
    void testFindById_Success() {
        // Given: 먼저 기업 조회하여 ID 확인
        Mono<Long> companyIdMono = companyRepository.findByCode("company-a")
                .map(Company::getId);

        // When & Then: ID로 조회 후 검증
        StepVerifier.create(
                companyIdMono.flatMap(companyRepository::findById)
        )
                .expectNextMatches(company ->
                        company.getCode().equals("company-a")
                )
                .verifyComplete();
    }
}
