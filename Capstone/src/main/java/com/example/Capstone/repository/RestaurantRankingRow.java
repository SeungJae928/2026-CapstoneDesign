package com.example.Capstone.repository;

import java.math.BigDecimal;

public record RestaurantRankingRow(
        Long restaurantId,
        String restaurantName,
        String regionName,
        String imageUrl,
        BigDecimal averageAutoScore,
        Long evaluationCount,
        BigDecimal adjustedScore
) {}
