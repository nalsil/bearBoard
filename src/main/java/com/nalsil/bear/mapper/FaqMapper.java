package com.nalsil.bear.mapper;

import com.nalsil.bear.domain.faq.Faq;
import org.mapstruct.*;

import java.time.LocalDateTime;

/**
 * Faq 엔티티 매핑을 담당하는 MapStruct Mapper
 */
@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        imports = LocalDateTime.class
)
public interface FaqMapper {

    /**
     * Faq 엔티티 생성 준비 (companyId 설정 전)
     *
     * @param faq 원본 Faq
     * @return 생성 준비된 Faq
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "companyId", ignore = true)
    @Mapping(target = "isHidden", expression = "java(faq.getIsHidden() != null ? faq.getIsHidden() : false)")
    @Mapping(target = "createdAt", expression = "java(LocalDateTime.now())")
    Faq prepareForCreate(Faq faq);

    /**
     * Faq 엔티티 업데이트
     * null이 아닌 필드만 업데이트
     *
     * @param source 업데이트할 데이터
     * @param target 업데이트 대상 Faq
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "companyId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "isHidden", expression = "java(source.getIsHidden() != null ? source.getIsHidden() : false)")
    void updateFaq(Faq source, @MappingTarget Faq target);
}
