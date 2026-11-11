# Git Convention

- 담당: 뼈대·설정 담당자

## 규칙

- 기본 브랜치는 `develop`을 사용한다.
- 브랜치는 `type/VIDI-번호-설명` 형식으로 만든다.
- type은 `feat`, `fix`, `refactor`, `test`, `docs`, `chore`, `hotfix`, `release` 중 하나를 사용한다.
- 브랜치 설명은 영문 소문자와 하이픈으로 작성한다.
- 커밋은 `type(scope): 작업 내용 [VIDI-번호]` 형식으로 작성한다.
- PR 제목은 `[TYPE] 작업 내용 요약` 형식으로 작성한다.
- PR 본문에 관련 이슈, 변경 사항, 검증 결과, 설정 변경, 후속 작업을 기록한다.
- API가 변경되면 백엔드 PR, Swagger 변경 내용, 생성 타입 갱신 여부를 기록한다.
- 리뷰 승인과 필수 CI 통과 후 squash merge한다.

## 하지 않는 것

- `develop`과 `main`에 직접 push하지 않는다.
- 서로 다른 목적의 변경을 한 PR에 섞지 않는다.
- 비밀값, 개인 설정, 빌드 산출물을 커밋하지 않는다.
- 실행하지 않은 검증 항목을 체크하지 않는다.

## 예시

- 브랜치: `chore/VIDI-12-project-scaffold`
- 커밋: `chore(config): 초기 환경을 구성한다 [VIDI-12]`
- PR: `[CHORE] 초기 환경과 하네스 구성`
