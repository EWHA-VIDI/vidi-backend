# VIDI Backend

VIDI는 유튜브 댓글의 조직적 바이럴 의심 패턴을 근거와 함께 찾는 조사 지원 도구다.

## 명령어

- Gradle은 JDK 21로 실행하고 앱은 JDK 25, DB는 MySQL 8.4를 사용한다.
- README에 따라 환경변수를 주입하고 `docker compose up -d --wait`로 DB를 시작한다.
- `./gradlew bootRun`으로 실행한다.
- `./gradlew build test ktlintCheck detekt`로 검사한다.
- `./gradlew ktlintFormat`으로 Kotlin을 정리한다.

## 꼭 지킬 규칙

- 작업 전에 관련 규칙 파일을 읽고 기존 변경을 확인한다.
- Jira 이슈 하나당 브랜치 하나와 PR 하나를 사용한다.
- main과 develop에 직접 push하지 않는다.
- domain마다 controller, service, repository, entity, dto를 함께 둔다.
- 조회와 변경 서비스를 분리하고 도메인 간 조립에는 Facade를 사용한다.
- 다른 도메인의 Repository 대신 QueryService를 사용한다.
- Entity를 Controller 밖으로 노출하지 않고 응답 DTO를 사용한다.
- MySQL과 Flyway, ddl-auto=validate를 유지하고 비밀값을 커밋하지 않는다.
- 담당 번호의 규칙 파일만 수정하고 공통 규칙은 두 레포에 함께 PR을 올린다.
- 실행하지 않은 검사나 팀원 리뷰를 완료로 기록하지 않는다.

## 규칙 파일

- docs/rules/00-git-convention.md
- docs/rules/01-setup-lint.md
- docs/rules/10-api-contract.md
- docs/rules/13-transaction-logging.md
- docs/rules/20-package-structure.md
- docs/rules/23-testing.md
