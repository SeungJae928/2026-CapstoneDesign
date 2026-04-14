# follow-policy.md

기준 날짜 및 시간: 2026-04-13 18:48:26 (Asia/Seoul)

## 1. 범위
이 문서는 follow / unfollow / follower / following 기능의 현재 동작 규칙을 다룬다.

대상 코드:
- `FollowController`
- `FollowService`
- `UserFollowRepository`

## 2. 현재 구현된 기능
- 팔로우
- 언팔로우
- 팔로워 목록 조회
- 팔로잉 목록 조회
- 팔로워 / 팔로잉 수 조회
- 팔로우 여부 확인

## 3. 현재 코드 기준 규칙
- 자기 자신은 팔로우할 수 없다.
- 이미 팔로우 중인 대상은 다시 팔로우할 수 없다.
- 언팔로우는 현재 팔로우 중인 관계에 대해서만 가능하다.
- 팔로우 생성 시 follower / following 모두 `isDeleted = false`인 사용자만 허용한다.
- hidden 사용자에 대한 별도 차단은 현재 없다.

## 4. 조회 동작
- 팔로워 목록은 `following_id = userId`인 row의 follower를 반환한다.
- 팔로잉 목록은 `follower_id = userId`인 row의 following을 반환한다.
- count는 `user_follows` 집계 기준이다.
- 응답 정렬이나 paging은 현재 없다.

## 5. 추가 확인 필요
- hidden 사용자를 목록과 카운트에서 제외할지
- soft delete 사용자와 기존 follow 관계 정리 정책

## 6. 후속 수정 후보
- 팔로워 / 팔로잉 조회에 paging 추가
- hidden 사용자 처리 정책 확정
