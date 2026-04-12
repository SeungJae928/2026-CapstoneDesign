package com.example.Capstone.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.Capstone.domain.Restaurant;
import com.example.Capstone.domain.RestaurantCategory;
import com.example.Capstone.dto.response.RestaurantRankingResponse;
import com.example.Capstone.exception.BusinessException;
import com.example.Capstone.repository.RestaurantCategoryRepository;
import com.example.Capstone.repository.RestaurantRankingRow;
import com.example.Capstone.repository.RestaurantRepository;

import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class RankingServiceTest {

    @Mock
    private RestaurantRepository restaurantRepository;

    @Mock
    private RestaurantCategoryRepository restaurantCategoryRepository;

    @InjectMocks
    private RankingService rankingService;

    @Test
    @DisplayName("limit가 없으면 기본값 20을 사용한다")
    void usesDefaultLimitWhenLimitIsNull() {
        when(restaurantRepository.findRestaurantRankings(null, null, 20, 5))
                .thenReturn(List.of());

        RestaurantRankingResponse response = rankingService.getRestaurantRankings(null, null, null);

        assertEquals(20, response.limit());
        assertEquals("NATIONAL", response.scope());
        verify(restaurantRepository).findRestaurantRankings(null, null, 20, 5);
        verify(restaurantCategoryRepository, never())
                .findAllByRestaurantIdInOrderByRestaurantIdAscCategoryNameAsc(org.mockito.ArgumentMatchers.anyList());
    }

    @Test
    @DisplayName("limit가 최대값을 넘으면 50으로 보정한다")
    void clampsLimitToMaximum() {
        when(restaurantRepository.findRestaurantRankings("서울", "한식", 50, 5))
                .thenReturn(List.of());

        RestaurantRankingResponse response = rankingService.getRestaurantRankings("서울", "한식", 99);

        assertEquals(50, response.limit());
        assertEquals("REGION", response.scope());
        verify(restaurantRepository).findRestaurantRankings("서울", "한식", 50, 5);
    }

    @Test
    @DisplayName("limit가 1 미만이면 예외가 발생한다")
    void throwsWhenLimitIsLessThanOne() {
        assertThrows(BusinessException.class, () -> rankingService.getRestaurantRankings(null, null, 0));

        verify(restaurantRepository, never())
                .findRestaurantRankings(
                        org.mockito.ArgumentMatchers.any(),
                        org.mockito.ArgumentMatchers.any(),
                        org.mockito.ArgumentMatchers.anyInt(),
                        org.mockito.ArgumentMatchers.anyInt()
                );
    }

    @Test
    @DisplayName("랭킹 결과에 카테고리를 매핑하고 순차 rank를 부여한다")
    void mapsCategoriesAndAssignsRanks() {
        RestaurantRankingRow first = new RestaurantRankingRow(
                10L,
                "식당 A",
                "서울",
                "a.png",
                new BigDecimal("91.5"),
                2L,
                new BigDecimal("87.428571")
        );
        RestaurantRankingRow second = new RestaurantRankingRow(
                11L,
                "식당 B",
                "서울",
                "b.png",
                new BigDecimal("88.0"),
                1L,
                new BigDecimal("84.000000")
        );

        when(restaurantRepository.findRestaurantRankings("서울", "한식", 20, 5))
                .thenReturn(List.of(first, second));
        when(restaurantCategoryRepository.findAllByRestaurantIdInOrderByRestaurantIdAscCategoryNameAsc(eq(List.of(10L, 11L))))
                .thenReturn(List.of(
                        category(10L, "국밥"),
                        category(10L, "한식"),
                        category(11L, "한식")
                ));

        RestaurantRankingResponse response = rankingService.getRestaurantRankings("서울", "한식", 20);

        assertEquals(2, response.items().size());
        assertEquals(1, response.items().get(0).rank());
        assertEquals(10L, response.items().get(0).restaurantId());
        assertEquals(List.of("국밥", "한식"), response.items().get(0).categories());
        assertEquals(new BigDecimal("87.43"), response.items().get(0).adjustedScore());
        assertEquals(2, response.items().get(0).evaluationCount());

        assertEquals(2, response.items().get(1).rank());
        assertEquals(List.of("한식"), response.items().get(1).categories());
    }

    private RestaurantCategory category(Long restaurantId, String categoryName) {
        Restaurant restaurant = Restaurant.builder()
                .name("식당")
                .address("주소")
                .regionName("서울")
                .imageUrl("image")
                .lat(new BigDecimal("37.0"))
                .lng(new BigDecimal("127.0"))
                .build();
        ReflectionTestUtils.setField(restaurant, "id", restaurantId);

        RestaurantCategory category = RestaurantCategory.builder()
                .restaurant(restaurant)
                .categoryName(categoryName)
                .build();
        ReflectionTestUtils.setField(category, "id", restaurantId * 100);
        return category;
    }
}
