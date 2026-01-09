package com.nalsil.bear.mapper;

import com.nalsil.bear.domain.post.Post;
import com.nalsil.bear.dto.request.CreatePostRequest;
import com.nalsil.bear.dto.response.PostResponse;
import org.mapstruct.*;

import java.time.LocalDateTime;

/**
 * Post 엔티티와 DTO 간의 매핑을 담당하는 MapStruct Mapper
 */
@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        imports = LocalDateTime.class
)
public interface PostMapper {

    /**
     * Post 엔티티를 PostResponse DTO로 변환
     *
     * @param post Post 엔티티
     * @return PostResponse DTO
     */
    PostResponse toResponse(Post post);

    /**
     * CreatePostRequest DTO를 Post 엔티티로 변환
     * 생성 시 필요한 필드들을 자동으로 설정
     *
     * @param request CreatePostRequest DTO
     * @return Post 엔티티
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "author", ignore = true)
    @Mapping(target = "viewCount", constant = "0")
    @Mapping(target = "createdAt", expression = "java(LocalDateTime.now())")
    @Mapping(target = "updatedAt", expression = "java(LocalDateTime.now())")
    Post toEntity(CreatePostRequest request);

    /**
     * CreatePostRequest DTO로 기존 Post 엔티티 업데이트
     * null이 아닌 필드만 업데이트
     *
     * @param request CreatePostRequest DTO
     * @param post 업데이트할 Post 엔티티
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "author", ignore = true)
    @Mapping(target = "viewCount", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", expression = "java(LocalDateTime.now())")
    void updateEntityFromRequest(CreatePostRequest request, @MappingTarget Post post);
}
