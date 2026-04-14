# recommendation-policy.md

기준 날짜 및 시간: 2026-04-13 18:48:26 (Asia/Seoul)

## 1. 범위
이 문서는 현재 구현된 식당 추천 API의 입력 데이터, 후보 필터, 점수 계산, 튜닝 규칙, 응답 구조를 정리한다.

대상 코드:
- `RecommendationController`
- `RestaurantRecommendationService`
- `RestaurantRecommendationScorer` (`com.example.Capstone.recommendation.scorer`)
- `RestaurantRecommendationModels` (`com.example.Capstone.recommendation.model`)
- `RestaurantRecommendationRepository`
- `RestaurantRecommendationRepositoryImpl`

구조 메모:
- `RestaurantRecommendationScorer`는 점수 계산 전용 컴포넌트다.
- `RestaurantRecommendationModels`는 추천 계산 중간값을 담는 내부 모델이며, 외부 응답 DTO가 아니다.

## 2. 기능 범위
- 추천 대상은 식당이다.
- 결과는 상위 4개 식당만 반환한다.
- 계산 방식은 실시간 계산이다.

## 3. 현재 코드 기준
### 엔드포인트
- `GET /recommendations/restaurants`

현재 보안 설정 기준:
- `/recommendations/**`는 permitAll이 아니다.
- 현재 코드는 인증된 사용자 기준 개인화 추천 API로 동작한다.

### 추천 입력
- 현재 사용자 전체 활성 리스트를 기준으로 프로필을 계산한다.
- 입력 조인:
  - `users`
  - `user_lists`
  - `list_restaurants`
  - `restaurants`
  - `restaurant_categories`

제외 조건:
- `User.isDeleted = true`
- `User.isHidden = true`
- `UserList.isDeleted = true`
- `UserList.isHidden = true`
- `Restaurant.isDeleted = true`
- `Restaurant.isHidden = true`

포함 조건:
- `UserList.isPublic = false`도 계산 포함

### 사용자-식당 축약
- 같은 사용자가 같은 식당을 여러 리스트에 담아도 `user_id + restaurant_id` 기준 `max(autoScore)` 1회만 사용한다.

### 주요 프로필 구성
- `ownedRestaurantIds`
- `restaurantBestScore`
- `categoryPreference`
- `regionPreference`
- `topLikedRestaurants`
- `dominantRegion`

### 후보 선택
- same-region 후보를 먼저 조회한다.
- same-region 후보가 4개 미만일 때만 fallback 지역 후보를 추가한다.

## 4. 점수 계산
```text
finalScore =
  0.45 * userPreferenceScore +
  0.20 * categoryFitScore +
  0.20 * rankingAdjustmentScore +
  0.05 * collaborativeScore +
  0.10 * regionScore
```

보정 규칙:
- 0 유사도 후보 감점 `0.25`
- `userPreferenceScore = 0`이면 `collaborativeScore` 50%만 반영
- fallback `regionScore = 0.55`

## 5. 응답 구조
루트:
- `generatedAt`
- `baseRegionName`
- `limit`
- `items`

아이템:
- `rank`
- `restaurantId`
- `restaurantName`
- `regionName`
- `imageUrl`
- `categories`
- `finalScore`
- `userPreferenceScore`
- `categoryFitScore`
- `rankingAdjustmentScore`
- `collaborativeScore`
- `regionScore`
- `fallbackRegion`

## 6. 추가 확인 필요
- cold start 기본 추천 정책
- same-region 우선 체감 강화 필요 여부
- 카테고리 alias 정규화

## 7. 후속 수정 후보
- 추천 snapshot / cache 계층 검토
- end-to-end 추천 품질 검증 자동화
