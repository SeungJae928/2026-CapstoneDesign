# 식당 추천 기능 구현 계획

## Status
archived

## 목적
현재 DB 구조를 기반으로 실시간 개인화 식당 추천 API를 구현한다.

## 사용자 관점 결과
로그인 사용자는 자신의 전체 리스트 취향을 반영한 식당 추천 4개를 받을 수 있다.

## 범위
- `GET /recommendations/restaurants` API 추가
- 실시간 추천 계산 로직 추가
- 추천 점수 상세 응답 추가
- 추천용 repository / service / scorer / dto / test 추가
- 관련 문서 반영

## 비범위
- 리스트 추천
- 유사 유저 노출
- 벡터 DB / 임베딩 기반 추천
- 추천 배치 / 캐시 / 스냅샷

## 관련 문서/코드
- `GUIDE.md`
- `AGENTS.md`
- `DB.md`
- `LOGIC.md`
- `docs/logic/ranking-policy.md`
- `docs/logic/validation-rules.md`
- `docs/current-gaps.md`
- `Capstone/src/main/java/com/example/Capstone/service/RankingService.java`
- `Capstone/src/main/java/com/example/Capstone/repository/RestaurantRankingRepositoryImpl.java`

## 사전 확인 사항
- 추천 기준 데이터는 사용자 전체 리스트를 사용한다.
- 후보는 hidden / deleted 제외, 이미 내 리스트에 포함된 식당 제외다.
- 동일 지역 후보를 우선 계산하고 4개 미만일 때만 fallback 지역을 허용한다.
- fallback 식당은 동일 점수식으로 계산하되 `regionScore = 0.7`을 적용한다.
- collaborative score는 optional이며 없으면 0 처리한다.

## 구현 단계
1. 추천 API 경계 정의
2. 추천 repository row / query 구현
3. 사용자 프로필 / 후보 feature / scorer 구현
4. service orchestration 구현
5. controller / response dto 구현
6. service / scorer / controller 테스트 추가
7. 추천 정책 문서 반영

## 검증 방법
- `RestaurantRecommendationScorerTest`
- `RestaurantRecommendationServiceTest`
- `RecommendationControllerTest`
- 기존 기반 테스트
  - `ListRestaurantAutoScoreTest`
  - `UserListServiceMinimumRestaurantCountTest`
  - `UserListServiceDuplicateRestaurantTest`
  - `UserListServiceRegionMatchTest`

## 리스크
- 추천용 native SQL이 랭킹 SQL과 유사해 중복이 생길 수 있다.
- collaborative neighbor 조회는 데이터 증가 시 추가 최적화가 필요할 수 있다.
- cold start 사용자의 정책은 구현 결과 기준으로 문서화가 필요하다.

## Progress
- [x] 계획 작성 완료
- [x] 사용자 검토 / 승인 완료
- [x] 구현 시작
- [x] 중간 검증 완료
- [x] 최종 검증 완료
- [x] 문서 반영 완료

## 결정 사항 / 변경 로그
- 2026-04-12: 이번 범위는 식당 추천만 구현하고 추천 점수 상세를 응답에 포함한다.
- 2026-04-12: 사용자 상호작용 데이터가 없으면 빈 추천 결과를 반환하도록 구현했다.

## 완료 조건
- 추천 API가 상위 4개 식당을 반환한다.
- 점수식이 고정된 가중치대로 구현되어 있다.
- service / scorer / controller 테스트가 추가되어 있다.
- 관련 문서가 추천 정책을 반영한다.
