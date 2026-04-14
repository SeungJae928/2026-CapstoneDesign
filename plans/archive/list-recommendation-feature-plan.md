# 리스트 추천 기능 구현 계획

## Status
completed

## 작업명
리스트 추천 기능 구현 직전 초안: 범위 고정 / 점수식 / API / 쿼리 / 서비스 구조

## 목적
현재 DB 구조를 유지하면서 실시간 개인화 리스트 추천 API를 구현할 수 있도록, 구현 직전 수준의 설계와 범위를 고정한다.

이번 문서는 구현 전 계획 문서이며, 아직 프로덕션 코드 작성은 포함하지 않는다.

## 사용자 관점 결과
- 로그인 사용자는 자신의 전체 리스트 취향을 반영한 리스트 추천 20개를 받을 수 있다.
- 추천 대상은 내 리스트가 아닌 다른 사용자의 공개 리스트다.
- 같은 지역 리스트를 우선 추천받고, 부족할 때만 다른 지역 리스트가 보충된다.
- 응답에는 리스트 기본 정보와 추천 점수, 디버깅 가능한 점수 상세가 함께 담긴다.

## 범위
- `GET /recommendations/lists` API 추가
- 실시간 리스트 추천 계산 로직 추가
- 사용자 취향 프로필 / 후보 리스트 feature / scorer 추가
- 리스트 추천용 repository / service / dto / test 추가
- 기존 식당 추천과 분리된 리스트 추천 문서 반영

## 비범위
- 기존 식당 추천 로직 수정 또는 재설계
- 유사 유저 자체를 별도 API 또는 응답으로 노출
- 벡터 DB / 임베딩 / 배치 추천 / 캐시 도입
- 추천 결과 paging / cursor
- 추천 로그 수집 / 실험 플랫폼

## 관련 문서/코드
- 문서
  - `GUIDE.md`
  - `AGENTS.md`
  - `PLANS.md`
  - `DB.md`
  - `LOGIC.md`
  - `docs/db/lists.md`
  - `docs/db/restaurants.md`
  - `docs/logic/list-policy.md`
  - `docs/logic/score-policy.md`
  - `docs/logic/recommendation-policy.md`
  - `docs/logic/ranking-policy.md`
  - `docs/logic/validation-rules.md`
  - `docs/logic/visibility-policy.md`
  - `docs/current-gaps.md`
- 코드
  - `Capstone/src/main/java/com/example/Capstone/controller/RecommendationController.java`
  - `Capstone/src/main/java/com/example/Capstone/service/RestaurantRecommendationService.java`
  - `Capstone/src/main/java/com/example/Capstone/service/RestaurantRecommendationScorer.java`
  - `Capstone/src/main/java/com/example/Capstone/service/RestaurantRecommendationModels.java`
  - `Capstone/src/main/java/com/example/Capstone/service/RankingService.java`
  - `Capstone/src/main/java/com/example/Capstone/service/UserListService.java`
  - `Capstone/src/main/java/com/example/Capstone/repository/RestaurantRecommendationRepositoryImpl.java`
  - `Capstone/src/main/java/com/example/Capstone/repository/RestaurantRankingRepositoryImpl.java`
  - `Capstone/src/main/java/com/example/Capstone/repository/UserListRepository.java`
  - `Capstone/src/main/java/com/example/Capstone/repository/ListRestaurantRepository.java`
  - `Capstone/src/main/java/com/example/Capstone/domain/User.java`
  - `Capstone/src/main/java/com/example/Capstone/domain/UserList.java`
  - `Capstone/src/main/java/com/example/Capstone/domain/ListRestaurant.java`
  - `Capstone/src/main/java/com/example/Capstone/domain/Restaurant.java`
  - `Capstone/src/main/java/com/example/Capstone/domain/RestaurantCategory.java`

