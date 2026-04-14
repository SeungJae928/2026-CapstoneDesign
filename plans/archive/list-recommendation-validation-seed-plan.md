# 리스트 추천 검증용 목업 시드 데이터셋 계획

## Status
completed

## 목적
리스트 추천 API를 실제 DB 기준으로 검증할 수 있도록, 현재 도메인 규칙을 지키는 최소 QA용 목업 시드 데이터셋과 검증 SQL을 추가한다.

## 사용자 관찰 결과
리스트 추천 기능 구현은 완료되었지만, 현재 개발 DB에는 same-region 우선, fallback, preferenceMatch, qualityScore, collaborativeScore를 함께 확인할 수 있는 데이터가 부족하다.

## 범위
- 리스트 추천 검증용 사용자/식당/카테고리/리스트/리스트-식당 시드 설계
- 실행 가능한 SQL 시드 스크립트 작성
- 검증용 조회 SQL 작성
- 시나리오와 기대 결과를 설명하는 문서 작성

## 비범위
- 리스트 추천 알고리즘 변경
- 기존 식당 추천 시드 교체
- 운영 DB 마이그레이션
- hide/delete 전 케이스를 모두 포괄하는 확장 QA 시드

## 관련 문서/코드
- `AGENTS.md`
- `DB.md`
- `LOGIC.md`
- `docs/logic/list-recommendation-policy.md`
- `docs/logic/validation-rules.md`
- `Capstone/src/main/java/com/example/Capstone/service/ListRecommendationService.java`
- `Capstone/src/main/java/com/example/Capstone/service/ListRecommendationScorer.java`
- `Capstone/src/main/java/com/example/Capstone/repository/ListRecommendationRepositoryImpl.java`

## 사전 확인 사항
- 리스트 추천 후보는 `public`, `visible`, `other user`, `visible restaurant 5개 이상` 조건을 만족해야 한다.
- 사용자 프로필은 내 전체 visible 리스트 기준으로 계산된다.
- dominant region은 지역별 `autoScore` 합계, row 수, 최신 `updatedAt` 순으로 결정된다.
- fallback은 same-region 후보가 20개 미만일 때만 허용된다.
- `autoScore`는 현재 공식 `ROUND(((taste * 0.6) + (value * 0.2) + (mood * 0.2)) * 10, 1)`을 그대로 사용해야 한다.

## 구현 단계
1. same-region 20개 충족, fallback 강제, owner overlap 검증이 모두 가능한 사용자/리스트 구성을 설계한다.
2. `users`, `restaurants`, `restaurant_categories`, `user_lists`, `list_restaurants` 순서의 실행 가능한 SQL 시드를 작성한다.
3. 공개 리스트 수, 리스트별 visible 식당 수, owner overlap, dominant region별 후보 수를 확인하는 검증 SQL을 작성한다.
4. 데이터셋 요약과 시나리오 기대 결과를 문서에 반영한다.

## 검증 방법
- 시드 SQL을 dev DB에 적재해 문법과 FK 정합성을 확인한다.
- 검증 SQL로 사용자 수, 식당 수, 지역 수, 공개/비공개 리스트 수, 리스트별 visible 식당 수, overlap 구조를 확인한다.
- 필요 시 `ListRestaurantAutoScoreTest`로 `autoScore` 공식이 현재 코드와 동일한지 다시 확인한다.

## 리스크
- same-region 후보 수가 부족하면 fallback 검증은 되더라도 20개 same-region-only 검증이 불가능하다.
- overlap이 너무 약하면 collaborativeScore가 항상 0으로 떨어질 수 있다.
- 카테고리 분포가 치우치면 zero-similarity 후보 감점 검증이 약해질 수 있다.

## Progress
- [x] 계획 작성 완료
- [x] 사용자 검토/승인 완료
- [x] 구현 시작
- [x] 중간 검증 완료
- [x] 최종 검증 완료
- [x] 문서 반영 완료

## 결정 사항 / 변경 로그
- 2026-04-12: 리스트 추천 검증용 시드는 기존 식당 추천 시드와 분리해 별도 SQL로 관리한다.
- 2026-04-12: 최소 구성은 사용자 9명, 식당 36개, 지역 3개, 리스트 41개, 공개 리스트 36개, 비공개 리스트 5개로 설계한다.
- 2026-04-12: 서울은 same-region 후보 20개 이상, 부산/대구는 fallback이 반드시 일어나는 구조로 설계한다.
- 2026-04-12: dev DB 검증 결과 `same-region = 26/3/4`, `fallback = 9/32/31`, `auto_score_mismatch = 0`, `region_mismatch = 0`을 확인했다.

## 완료 조건
- 리스트 추천 검증용 시드 SQL과 검증 SQL이 저장소에 추가되어 있다.
- same-region 우선, fallback, collaborative overlap, quality 차이를 설명하는 문서가 추가되어 있다.
- 허브 문서와 검증 문서에서 새 시드를 바로 찾을 수 있다.
