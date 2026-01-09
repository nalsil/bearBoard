package com.nalsil.bear.mapper;

import com.nalsil.bear.domain.youtube.YoutubeVideo;
import org.mapstruct.*;

import java.time.LocalDateTime;

/**
 * YoutubeVideo 엔티티 매핑을 담당하는 MapStruct Mapper
 */
@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        imports = LocalDateTime.class
)
public interface YoutubeVideoMapper {

    /**
     * YoutubeVideo 엔티티 생성 준비 (companyId, thumbnailUrl 설정 전)
     *
     * @param video 원본 YoutubeVideo
     * @return 생성 준비된 YoutubeVideo
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "companyId", ignore = true)
    @Mapping(target = "thumbnailUrl", ignore = true)
    @Mapping(target = "isHidden", expression = "java(video.getIsHidden() != null ? video.getIsHidden() : false)")
    @Mapping(target = "createdAt", expression = "java(LocalDateTime.now())")
    YoutubeVideo prepareForCreate(YoutubeVideo video);

    /**
     * YoutubeVideo 엔티티 업데이트
     * null이 아닌 필드만 업데이트
     *
     * @param source 업데이트할 데이터
     * @param target 업데이트 대상 YoutubeVideo
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "companyId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "thumbnailUrl", ignore = true)
    @Mapping(target = "isHidden", expression = "java(source.getIsHidden() != null ? source.getIsHidden() : false)")
    void updateVideo(YoutubeVideo source, @MappingTarget YoutubeVideo target);
}
