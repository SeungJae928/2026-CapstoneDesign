# seed-import.md

기준 날짜 및 시간: 2026-04-13 18:48:26 (Asia/Seoul)

## 1. 범위
이 문서는 식당 seed preview import의 현재 입력 방식과 동작 규칙을 다룬다.

대상 코드:
- `RestaurantSeedImportService`
- `SeedImportRunner` (`com.example.Capstone.runner`)
- `ImportRestaurantSeedRequest`

구조 메모:
- `RestaurantSeedImportService`는 import 오케스트레이션 서비스다.
- `SeedImportRunner`는 애플리케이션 기동 시 import를 연결하는 runner다.

## 2. 현재 구현된 기능
- 식당 preview JSON import
- 카테고리 preview JSON import
- 정규화 메뉴 preview JSON import
- 태그 preview JSON import
- 식당-태그 preview JSON import

## 3. 현재 코드 기준 동작
### 실행 조건
- `seed.import.enabled=true`일 때 애플리케이션 시작 시 import를 수행한다.
- `seed.import.exit-after-run=true`면 import 후 애플리케이션을 종료한다.

### 기본 경로
- `seed-data/restaurants-seed-preview.json`
- `seed-data/restaurant-categories-seed-preview.json`
- `seed-data/restaurant-menu-items-seed-preview.json`
- `seed-data/tags-seed-preview.json`
- `seed-data/restaurant-tags-seed-preview.json`

### import 방식
- 식당은 `pcmapPlaceId` 우선, 없으면 `name + address`로 기존 row를 찾는다.
- 식당은 create 또는 update한다.
- 태그는 `tagKey` 기준으로 create 또는 update한다.
- 카테고리, 메뉴, 식당-태그는 식당별 delete 후 replace 방식으로 다시 적재한다.

### 정규화 포인트
- `regionCountyName`이 있으면 city/district 대신 county 기준으로 region filter를 만든다.
- 음수 가격이나 비정상적으로 큰 가격은 `null` 처리한다.
- `parentTagKey`는 허용된 prefix가 아니면 `null` 처리한다.

## 4. 추가 확인 필요
- import를 API로 노출할지
- 운영/개발 환경별 seed import 사용 범위를 더 분리할지

## 5. 후속 수정 후보
- seed import 전용 운영 문서 보강
- import 결과 검증 스크립트 추가
