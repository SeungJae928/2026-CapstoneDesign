package com.example.Capstone.dto.response;

public record RecommendationOwnerResponse(
        Long ownerId,
        String nickname,
        String profileImageUrl
) {
}
