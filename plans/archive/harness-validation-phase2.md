# harness-validation-phase2.md

## Status
archived

## 작업명
하네스 엔지니어링 2단계: 공통 기반 검증 확장 및 정책 승격

## 목적
추천/랭킹 입력 데이터 보호 관점에서 공통 기반 검증을 더 강한 테스트 세트로 승격한다.

이번 작업은 문서 기준 정책을 테스트 코드로 끌어올리고, 이후 추천/랭킹/리스트 관련 작업 전반에서 재사용할 베이스라인 검증을 늘리는 것을 목표로 한다.

## 사용자 관점 결과
- `autoScore` 정책이 문서 기준 공식으로 정리된다.
- 공통 기반 검증 테스트가 늘어나서, 추천/랭킹 입력 데이터 품질이 깨지는 변경을 더 빨리 감지할 수 있다.
- 이후 기능 작업 시 무엇을 항상 돌리고 무엇을 작업별로 추가해야 하는지 더 분명해진다.

## 범위
- `validation-rules.md`의 `autoScore` 공식 기준 업데이트
- `ListRestaurantAutoScoreTest`의 공식 기준 수정
- 공통 기반 검증 테스트 후보 구체화
- 아래 요청 항목을 실제 테스트로 승격할 수 있는지 확인
  - 리스트 최소 5개 규칙
  - 동일 리스트 내 중복 식당 금지
  - 지역 불일치 식당 추가 차단
  - 노출 vs 계산 분리 규칙

## 비범위
- 추천 알고리즘 구현
- 배치 구현
- API 구조 재설계
- 승인 전 프로덕션 코드 변경

## 관련 문서/코드
- 문서
  - `AGENTS.md`
  - `PLANS.md`
  - `docs/logic/validation-rules.md`
  - `docs/logic/score-policy.md`
  - `docs/logic/visibility-policy.md`
  - `docs/current-gaps.md`
- 코드
  - `Capstone/src/main/java/com/example/Capstone/domain/ListRestaurant.java`
  - `Capstone/src/main/java/com/example/Capstone/domain/UserList.java`
  - `Capstone/src/main/java/com/example/Capstone/domain/Restaurant.java`
  - `Capstone/src/main/java/com/example/Capstone/service/UserListService.java`
  - `Capstone/src/main/java/com/example/Capstone/repository/ListRestaurantRepository.java`
  - `Capstone/src/main/java/com/example/Capstone/repository/UserListRepository.java`
  - `Capstone/src/main/java/com/example/Capstone/repository/RestaurantRepository.java`
  - `Capstone/src/test/java/com/example/Capstone/domain/ListRestaurantAutoScoreTest.java`

## 사전 확인 사항
- 현재 코드 기준 `autoScore` 공식은 `taste * 4.0 + value * 3.0 + mood * 3.0`이다.
- 사용자 요청 기준 공식은 `(taste * 0.6 + value * 0.2 + mood * 0.2) * 10`이다.
- 따라서 이번 작업은 테스트 보강만이 아니라, 문서 기준 정책과 현재 코드 간 차이를 의도적으로 메우는 작업이다.
- 현재 코드에는 아래 규칙이 서비스 레벨로 구현되어 있지 않다.
  - 리스트 최소 5개 규칙
  - 동일 리스트 내 중복 식당 금지
  - 지역 exact match 규칙
  - 공개/비공개와 계산 참여 분리 규칙
- 위 항목을 “실행 가능한 테스트”로 추가하면, 테스트만 추가하는 방식으로는 통과하지 않을 가능성이 높다.
- 추가 확인 필요:
  - 이번 승인 범위가 “실패하는 정책 테스트를 먼저 추가”인지, “프로덕션 코드까지 함께 맞춰서 통과 상태를 만든다”인지
  - 비공개 리스트의 계산 포함 정책을 실제로 어떤 계층에서 보장할지

## 구현 단계
1. 문서 기준 정책과 현재 코드의 차이를 확정한다.
2. `autoScore` 정책 변경이 테스트만으로 가능한지, 프로덕션 코드 변경이 필요한지 분리한다.
3. 공통 기반 검증을 단위 테스트로 둘지, 서비스 테스트로 둘지 결정한다.
4. 각 규칙별 테스트 방식과 필요한 의존성 범위를 정한다.
5. 승인 후 테스트 코드와 필요한 최소 프로덕션 수정 범위를 함께 반영한다.
6. 검증 결과를 `validation-rules.md`와 필요 시 `docs/current-gaps.md`에 반영한다.

## 검증 방법
- `ListRestaurantAutoScoreTest` 실행
- 추가되는 공통 기반 검증 테스트 클래스 실행
- 테스트로 충분하지 않은 항목은 수동 검증 포인트를 따로 남긴다.
- 승인 전에는 구현을 하지 않으므로 실제 테스트 실행은 계획 범위 확인까지만 본다.

## 리스크
- 문서 기준 정책이 현재 코드와 다르므로, 테스트만 먼저 추가하면 의도적으로 실패하는 상태가 될 수 있다.
- 리스트 최소 5개, 중복 금지, 지역 exact match는 현재 DTO/서비스 구조상 입력 모델 자체가 바뀔 수 있다.
- 공개/비공개와 계산 참여 분리는 현재 조회/추천 계층이 충분히 구현되어 있지 않아 테스트 경계가 애매할 수 있다.
- 이번 요청은 “하네스 테스트 추가”처럼 보이지만 실제로는 정책 확정과 프로덕션 반영 범위를 함께 결정해야 한다.

## Progress
- [x] 계획 작성 완료
- [x] 사용자 검토 / 승인 완료
- [x] 구현 시작
- [x] 중간 검증 완료
- [x] 최종 검증 완료
- [x] 문서 반영 완료

## 결정 사항 / 변경 로그
- 2026-04-12: 현재 요청은 복합 작업으로 판단하여 plan-first workflow에 따라 구현 전 계획 문서를 작성했다.
- 2026-04-12: 현재 코드에는 요청된 3개 공통 센서와 노출/계산 분리 규칙이 직접 구현되어 있지 않음을 확인했다.
- 2026-04-12: 구현 단계에서 `autoScore` 공식, 리스트 최소 5개, 중복 식당 금지, 지역 exact match를 최소 범위 서비스 검증으로 반영했다.
- 2026-04-12: `ListRestaurantAutoScoreTest`, `UserListServiceMinimumRestaurantCountTest`, `UserListServiceDuplicateRestaurantTest`, `UserListServiceRegionMatchTest` 실행을 확인했다.

## 완료 조건
- 사용자 승인 후 구현을 시작한다.
- `autoScore` 정책과 테스트가 같은 공식으로 정리된다.
- 공통 기반 검증 세트가 실행 가능한 테스트로 추가된다.
- 어떤 검증이 현재 정책 테스트이고, 어떤 검증이 아직 추가 구현 필요 상태인지 문서와 테스트에서 혼동되지 않는다.
