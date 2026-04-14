# follow.md

기준 날짜 및 시간: 2026-04-13 18:48:26 (Asia/Seoul)

## 1. 범위
이 문서는 `UserFollow` 저장 구조를 다룬다.

대상 클래스:
- `Capstone/src/main/java/com/example/Capstone/domain/UserFollow.java`
- `Capstone/src/main/java/com/example/Capstone/repository/UserFollowRepository.java`

## 2. 현재 코드 기준
### 테이블
- `user_follows`

### 주요 컬럼
- `id`
- `follower_id`
- `following_id`
- `created_at`

### 현재 코드 해석
- 한 행은 `follower -> following` 단방향 팔로우 관계다.
- `follower_id + following_id` 조합에는 DB unique 제약이 있다.
- follower와 following 모두 `users.id`를 참조한다.

## 3. 저장 구조 관점에서 읽어야 할 점
- 자기 자신 팔로우 금지는 DB가 아니라 서비스 로직에서 막는다.
- follow 목록 / count는 `user_follows` 테이블 기준으로 계산된다.
- hidden 사용자 필터는 현재 follow 저장 구조 자체에는 반영되지 않는다.

## 4. 추가 확인 필요
- hidden 사용자와 follow 응답 관계
- 삭제된 사용자 row 정리 정책

## 5. 후속 수정 후보
- follow 응답용 노출 정책과 저장 구조 문서 연결 강화
