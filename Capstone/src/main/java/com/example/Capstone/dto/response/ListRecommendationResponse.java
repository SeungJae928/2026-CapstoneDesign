package com.example.Capstone.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record ListRecommendationResponse(
        LocalDateTime generatedAt,
        String baseRegionName,
        int limit,
        List<ListRecommendationItemResponse> items
) {
    public static ListRecommendationResponse of(
            LocalDateTime generatedAt,
            String baseRegionName,
            int limit,
            List<ListRecommendationItemResponse> items
    ) {
        return new ListRecommendationResponse(
                generatedAt,
                baseRegionName,
                limit,
                items
        );
    }
}
