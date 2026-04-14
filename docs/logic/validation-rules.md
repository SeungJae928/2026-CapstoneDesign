# validation-rules.md

기준 날짜 및 시간: 2026-04-13 18:48:26 (Asia/Seoul)

## 1. 목적
이 문서는 현재 프로젝트 전반에서 반복 재사용하는 공통 기반 검증만 정리한다.

## 2. 공통 기반 검증
### 2-1. 점수 계산 일관성
확인 항목:
- `autoScore` 공식이 현재 코드와 일치하는지
- 생성/수정 흐름 모두에서 같은 공식이 적용되는지

관련 테스트:
- `ListRestaurantAutoScoreTest`

### 2-2. 리스트 최소 5개 규칙
확인 항목:
- 생성 시 5개 미만이면 실패하는지
- 제거 후 5개 미만이 되면 실패하는지

관련 테스트:
- `UserListServiceMinimumRestaurantCountTest`

### 2-3. 동일 리스트 내 동일 식당 중복 금지
확인 항목:
- 초기 생성 payload 중복 금지
- 추가 시 중복 금지

관련 테스트:
- `UserListServiceDuplicateRestaurantTest`

### 2-4. 리스트와 식당 지역 exact match
확인 항목:
- 리스트 지역과 식당 지역이 완전히 같을 때만 허용되는지

관련 테스트:
- `UserListServiceRegionMatchTest`

## 3. 사용 원칙
- 점수 계산, 리스트 입력, 추천/랭킹 입력 구조를 건드리면 공통 기반 검증부터 확인한다.
- 공통 기반 검증은 프로젝트 전반에서 반복 재사용되는 베이스라인 검증이다.
- 기능별 품질 검증이나 응답 시나리오는 해당 기능 문서 또는 archive 문서에서 다룬다.

## 4. 추가 확인 필요
- hide / delete 필터를 공통 기반 검증으로 승격할지
- 공개/비공개 해석 차이를 공통 검증으로 고정할지

## 5. 후속 수정 후보
- 공통 기반 검증 추가
- 실행 순서 체크리스트 분리

## 6. 최근 검증 메모
### 2026-04-14 패키지 구조 리팩터링 검증
- `.\gradlew.bat compileJava` 통과
- 아래 추천 관련 테스트 묶음 통과
  - `ListRecommendationScorerTest`
  - `RestaurantRecommendationScorerTest`
  - `ListRecommendationServiceTest`
  - `RestaurantRecommendationServiceTest`
  - `RecommendationControllerTest`
- `.\gradlew.bat test` 전체 실행은 로컬 PostgreSQL 연결 실패로 중단
  - `CapstoneApplicationTests`
  - `ListRecommendationRepositoryTest`
  - `RestaurantRankingRepositoryTest`
- 원인 메모:
  - Docker daemon 재기동 후 `postgres-dev` 컨테이너가 `pg_notify` 디렉터리 누락으로 바로 종료됨
  - 이번 패키지 이동 자체의 컴파일 오류와는 분리해서 본다
