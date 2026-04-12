package com.example.Capstone.controller;

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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.example.Capstone.dto.response.ListRecommendationItemResponse;
import com.example.Capstone.dto.response.ListRecommendationResponse;
import com.example.Capstone.dto.response.ListRecommendationScoreDetailResponse;
import com.example.Capstone.dto.response.RecommendationOwnerResponse;
import com.example.Capstone.dto.response.RestaurantRecommendationItemResponse;
import com.example.Capstone.dto.response.RestaurantRecommendationResponse;
import com.example.Capstone.exception.GlobalExceptionHandler;
import com.example.Capstone.service.ListRecommendationService;
import com.example.Capstone.service.RestaurantRecommendationService;

@ExtendWith(MockitoExtension.class)
class RecommendationControllerTest {

    private MockMvc mockMvc;

    @Mock
    private RestaurantRecommendationService restaurantRecommendationService;

    @Mock
    private ListRecommendationService listRecommendationService;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(
                        new RecommendationController(listRecommendationService, restaurantRecommendationService))
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("인증 사용자 기준 리스트 추천 응답을 반환한다")
    void returnsListRecommendationsForAuthenticatedUser() throws Exception {
        when(listRecommendationService.getListRecommendations(1L))
                .thenReturn(ListRecommendationResponse.of(
                        LocalDateTime.of(2026, 4, 12, 11, 0, 0),
                        "서울",
                        20,
                        List.of(
                                new ListRecommendationItemResponse(
                                        1,
                                        42L,
                                        "서울 리스트",
                                        "설명",
                                        "서울",
                                        new RecommendationOwnerResponse(7L, "mango", "profile.png"),
                                        List.of("한식", "국밥"),
                                        8,
                                        new BigDecimal("0.8421"),
                                        false,
                                        new ListRecommendationScoreDetailResponse(
                                                new BigDecimal("0.7812"),
                                                new BigDecimal("0.7200"),
                                                new BigDecimal("0.8300"),
                                                new BigDecimal("0.7900"),
                                                new BigDecimal("0.8100"),
                                                new BigDecimal("0.8450"),
                                                new BigDecimal("0.8000"),
                                                new BigDecimal("0.2200"),
                                                new BigDecimal("1.0000")
                                        )
                                )
                        )
                ));

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken(1L, "token"));
        SecurityContextHolder.setContext(context);

        try {
            mockMvc.perform(get("/recommendations/lists"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.baseRegionName").value("서울"))
                    .andExpect(jsonPath("$.limit").value(20))
                    .andExpect(jsonPath("$.items[0].rank").value(1))
                    .andExpect(jsonPath("$.items[0].listId").value(42))
                    .andExpect(jsonPath("$.items[0].title").value("서울 리스트"))
                    .andExpect(jsonPath("$.items[0].owner.ownerId").value(7))
                    .andExpect(jsonPath("$.items[0].recommendationScore").value(0.8421))
                    .andExpect(jsonPath("$.items[0].fallbackRegion").value(false));
        } finally {
            SecurityContextHolder.clearContext();
        }

        verify(listRecommendationService).getListRecommendations(1L);
    }

    @Test
    @DisplayName("인증 사용자 기준 식당 추천 응답을 반환한다")
    void returnsRestaurantRecommendationsForAuthenticatedUser() throws Exception {
        when(restaurantRecommendationService.getRestaurantRecommendations(1L))
                .thenReturn(RestaurantRecommendationResponse.of(
                        LocalDateTime.of(2026, 4, 12, 11, 0, 0),
                        "서울",
                        4,
                        List.of(
                                new RestaurantRecommendationItemResponse(
                                        1,
                                        101L,
                                        "식당 A",
                                        "서울",
                                        "a.png",
                                        List.of("한식"),
                                        new BigDecimal("0.9000"),
                                        new BigDecimal("0.8000"),
                                        new BigDecimal("0.7000"),
                                        new BigDecimal("0.6000"),
                                        new BigDecimal("0.0000"),
                                        new BigDecimal("1.0000"),
                                        false
                                )
                        )
                ));

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken(1L, "token"));
        SecurityContextHolder.setContext(context);

        try {
            mockMvc.perform(get("/recommendations/restaurants"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.baseRegionName").value("서울"))
                    .andExpect(jsonPath("$.limit").value(4))
                    .andExpect(jsonPath("$.items[0].rank").value(1))
                    .andExpect(jsonPath("$.items[0].restaurantId").value(101))
                    .andExpect(jsonPath("$.items[0].restaurantName").value("식당 A"))
                    .andExpect(jsonPath("$.items[0].finalScore").value(0.9))
                    .andExpect(jsonPath("$.items[0].fallbackRegion").value(false));
        } finally {
            SecurityContextHolder.clearContext();
        }

        verify(restaurantRecommendationService).getRestaurantRecommendations(1L);
    }
}
