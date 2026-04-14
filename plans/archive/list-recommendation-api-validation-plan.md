# 리스트 추천 API 응답 품질 검증 계획

## Status
completed

## 목적
이미 적재된 리스트 추천 mock seed를 기준으로 `GET /recommendations/lists` API의 실제 응답 품질을 검증한다.

## 사용자 관찰 결과
리스트 추천 시드와 검증 SQL은 이미 준비되어 있지만, 실제 API 응답 기준으로 same-region 우선, fallback, collaborative 영향이 기대대로 드러나는지 확인이 필요하다.

## 범위
- `userId = 1, 2, 3`에 대한 실제 API 호출
- 응답 JSON 수집
- 기본 shape 검증
- 시나리오 기반 품질 검증
- 이상 케이스 원인 후보 정리
- 검증 결과 문서화

## 비범위
- 추천 알고리즘 변경
- 시드 데이터 재설계
- 운영 환경 검증

## 관련 문서/코드
- `plans/list-recommendation-validation-seed-plan.md`
- `docs/logic/list-recommendation-validation-dataset.md`
- `docs/logic/list-recommendation-policy.md`
- `Capstone/src/main/java/com/example/Capstone/controller/RecommendationController.java`
- `Capstone/src/main/java/com/example/Capstone/service/ListRecommendationService.java`
- `Capstone/src/main/java/com/example/Capstone/service/ListRecommendationScorer.java`

## 사전 확인 사항
- `/recommendations/lists`는 인증이 필요하다.
- principal은 `Long userId`로 주입된다.
- 검증 판단은 SQL이 아니라 실제 API JSON 응답 기준으로 한다.

## 구현 단계
1. 서버 실행 상태와 인증 경로를 확인한다.
2. user 1, 2, 3용 실제 요청을 만들어 응답 JSON을 저장한다.
3. 결과 개수, 필수 필드, null 여부를 확인한다.
4. 사용자별 기대 시나리오와 응답 순위를 대조한다.
5. PASS/FAIL과 개선 포인트를 문서로 정리한다.

## 검증 방법
- localhost API를 실제 호출한다.
- 응답 원본 JSON을 보존한다.
- 기본 검증과 시나리오 검증을 분리해 기록한다.

## 리스크
- 인증 토큰 생성 경로가 막히면 실호출이 지연될 수 있다.
- same-region/fallback 평가는 점수와 후보 풀을 함께 봐야 해서 해석 누락 위험이 있다.

## Progress
- [x] 계획 작성 완료
- [x] 사용자 검토/승인 완료
- [x] 구현 시작
- [x] 중간 검증 완료
- [x] 최종 검증 완료
- [x] 문서 반영 완료

## 결정 사항 / 변경 로그
- 2026-04-12: 실검증은 localhost API 응답 JSON을 기준으로 수행한다.
- 2026-04-12: JWT access token을 로컬에서 생성해 user 1, 2, 3 요청을 실제 호출했다.
- 2026-04-12: user 1, 2, 3 시나리오 모두 PASS로 정리했다.

## 완료 조건
- user 1, 2, 3의 실제 응답 JSON이 수집되어 있다.
- PASS/FAIL과 이상 케이스가 문서로 정리되어 있다.
