# ranking-feature-plan.md

기준 날짜 및 시간: 2026-04-13 18:48:26 (Asia/Seoul)

## Status
planned

## 작업명
랭킹 기능 구현 직전 초안: API / 쿼리 / 서비스 구조 구체화

## 목적
이 문서는 기존 랭킹 계획을 바탕으로, 실제 구현에 바로 들어갈 수 있을 정도로 구체적인 랭킹 API/쿼리/서비스 구조를 정리한다.

이번 초안은 구현 전 문서이며, 아직 프로덕션 코드 작성은 포함하지 않는다.

## 사용자 관점 결과
- 전국/지역/카테고리 식당 랭킹 API를 어떤 구조로 만들지 바로 이해할 수 있다.
- 랭킹 계산에 어떤 데이터가 들어가고 어떤 데이터가 빠지는지 명확해진다.
- 백엔드 구현 시 필요한 서비스, 리포지토리, DTO, 집계 쿼리 초안이 준비된다.

## 범위
- 랭킹 계산 입력 데이터 재정의
- 보정 점수 계산식 확정
- 사용자-식당 최고 점수 1회 반영 규칙 정리
- 전국/지역/카테고리 랭킹용 쿼리 구조 초안
- 서비스 계층 구조 초안
- API 초안
- 1차 구현 범위 제안

## 비범위
- 실제 Controller / Service / Repository 코드 작성
- 추천 알고리즘
- 배치 스케줄러 구현
- 스냅샷 테이블 생성
- 카테고리 표준화 정책 최종 확정

## 관련 문서/코드
- 문서
  - `AGENTS.md`
  - `PLANS.md`
  - `DB.md`
  - `LOGIC.md`
  - `docs/db/lists.md`
  - `docs/db/restaurants.md`
  - `docs/logic/list-policy.md`
  - `docs/logic/score-policy.md`
  - `docs/logic/validation-rules.md`
  - `docs/logic/visibility-policy.md`
  - `docs/current-gaps.md`
- 코드
  - `Capstone/build.gradle`
  - `Capstone/src/main/java/com/example/Capstone/domain/User.java`
  - `Capstone/src/main/java/com/example/Capstone/domain/UserList.java`
  - `Capstone/src/main/java/com/example/Capstone/domain/ListRestaurant.java`
  - `Capstone/src/main/java/com/example/Capstone/domain/Restaurant.java`
  - `Capstone/src/main/java/com/example/Capstone/domain/RestaurantCategory.java`
  - `Capstone/src/main/java/com/example/Capstone/repository/UserRepository.java`
  - `Capstone/src/main/java/com/example/Capstone/repository/UserListRepository.java`
  - `Capstone/src/main/java/com/example/Capstone/repository/ListRestaurantRepository.java`
  - `Capstone/src/main/java/com/example/Capstone/repository/RestaurantRepository.java`
  - `Capstone/src/main/java/com/example/Capstone/repository/RestaurantCategoryRepository.java`
  - `Capstone/src/main/java/com/example/Capstone/service/UserListService.java`
  - `Capstone/src/main/java/com/example/Capstone/service/RestaurantService.java`
  - `Capstone/src/main/java/com/example/Capstone/controller/RestaurantController.java`

## 사전 확인 사항
- 하네스 기준으로 아래 입력 데이터 규칙은 이미 랭킹 전제 조건으로 본다.
  - 리스트 최소 식당 수 5개
  - 동일 리스트 내 동일 식당 중복 금지
  - 리스트 지역과 식당 지역 exact match
  - `autoScore` 계산 일관성
- 현재 `autoScore` 공식은 `(taste * 0.6 + value * 0.2 + mood * 0.2) * 10`이다.
- 랭킹 단위는 `식당`이다.
- 비공개 리스트는 랭킹 계산에 포함한다.
- 동일 사용자가 같은 식당을 여러 리스트에 담은 경우, 최고 `autoScore` 1회만 반영한다.
- 현재 `build.gradle`에는 QueryDSL 의존성과 설정이 없다.
- 현재 저장소는 Spring Data JPA + PostgreSQL 중심 구조다.

## 1. 랭킹 계산 입력 데이터 재정의

### 1-1. 기본 조인 기준
랭킹 계산의 기본 입력 집합은 아래 4개 테이블 조인으로 만든다.

