# 설정과 린트

담당: 박지연

## 규칙

- Gradle과 애플리케이션은 JDK 21로 통일하고 JAVA_HOME을 JDK 21 경로로 지정한다.
- Detekt 안정판 1.23.8은 JDK 21 기본 설정을 사용하고 별도 jvmTarget을 덮어쓰지 않는다.
- Detekt 구성의 Kotlin 2.0.21 고정은 내장 분석기 호환성에만 적용한다.
- `.editorconfig`와 ktlint 1.8.0의 ktlint_official 스타일을 사용한다.
- Kotlin은 공백 4칸, yml은 공백 2칸, UTF-8과 LF, 마지막 개행을 사용한다.
- 커밋 전 `./gradlew ktlintCheck detekt test`를 실행한다.
- PR 전 MySQL을 기동하고 `./gradlew build test ktlintCheck detekt`를 실행한다.
- 자동 포맷은 `./gradlew ktlintFormat`으로 수행하고 변경을 확인한다.
- 공통 설정은 application.yml, 프로필 차이는 application-local.yml과 application-prod.yml에 둔다.
- 새 설정값은 환경변수 이름과 용도를 `.env.example` 및 README에 함께 기록한다.
- local 프로필은 작업 디렉터리의 `.env`를 properties 형식으로 자동으로 읽는다.
- `.env` 값은 따옴표와 export 없이 KEY=value로 적고 prod에서는 외부 환경변수를 주입한다.
- 날짜·DB 세션·로그는 UTC, API 날짜는 ISO-8601 문자열로 유지한다.
- MySQL과 Flyway를 사용하고 ddl-auto=validate, open-in-view=false를 유지한다.
- DB 변경 시 도메인 담당자가 새 버전 migration을 작성하고 이미 적용한 파일은 유지한다.
- build.gradle.kts, application yml의 변경은 설정 담당자에게 요청한다.

## 하지 않는 것

- 검사를 통과시키기 위해 H2로 대체하거나 Flyway·스키마 검증을 끄지 않는다.
- 프로젝트 전체에 Detekt의 Kotlin 버전을 강제하지 않는다.
- 광범위한 린트 비활성화나 경고 무시로 오류를 숨기지 않는다.
- prod에서 springdoc 또는 상세 health 정보를 공개하지 않는다.
- 실제 RDS 비밀번호를 예제·로그·커밋에 넣지 않는다.

## 예시

- `compose.yml`의 MySQL은 호스트 3307, 컨테이너 3306을 사용한다.
- `application.yml`은 `hibernate.jdbc.time_zone: UTC`를 사용한다.
- `VidiBackendApplicationTests`는 Jackson ISO 문자열과 MySQL 세션 UTC를 검사한다.
- `VidiBackendApplication.kt`의 SpreadOperator 억제는 Spring Boot vararg 진입점 하나에만 적용한다.
