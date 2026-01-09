package com.nalsil.bear.mapper;

import com.nalsil.bear.domain.product.Product;
import org.mapstruct.*;

import java.time.LocalDateTime;

/**
 * Product 엔티티 매핑을 담당하는 MapStruct Mapper
 */
@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        imports = LocalDateTime.class
)
public interface ProductMapper {

    /**
     * Product 엔티티 생성 준비 (companyId 설정 전)
     *
     * @param product 원본 Product
     * @return 생성 준비된 Product
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "companyId", ignore = true)
    @Mapping(target = "isHidden", expression = "java(product.getIsHidden() != null ? product.getIsHidden() : false)")
    @Mapping(target = "createdAt", expression = "java(LocalDateTime.now())")
    @Mapping(target = "updatedAt", ignore = true)
    Product prepareForCreate(Product product);

    /**
     * Product 엔티티 업데이트
     * null이 아닌 필드만 업데이트
     *
     * @param source 업데이트할 데이터
     * @param target 업데이트 대상 Product
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "companyId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "isHidden", expression = "java(source.getIsHidden() != null ? source.getIsHidden() : false)")
    @Mapping(target = "updatedAt", expression = "java(LocalDateTime.now())")
    void updateProduct(Product source, @MappingTarget Product target);
}
