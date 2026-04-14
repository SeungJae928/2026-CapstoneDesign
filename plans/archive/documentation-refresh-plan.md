# 프로젝트 문서 최신화 계획

## Status
archived

## 목적
현재 코드 기준으로 프로젝트 문서를 전면 정리해, 처음 보는 개발자도 바로 구조를 이해하고 다음 작업을 이어갈 수 있는 상태를 만든다.

## 사용자 관찰 결과
추천 구현, 검증용 시드 데이터, 품질 튜닝, 테스트가 반영되면서 일부 허브 문서와 세부 문서 사이에 최신 상태 반영 수준 차이가 생겼다. 특히 추천 관련 문서는 구현 완료 상태와 남은 이슈를 더 명확히 연결할 필요가 있다.

## 범위
- 루트 허브 문서(`README.md`, `GUIDE.md`, `DB.md`, `LOGIC.md`) 정리
- 추천 구현/검증/튜닝/남은 이슈 문서 정리
- 문서 간 충돌 제거
- 다음 단계 작업 목록 명시

## 비범위
- 코드 로직 변경
- DB 스키마 변경
- 새 기능 구현

## 관련 문서/코드
- `AGENTS.md`
- `README.md`
- `GUIDE.md`
- `DB.md`
- `LOGIC.md`
- `docs/current-gaps.md`
- `docs/db/*`
- `docs/logic/*`
- `Capstone/src/main/java/com/example/Capstone/service/RestaurantRecommendationService.java`
- `Capstone/src/main/java/com/example/Capstone/service/RestaurantRecommendationScorer.java`
- `Capstone/src/test/resources/sql/recommendation_mock_seed.sql`

## 사전 확인 사항
- 현재 구현 완료 범위는 OAuth2/JWT, 리스트 CRUD, 랭킹 API, 식당 추천 API다.
- 추천 품질 검증용 시드 데이터가 dev DB 기준으로 설계 및 적재 가능하다.
- 추천 품질 튜닝은 zero-similarity 감점, fallback regionScore 하향, collaborative 감쇠까지 반영된 상태다.

## 구현 단계
1. 허브 문서와 세부 문서의 현재 상태를 점검한다.
2. 추천 관련 문서를 구현, 시드, 품질 검증, 남은 이슈 기준으로 재구성한다.
3. 온보딩에 필요한 README/GUIDE를 현재 코드 기준으로 업데이트한다.
4. 현재 남은 이슈와 다음 단계 작업을 `docs/current-gaps.md`에 정리한다.

## 검증 방법
- 허브 문서에서 세부 문서로 자연스럽게 이동 가능한지 확인한다.
- 추천 구현, 시드, 품질 튜닝, 테스트 현황이 문서 어디엔가 빠짐없이 존재하는지 확인한다.
- 더 이상 “현재 코드 기준”과 충돌하는 문구가 없는지 확인한다.

## 리스크
- 문서 간 역할이 겹치면 중복이 늘어날 수 있다.
- 현재 코드로 확정된 항목과 미정 정책을 섞어 쓰면 다시 혼선이 생길 수 있다.

## Progress
- [x] 계획 작성 완료
- [x] 사용자 검토 / 승인 완료
- [x] 구현 시작
- [x] 중간 검증 완료
- [x] 최종 검증 완료
- [x] 문서 반영 완료

## 결정 사항 / 변경 로그
- 2026-04-12: 추천 구현/검증/튜닝은 허브 문서 요약 + 세부 문서 분리 구조로 정리한다.
- 2026-04-12: 실제 API 검증 결과는 별도 품질 검증 문서로 분리한다.

## 완료 조건
- 루트 허브 문서가 현재 구현 상태를 정확히 설명한다.
- 추천 관련 문서가 구현, 데이터셋, 검증, 튜닝, 남은 이슈를 일관되게 다룬다.
- 다음 작업 후보가 문서에 명시되어 있다.