```sql
list_restaurants lr
join user_lists ul on ul.id = lr.list_id
join users u on u.id = ul.user_id
join restaurants r on r.id = lr.restaurant_id
```

카테고리 필터가 있을 때만 `restaurant_categories`를 직접 조인하지 않고, `exists` 조건으로 사용한다.

이유:
- 카테고리를 직접 조인하면 식당당 다중 카테고리 때문에 집계 row가 중복될 수 있다.
- 랭킹 집계의 핵심은 식당 점수 row 수를 정확히 세는 것이므로, 카테고리는 필터 용도로만 붙이는 편이 안전하다.

### 1-2. 계산 필터 기준
랭킹 계산 대상은 아래 조건을 모두 만족하는 row만 포함한다.

- `ul.is_deleted = false`
- `ul.is_hidden = false`
- `u.is_deleted = false`
- `u.is_hidden = false`
- `r.is_deleted = false`
- `r.is_hidden = false`

### 1-3. `isDeleted`, `isHidden`, `isPublic` 해석
| 필드 | 계산 영향 | 정리 |
| --- | --- | --- |
| `UserList.isDeleted` | 제외 | soft delete 제외 |
| `UserList.isHidden` | 제외 | 운영 비노출 상태는 랭킹 계산에서도 제외 |
| `UserList.isPublic` | 영향 없음 | 비공개 여부는 랭킹 계산 제외 조건이 아니다 |
| `User.isDeleted` | 제외 | soft delete 제외 |
| `User.isHidden` | 제외 | 운영 비노출 상태는 랭킹 계산에서도 제외 |
| `Restaurant.isDeleted` | 제외 | soft delete 제외 |
| `Restaurant.isHidden` | 제외 | 일반 조회와 동일하게 제외 |

### 1-4. 지역 필터 기준
지역 랭킹은 `Restaurant.regionName` 기준으로 필터링한다.

```sql
and r.region_name = :regionName
```

이유:
- 랭킹 결과의 주체가 식당이므로, 지역 기준도 식당 엔티티 기준으로 두는 편이 자연스럽다.
- 현재 입력 검증에서 리스트 지역과 식당 지역 exact match가 이미 보장되므로, `UserList.regionName`과 충돌할 가능성이 낮다.

### 1-5. 동일 사용자 / 동일 식당 다중 기여 축약
동일 사용자가 같은 식당을 여러 리스트에 담은 경우, 최고 `autoScore` 1회만 반영한다.

중간 집계 단위:

```sql
group by u.id, r.id
max(lr.auto_score) as user_best_auto_score
```

즉 랭킹 계산에 들어가는 실질 입력 단위는 `ListRestaurant`가 아니라 아래의 축약 row다.

- `user_id`
- `restaurant_id`
- `user_best_auto_score`

이 축약 집합을 문서상 `user_restaurant_best`로 부른다.

### 1-6. 평가 수 정의
랭킹에서 식당별 평가 수는 `ListRestaurant` row 수가 아니라, 축약 이후의 사용자 수다.

즉:

```text
evaluationCount = COUNT(user_restaurant_best rows by restaurant)
```

의미:
- 한 사용자가 여러 리스트로 같은 식당에 여러 번 점수를 남겨도 평가 수는 1이다.

## 2. 보정 점수 계산식 확정안

### 2-1. 사용할 공식
1차 랭킹 점수는 베이지안 평균 형태의 보정 점수를 사용한다.

```text
adjustedScore = (v / (v + m)) * R + (m / (v + m)) * C
```

### 2-2. 값 정의
- `R`
  - 식당별 평균 점수
  - `AVG(user_best_auto_score)`
- `v`
  - 식당별 평가 수
  - `COUNT(user_restaurant_best rows)`
- `C`
  - 현재 조회 scope의 전역 평균
  - `AVG(user_best_auto_score)` across current filtered scope
- `m`
  - 보정 상수
  - 1차 제안값: `5`

### 2-3. 전역 평균 `C`의 범위
전역 평균 `C`는 “현재 요청 scope에서 필터를 적용한 뒤의 전역 평균”으로 잡는다.

예:
- 전국 랭킹: 전국 eligible pool 기준 평균
- 서울 랭킹: `regionName = 서울` 필터 후 평균
- 서울 + 한식 랭킹: `regionName = 서울`, `category = 한식` 필터 후 평균

