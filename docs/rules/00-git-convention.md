# Git 컨벤션

담당: 박지연

## 규칙

- 작업 전에 [전체 Git 컨벤션](../git-convention.md)을 읽는다.
- Jira를 작업 관리 기준으로 사용하고 이슈 하나에 브랜치 하나와 PR 하나를 연결한다.
- 최신 develop에서 `type/VIDI-번호-영문-kebab-case` 브랜치를 만든다.
- 커밋과 PR 제목은 `type(scope): 작업 내용 [VIDI-번호]`로 작성한다.
- scope는 선택으로 사용하고 제목은 120자 이내로 작성한다.
- type은 feat, fix, style, refactor, docs, test, chore, build, ci, perf, revert, infra, release를 사용한다.
- 호환성 변경은 type 또는 scope 뒤에 느낌표를 붙이고 내용을 설명한다.
- 제목 끝에 대문자 Jira 키 하나를 넣고 작업 내용 끝에 마침표를 붙이지 않는다.
- PR에 실제 검증 결과, 설정 변경, 제한을 적고 작성자 외 팀원 두 명에게 리뷰를 요청한다.
- 두 명의 승인과 필수 CI 통과 후 작업 PR을 develop에 squash merge한다.
- release 브랜치는 `release/x.y.z`를 사용하고 main에는 merge commit으로 병합한다.
- hotfix는 main에서 분기하고 Jira 키를 포함하며 main과 develop에 반영한다.
- Jira 상태는 브랜치 생성과 병합 자동화로 전환한다.
- 이 공통 파일을 바꿀 때는 두 레포에 같은 내용으로 PR을 올린다.

## 하지 않는 것

- main과 develop에 직접 push하거나 강제 push하지 않는다.
- 하나의 제목이나 브랜치에 여러 Jira Task를 섞지 않는다.
- AI 봇의 리뷰를 팀원 승인 수에 포함하지 않는다.
- 실행하지 않은 검증과 팀원의 로컬 확인을 완료로 표시하지 않는다.
- 자동화 대신 Jira 상태를 수동으로 바꾸지 않는다.
- 비밀번호, 키, 개인 환경 파일을 커밋하지 않는다.

## 예시

- 브랜치: `build/VIDI-9-backend-foundation`
- 커밋과 PR: `build(config): 백엔드 초기 환경 구성 [VIDI-9]`
- scope 생략: `ci: 프론트엔드 CI와 커밋 검사를 구성 [VIDI-13]`
- 형식 검사: `node --test scripts/conventions.test.cjs`
