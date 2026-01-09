package com.nalsil.bear.service;

import com.nalsil.bear.domain.company.Company;
import com.nalsil.bear.domain.company.CompanyRepository;
import com.nalsil.bear.exception.CompanyNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * CompanyService
 * 기업 정보 조회 비즈니스 로직
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CompanyService {

    private final CompanyRepository companyRepository;

    /**
     * 모든 활성화된 기업 조회
     *
     * @return 활성화된 기업 목록 (Flux)
     */
    public Flux<Company> getAllActiveCompanies() {
        log.debug("Fetching all active companies");

        return companyRepository.findAll()
                .filter(company -> Boolean.TRUE.equals(company.getIsActive()))
                .sort((a, b) -> a.getName().compareTo(b.getName()))
                .doOnComplete(() -> log.debug("Fetched all active companies"));
    }

    /**
     * 모든 기업 조회 (슈퍼유저용)
     *
     * @return 모든 기업 목록 (Flux)
     */
    public Flux<Company> getAllCompanies() {
        log.debug("Fetching all companies (super admin)");

        return companyRepository.findAll()
                .sort((a, b) -> a.getName().compareTo(b.getName()))
                .doOnComplete(() -> log.debug("Fetched all companies"));
    }

    /**
     * 기업 코드로 활성화된 기업 조회
     *
     * @param code 기업 코드
     * @return 기업 정보 (Mono<Company>)
     */
    public Mono<Company> getActiveCompanyByCode(String code) {
        log.debug("Fetching active company by code: {}", code);

        return companyRepository.findByCodeAndIsActive(code, true)
                .switchIfEmpty(Mono.error(CompanyNotFoundException.forCode(code)))
                .doOnSuccess(company -> log.info("Found active company: {} ({})", company.getName(), code))
                .doOnError(error -> log.error("Failed to fetch company by code: {}", code, error));
    }

    /**
     * 기업 코드 존재 여부 확인
     *
     * @param code 기업 코드
     * @return 존재 여부 (Mono<Boolean>)
     */
    public Mono<Boolean> existsByCode(String code) {
        log.debug("Checking if company exists by code: {}", code);

        return companyRepository.existsByCode(code)
                .doOnSuccess(exists -> log.debug("Company exists check for {}: {}", code, exists));
    }

    /**
     * 기업 ID로 조회
     *
     * @param id 기업 ID
     * @return 기업 정보 (Mono<Company>)
     */
    public Mono<Company> getCompanyById(Long id) {
        log.debug("Fetching company by ID: {}", id);

        return companyRepository.findById(id)
                .switchIfEmpty(Mono.error(new CompanyNotFoundException(
                        String.format("기업 ID %d에 해당하는 정보를 찾을 수 없습니다.", id))))
                .doOnSuccess(company -> log.info("Found company: {} (ID: {})", company.getName(), id))
                .doOnError(error -> log.error("Failed to fetch company by ID: {}", id, error));
    }
}
