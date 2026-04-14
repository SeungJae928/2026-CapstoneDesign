# restaurants.md

기준 날짜 및 시간: 2026-04-13 18:48:26 (Asia/Seoul)

## 1. 범위
이 문서는 `Restaurant`, `RestaurantCategory`, `RestaurantMenuItem`, `Tag`, `RestaurantTag` 저장 구조를 다룬다.

대상 파일:
- `Capstone/src/main/java/com/example/Capstone/domain/Restaurant.java`
- `Capstone/src/main/java/com/example/Capstone/domain/RestaurantCategory.java`
- `Capstone/src/main/java/com/example/Capstone/domain/RestaurantMenuItem.java`
- `Capstone/src/main/java/com/example/Capstone/domain/Tag.java`
- `Capstone/src/main/java/com/example/Capstone/domain/RestaurantTag.java`

## 2. 현재 코드 기준
### 2-1. `restaurants`
주요 컬럼:
- `id`
- `name`
- `address`
- `region_name`
- `region_city_name`
- `region_district_name`
- `region_county_name`
- `region_filter_names`
- `lat`
- `lng`
- `image_url`
- `pcmap_place_id`
- `menu_json`
- `menu_updated_at`
- `is_hidden`
- `is_deleted`
- `created_at`
- `updated_at`
- `deleted_at`

### 2-2. `restaurant_categories`
주요 컬럼:
- `id`
- `restaurant_id`
- `category_name`
- `created_at`
- `updated_at`

### 2-3. `restaurant_menu_items`
주요 컬럼:
- `id`
- `restaurant_id`
- `source_menu_id`
- `display_order`
- `menu_name`
- `normalized_menu_name`
- `menu_tag_key`
- `price_text`
- `price_value`
- `description`
- `created_at`
- `updated_at`

### 2-4. `tags`
주요 컬럼:
- `id`
- `tag_key`
- `tag_name`
- `tag_type`
- `parent_tag_key`
- `is_active`
- `created_at`
- `updated_at`

### 2-5. `restaurant_tags`
주요 컬럼:
- `id`
- `restaurant_id`
- `tag_id`
- `source_type`
- `source_text`
- `weight`
- `confidence`
- `matched_menu_count`
- `is_primary`
- `created_at`
- `updated_at`

## 3. 저장 구조 관점에서 읽어야 할 점
- `pcmapPlaceId`는 unique 제약이 있다.
- `menu_json`, `region_filter_names`는 JSON 기반 메타데이터다.
- 카테고리는 문자열 exact match 구조이며 별도 마스터/alias 테이블은 없다.
- `restaurant_id + tag_id + source_type` 조합은 unique다.

## 4. 추천 / 랭킹에서 직접 쓰이는 부분
- `restaurants.region_name`
- `restaurants.is_hidden`, `restaurants.is_deleted`
- `restaurant_categories.category_name`

현재 코드 기준으로 `restaurant_menu_items`, `tags`, `restaurant_tags`, `menu_json`은 추천/랭킹 점수 계산에 직접 쓰이지 않는다.

## 5. seed import와의 관계
- seed import는 식당, 카테고리, 메뉴, 태그, 식당-태그 데이터를 replace 방식으로 적재한다.
- 운영 규칙은 `docs/logic/seed-import.md`에서 다룬다.

## 6. 추가 확인 필요
- 메뉴 / 태그를 일반 API 응답으로 노출할지
- 카테고리 alias 정규화를 둘지
- 식당 식별자 강화가 필요한지

## 7. 후속 수정 후보
- 추천 입력에 메뉴 / 태그 활용 검토
- 카테고리 정규화 정책 별도 문서화
