# VIDI Backend

광고 표시가 없는 유튜브 후기 댓글의 조직적 바이럴 의심 패턴을 근거와 함께 조사하는 도구다.
점수는 패턴의 강도이며 최종 판단은 사람이 한다.

## 로컬 실행

JDK 21과 Docker Compose v2를 사용한다. MySQL 8.4를 사용하며 H2로 대체하지 않는다.

```bash
cp .env.example .env
# .env의 DB_PASSWORD에 로컬 개발용 비밀번호를 입력한다.
docker compose up -d --wait
./gradlew build
./gradlew bootRun
```

PowerShell에서는 `Copy-Item .env.example .env`와 `./gradlew.bat`을 사용한다.
local 프로필은 작업 디렉터리의 `.env`를 Java properties 형식으로 자동으로 읽는다.
값은 `KEY=value`로 작성하고 따옴표로 감싸거나 `export`를 붙이지 않는다.
Compose와 properties에서 해석이 달라지는 `$`, 역슬래시 등은 로컬 비밀번호에서 피한다.
이미 주입한 운영체제 환경변수가 있으면 `.env`보다 우선한다.
MySQL root 비밀번호는 최초 초기화 때 자동 생성하며 앱은 DB_USERNAME 계정만 사용한다.
prod 프로필은 `.env`를 읽지 않고 배포 환경에서 주입한 값을 사용한다.
기본 프로필은 `local`이고 운영에서는 `SPRING_PROFILES_ACTIVE=prod`를 명시한다.
로컬 MySQL 포트 기본값은 3307이다. 변경하면 DB_URL도 함께 수정한다.
상태 확인은 `curl --fail http://localhost:8080/actuator/health`로 한다.
Spring Security 기본 보안이 적용되어 비공개 경로는 인증 없이 접근할 수 없다.
인증 정책과 JWT 필터는 별도 담당자가 구현한다. 실제 사용자 인증이 준비되기 전 외부 서비스로 공개하지 않는다.

## 검사

```bash
./gradlew build test ktlintCheck detekt
./gradlew ktlintFormat
```

테스트는 로컬 MySQL과 같은 환경변수로 실행하며 DB가 먼저 준비되어야 한다.
Flyway는 `src/main/resources/db/migration`의 버전 마이그레이션을 실행한다.
초기 뼈대에는 테이블을 만들지 않는다. 도메인 담당자가 스키마와 migration을 함께 추가한다.
Hibernate `ddl-auto=validate`를 유지하며 기존 migration을 수정하지 않는다.
UTC로 DB 세션·JDBC·Jackson·로그를 통일하고 날짜는 ISO-8601 문자열로 직렬화한다.
Swagger는 local에서만 켜며 `/v3/api-docs`, `/swagger-ui/index.html`도 현재 인증이 필요하다.

## 구조

`global`은 공통 설정, `domain/<도메인>`은 controller, service, repository, entity, dto를 둔다.
조회 서비스와 변경 서비스는 분리하고 다른 도메인의 Repository를 직접 사용하지 않는다.
작업 전에 `AGENTS.md`와 관련 규칙 파일을 읽는다.
팀원 환경에서의 실행과 2인 리뷰는 PR 체크리스트에서 별도로 확인한다.