이 해석을 쓰는 이유:
- 전국/지역/카테고리 랭킹 각각이 자기 scope 안에서 자연스럽게 보정된다.
- 특정 지역만 평균 수준이 다를 때, 전국 평균을 강제로 끌어오지 않아도 된다.

### 2-4. 보정 상수 `m = 5` 제안 이유
- 현재 프로젝트 전반에서 “최소 5개”가 데이터 품질 기준점으로 이미 반복 등장한다.
- 1차 MVP에서 보정 강도를 너무 크게 잡으면 신규 식당이 과하게 평균으로 수렴한다.
- `m = 5`는 “평가 수 5 전후부터 본 점수 영향이 눈에 띄게 커지는” 보수적이지만 과하지 않은 기본값이다.

### 2-5. 정렬과 반올림 기준
- 내부 정렬은 반올림 전 `adjustedScore` 원값으로 수행한다.
- 응답 노출용 `adjustedScore`는 소수점 둘째 자리까지 반올림한다.
- `averageAutoScore`도 소수점 둘째 자리까지 반올림한다.

### 2-6. 동점 처리 기준
1. `adjustedScore` 내림차순
2. `evaluationCount` 내림차순
3. `averageAutoScore` 내림차순
4. `restaurantId` 오름차순

### 2-7. 응답 rank 정의
1차 구현에서는 공동 순위를 두지 않고, 정렬 후 1부터 순차 부여한다.

즉:
- `rank`는 `ROW_NUMBER()` 의미 또는 서비스 조립 시 1-based index다.

## 3. 쿼리 구조 초안

### 3-1. 집계 단계 개요
랭킹 쿼리는 아래 4단계로 나눈다.

1. `eligible_entries`
   - 삭제/숨김/지역/카테고리 필터를 통과한 원본 점수 row 집합
2. `user_restaurant_best`
   - 동일 사용자-식당 조합에서 최고 `autoScore`만 남긴 축약 집합
3. `scope_stats`
   - 현재 scope 전역 평균 `C`
4. `restaurant_stats`
   - 식당별 `R`, `v`, `adjustedScore` 계산

### 3-2. 공통 CTE 구조 초안
```sql
with eligible_entries as (
    select
        u.id as user_id,
        r.id as restaurant_id,
        lr.auto_score as auto_score
    from list_restaurants lr
    join user_lists ul on ul.id = lr.list_id
    join users u on u.id = ul.user_id
    join restaurants r on r.id = lr.restaurant_id
    where ul.is_deleted = false
      and ul.is_hidden = false
      and u.is_deleted = false
      and u.is_hidden = false
      and r.is_deleted = false
      and r.is_hidden = false
      and (:regionName is null or r.region_name = :regionName)
      and (
            :category is null
            or exists (
                select 1
                from restaurant_categories rc
                where rc.restaurant_id = r.id
                  and rc.category_name = :category
            )
      )
),
user_restaurant_best as (
    select
        user_id,
        restaurant_id,
        max(auto_score) as user_best_auto_score
    from eligible_entries
    group by user_id, restaurant_id
),
scope_stats as (
    select
        avg(user_best_auto_score) as global_mean
    from user_restaurant_best
),
restaurant_stats as (
    select
        restaurant_id,
        avg(user_best_auto_score) as average_auto_score,
        count(*) as evaluation_count
    from user_restaurant_best
    group by restaurant_id
)
select
    rs.restaurant_id,
    r.name as restaurant_name,
    r.region_name,
    r.image_url,
    rs.average_auto_score,
    rs.evaluation_count,
    ss.global_mean,
    (
        (rs.evaluation_count::numeric / (rs.evaluation_count + :m))
        * rs.average_auto_score
        +
        (:m::numeric / (rs.evaluation_count + :m))
        * ss.global_mean
    ) as adjusted_score
from restaurant_stats rs
join scope_stats ss on 1 = 1
join restaurants r on r.id = rs.restaurant_id
order by
    adjusted_score desc,
    rs.evaluation_count desc,
    rs.average_auto_score desc,
    rs.restaurant_id asc
limit :limit
```

### 3-3. 사용자-식당 최고 점수 1회 반영 단계
핵심은 `user_restaurant_best`다.

