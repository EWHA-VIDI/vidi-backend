# Setup and Lint

- 담당: 뼈대·설정 담당자

## 규칙

- Kotlin 코드는 ktlint 기본 규칙을 따른다.
- 정적 분석은 `config/detekt/detekt.yml`을 기준으로 실행한다.
- 커밋 전 `./scripts/verify.sh quick`을 실행한다.
- PR 전 `./scripts/verify.sh full`을 실행한다.
- 공통 설정은 `application.yml`, 환경별 설정은 `application-local.yml`과 `application-prod.yml`에 둔다.
- 비밀값은 환경변수로 받고 `.env.example`에는 변수명과 안전한 예시만 작성한다.
- 새 환경변수를 추가하면 `.env.example`, README, PR의 설정 항목을 함께 수정한다.
- 운영에서 Swagger를 비활성화한다.
- 날짜와 시간은 서버와 DB에서 UTC로 저장하고 ISO 8601 형식으로 응답한다.

## 하지 않는 것

- ktlint 또는 detekt 실패를 숨기기 위해 규칙을 임의로 끄지 않는다.
- 실제 비밀번호, JWT secret, API key를 설정 파일에 작성하지 않는다.
- `ddl-auto`를 `create`, `create-drop`, `update`로 변경하지 않는다.
- 이미 적용된 Flyway 마이그레이션 파일을 수정하지 않는다.
- 민감한 인증 헤더와 쿠키를 로그로 출력하지 않는다.

## 예시

- 마이그레이션: `V1__create_users.sql`
- 환경변수: `${DB_URL}`
- 전체 검증: `./scripts/verify.sh full`
