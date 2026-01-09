package com.nalsil.bear.service;

import com.nalsil.bear.domain.qna.Qna;
import com.nalsil.bear.domain.qna.QnaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

/**
 * QnA 서비스
 *
 * QnA 조회, 질문 등록, 이메일 검증 비즈니스 로직을 처리합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QnaService {

    private final QnaRepository qnaRepository;

    /**
     * 기업별 공개 QnA 목록 조회 (최신순, 페이징)
     *
     * @param companyId 기업 ID
     * @param page 페이지 번호 (0부터 시작)
     * @param size 페이지 크기
     * @return QnA 목록
     */
    public Flux<Qna> getQnasByCompanyId(Long companyId, int page, int size) {
        log.debug("기업 ID로 QnA 목록 조회: companyId={}, page={}, size={}", companyId, page, size);
        Pageable pageable = PageRequest.of(page, size);
        return qnaRepository.findByCompanyIdAndIsHiddenOrderByCreatedAtDesc(companyId, false, pageable);
    }

    /**
     * 기업별 QnA 전체 개수 조회
     *
     * @param companyId 기업 ID
     * @return QnA 개수
     */
    public Mono<Long> countQnasByCompanyId(Long companyId) {
        log.debug("기업별 QnA 개수 조회: companyId={}", companyId);
        return qnaRepository.countByCompanyIdAndIsHidden(companyId, false);
    }

    /**
     * QnA ID로 조회
     *
     * @param id QnA ID
     * @return QnA
     */
    public Mono<Qna> getQnaById(Long id) {
        log.debug("QnA ID로 조회: id={}", id);
        return qnaRepository.findById(id);
    }

    /**
     * QnA 질문 등록
     *
     * @param qna QnA 엔티티
     * @return 저장된 QnA
     */
    public Mono<Qna> createQna(Qna qna) {
        log.info("QnA 질문 등록: companyId={}, email={}", qna.getCompanyId(), qna.getAskerEmail());

        // 질문 등록 시각 설정
        qna.setCreatedAt(LocalDateTime.now());
        qna.setIsAnswered(false);
        qna.setIsHidden(false);

        return qnaRepository.save(qna);
    }

    /**
     * 이메일 형식 검증
     *
     * @param email 이메일 주소
     * @return 유효한 이메일 형식이면 true
     */
    public boolean isValidEmail(String email) {
        if (email == null || email.isEmpty()) {
            return false;
        }

        // 간단한 이메일 형식 검증 정규표현식
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email.matches(emailRegex);
    }

    /**
     * 답변 여부별 QnA 조회
     *
     * @param companyId 기업 ID
     * @param isAnswered 답변 여부
     * @return QnA 목록
     */
    public Flux<Qna> getQnasByAnswerStatus(Long companyId, boolean isAnswered, int page, int size) {
        log.debug("답변 여부별 QnA 조회: companyId={}, isAnswered={}", companyId, isAnswered);
        Pageable pageable = PageRequest.of(page, size);
        return qnaRepository.findByCompanyIdAndIsAnsweredOrderByCreatedAtDesc(
                companyId, isAnswered, pageable);
    }

    /**
     * 기업별 모든 QnA 목록 조회 (관리자용, 숨김 포함)
     *
     * @param companyId 기업 ID
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @return QnA 목록
     */
    public Flux<Qna> getAllQnasByCompanyId(Long companyId, int page, int size) {
        log.debug("기업 ID로 모든 QnA 목록 조회 (관리자용): companyId={}, page={}, size={}", companyId, page, size);
        Pageable pageable = PageRequest.of(page, size);
        return qnaRepository.findByCompanyIdOrderByCreatedAtDesc(companyId, pageable);
    }

    /**
     * QnA 수정
     *
     * @param qna QnA 엔티티
     * @return 수정된 QnA
     */
    public Mono<Qna> updateQna(Qna qna) {
        log.info("Updating QnA: id={}", qna.getId());
        return qnaRepository.save(qna);
    }

    /**
     * QnA 삭제
     *
     * @param qnaId QnA ID
     * @return 삭제 결과
     */
    public Mono<Void> deleteQna(Long qnaId) {
        log.info("Deleting QnA: id={}", qnaId);
        return qnaRepository.deleteById(qnaId);
    }
}