```sql
select
    user_id,
    restaurant_id,
    max(auto_score) as user_best_auto_score
from eligible_entries
group by user_id, restaurant_id
```

이 단계가 없으면:
- 같은 사용자가 여러 리스트로 같은 식당에 중복 기여하게 된다.
- 평가 수와 평균이 동시에 부풀려질 수 있다.

### 3-4. 식당별 랭킹 점수 집계 단계
`restaurant_stats`에서는 축약 이후 데이터를 다시 식당 기준으로 묶는다.

```sql
select
    restaurant_id,
    avg(user_best_auto_score) as average_auto_score,
    count(*) as evaluation_count
from user_restaurant_best
group by restaurant_id
```

여기서 구한:
- `average_auto_score = R`
- `evaluation_count = v`

그리고 `scope_stats.global_mean = C`

를 합쳐 `adjusted_score`를 계산한다.

### 3-5. 전국 랭킹 쿼리 구조
전국 랭킹은 `regionName = null`로 호출한다.

```text
region filter 없음
category optional
```

즉 같은 공통 쿼리에서 지역 필터만 비활성화한다.

### 3-6. 지역 랭킹 쿼리 구조
지역 랭킹은 아래 조건만 추가된다.

```sql
and r.region_name = :regionName
```

다른 계산 로직은 전국과 동일하다.

### 3-7. 카테고리 필터 적용 방식
카테고리 필터는 `exists`로 처리한다.

```sql
and (
      :category is null
      or exists (
          select 1
          from restaurant_categories rc
          where rc.restaurant_id = r.id
            and rc.category_name = :category
      )
)
```

이 방식을 쓰는 이유:
- 다중 카테고리로 인한 집계 중복을 막을 수 있다.
- 메인 랭킹 쿼리를 식당 점수 집계에 집중시킬 수 있다.

### 3-8. 카테고리 응답 구성 방식
응답에 카테고리 목록이 필요하므로, 메인 랭킹 쿼리 후 상위 식당 ID 집합에 대해 별도 조회를 한 번 더 수행한다.

권장 보조 조회:

```sql
select
    rc.restaurant_id,
    rc.category_name
from restaurant_categories rc
where rc.restaurant_id in (:restaurantIds)
order by rc.restaurant_id asc, rc.category_name asc
```

이유:
- 메인 랭킹 쿼리에서 `string_agg`나 직접 조인을 넣지 않아도 된다.
- 랭킹 계산과 응답 표시용 보조 데이터 로딩이 분리된다.

### 3-9. JPA / QueryDSL / 네이티브 SQL 판단
#### JPA JPQL
- 이론상 가능하지만 권장하지 않는다.
- 이유:
  - 사용자-식당 축약
  - scope 전역 평균
  - 보정 점수 계산
  - optional filter
  - 정렬 + limit
  를 한 번에 표현하면 쿼리가 지나치게 복잡해진다.

#### QueryDSL
- 현재 프로젝트에는 QueryDSL 의존성과 설정이 없다.
- 지금 시점에 QueryDSL을 새로 넣는 것은 랭킹 기능 자체보다 인프라 변경 비용이 더 크다.

#### 네이티브 SQL
- 1차 구현에 가장 적합하다.
- 이유:
  - PostgreSQL 런타임이 이미 존재한다.
  - CTE 기반 집계 구조를 가장 자연스럽게 표현할 수 있다.
  - 보정 점수 계산과 정렬 기준을 DB에서 한 번에 처리할 수 있다.
  - 구현 직전 초안 기준으로 가장 읽기 쉽고 유지보수도 쉽다.

결론:
- 1차 구현은 `custom repository + native SQL`을 권장한다.

## 4. 서비스 계층 구조 초안

### 4-1. 권장 클래스 구성
- `RankingController`
- `RankingQueryService`
- `RankingPolicy`
- `RankingReadRepository`
- `RankingReadRepositoryImpl`
- `RankingResponseAssembler`
- `RankingSearchCondition`
- `RestaurantRankingRow`
- `RestaurantRankingResponse`
- `RestaurantRankingItemResponse`

### 4-2. 책임 분리
#### `RankingController`
- HTTP 요청 파라미터 바인딩
- `regionName`, `category`, `limit` 수신
- 서비스 호출
- 응답 반환

