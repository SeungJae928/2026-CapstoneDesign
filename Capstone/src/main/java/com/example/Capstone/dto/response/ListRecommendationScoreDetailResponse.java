package com.example.Capstone.dto.response;

import java.math.BigDecimal;

public record ListRecommendationScoreDetailResponse(
        BigDecimal preferenceMatchScore,
        BigDecimal restaurantMatchScore,
        BigDecimal categoryMatchScore,
        BigDecimal scoreStyleMatchScore,
        BigDecimal qualityScore,
        BigDecimal adjustedQualityScore,
        BigDecimal sizeConfidenceScore,
        BigDecimal collaborativeScore,
        BigDecimal regionScore
) {
}