## 사전 확인 사항
- 이번 범위는 `리스트 추천`만 구현한다.
- 기존 `GET /recommendations/restaurants`와 식당 추천 서비스는 건드리지 않는다.
- 추천 기준 데이터는 현재 사용자의 전체 활성 리스트다.
- 후보는 다른 사용자의 리스트만 허용한다.
- 후보 리스트는 `isPublic = true` 이고, hidden / deleted 데이터는 제외한다.
- 후보 리스트는 visible restaurant 기준 최소 5개 이상이어야 한다.
- 같은 지역 리스트를 먼저 채우고, 20개에 못 미칠 때만 다른 지역 리스트를 fallback으로 사용한다.
- 유사 유저 신호는 내부 보조 점수로만 쓰고 외부 응답에는 유저 추천 결과를 노출하지 않는다.
- 현재 스택은 Spring Data JPA + native SQL 기반 read repository 패턴이 가장 자연스럽다.

## 1. 추천 알고리즘 흐름

### 1-1. 요청 진입
1. `@AuthenticationPrincipal Long userId`로 인증 사용자를 식별한다.
2. 삭제되지 않은 사용자 여부를 확인한다.
3. 추천 기준 지역은 사용자의 전체 활성 리스트에서 계산한 `dominantRegion`으로 잡는다.

### 1-2. 사용자 프로필 생성
1. 현재 사용자의 활성 리스트를 조회한다.
   - `user_lists.is_deleted = false`
   - `user_lists.is_hidden = false`
   - 연결된 식당도 `is_deleted = false`, `is_hidden = false`
2. 프로필 계산용 입력은 전체 리스트를 사용한다.
3. 동일 식당이 여러 리스트에 있더라도 `restaurantBestScore`는 식당별 최고 `autoScore` 1회만 유지한다.
4. 카테고리/지역/점수 성향은 전체 리스트 row를 가중합으로 반영한다.
5. tie-break 규칙까지 포함해 `dominantRegion`을 계산한다.

### 1-3. 동일 지역 후보 리스트 조회
1. 내 리스트를 제외한다.
2. 다른 사용자의 공개 리스트만 조회한다.
3. list / owner / restaurant의 hidden / deleted 데이터를 제외한다.
4. visible restaurant 수가 5개 이상인 리스트만 남긴다.
5. 동일 지역 리스트만 먼저 조회한다.
6. DB에서 1차 품질 정렬을 적용해 상위 후보 풀만 가져온다.

### 1-4. fallback 지역 후보 조회
1. 동일 지역 결과가 20개 미만일 때만 실행한다.
2. 동일한 필터를 유지하되 지역만 `dominantRegion != candidateRegion`으로 바꾼다.
3. fallback 후보도 같은 점수 구조를 쓰되 지역 점수는 낮게 준다.
4. fallback 결과는 부족한 개수만 채우기 위한 보충 풀로만 사용한다.

### 1-5. 후보 리스트 feature 생성
1. 후보 리스트의 식당 row를 배치 조회한다.
2. 식당 카테고리를 한 번에 조회해 리스트별 카테고리 분포를 만든다.
3. 리스트별로 아래 값을 만든다.
   - restaurant weight distribution
   - category distribution
   - score vector
   - average auto score
   - adjusted quality score
   - restaurant count
   - top categories summary

### 1-6. 선택적 협업 신호 계산
1. 후보 리스트 소유자들만 대상으로 owner similarity를 계산한다.
2. 기준은 `user_restaurant_best` 기반 cosine similarity다.
3. overlap 2개 미만이면 유사 소유자로 보지 않는다.
4. 값이 없으면 `collaborativeScore = 0`으로 둔다.
5. 이 점수는 보조 신호이며 추천 결과에서 별도 노출하지 않는다.

### 1-7. 점수 계산과 정렬
1. 동일 지역 후보를 모두 score 계산한다.
2. 동일 지역 후보는 `finalScore` 기준으로 정렬해 우선 채운다.
3. 같은 지역 후보가 20개 미만이면 fallback 후보를 score 계산한다.
4. fallback 후보도 `finalScore` 기준 정렬한 뒤 부족한 수만큼 뒤에 붙인다.
5. 최종 응답은 최대 20개다.

이 흐름이면 “최종 점수 기반 정렬”과 “같은 지역 우선”을 동시에 만족할 수 있다.
같은 지역 우선은 풀 선택 단계에서 보장하고, 각 풀 내부에서는 최종 점수로 정렬한다.

