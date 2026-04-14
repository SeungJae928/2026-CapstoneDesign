package com.example.Capstone.recommendation.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class RestaurantRecommendationModels {

    private RestaurantRecommendationModels() {
    }

    public record UserPreferenceProfile(
            Set<Long> ownedRestaurantIds,
            Map<Long, Double> restaurantBestScore,
            Map<String, Double> categoryPreference,
            Map<String, Double> regionPreference,
            List<LikedRestaurantFeature> topLikedRestaurants,
            String dominantRegion
    ) {
        public static UserPreferenceProfile empty() {
            return new UserPreferenceProfile(
                    Set.of(),
                    Map.of(),
                    Map.of(),
                    Map.of(),
                    List.of(),
                    null
            );
        }
    }

    public record LikedRestaurantFeature(
            Long restaurantId,
            double score,
            String regionName,
            Set<String> categories,
            LocalDateTime updatedAt
    ) {
    }

    public record RestaurantFeature(
            Long restaurantId,
            String restaurantName,
            String address,
            String regionName,
            String imageUrl,
            List<String> categories,
            double rankingAdjustmentScore,
            long evaluationCount
    ) {
    }

    public record NeighborProfile(
            Long userId,
            double similarity,
            Map<Long, Double> restaurantBestScore
    ) {
    }

    public record RecommendationScoreComponents(
            double finalScore,
            double userPreferenceScore,
            double categoryFitScore,
            double rankingAdjustmentScore,
            double collaborativeScore,
            double regionScore
    ) {
    }
}
