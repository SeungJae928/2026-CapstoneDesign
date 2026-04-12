package com.example.Capstone.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface RestaurantRecommendationRepository {

    List<UserInteractionRow> findUserInteractions(Long userId);

    List<CandidateRestaurantRow> findSameRegionCandidates(Long userId, String regionName, int limit, int smoothingConstant);

    List<CandidateRestaurantRow> findFallbackCandidates(Long userId, String excludedRegionName, int limit, int smoothingConstant);

    List<NeighborInteractionRow> findNeighborInteractions(Long userId, int minimumOverlap, int limit);

    List<RankingSignalRow> findRankingSignals(List<Long> restaurantIds, int smoothingConstant);

    record UserInteractionRow(
            Long restaurantId,
            String regionName,
            BigDecimal autoScore,
            LocalDateTime updatedAt
    ) {
    }

    record CandidateRestaurantRow(
            Long restaurantId,
            String restaurantName,
            String address,
            String regionName,
            String imageUrl
    ) {
    }

    record NeighborInteractionRow(
            Long userId,
            Long restaurantId,
            BigDecimal autoScore
    ) {
    }

    record RankingSignalRow(
            Long restaurantId,
            BigDecimal adjustedScore,
            BigDecimal averageAutoScore,
            Long evaluationCount
    ) {
    }
}
