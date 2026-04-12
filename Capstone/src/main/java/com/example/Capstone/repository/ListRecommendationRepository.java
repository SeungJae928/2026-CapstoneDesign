package com.example.Capstone.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface ListRecommendationRepository {

    List<UserListInteractionRow> findUserListInteractions(Long userId);

    List<CandidateListSummaryRow> findSameRegionCandidateLists(
            Long userId,
            String regionName,
            int limit,
            int minRestaurantCount,
            int smoothingConstant
    );

    List<CandidateListSummaryRow> findFallbackCandidateLists(
            Long userId,
            String regionName,
            int limit,
            int minRestaurantCount,
            int smoothingConstant
    );

    List<CandidateListRestaurantRow> findCandidateListRestaurants(List<Long> listIds);

    List<OwnerInteractionRow> findCandidateOwnerInteractions(Long userId, List<Long> ownerIds, int minimumOverlap);

    record UserListInteractionRow(
            Long listId,
            Long restaurantId,
            String regionName,
            BigDecimal tasteScore,
            BigDecimal valueScore,
            BigDecimal moodScore,
            BigDecimal autoScore,
            LocalDateTime updatedAt
    ) {
    }

    record CandidateListSummaryRow(
            Long listId,
            String title,
            String description,
            String regionName,
            Long ownerId,
            String ownerNickname,
            String ownerProfileImageUrl,
            Long restaurantCount,
            BigDecimal averageAutoScore,
            BigDecimal adjustedQualityScore,
            LocalDateTime updatedAt
    ) {
    }

    record CandidateListRestaurantRow(
            Long listId,
            Long restaurantId,
            String restaurantName,
            BigDecimal tasteScore,
            BigDecimal valueScore,
            BigDecimal moodScore,
            BigDecimal autoScore
    ) {
    }

    record OwnerInteractionRow(
            Long ownerId,
            Long restaurantId,
            BigDecimal autoScore
    ) {
    }
}