## 2. 추천 점수 계산식

### 2-1. 최종 점수식
`finalScore = 0.55 * preferenceMatchScore + 0.20 * qualityScore + 0.10 * collaborativeScore + 0.15 * regionScore`

### 2-2. 선호 일치 점수
`preferenceMatchScore = 0.45 * restaurantMatchScore + 0.35 * categoryMatchScore + 0.20 * scoreStyleMatchScore`

구성 의미:
- `restaurantMatchScore`
  - 후보 리스트에 들어 있는 식당이 현재 사용자의 고득점 식당 취향과 얼마나 겹치는지
  - 후보 리스트의 restaurant weight를 가중치로 사용한다
- `categoryMatchScore`
  - 사용자 카테고리 분포와 후보 리스트 카테고리 분포의 적합도
  - 정규화된 분포의 dot product를 사용한다
- `scoreStyleMatchScore`
  - 사용자와 후보 리스트의 `taste / value / mood` 평균 벡터 유사도
  - 3차원 cosine similarity를 사용한다

### 2-3. 리스트 품질 점수
`qualityScore = 0.75 * adjustedQualityScore + 0.25 * sizeConfidenceScore`

구성 의미:
- `adjustedQualityScore`
  - 리스트 내부 식당들의 평균 `autoScore`를 베이지안 방식으로 보정한 값
  - `adjustedQualityScore = (n / (n + m)) * R + (m / (n + m)) * C`
  - `R`: 리스트 평균 `autoScore` 정규화값
  - `n`: visible restaurant 수
  - `C`: eligible public list 전체의 평균 리스트 점수 정규화값
  - `m`: 보정 상수 `5`
- `sizeConfidenceScore`
  - 표본성 보조 점수
  - `min(1.0, restaurantCount / 10.0)`
  - 최소 기준 5개 리스트는 `0.5`, 10개 이상이면 `1.0`

### 2-4. 지역 점수
- 같은 지역: `1.0`
- fallback 지역: `0.65`

### 2-5. 협업 점수
- `collaborativeScore = ownerSimilarity`
- candidate owner와 현재 사용자 사이의 cosine similarity
- 값이 없으면 `0`

### 2-6. 보정 규칙
- `restaurantMatchScore = 0` 이고 `categoryMatchScore = 0` 인 후보는 유사도 부족 후보로 본다.
- 이 경우 `collaborativeScore`는 50%만 반영한다.
- 그래도 `preferenceMatchScore = 0` 이면 최종 점수에서 `0.10` 감점한다.

이 보정은 인기 있지만 취향 근거가 없는 공개 리스트가 품질 점수만으로 과하게 올라오는 현상을 줄이기 위한 최소 장치다.

## 3. 사용자 특성 / 리스트 특성 표현 구조

### 3-1. 사용자 특성
```java
record ListRecommendationUserProfile(
    Set<Long> ownedRestaurantIds,
    Map<Long, Double> restaurantBestScore,
    Map<String, Double> categoryPreference,
    Map<String, Double> regionPreference,
    ScoreVector scorePreference,
    String dominantRegion
) {}
```

### 3-2. 후보 리스트 특성
```java
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
) {}
```

### 3-3. 공통 벡터 구조
```java
record ScoreVector(
    double taste,
    double value,
    double mood
) {}
```

### 3-4. 점수 컴포넌트 구조
```java
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
) {}
```

이 구조를 Map / record / 배열 중심으로 두면 현재 단계 구현이 단순하고, 나중에는 같은 데이터를 dense vector로 바꾸기 쉽다.

## 4. 현재 구조에서의 구현 방식

### 4-1. 왜 native SQL + 서비스 조립인가
- 이미 랭킹과 식당 추천이 native SQL read repository 패턴을 사용하고 있다.
- 현재 프로젝트에는 QueryDSL 설정이 없다.
- 리스트 추천도 집계 성격이 강해서 DB에서 후보를 먼저 잘라내는 편이 효율적이다.
- 식당 추천 로직을 건드리지 않고 새 repository를 분리할 수 있다.

### 4-2. 추천 입력 데이터
현재 사용자 프로필 입력:
- `user_lists`
- `list_restaurants`
- `restaurants`
- `restaurant_categories`

