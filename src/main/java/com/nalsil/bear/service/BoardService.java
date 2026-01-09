package com.nalsil.bear.service;

import com.nalsil.bear.domain.board.Board;
import com.nalsil.bear.domain.board.BoardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * BoardService
 * 게시판 조회 비즈니스 로직
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;

    /**
     * 기업 ID로 게시판 목록 조회
     *
     * @param companyId 기업 ID
     * @return 게시판 목록 (Flux<Board>)
     */
    public Flux<Board> getBoardsByCompanyId(Long companyId) {
        log.debug("Fetching boards for company ID: {}", companyId);

        return boardRepository.findByCompanyId(companyId)
                .doOnComplete(() -> log.debug("Fetched boards for company ID: {}", companyId))
                .doOnError(error -> log.error("Failed to fetch boards for company ID: {}", companyId, error));
    }

    /**
     * 기업 ID와 게시판 타입으로 게시판 조회
     *
     * @param companyId 기업 ID
     * @param type 게시판 타입
     * @return 게시판 정보 (Mono<Board>)
     */
    public Mono<Board> getBoardByCompanyIdAndType(Long companyId, String type) {
        log.debug("Fetching board for company ID: {}, type: {}", companyId, type);

        return boardRepository.findByCompanyIdAndType(companyId, type)
                .doOnSuccess(board -> {
                    if (board != null) {
                        log.info("Found board: {} for company ID: {}", board.getName(), companyId);
                    } else {
                        log.warn("Board not found for company ID: {}, type: {}", companyId, type);
                    }
                })
                .doOnError(error -> log.error("Failed to fetch board for company ID: {}, type: {}",
                        companyId, type, error));
    }

    /**
     * 게시판 ID로 조회
     *
     * @param id 게시판 ID
     * @return 게시판 정보 (Mono<Board>)
     */
    public Mono<Board> getBoardById(Long id) {
        log.debug("Fetching board by ID: {}", id);

        return boardRepository.findById(id)
                .doOnSuccess(board -> {
                    if (board != null) {
                        log.info("Found board: {} (ID: {})", board.getName(), id);
                    } else {
                        log.warn("Board not found for ID: {}", id);
                    }
                })
                .doOnError(error -> log.error("Failed to fetch board by ID: {}", id, error));
    }
}
