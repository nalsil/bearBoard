package com.nalsil.bear.service;

import com.nalsil.bear.domain.product.Product;
import com.nalsil.bear.domain.product.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

/**
 * ProductService 단위 테스트
 * 상품 조회 비즈니스 로직 검증
 */
@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private Product testProduct1;
    private Product testProduct2;

    @BeforeEach
    void setUp() {
        // 테스트용 상품 데이터 준비
        testProduct1 = Product.builder()
                .id(1L)
                .companyId(1L)
                .name("상품 1")
                .category("전자제품")
                .description("상품 설명 1")
                .price(new BigDecimal("100000.00"))
                .imageUrl("https://example.com/product1.jpg")
                .displayOrder(1)
                .isHidden(false)
                .build();

        testProduct2 = Product.builder()
                .id(2L)
                .companyId(1L)
                .name("상품 2")
                .category("전자제품")
                .description("상품 설명 2")
                .price(new BigDecimal("200000.00"))
                .imageUrl("https://example.com/product2.jpg")
                .displayOrder(2)
                .isHidden(false)
                .build();
    }

    @Test
    @DisplayName("기업 ID로 상품 목록 조회 (숨김 제외) - 성공")
    void testGetVisibleProductsByCompanyId_Success() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        when(productRepository.findByCompanyIdAndIsHiddenOrderByDisplayOrderAsc(1L, false, pageable))
                .thenReturn(Flux.just(testProduct1, testProduct2));

        // When
        Flux<Product> result = productService.getVisibleProductsByCompanyId(1L, pageable);

        // Then
        StepVerifier.create(result)
                .expectNextMatches(product ->
                        product.getId().equals(1L) &&
                        product.getName().equals("상품 1") &&
                        product.getIsHidden().equals(false)
                )
                .expectNextMatches(product ->
                        product.getId().equals(2L) &&
                        product.getName().equals("상품 2")
                )
                .verifyComplete();
    }

    @Test
    @DisplayName("기업 ID로 상품 목록 조회 - 빈 결과")
    void testGetVisibleProductsByCompanyId_Empty() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        when(productRepository.findByCompanyIdAndIsHiddenOrderByDisplayOrderAsc(
                anyLong(), anyBoolean(), any(Pageable.class)))
                .thenReturn(Flux.empty());

        // When
        Flux<Product> result = productService.getVisibleProductsByCompanyId(999L, pageable);

        // Then
        StepVerifier.create(result)
                .verifyComplete();
    }

    @Test
    @DisplayName("기업 ID와 카테고리로 상품 목록 조회 - 성공")
    void testGetVisibleProductsByCompanyIdAndCategory_Success() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        when(productRepository.findByCompanyIdAndCategoryAndIsHiddenOrderByDisplayOrderAsc(
                1L, "전자제품", false, pageable))
                .thenReturn(Flux.just(testProduct1, testProduct2));

        // When
        Flux<Product> result = productService.getVisibleProductsByCompanyIdAndCategory(
                1L, "전자제품", pageable);

        // Then
        StepVerifier.create(result)
                .expectNextMatches(product ->
                        product.getCategory().equals("전자제품") &&
                        product.getName().equals("상품 1")
                )
                .expectNextMatches(product ->
                        product.getCategory().equals("전자제품") &&
                        product.getName().equals("상품 2")
                )
                .verifyComplete();
    }

    @Test
    @DisplayName("기업 ID로 상품 개수 조회 (숨김 제외) - 성공")
    void testCountVisibleProductsByCompanyId_Success() {
        // Given
        when(productRepository.countByCompanyIdAndIsHidden(1L, false))
                .thenReturn(Mono.just(2L));

        // When
        Mono<Long> result = productService.countVisibleProductsByCompanyId(1L);

        // Then
        StepVerifier.create(result)
                .expectNext(2L)
                .verifyComplete();
    }

    @Test
    @DisplayName("기업 ID로 상품 개수 조회 - 0개")
    void testCountVisibleProductsByCompanyId_Zero() {
        // Given
        when(productRepository.countByCompanyIdAndIsHidden(anyLong(), anyBoolean()))
                .thenReturn(Mono.just(0L));

        // When
        Mono<Long> result = productService.countVisibleProductsByCompanyId(999L);

        // Then
        StepVerifier.create(result)
                .expectNext(0L)
                .verifyComplete();
    }

    @Test
    @DisplayName("상품 ID로 조회 - 성공")
    void testGetProductById_Success() {
        // Given
        when(productRepository.findById(1L))
                .thenReturn(Mono.just(testProduct1));

        // When
        Mono<Product> result = productService.getProductById(1L);

        // Then
        StepVerifier.create(result)
                .expectNextMatches(product ->
                        product.getId().equals(1L) &&
                        product.getName().equals("상품 1")
                )
                .verifyComplete();
    }

    @Test
    @DisplayName("상품 ID로 조회 - 존재하지 않음")
    void testGetProductById_NotFound() {
        // Given
        when(productRepository.findById(999L))
                .thenReturn(Mono.empty());

        // When
        Mono<Product> result = productService.getProductById(999L);

        // Then
        StepVerifier.create(result)
                .verifyComplete();
    }
}
