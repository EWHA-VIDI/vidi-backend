# VIDI Backend 작업 지침

VIDI는 유튜브 댓글의 계정·시간·문장 관계를 분석해 조직적 바이럴 의심 패턴을 탐지하는 조사 지원 서비스다.

## 작업 시작

- 변경 전에 `git status --short --branch`로 브랜치와 기존 변경을 확인한다.
- 작업 전에 변경 범위와 관련된 `docs/rules` 파일을 읽는다.
- 기존 변경과 관련 없는 파일을 수정하거나 되돌리지 않는다.
- 실행하지 않은 검사와 테스트를 성공했다고 기록하지 않는다.

## 명령어

- 로컬 DB 실행: `docker compose up -d`
- 애플리케이션 실행: `./gradlew bootRun`
- 테스트: `./gradlew test`
- 포맷 검사: `./gradlew ktlintCheck`
- 정적 분석: `./gradlew detekt`
- 전체 빌드: `./gradlew build`
- 빠른 검증: `./scripts/verify.sh quick`
- 전체 검증: `./scripts/verify.sh full`

## 꼭 지킬 규칙

- 도메인 폴더 안에 controller, service, repository, entity, dto를 함께 둔다.
- 조회 Service와 변경 Service를 각각 `XxxQueryService`, `XxxCommandService`로 분리한다.
- 다른 도메인의 Repository를 직접 사용하지 않고 해당 도메인의 QueryService를 사용한다.
- 여러 도메인 Service를 조합하는 기능은 Facade에 둔다.
- Entity를 Controller 응답으로 반환하지 않고 Response DTO로 변환한다.
- JPA Entity에 `data class`와 공개 setter를 사용하지 않는다.
- 스키마 변경은 Flyway 마이그레이션으로만 수행한다.
- 비밀번호, 토큰, 키, 인증 자료를 코드와 로그에 남기지 않는다.
- 공통 설정 파일을 변경하면 영향과 새 환경변수를 PR에 기록한다.

## 규칙 파일

- `docs/rules/00-git-convention.md`: 브랜치, 커밋, PR 규칙
- `docs/rules/01-setup-lint.md`: 설정, ktlint, detekt 규칙
- `docs/rules/10-owner.md`: 담당자 2 규칙
- `docs/rules/20-owner.md`: 담당자 3 규칙
- `docs/api-contract.md`: 공통 에러와 페이지네이션 응답 계약
