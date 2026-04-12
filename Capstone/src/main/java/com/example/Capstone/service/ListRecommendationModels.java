package com.example.Capstone.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

record ListRecommendationUserProfile(
        Set<Long> ownedRestaurantIds,
        Map<Long, Double> restaurantBestScore,
        Map<String, Double> categoryPreference,
        Map<String, Double> regionPreference,
        ScoreVector scorePreference,
        String dominantRegion
) {
    static ListRecommendationUserProfile empty() {
        return new ListRecommendationUserProfile(
                Set.of(),
                Map.of(),
                Map.of(),
                Map.of(),
                ScoreVector.zero(),
                null
        );
    }
}

record ListRecommendationFeature(
        Long listId,
        String title,
        String description,
        String regionName,
        OwnerFeature owner,
        int restaurantCount,
        Map<Long, Double> restaurantWeight,
        Map<String, Double> categoryDistribution,
        ScoreVector scoreVector,
        double averageAutoScore,
        double adjustedQualityScore,
        List<String> categorySummary
) {
}

record OwnerFeature(
        Long ownerId,
        String nickname,
        String profileImageUrl
) {
}

record ScoreVector(
        double taste,
        double value,
        double mood
) {
    static ScoreVector zero() {
        return new ScoreVector(0.0, 0.0, 0.0);
    }
}

record ListRecommendationScoreComponents(
        double finalScore,
        double preferenceMatchScore,
        double restaurantMatchScore,
        double categoryMatchScore,
        double scoreStyleMatchScore,
        double qualityScore,
        double adjustedQualityScore,
        double sizeConfidenceScore,
        double collaborativeScore,
        double regionScore
) {
}