#### `RankingQueryService`
- 랭킹 조회 유스케이스의 진입점
- `RankingSearchCondition` 생성
- `RankingPolicy`로 limit / scope 정규화
- `RankingReadRepository` 호출
- 상위 식당 ID 추출 후 카테고리 보조 조회 호출
- `RankingResponseAssembler`로 응답 조립

#### `RankingPolicy`
- 보정 상수 `m`
- 기본 limit
- 최대 limit
- scope 판정 규칙
- 정렬/동점 처리 규칙 문서화

권장 상수:
- `DEFAULT_LIMIT = 20`
- `MAX_LIMIT = 50`
- `SMOOTHING_CONSTANT = 5`

#### `RankingReadRepository`
- 랭킹 집계 read model 전용 인터페이스
- 전국/지역/카테고리 필터를 포함한 랭킹 row 조회
- 상위 결과만 limit 적용해서 반환

예상 메서드:

```java
List<RestaurantRankingRow> findRestaurantRankings(RankingSearchCondition condition, int limit, int smoothingConstant);
Map<Long, List<String>> findCategoriesByRestaurantIds(List<Long> restaurantIds);
```

#### `RankingReadRepositoryImpl`
- 네이티브 SQL 조립
- named parameter 바인딩
- projection 매핑

#### `RankingResponseAssembler`
- `RestaurantRankingRow + categories`를 응답 DTO로 조립
- 순차 rank 부여
- `generatedAt`, `scope`, `regionName`, `category`, `limit` 세팅

### 4-3. 요청 조건 객체 초안
```java
public record RankingSearchCondition(
    String regionName,
    String category,
    Integer requestedLimit
) {}
```

정규화 후 내부 사용 예:

```java
public record NormalizedRankingCondition(
    String regionName,
    String category,
    int limit,
    RankingScope scope
) {}
```

### 4-4. 랭킹 row projection 초안
```java
public record RestaurantRankingRow(
    Long restaurantId,
    String restaurantName,
    String regionName,
    String imageUrl,
    BigDecimal averageAutoScore,
    Long evaluationCount,
    BigDecimal globalMean,
    BigDecimal adjustedScore
) {}
```

`globalMean`은 디버깅과 검증에는 유용하지만, 외부 API 응답에는 꼭 내보낼 필요는 없다.

## 5. API 초안

### 5-1. 권장 엔드포인트
```text
GET /rankings/restaurants
```

이유:
- 전국/지역/카테고리는 집계 로직은 같고 필터만 다르다.
- endpoint를 나누면 구현보다 문서와 라우팅 복잡도만 늘어난다.

### 5-2. 요청 파라미터
- `regionName`
  - 선택
  - 없으면 전국 랭킹
  - 있으면 지역 랭킹
- `category`
  - 선택
  - exact match 필터
- `limit`
  - 선택
  - 기본 `20`
  - 최대 `50`

### 5-3. 기본 limit / 최대 limit 제안
- 기본 `20`
  - 홈/탐색 화면 top-N 사용에 적합
- 최대 `50`
  - 1차는 랭킹 조회를 directory API가 아니라 top ranking API로 본다
  - 카테고리 보조 조회까지 포함해도 부담이 과하지 않다

### 5-4. 응답 필드 초안
```json
{
  "generatedAt": "2026-04-12T02:10:00",
  "scope": "REGION",
  "regionName": "서울",
  "category": "한식",
  "limit": 20,
  "items": [
    {
      "rank": 1,
      "restaurantId": 101,
      "restaurantName": "식당명",
      "regionName": "서울",
      "imageUrl": "https://...",
      "categories": ["한식", "국밥"],
      "adjustedScore": 87.42,
      "averageAutoScore": 91.50,
      "evaluationCount": 8
    }
  ]
}
```

### 5-5. 응답 DTO 초안
```java
public record RestaurantRankingResponse(
    LocalDateTime generatedAt,
    RankingScope scope,
    String regionName,
    String category,
    int limit,
    List<RestaurantRankingItemResponse> items
) {}

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
) {}
```

### 5-6. API 정책 메모
- `isPublic` 여부는 응답에 노출하지 않는다.
- 리스트 정보는 응답에 넣지 않는다.
- 랭킹 API는 식당 결과만 반환한다.

