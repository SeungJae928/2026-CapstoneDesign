package com.example.Capstone.recommendation.model;

import java.util.List;
import java.util.Map;
import java.util.Set;

public final class ListRecommendationModels {

    private ListRecommendationModels() {
    }

    public record ListRecommendationUserProfile(
            Set<Long> ownedRestaurantIds,
            Map<Long, Double> restaurantBestScore,
            Map<String, Double> categoryPreference,
            Map<String, Double> regionPreference,
            ScoreVector scorePreference,
            String dominantRegion
    ) {
        public static ListRecommendationUserProfile empty() {
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

    public record ListRecommendationFeature(
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

    public record OwnerFeature(
            Long ownerId,
            String nickname,
            String profileImageUrl
    ) {
    }

    public record ScoreVector(
            double taste,
            double value,
            double mood
    ) {
        public static ScoreVector zero() {
            return new ScoreVector(0.0, 0.0, 0.0);
        }
    }

    public record ListRecommendationScoreComponents(
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
}
