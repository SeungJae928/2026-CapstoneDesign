# 추천 검증용 목업 시드 데이터셋 계획

## Status
archived

## 목적
실제 추천 API를 검증할 수 있도록 도메인 규칙을 지키는 최소 목업 데이터셋과 검증 쿼리를 추가한다.

## 사용자 관찰 결과
개발 DB에 추천 입력 데이터가 없어 추천 품질 검증이 불가능한 상태를 해소한다. 시드 적용 후에는 추천 API가 실제로 상위 4개 식당을 반환하고, `userPreferenceScore`, `categoryFitScore`, `rankingAdjustmentScore`, `collaborativeScore`, `region fallback`을 모두 확인할 수 있다.

## 범위
- 추천 검증용 사용자, 식당, 카테고리, 리스트, 리스트-식당 관계 시드 설계
- 실행 가능한 SQL 시드 스크립트 작성
- 검증용 SQL 조회 쿼리 작성
- 시나리오 설명 문서 작성

## 비범위
- 추천 알고리즘 자체 변경
- 운영 DB 마이그레이션
- 숨김/삭제 데이터 검증용 확장 시드

## 관련 문서/코드
- `AGENTS.md`
- `DB.md`
- `LOGIC.md`
- `docs/logic/score-policy.md`
- `docs/logic/recommendation-policy.md`
- `docs/logic/validation-rules.md`
- `Capstone/src/main/java/com/example/Capstone/domain/ListRestaurant.java`
- `Capstone/src/main/java/com/example/Capstone/service/RestaurantRecommendationService.java`

## 사전 확인 사항
- `autoScore` 공식은 `(taste * 0.6 + value * 0.2 + mood * 0.2) * 10` 이다.
- 리스트는 최소 5개 식당을 가져야 한다.
- 리스트와 식당 지역은 exact match 여야 한다.
- 동일 리스트 내 동일 식당은 중복될 수 없다.
- 추천 입력에는 `isHidden = false`, `isDeleted = false` 인 정상 데이터만 사용한다.

## 구현 단계
1. 추천 시나리오와 overlap 구조를 먼저 설계한다.
2. 시나리오가 드러나는 사용자/식당/카테고리/리스트 구성을 SQL로 옮긴다.
3. `list_restaurants` 는 `autoScore` 를 SQL 계산식으로 생성해 일관성을 보장한다.
4. 검증용 쿼리와 문서를 추가해 바로 확인 가능한 상태로 만든다.

## 검증 방법
- SQL 시드 적용 후 사용자 수, 식당 수, 지역 수, 카테고리 수, 리스트 수, 리스트-식당 수를 확인한다.
- 리스트별 식당 수가 모두 5 이상인지 확인한다.
- 유저 간 공통 식당 수가 collaborative 검증에 충분한지 확인한다.
- 추천 입력 eligible rows 가 비어 있지 않은지 확인한다.

## 리스크
- 시드가 지나치게 단순하면 ranking/collaborative 영향이 약해질 수 있다.
- 시드가 과도하게 복잡하면 추천 결과 해석이 어려워질 수 있다.
- dev DB 초기화 없이 반복 삽입하면 중복이 생길 수 있다.

## Progress
- [x] 계획 작성 완료
- [x] 사용자 검토 / 승인 완료
- [x] 구현 시작
- [x] 중간 검증 완료
- [x] 최종 검증 완료
- [x] 문서 반영 완료

## 결정 사항 / 변경 로그
- 2026-04-12: 지역은 `서울`, `부산`, `대구` 3개로 고정했다.
- 2026-04-12: 사용자 7명, 식당 27개, 리스트 9개, 리스트-식당 46개를 최소 검증 세트로 확정했다.
- 2026-04-12: `list_restaurants.autoScore` 는 insert value 하드코딩 대신 SQL 계산식으로 생성하도록 결정했다.

## 완료 조건
- 시드 SQL과 검증 SQL이 저장소에 추가되어 있다.
- 시나리오 설명 문서가 있고 허브 문서에서 접근 가능하다.
- 추천 검증에 필요한 핵심 신호가 데이터셋에 포함되어 있다.
