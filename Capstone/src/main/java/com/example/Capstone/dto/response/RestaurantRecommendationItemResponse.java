package com.example.Capstone.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record RestaurantRecommendationItemResponse(
        int rank,
        Long restaurantId,
        String restaurantName,
        String regionName,
        String imageUrl,
        List<String> categories,
        BigDecimal finalScore,
        BigDecimal userPreferenceScore,
        BigDecimal categoryFitScore,
        BigDecimal rankingAdjustmentScore,
        BigDecimal collaborativeScore,
        BigDecimal regionScore,
        boolean fallbackRegion
) {
}
