package com.nalsil.bear.service;

import com.nalsil.bear.domain.company.Company;
import com.nalsil.bear.domain.company.CompanyRepository;
import com.nalsil.bear.exception.CompanyNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.when;

/**
 * CompanyService 단위 테스트
 * 기업 조회 비즈니스 로직 검증
 */
@ExtendWith(MockitoExtension.class)
class CompanyServiceTest {

    @Mock
    private CompanyRepository companyRepository;

    @InjectMocks
    private CompanyService companyService;

    private Company testCompany;

    @BeforeEach
    void setUp() {
        // 테스트용 기업 데이터 준비
        testCompany = Company.builder()
                .id(1L)
                .code("company-a")
                .name("테크솔루션 주식회사")
                .logoUrl("https://example.com/logo.png")
                .primaryColor("#2563EB")
                .secondaryColor("#1E40AF")
                .description("혁신적인 기술 기업")
                .address("서울특별시 강남구 테헤란로 123")
                .phone("02-1234-5678")
                .email("contact@techsolution.co.kr")
                .latitude(new BigDecimal("37.498095"))
                .longitude(new BigDecimal("127.027610"))
                .foundedDate(LocalDate.of(2015, 3, 15))
                .isActive(true)
                .build();
    }

    @Test
    @DisplayName("기업 코드로 활성화된 기업 조회 - 성공")
    void testGetActiveCompanyByCode_Success() {
        // Given
        when(companyRepository.findByCodeAndIsActive("company-a", true))
                .thenReturn(Mono.just(testCompany));

        // When
        Mono<Company> result = companyService.getActiveCompanyByCode("company-a");

        // Then
        StepVerifier.create(result)
                .expectNextMatches(company ->
                        company.getCode().equals("company-a") &&
                        company.getName().equals("테크솔루션 주식회사") &&
                        company.getIsActive().equals(true)
                )
                .verifyComplete();
    }

    @Test
    @DisplayName("기업 코드로 활성화된 기업 조회 - 존재하지 않음")
    void testGetActiveCompanyByCode_NotFound() {
        // Given
        when(companyRepository.findByCodeAndIsActive(anyString(), anyBoolean()))
                .thenReturn(Mono.empty());

        // When
        Mono<Company> result = companyService.getActiveCompanyByCode("non-existent");

        // Then
        StepVerifier.create(result)
                .expectError(CompanyNotFoundException.class)
                .verify();
    }

    @Test
    @DisplayName("기업 코드로 활성화된 기업 조회 - 비활성화된 기업")
    void testGetActiveCompanyByCode_Inactive() {
        // Given: 비활성화된 기업은 조회되지 않음
        when(companyRepository.findByCodeAndIsActive("company-a", true))
                .thenReturn(Mono.empty());

        // When
        Mono<Company> result = companyService.getActiveCompanyByCode("company-a");

        // Then
        StepVerifier.create(result)
                .expectError(CompanyNotFoundException.class)
                .verify();
    }

    @Test
    @DisplayName("기업 코드 존재 여부 확인 - 존재함")
    void testExistsByCode_Exists() {
        // Given
        when(companyRepository.existsByCode("company-a"))
                .thenReturn(Mono.just(true));

        // When
        Mono<Boolean> result = companyService.existsByCode("company-a");

        // Then
        StepVerifier.create(result)
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    @DisplayName("기업 코드 존재 여부 확인 - 존재하지 않음")
    void testExistsByCode_NotExists() {
        // Given
        when(companyRepository.existsByCode("non-existent"))
                .thenReturn(Mono.just(false));

        // When
        Mono<Boolean> result = companyService.existsByCode("non-existent");

        // Then
        StepVerifier.create(result)
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    @DisplayName("기업 ID로 조회 - 성공")
    void testGetCompanyById_Success() {
        // Given
        when(companyRepository.findById(1L))
                .thenReturn(Mono.just(testCompany));

        // When
        Mono<Company> result = companyService.getCompanyById(1L);

        // Then
        StepVerifier.create(result)
                .expectNextMatches(company ->
                        company.getId().equals(1L) &&
                        company.getCode().equals("company-a")
                )
                .verifyComplete();
    }

    @Test
    @DisplayName("기업 ID로 조회 - 존재하지 않음")
    void testGetCompanyById_NotFound() {
        // Given
        when(companyRepository.findById(999L))
                .thenReturn(Mono.empty());

        // When
        Mono<Company> result = companyService.getCompanyById(999L);

        // Then
        StepVerifier.create(result)
                .expectError(CompanyNotFoundException.class)
                .verify();
    }
}