후보 리스트 입력:
- `user_lists`
- `users`
- `list_restaurants`
- `restaurants`
- `restaurant_categories`

### 4-3. 후보 리스트 필터
- `ul.user_id <> :userId`
- `ul.is_public = true`
- `ul.is_deleted = false`
- `ul.is_hidden = false`
- `u.is_deleted = false`
- `u.is_hidden = false`
- `r.is_deleted = false`
- `r.is_hidden = false`
- visible restaurant 수 `>= 5`

### 4-4. 같은 지역 우선 처리 방식
권장 구현은 두 단계 조회다.

1. same-region candidate query
2. fallback candidate query only if needed

이렇게 두 query로 나누면:
- 정책이 명확하다
- 서비스 로직이 읽기 쉽다
- fallback이 메인 후보를 덮어쓰지 않는다

### 4-5. 리스트 품질 보정 방식
후보 summary query에서 아래 값을 미리 계산한다.
- `averageAutoScore`
- `restaurantCount`
- `adjustedQualityScore`

즉 scorer는 “이미 정리된 feature”를 받아 가중합만 담당하고, SQL은 후보 압축과 1차 품질 정렬을 담당한다.

## 5. Spring Boot 기준 코드 설계

### 5-1. Controller
- 파일: `RecommendationController`
- 기존 `/recommendations/restaurants`는 유지
- 새 메서드만 추가

```java
@GetMapping("/lists")
public ResponseEntity<ListRecommendationResponse> getListRecommendations(
        @AuthenticationPrincipal Long userId) {
    return ResponseEntity.ok(listRecommendationService.getListRecommendations(userId));
}
```

변경 범위는 controller 1개 메서드 추가 + service 주입 추가에 그친다.

### 5-2. Service
- 새 클래스: `ListRecommendationService`
- 책임:
  - 사용자 검증
  - user profile 생성
  - same-region / fallback 후보 조회 orchestration
  - owner similarity 계산
  - feature 조립
  - scorer 호출
  - 응답 DTO 조립

권장 상수:
- `RESULT_LIMIT = 20`
- `SAME_REGION_CANDIDATE_LIMIT = 200`
- `FALLBACK_CANDIDATE_LIMIT = 120`
- `MIN_COMMON_RESTAURANTS = 2`
- `QUALITY_SMOOTHING_CONSTANT = 5`

### 5-3. Scorer
- 새 클래스: `ListRecommendationScorer`
- 책임:
  - feature 간 유사도 계산
  - 점수식 적용
  - 보정 규칙 적용
  - `ListRecommendationScoreComponents` 반환

식당 추천과 동일하게 “scorer는 순수 계산 전담”으로 분리하는 편이 가장 안전하다.

### 5-4. Repository
- 새 인터페이스: `ListRecommendationRepository`
- 새 구현체: `ListRecommendationRepositoryImpl`

권장 메서드:
```java
List<UserListInteractionRow> findUserListInteractions(Long userId);
List<CandidateListSummaryRow> findSameRegionCandidateLists(Long userId, String regionName, int limit, int minRestaurantCount, int smoothingConstant);
List<CandidateListSummaryRow> findFallbackCandidateLists(Long userId, String regionName, int limit, int minRestaurantCount, int smoothingConstant);
List<CandidateListRestaurantRow> findCandidateListRestaurants(List<Long> listIds);
List<OwnerInteractionRow> findCandidateOwnerInteractions(Long userId, List<Long> ownerIds, int minimumOverlap);
```

### 5-5. DTO
- 새 응답 루트: `ListRecommendationResponse`
- 새 item DTO: `ListRecommendationItemResponse`
- 새 owner DTO: `RecommendationOwnerResponse`
- 새 debug DTO: `ListRecommendationScoreDetailResponse`

