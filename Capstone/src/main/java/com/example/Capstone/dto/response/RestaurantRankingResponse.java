package com.example.Capstone.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record RestaurantRankingResponse(
        LocalDateTime generatedAt,
        String scope,
        String regionName,
        String category,
        int limit,
        List<RestaurantRankingItemResponse> items
) {
    public static RestaurantRankingResponse of(
            LocalDateTime generatedAt,
            String scope,
            String regionName,
            String category,
            int limit,
            List<RestaurantRankingItemResponse> items
    ) {
        return new RestaurantRankingResponse(
                generatedAt,
                scope,
                regionName,
                category,
                limit,
                items
        );
    }
}
