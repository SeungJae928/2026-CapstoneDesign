package com.example.Capstone.controller;

import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.example.Capstone.dto.response.RestaurantRankingItemResponse;
import com.example.Capstone.dto.response.RestaurantRankingResponse;
import com.example.Capstone.exception.BusinessException;
import com.example.Capstone.exception.GlobalExceptionHandler;
import com.example.Capstone.service.RankingService;

@ExtendWith(MockitoExtension.class)
class RankingControllerTest {

    private MockMvc mockMvc;

    @Mock
    private RankingService rankingService;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new RankingController(rankingService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("파라미터 없이 요청하면 서비스에 null 파라미터가 전달되고 응답 구조가 반환된다")
    void getRestaurantRankingsWithoutParams() throws Exception {
        when(rankingService.getRestaurantRankings(null, null, null))
                .thenReturn(response(
                        "NATIONAL",
                        null,
                        null,
                        20,
                        List.of(item(1, 101L, "식당 A", "서울", List.of("한식"), "87.43", "91.50", 8))
                ));

        mockMvc.perform(get("/rankings/restaurants"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.scope").value("NATIONAL"))
                .andExpect(jsonPath("$.regionName").value(nullValue()))
                .andExpect(jsonPath("$.category").value(nullValue()))
                .andExpect(jsonPath("$.limit").value(20))
                .andExpect(jsonPath("$.items[0].rank").value(1))
                .andExpect(jsonPath("$.items[0].restaurantId").value(101))
                .andExpect(jsonPath("$.items[0].restaurantName").value("식당 A"))
                .andExpect(jsonPath("$.items[0].categories[0]").value("한식"))
                .andExpect(jsonPath("$.items[0].adjustedScore").value(87.43))
                .andExpect(jsonPath("$.items[0].averageAutoScore").value(91.5))
                .andExpect(jsonPath("$.items[0].evaluationCount").value(8));

        verify(rankingService).getRestaurantRankings(null, null, null);
    }

    @Test
    @DisplayName("region/category/limit 쿼리 파라미터가 서비스에 그대로 전달된다")
    void bindsRegionCategoryAndLimitParams() throws Exception {
        when(rankingService.getRestaurantRankings("서울", "한식", 10))
                .thenReturn(response("REGION", "서울", "한식", 10, List.of()));

        mockMvc.perform(get("/rankings/restaurants")
                        .param("regionName", "서울")
                        .param("category", "한식")
                        .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.scope").value("REGION"))
                .andExpect(jsonPath("$.regionName").value("서울"))
                .andExpect(jsonPath("$.category").value("한식"))
                .andExpect(jsonPath("$.limit").value(10));

        verify(rankingService).getRestaurantRankings("서울", "한식", 10);
    }

    @Test
    @DisplayName("limit 검증 실패 시 400 에러 응답을 반환한다")
    void returnsBadRequestWhenLimitValidationFails() throws Exception {
        when(rankingService.getRestaurantRankings(null, null, 0))
                .thenThrow(new BusinessException("limit는 1 이상이어야 합니다.", HttpStatus.BAD_REQUEST));

        mockMvc.perform(get("/rankings/restaurants")
                        .param("limit", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("limit는 1 이상이어야 합니다."));
    }

    @Test
    @DisplayName("응답에 랭킹 item 배열 구조가 그대로 노출된다")
    void returnsExpectedResponseStructure() throws Exception {
        when(rankingService.getRestaurantRankings("서울", null, null))
                .thenReturn(response(
                        "REGION",
                        "서울",
                        null,
                        20,
                        List.of(
                                item(1, 201L, "식당 B", "서울", List.of("한식", "국밥"), "90.12", "92.00", 12),
                                item(2, 202L, "식당 C", "서울", List.of("한식"), "88.33", "89.50", 7)
                        )
                ));

        mockMvc.perform(get("/rankings/restaurants")
                        .param("regionName", "서울"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.generatedAt").exists())
                .andExpect(jsonPath("$.items.length()").value(2))
                .andExpect(jsonPath("$.items[0].rank").value(1))
                .andExpect(jsonPath("$.items[0].restaurantId").value(201))
                .andExpect(jsonPath("$.items[0].regionName").value("서울"))
                .andExpect(jsonPath("$.items[0].imageUrl").value("image-201"))
                .andExpect(jsonPath("$.items[0].categories[0]").value("한식"))
                .andExpect(jsonPath("$.items[0].categories[1]").value("국밥"))
                .andExpect(jsonPath("$.items[1].rank").value(2))
                .andExpect(jsonPath("$.items[1].restaurantId").value(202));

        verify(rankingService).getRestaurantRankings("서울", null, null);
    }

    private RestaurantRankingResponse response(
            String scope,
            String regionName,
            String category,
            int limit,
            List<RestaurantRankingItemResponse> items
    ) {
        return RestaurantRankingResponse.of(
                LocalDateTime.of(2026, 4, 12, 2, 30, 0),
                scope,
                regionName,
                category,
                limit,
                items
        );
    }

    private RestaurantRankingItemResponse item(
            int rank,
            Long restaurantId,
            String restaurantName,
            String regionName,
            List<String> categories,
            String adjustedScore,
            String averageAutoScore,
            long evaluationCount
    ) {
        return new RestaurantRankingItemResponse(
                rank,
                restaurantId,
                restaurantName,
                regionName,
                "image-" + restaurantId,
                categories,
                new BigDecimal(adjustedScore),
                new BigDecimal(averageAutoScore),
                evaluationCount
        );
    }
}