## 6. 1차 구현 범위 제안

### 6-1. 지금 바로 구현할 최소 범위
1차 구현에서 바로 들어갈 범위는 아래로 제안한다.

- `GET /rankings/restaurants`
- 전국 랭킹
- 지역 랭킹
- 카테고리 exact match 필터
- 비공개 리스트 포함
- 숨김/삭제 user/list/restaurant 제외
- 동일 사용자 동일 식당 최고 `autoScore` 1회 반영
- 보정 점수 기반 정렬
- 기본/최대 limit 적용

이 범위면 이후 추천과 분리된 상태로도 랭킹 기능을 독립 배포할 수 있다.

### 6-2. 후속 확장 범위
- category normalization 또는 alias 정책
- pagination 또는 cursor 기반 조회
- score policy version 노출
- 인기 급상승/최근 랭킹 분기
- region slug / enum 표준화
- 캐시 계층 추가

### 6-3. 배치 스냅샷으로 넘어가는 기준
아래 중 하나가 보이면 배치 또는 materialized read model 전환을 검토한다.

- 홈/탐색에서 같은 랭킹 조합이 매우 자주 반복 호출된다
- 실시간 집계 쿼리의 p95가 운영 기준을 지속적으로 넘는다
  - 1차 기준 예시: p95 300ms 초과
- `user_restaurant_best` 기준 대상 row가 수십만 단위 이상으로 커진다
- 특정 시점 기준 랭킹 재현이 운영 요구사항이 된다

### 6-4. 배치 전환 시 유지할 정책
배치로 가더라도 아래 정책은 그대로 유지한다.

- 비공개 리스트 계산 포함
- 숨김/삭제 데이터 계산 제외
- 동일 사용자-동일 식당 최고 점수 1회 반영
- 보정 점수 공식 동일 유지

## 검증 방법
- 구현 전/후 공통 기반 검증 우선 확인
  - `ListRestaurantAutoScoreTest`
  - `UserListServiceMinimumRestaurantCountTest`
  - `UserListServiceDuplicateRestaurantTest`
  - `UserListServiceRegionMatchTest`
- 랭킹 전용 검증 추가 초안
  - 비공개 리스트 포함 테스트
  - 숨김/삭제 제외 테스트
  - 동일 사용자 동일 식당 최고 점수 1회 반영 테스트
  - 보정 점수 계산 테스트
  - 전국/지역 필터 테스트
  - 카테고리 exact match 테스트
  - limit clamp 테스트
  - 동점 처리 테스트

## 리스크
- `m = 5`는 합리적인 1차 기본값이지만, 실제 데이터 분포를 본 뒤 조정 필요 가능성이 있다.
- 카테고리 문자열이 정규화되어 있지 않으면 exact match 필터 경험이 거칠 수 있다.
- native SQL은 1차 속도는 빠르지만, 쿼리 책임이 커지면 테스트와 문서 동기화가 중요해진다.
- 지역 문자열 표준화가 약하면 regionName 파라미터 품질이 API 품질에 직접 연결된다.

## Progress
- [x] 계획 작성 완료
- [ ] 사용자 검토 / 승인 완료
- [ ] 구현 시작
- [ ] 중간 검증 완료
- [ ] 최종 검증 완료
- [x] 문서 반영 완료

## 결정 사항 / 변경 로그
- 2026-04-12: 비공개 리스트는 랭킹 계산에 포함하는 것으로 확정했다.
- 2026-04-12: 동일 사용자-동일 식당 다중 기여는 최고 `autoScore` 1회만 반영하는 것으로 확정했다.
- 2026-04-12: 1차 랭킹 점수는 단순 평균이 아니라 보정 점수를 사용하도록 확정했다.
- 2026-04-12: 현재 기술 스택과 의존성 기준으로 QueryDSL 추가보다 native SQL 기반 read repository가 적합하다고 정리했다.

## 완료 조건
- 구현에 필요한 쿼리 단계와 필터 기준이 문서로 정리된다.
- 보정 점수 공식과 필요한 변수 정의가 확정된다.
- API 요청/응답 구조가 DTO 수준으로 초안화된다.
- 서비스 계층 책임 분리가 구현 가능한 수준으로 정리된다.
- 1차 구현 범위와 배치 전환 기준이 명확해진다.
