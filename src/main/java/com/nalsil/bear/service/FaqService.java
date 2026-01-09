package com.nalsil.bear.service;

import com.nalsil.bear.domain.faq.Faq;
import com.nalsil.bear.domain.faq.FaqRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * FAQ 서비스
 *
 * FAQ 조회 및 검색 비즈니스 로직을 처리합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FaqService {

    private final FaqRepository faqRepository;

    /**
     * 기업별 공개 FAQ 목록 조회 (정렬 순서대로)
     *
     * @param companyId 기업 ID
     * @return FAQ 목록
     */
    public Flux<Faq> getFaqsByCompanyId(Long companyId) {
        log.debug("기업 ID로 FAQ 목록 조회: companyId={}", companyId);
        return faqRepository.findByCompanyIdAndIsHiddenOrderByDisplayOrderAsc(companyId, false);
    }

    /**
     * 기업별 카테고리별 FAQ 목록 조회
     *
     * @param companyId 기업 ID
     * @param category 카테고리
     * @return FAQ 목록
     */
    public Flux<Faq> getFaqsByCompanyIdAndCategory(Long companyId, String category) {
        log.debug("기업 ID와 카테고리로 FAQ 목록 조회: companyId={}, category={}", companyId, category);
        return faqRepository.findByCompanyIdAndCategoryAndIsHiddenOrderByDisplayOrderAsc(
                companyId, category, false);
    }

    /**
     * FAQ 검색 (질문 내용으로 검색)
     *
     * @param companyId 기업 ID
     * @param keyword 검색 키워드
     * @return FAQ 목록
     */
    public Flux<Faq> searchFaqs(Long companyId, String keyword) {
        log.debug("FAQ 검색: companyId={}, keyword={}", companyId, keyword);
        return faqRepository.findByCompanyIdAndIsHiddenOrderByDisplayOrderAsc(companyId, false)
                .filter(faq -> faq.getQuestion().contains(keyword) ||
                               faq.getAnswer().contains(keyword));
    }

    /**
     * FAQ ID로 조회
     *
     * @param id FAQ ID
     * @return FAQ
     */
    public Mono<Faq> getFaqById(Long id) {
        log.debug("FAQ ID로 조회: id={}", id);
        return faqRepository.findById(id);
    }

    /**
     * 기업의 모든 카테고리 목록 조회
     *
     * @param companyId 기업 ID
     * @return 카테고리 목록
     */
    public Flux<String> getCategoriesByCompanyId(Long companyId) {
        log.debug("기업의 카테고리 목록 조회: companyId={}", companyId);
        return faqRepository.findByCompanyIdAndIsHiddenOrderByDisplayOrderAsc(companyId, false)
                .map(Faq::getCategory)
                .filter(category -> category != null && !category.isEmpty())
                .distinct();
    }

    /**
     * 기업별 모든 FAQ 목록 조회 (관리자용, 숨김 포함)
     *
     * @param companyId 기업 ID
     * @return FAQ 목록
     */
    public Flux<Faq> getAllFaqsByCompanyId(Long companyId) {
        log.debug("기업 ID로 모든 FAQ 목록 조회 (관리자용): companyId={}", companyId);
        return faqRepository.findByCompanyIdOrderByDisplayOrderAsc(companyId);
    }

    /**
     * FAQ 생성
     *
     * @param faq FAQ 엔티티
     * @return 생성된 FAQ
     */
    public Mono<Faq> createFaq(Faq faq) {
        log.info("Creating FAQ: question={}", faq.getQuestion());
        return faqRepository.save(faq);
    }

    /**
     * FAQ 수정
     *
     * @param faq FAQ 엔티티
     * @return 수정된 FAQ
     */
    public Mono<Faq> updateFaq(Faq faq) {
        log.info("Updating FAQ: id={}, question={}", faq.getId(), faq.getQuestion());
        return faqRepository.save(faq);
    }

    /**
     * FAQ 삭제
     *
     * @param faqId FAQ ID
     * @return 삭제 결과
     */
    public Mono<Void> deleteFaq(Long faqId) {
        log.info("Deleting FAQ: id={}", faqId);
        return faqRepository.deleteById(faqId);
    }
}