권장 응답 구조:
```json
{
  "generatedAt": "2026-04-12T15:30:00",
  "baseRegionName": "서울",
  "limit": 20,
  "items": [
    {
      "rank": 1,
      "listId": 42,
      "title": "서울 국밥 리스트",
      "description": "비 오는 날 가기 좋은 곳",
      "regionName": "서울",
      "owner": {
        "ownerId": 7,
        "nickname": "mango",
        "profileImageUrl": "https://..."
      },
      "categorySummary": ["국밥", "한식", "해장"],
      "restaurantCount": 8,
      "recommendationScore": 0.8421,
      "fallbackRegion": false,
      "scoreDetails": {
        "preferenceMatchScore": 0.7812,
        "restaurantMatchScore": 0.7200,
        "categoryMatchScore": 0.8300,
        "scoreStyleMatchScore": 0.7900,
        "qualityScore": 0.8100,
        "adjustedQualityScore": 0.8450,
        "sizeConfidenceScore": 0.8000,
        "collaborativeScore": 0.2200,
        "regionScore": 1.0000
      }
    }
  ]
}
```

### 5-6. Models
- 새 파일: `ListRecommendationModels.java`
- 역할:
  - user profile
  - list feature
  - score vector
  - owner feature
  - score components

현재 식당 추천의 `RestaurantRecommendationModels.java`와 같은 패턴을 유지한다.

## 6. 쿼리 구조 초안

### 6-1. 사용자 프로필 입력 query
목표:
- 현재 사용자 전체 활성 리스트 row 수집
- restaurant best score와 category/region/score 성향 계산에 필요한 raw row 반환

기본 조건:
```sql
ul.user_id = :userId
and ul.is_deleted = false
and ul.is_hidden = false
and r.is_deleted = false
and r.is_hidden = false
```

반환 예시 필드:
- `list_id`
- `region_name`
- `restaurant_id`
- `taste_score`
- `value_score`
- `mood_score`
- `auto_score`
- `updated_at`

### 6-2. 후보 summary query
핵심은 “visible restaurant 기준 정상 public list”만 남기는 것이다.

권장 CTE 구조:
1. `eligible_entries`
   - public / visible list row
2. `candidate_list_stats`
   - 리스트별 평균 점수, 개수, 평균 taste/value/mood 집계
3. `scope_stats`
   - 전체 후보 리스트 평균 점수
4. final select
   - adjusted quality score 계산

same-region query는 `ul.region_name = :regionName`

fallback query는 `ul.region_name <> :regionName`

정렬 기준:
1. `adjusted_quality_score desc`
2. `restaurant_count desc`
3. `updated_at desc`
4. `list_id asc`

### 6-3. 후보 리스트 식당 query
요약 row만으로는 restaurantMatch 계산이 불가능하므로, 최종 후보 listIds에 대해 식당 row를 한 번 더 읽는다.

반환 필드 예시:
- `list_id`
- `restaurant_id`
- `restaurant_name`
- `taste_score`
- `value_score`
- `mood_score`
- `auto_score`

이 결과와 `restaurant_categories` 배치 조회를 합쳐 list feature를 만든다.

### 6-4. owner similarity query
후보 owner만 대상으로 `user_restaurant_best`를 조회한다.

이 방식의 장점:
- 모든 사용자를 neighbor로 읽지 않아도 된다
- 현재 추천 결과에 실제로 필요한 owner만 본다
- 협업 점수를 optional 신호로 유지하기 쉽다

## 7. 검증 방법

### 7-1. 공통 기반 검증
- `ListRestaurantAutoScoreTest`
- `UserListServiceMinimumRestaurantCountTest`
- `UserListServiceDuplicateRestaurantTest`
- `UserListServiceRegionMatchTest`

### 7-2. 리스트 추천 전용 검증
- `ListRecommendationScorerTest`
  - 고정 가중치 계산
  - fallback 지역 점수
  - zero-similarity 보정
- `ListRecommendationServiceTest`
  - same-region 우선 채우기
  - fallback 보충 조건
  - 내 리스트 제외
  - public list만 후보 사용
  - empty profile 시 빈 결과
  - owner similarity가 없어도 정상 동작
- `RecommendationControllerTest`
  - `/recommendations/lists` 응답 구조와 인증 사용자 바인딩
- `ListRecommendationRepositoryTest`
  - hidden / deleted / public 필터
  - 최소 5개 visible restaurant 필터
  - same-region / fallback 분리
  - adjustedQualityScore 계산

