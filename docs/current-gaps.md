# current-gaps.md

기준 날짜 및 시간: 2026-04-13 18:48:26 (Asia/Seoul)

## 1. 목적
이 문서는 아직 확정되지 않았거나, 코드와 문서가 어긋나는 정책 항목만 남긴다.

확정된 구조와 정책은 `DB.md`, `LOGIC.md`, `docs/db/*`, `docs/logic/*`에 기록한다.

## 2. 미확정 정책
### 2-1. 리스트 상세 조회 공개 범위
- `GET /lists/{id}`는 현재 `isPublic` 또는 owner 여부를 검증하지 않는다.
- 외부 노출 정책을 API 계약으로 더 명확히 정리할 필요가 있다.

### 2-2. 점수 수정 API path variable 의미
- 경로는 `/lists/{id}/restaurants/{restaurantId}` 형태지만 실제 수정 대상 조회는 `list_restaurants.id` 기준이다.
- 외부 계약과 구현 의미가 다르다.

### 2-3. refresh token 재발급 시 role 복원 경로
- refresh token 자체에는 role claim이 없다.
- `AuthService.refresh()`는 `jwtProvider.getRole(refreshToken)`에 의존한다.

### 2-4. 대표 리스트와 공개 상태 관계
- 대표 리스트를 비공개로 바꾸지 못하게 막는 로직은 있다.
- 대표 리스트 삭제 후 후속 승격, 사용자당 대표 1개 보장의 운영 정책은 더 명확히 정리할 여지가 있다.

### 2-5. cold start 추천
- 식당 추천과 리스트 추천 모두 사용자 상호작용이 없으면 빈 결과를 반환한다.
- 기본 추천 정책은 아직 확정되지 않았다.

### 2-6. follow와 hidden 사용자 관계
- follow 생성은 `isDeleted = false`만 확인한다.
- hidden 사용자를 follow 대상 / 응답에서 어떻게 다룰지는 정리가 필요하다.

### 2-7. placeholder OAuth 엔드포인트
- 실제 로그인 진입점은 `/oauth2/authorization/{provider}`다.
- `/auth/oauth/{provider}`는 현재 `200 OK` placeholder 성격이다.

### 2-8. `PcmapSearchClient` 사용처 부재
- `PcmapSearchClient`와 `PcmapSearchClientImpl`는 현재 `client/` 패키지로 분리되어 있다.
- 하지만 현재 코드 기준으로 실제 주입 소비처는 없다.
- 외부 검색 fallback 기능을 연결할지, 미사용 어댑터로 유지할지 결정이 필요하다.

## 3. 현재 gap에서 제외한 항목
- 리스트 최소 5개 규칙
- 동일 리스트 내 동일 식당 중복 금지
- 리스트와 식당 지역 exact match
- 식당 추천 API 존재
- 리스트 추천 API 존재
- recommendation scorer / model 패키지 분리
- seed import runner 패키지 분리
