package com.nalsil.bear.service;

import com.nalsil.bear.domain.board.Board;
import com.nalsil.bear.domain.board.BoardRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * BoardService 단위 테스트
 * 게시판 조회 비즈니스 로직 검증
 */
@ExtendWith(MockitoExtension.class)
class BoardServiceTest {

    @Mock
    private BoardRepository boardRepository;

    @InjectMocks
    private BoardService boardService;

    private Board noticeBoard;
    private Board pressBoard;

    @BeforeEach
    void setUp() {
        // 테스트용 게시판 데이터 준비
        noticeBoard = Board.builder()
                .id(1L)
                .companyId(1L)
                .name("공지사항")
                .type("notice")
                .build();

        pressBoard = Board.builder()
                .id(2L)
                .companyId(1L)
                .name("보도자료")
                .type("press")
                .build();
    }

    @Test
    @DisplayName("기업 ID로 게시판 목록 조회 - 성공")
    void testGetBoardsByCompanyId_Success() {
        // Given
        when(boardRepository.findByCompanyId(1L))
                .thenReturn(Flux.just(noticeBoard, pressBoard));

        // When
        Flux<Board> result = boardService.getBoardsByCompanyId(1L);

        // Then
        StepVerifier.create(result)
                .expectNextMatches(board ->
                        board.getId().equals(1L) &&
                        board.getName().equals("공지사항") &&
                        board.getType().equals("notice")
                )
                .expectNextMatches(board ->
                        board.getId().equals(2L) &&
                        board.getName().equals("보도자료") &&
                        board.getType().equals("press")
                )
                .verifyComplete();
    }

    @Test
    @DisplayName("기업 ID로 게시판 목록 조회 - 빈 결과")
    void testGetBoardsByCompanyId_Empty() {
        // Given
        when(boardRepository.findByCompanyId(anyLong()))
                .thenReturn(Flux.empty());

        // When
        Flux<Board> result = boardService.getBoardsByCompanyId(999L);

        // Then
        StepVerifier.create(result)
                .verifyComplete();
    }

    @Test
    @DisplayName("기업 ID와 타입으로 게시판 조회 - 성공")
    void testGetBoardByCompanyIdAndType_Success() {
        // Given
        when(boardRepository.findByCompanyIdAndType(1L, "notice"))
                .thenReturn(Mono.just(noticeBoard));

        // When
        Mono<Board> result = boardService.getBoardByCompanyIdAndType(1L, "notice");

        // Then
        StepVerifier.create(result)
                .expectNextMatches(board ->
                        board.getCompanyId().equals(1L) &&
                        board.getType().equals("notice")
                )
                .verifyComplete();
    }

    @Test
    @DisplayName("기업 ID와 타입으로 게시판 조회 - 존재하지 않음")
    void testGetBoardByCompanyIdAndType_NotFound() {
        // Given
        when(boardRepository.findByCompanyIdAndType(anyLong(), anyString()))
                .thenReturn(Mono.empty());

        // When
        Mono<Board> result = boardService.getBoardByCompanyIdAndType(1L, "non-existent");

        // Then
        StepVerifier.create(result)
                .verifyComplete();
    }

    @Test
    @DisplayName("게시판 ID로 조회 - 성공")
    void testGetBoardById_Success() {
        // Given
        when(boardRepository.findById(1L))
                .thenReturn(Mono.just(noticeBoard));

        // When
        Mono<Board> result = boardService.getBoardById(1L);

        // Then
        StepVerifier.create(result)
                .expectNextMatches(board ->
                        board.getId().equals(1L) &&
                        board.getName().equals("공지사항")
                )
                .verifyComplete();
    }

    @Test
    @DisplayName("게시판 ID로 조회 - 존재하지 않음")
    void testGetBoardById_NotFound() {
        // Given
        when(boardRepository.findById(999L))
                .thenReturn(Mono.empty());

        // When
        Mono<Board> result = boardService.getBoardById(999L);

        // Then
        StepVerifier.create(result)
                .verifyComplete();
    }
}