### 7-3. 현재 확인된 실행 명령
현재 코드베이스에서 아래 명령 실행을 확인했다.

```powershell
./gradlew.bat test --tests com.example.Capstone.domain.ListRestaurantAutoScoreTest --tests com.example.Capstone.service.RestaurantRecommendationScorerTest --tests com.example.Capstone.service.RestaurantRecommendationServiceTest --tests com.example.Capstone.controller.RecommendationControllerTest
```

## 8. 성능 고려사항
- 후보를 먼저 DB에서 줄이고, feature 조립은 상위 후보 풀에 대해서만 수행한다.
- same-region / fallback을 분리해 불필요한 fallback query를 줄인다.
- 카테고리는 `RestaurantCategoryRepository.findAllByRestaurantIdInOrderByRestaurantIdAscCategoryNameAsc(...)` 패턴을 재사용한다.
- owner similarity는 후보 owner만 대상으로 제한한다.
- 응답 limit는 20이지만 candidate limit는 더 크게 둬 재정렬 여유를 확보한다.
- 실시간 계산은 유지하되, 추후 p95 지연이 커지면 snapshot / cache 계층으로 교체할 수 있게 service-repository-scoring 경계를 유지한다.

## 9. 확장 가능하도록 설계된 부분
- `Map<String, Double>`와 `ScoreVector` 기반 feature 구조는 나중에 dense vector로 쉽게 변환할 수 있다.
- scorer가 repository와 분리되어 있어 weight tuning이나 formula 교체가 쉽다.
- owner similarity는 optional이므로 cold start나 sparse data에서도 동작이 깨지지 않는다.
- candidate summary query와 feature query를 분리해 두면 캐시/스냅샷 적용 시 repository만 교체하면 된다.
- debug score detail을 응답에 두면 운영 로그나 오프라인 튜닝에 바로 활용할 수 있다.
- 기존 식당 추천 서비스와 완전히 분리하면 이후 `RecommendationController` 아래에 추천 유형을 늘려도 서로 간섭이 적다.

## 리스크
- 리스트 상세 공개/비공개 정책 gap은 여전히 일반 조회와 추천 계산을 분리해서 관리해야 한다.
- public list만 후보로 쓰면 데이터가 적은 사용자 지역에서는 fallback 의존도가 높아질 수 있다.
- 문자열 기반 카테고리 체계라 alias 문제가 생기면 categoryMatchScore 품질이 흔들릴 수 있다.
- owner similarity는 데이터가 커지면 별도 캐시 또는 오프라인 계산이 필요할 수 있다.

## Progress
- [x] 계획 작성 완료
- [x] 사용자 검토 / 승인 완료
- [x] 구현 시작
- [x] 중간 검증 완료
- [x] 최종 검증 완료
- [x] 문서 반영 완료

## 결정 사항 / 변경 로그
- 2026-04-12: 이번 범위는 식당 추천이 아니라 리스트 추천만 구현한다.
- 2026-04-12: 추천 대상은 다른 사용자의 공개 리스트로 제한한다.
- 2026-04-12: hidden / deleted 데이터는 추천 입력과 후보에서 제외한다.
- 2026-04-12: 같은 지역 후보를 먼저 채우고, 20개 미만일 때만 fallback 지역을 사용한다.
- 2026-04-12: 유사 유저는 내부 보조 신호로만 사용하고 외부 API에서는 노출하지 않는다.
- 2026-04-12: feature 표현은 Map / record / DTO 기반으로 두고, 이후 벡터화 가능하도록 설계한다.
- 2026-04-12: `GET /recommendations/lists`, 서비스/스코어러/리포지토리/DTO/테스트 구현과 정책 문서 반영을 완료했다.

## 완료 조건
- 구현 전에 API / 점수식 / feature 구조 / query 구조 / 검증 기준이 문서로 고정된다.
- 범위와 비범위가 식당 추천과 명확히 분리되어 있다.
- same-region 우선 / fallback 보충 정책이 구현 가능한 수준으로 정의되어 있다.
- 추천 점수와 응답 DTO가 바로 코딩 가능한 수준으로 정리되어 있다.
