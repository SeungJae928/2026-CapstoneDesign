# 추천 API 실데이터 검증 계획

## Status
archived

## 목적
dev DB에 적재된 추천 검증용 시드 데이터를 기준으로 실제 추천 API를 호출하고, 사용자별 결과와 점수 분해를 분석한다.

## 사용자 관찰 결과
추천 검증용 시드 데이터가 dev DB에 적재되어 있어 민준, 서연, 지훈 기준 추천 응답을 실제로 확인할 수 있다.

## 범위
- 추천 API 실행 경로와 인증 방식 확인
- 민준, 서연, 지훈 3개 사용자 케이스 API 호출
- 추천 결과 4개와 점수 분해 수집
- same-region, fallback, collaborative 정책 검증
- 품질 평가와 문제점 정리

## 비범위
- 추천 알고리즘 수정
- 가중치 조정 적용
- 추가 시드 데이터 설계

## 관련 문서/코드
- `AGENTS.md`
- `LOGIC.md`
- `docs/logic/recommendation-policy.md`
- `docs/logic/recommendation-validation-dataset.md`
- `Capstone/src/main/java/com/example/Capstone/controller/RecommendationController.java`
- `Capstone/src/main/java/com/example/Capstone/service/RestaurantRecommendationService.java`
- `Capstone/src/main/java/com/example/Capstone/service/RestaurantRecommendationScorer.java`
- `Capstone/src/main/java/com/example/Capstone/common/jwt/JwtProvider.java`
- `Capstone/src/main/java/com/example/Capstone/common/jwt/JwtFilter.java`

## 사전 확인 사항
- 추천 API는 `GET /recommendations/restaurants` 이다.
- 추천 API는 JWT access token 인증이 필요하다.
- principal 은 `Long userId` 형태로 주입된다.
- dev DB에는 추천 검증용 시드가 이미 적재되어 있다.

## 구현 단계
1. 인증 헤더 생성 방식과 서버 실행 경로를 확인한다.
2. 로컬 서버를 실행한다.
3. 민준, 서연, 지훈 토큰으로 추천 API를 호출한다.
4. 응답 본문에서 식당 4개와 점수 분해 값을 추출한다.
5. 사용자별 결과 해석과 정책 검증을 정리한다.

## 검증 방법
- 응답이 200 OK 인지 확인한다.
- 각 사용자별로 최대 4개 추천이 반환되는지 확인한다.
- 민준은 same-region 만으로 4개가 채워지는지 확인한다.
- 서연은 fallback 이 실제로 발생하는지 확인한다.
- 지훈은 collaborativeScore 가 0보다 큰 후보가 존재하는지 확인한다.

## 리스크
- 로컬 앱 실행 중 포트 충돌이 있을 수 있다.
- JWT 토큰 생성 방식이 필터 기대값과 다르면 인증이 실패할 수 있다.
- 응답 순위는 rankingAdjustment 와 collaborative 가 동시에 작용해 예상과 일부 다를 수 있다.

## Progress
- [x] 계획 작성 완료
- [x] 사용자 검토 / 승인 완료
- [x] 구현 시작
- [x] 중간 검증 완료
- [x] 최종 검증 완료
- [x] 문서 반영 완료

## 결정 사항 / 변경 로그
- 2026-04-12: 실제 HTTP 호출 기반 검증을 우선하고, 코드 수정 없이 현재 상태만 평가한다.
- 2026-04-12: JWT access token 을 로컬에서 생성해 민준(1), 서연(2), 지훈(3) 사용자로 실제 API 요청을 재현했다.

## 완료 조건
- 민준, 서연, 지훈 각각의 실제 API 응답과 점수 분해가 수집되어 있다.
- 사용자별 품질 평가와 정책 검증 결과가 정리되어 있다.
