package com.example.Capstone.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record RestaurantRecommendationResponse(
        LocalDateTime generatedAt,
        String baseRegionName,
        int limit,
        List<RestaurantRecommendationItemResponse> items
) {
    public static RestaurantRecommendationResponse of(
            LocalDateTime generatedAt,
            String baseRegionName,
            int limit,
            List<RestaurantRecommendationItemResponse> items
    ) {
        return new RestaurantRecommendationResponse(
                generatedAt,
                baseRegionName,
                limit,
                items
        );
    }
}
