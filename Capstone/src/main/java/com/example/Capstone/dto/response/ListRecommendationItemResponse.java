package com.example.Capstone.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record ListRecommendationItemResponse(
        int rank,
        Long listId,
        String title,
        String description,
        String regionName,
        RecommendationOwnerResponse owner,
        List<String> categorySummary,
        int restaurantCount,
        BigDecimal recommendationScore,
        boolean fallbackRegion,
        ListRecommendationScoreDetailResponse scoreDetails
) {
}
