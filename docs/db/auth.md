# auth.md

기준 날짜 및 시간: 2026-04-13 18:48:26 (Asia/Seoul)

## 1. 범위
이 문서는 인증 관련 저장 구조, 특히 `RefreshToken` 엔티티를 다룬다.

대상 클래스:
- `Capstone/src/main/java/com/example/Capstone/domain/RefreshToken.java`
- `Capstone/src/main/java/com/example/Capstone/repository/RefreshTokenRepository.java`

## 2. 현재 코드 기준
### 테이블
- `refresh_tokens`

### 주요 컬럼
- `id`
- `user_id`
- `token`
- `expires_at`
- `created_at`

### 현재 코드 해석
- JPA 관계로 `User`를 참조하지 않고 `userId` 숫자값만 저장한다.
- 토큰 문자열과 만료 시각을 함께 저장한다.
- `findByUserId()`를 사용해 사용자당 1개 refresh token처럼 다루지만, 엔티티에는 DB unique 제약이 없다.

## 3. 저장 구조 관점에서 읽어야 할 점
- 로그인 성공 시 사용자 기준으로 저장 또는 갱신한다.
- 만료 판단은 `expiresAt`과 `isExpired()`를 사용한다.
- role claim 저장 구조는 refresh token 테이블에 없다.

## 4. 추가 확인 필요
- `refresh_tokens.user_id`를 DB unique로 강제할지
- 사용자 삭제 시 refresh token 정리 정책
- refresh 재발급 시 role 복원 경로 정리

## 5. 후속 수정 후보
- 인증 저장 구조가 커지면 분리 문서화
