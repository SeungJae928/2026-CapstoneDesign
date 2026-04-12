package com.example.Capstone.dto.response;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import com.example.Capstone.repository.RestaurantRankingRow;

public record RestaurantRankingItemResponse(
        int rank,
        Long restaurantId,
        String restaurantName,
        String regionName,
        String imageUrl,
        List<String> categories,
        BigDecimal adjustedScore,
        BigDecimal averageAutoScore,
        long evaluationCount
) {
    public static RestaurantRankingItemResponse of(
            int rank,
            RestaurantRankingRow row,
            List<String> categories
    ) {
        return new RestaurantRankingItemResponse(
                rank,
                row.restaurantId(),
                row.restaurantName(),
                row.regionName(),
                row.imageUrl(),
                categories,
                scale(row.adjustedScore()),
                scale(row.averageAutoScore()),
                row.evaluationCount()
        );
    }

    private static BigDecimal scale(BigDecimal value) {
        return value == null ? null : value.setScale(2, RoundingMode.HALF_UP);
    }
}
