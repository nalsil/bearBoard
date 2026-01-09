package com.nalsil.bear.mapper;

import com.nalsil.bear.domain.qna.Qna;
import com.nalsil.bear.dto.request.CreateQnaRequest;
import org.mapstruct.*;

import java.time.LocalDateTime;

/**
 * Qna 엔티티와 DTO 간의 매핑을 담당하는 MapStruct Mapper
 */
@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        imports = LocalDateTime.class
)
public interface QnaMapper {

    /**
     * CreateQnaRequest DTO를 Qna 엔티티로 변환
     * 생성 시 필요한 필드들을 자동으로 설정
     *
     * @param request CreateQnaRequest DTO
     * @return Qna 엔티티
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "companyId", ignore = true)
    @Mapping(target = "answerBody", ignore = true)
    @Mapping(target = "answererId", ignore = true)
    @Mapping(target = "isAnswered", constant = "false")
    @Mapping(target = "isHidden", constant = "false")
    @Mapping(target = "createdAt", expression = "java(LocalDateTime.now())")
    @Mapping(target = "answeredAt", ignore = true)
    Qna toEntity(CreateQnaRequest request);

    /**
     * Qna 엔티티 업데이트 (답변 추가용)
     *
     * @param answerBody 답변 내용
     * @param answererId 답변자 ID
     * @param qna 업데이트할 Qna 엔티티
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "companyId", ignore = true)
    @Mapping(target = "questionTitle", ignore = true)
    @Mapping(target = "questionBody", ignore = true)
    @Mapping(target = "askerEmail", ignore = true)
    @Mapping(target = "isAnswered", constant = "true")
    @Mapping(target = "isHidden", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "answeredAt", expression = "java(LocalDateTime.now())")
    void updateWithAnswer(String answerBody, Long answererId, @MappingTarget Qna qna);
}
