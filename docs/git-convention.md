Jira가 작업 관리의 기준(Single Source of Truth)이고, Jira 이슈 키 하나로 Branch, Commit, PR을 연결한다. GitHub는 코드 저장소, Pull Request, Code Review, CI/CD를 담당한다. Jira 상태는 자동화로 바뀌므로 사람이 직접 옮기지 않는다.

| 항목      | 형식                            | 예시                                         |
| --------- | ------------------------------- | -------------------------------------------- |
| Jira Task | [영역] 작업 내용                | VIDI-101 [BE] Gradle 의존성 설정             |
| Branch    | type/이슈키-설명                | build/VIDI-101-gradle-dependency             |
| Commit    | type(scope): 작업 내용 [이슈키] | build(config): Gradle 의존성 추가 [VIDI-101] |
| PR 제목   | 커밋과 같은 형식                | build(config): Gradle 의존성 설정 [VIDI-101] |

### 핵심 원칙

1. Jira를 실제 작업 관리의 기준으로 사용한다.
2. Jira 이슈 키를 Branch, Commit, PR에 공통으로 넣는다.
3. 하나의 Task는 하나의 논리적 작업 단위로 만든다.
4. Backend와 Frontend 작업은 별도 Task로 분리한다.
5. 하나의 Jira Task에는 하나의 Branch와 하나의 PR을 연결한다.
6. Commit은 하나의 목적만 갖는다.
7. main, develop에는 직접 작업하지 않는다.
8. 리뷰 승인과 CI 통과 후 Squash and Merge 한다.
9. Jira 상태는 자동화가 바꾼다. 사람은 예외 상황만 직접 처리한다.
10. 규칙은 문서가 아니라 도구(commitlint, PR 제목 검사, 브랜치 보호)로 강제한다.

## 1. Commit Convention

커밋은 Conventional Commits 형식을 따르고, 제목 끝에 Jira 이슈 키를 붙인다.

### 1-1. 형식

```
type(scope): 작업 내용 [Jira 이슈 키]
```

```
feat(auth): 로그인 기능 구현 [VIDI-12]
fix(ui): 로그인 페이지 레이아웃 오류 수정 [VIDI-15]
refactor(user): 사용자 서비스 로직 분리 [VIDI-20]
test(auth): 로그인 API 테스트 작성 [VIDI-30]
build(config): Gradle 의존성 추가 [VIDI-101]
ci: GitHub Actions 테스트 자동화 설정 [VIDI-45]
infra: AWS 배포 환경 구성 [VIDI-55]
release: v1.0.0 배포 준비 [VIDI-70]
revert: 로그인 기능 커밋 되돌리기 [VIDI-60]
```

### 1-2. Commit Type

| Type     | 설명                                             |
| -------- | ------------------------------------------------ |
| feat     | 새로운 기능 추가                                 |
| fix      | 버그 및 오류 수정                                |
| style    | 포맷팅, 들여쓰기 등 스타일 수정 (기능 변경 없음) |
| refactor | 기능 변경 없이 코드 구조 개선                    |
| docs     | 문서 및 주석 수정                                |
| test     | 테스트 코드 작성 및 수정                         |
| chore    | 기타 작업, 패키지 관리, 설정 변경                |
| build    | 빌드 시스템, 의존성(Gradle, npm) 변경            |
| ci       | CI/CD 파이프라인 및 자동화 설정                  |
| perf     | 성능 개선                                        |
| revert   | 이전 커밋 되돌리기                               |
| infra    | 서버, DB, 클라우드 등 인프라 구축 및 설정        |
| release  | 배포 준비 및 버전 관리                           |

infra, release는 Conventional Commits 기본 타입이 아니다. commitlint를 쓸 때 type-enum에 직접 추가해야 한다(10장 참고).

### 1-3. 작성 규칙

- Type은 영문 소문자로 쓴다.
- Scope는 선택이며, 가장 핵심적인 작업 영역 하나만 쓴다.
- 작업 내용은 한글로 간결하게 쓴다. 기술 용어와 라이브러리명은 영어를 쓸 수 있다.
- 끝에 마침표를 붙이지 않는다.
- 모든 커밋에 Jira 이슈 키를 넣는다. 이슈 키는 대문자(VIDI-12)로 쓴다.
- 별도 Task가 없는 자잘한 설정 작업은 키를 빼지 않고, 공용 관리 Task(예: [공통] 프로젝트 설정 및 관리)의 키를 쓴다.
- 하나의 커밋은 하나의 목적만 갖는다. 서로 다른 목적의 변경을 한 커밋에 넣지 않는다.
- 호환성이 깨지는 변경(API 스펙 변경 등)은 type 뒤에 !를 붙인다. 예: feat(api)!: 사용자 응답 필드 변경 [VIDI-88]

### 1-4. Scope

Scope는 선택이며, 분야별 Scope를 우선 쓴다. 목록에 없는 Scope도 필요하면 추가할 수 있다. 같은 이름이라도 분야에 따라 뜻이 다를 수 있다.

**공통**

| Scope  | 설명                          |
| ------ | ----------------------------- |
| common | 공통 기능 및 유틸리티         |
| config | 프로젝트 및 환경 설정         |
| docs   | 문서 및 주석                  |
| test   | 테스트 관련 작업              |
| ci     | CI/CD 및 자동화 설정          |
| infra  | 서버, 클라우드 및 인프라 설정 |
| build  | 빌드 시스템 및 관련 설정      |

**Frontend**

| Scope     | 설명                          |
| --------- | ----------------------------- |
| page      | 페이지 구현                   |
| component | 공통 및 재사용 컴포넌트       |
| ui        | UI 및 스타일                  |
| router    | 라우팅 및 페이지 이동         |
| auth      | 인증 및 인가 관련 화면과 기능 |
| api       | API 연동 및 요청·응답 처리    |
| state     | 전역 및 로컬 상태 관리        |

**Backend**

| Scope    | 설명                        |
| -------- | --------------------------- |
| auth     | 인증 및 인가                |
| user     | 사용자 관련 기능            |
| api      | 공통 API 및 요청·응답 처리  |
| db       | 데이터베이스 및 데이터 접근 |
| security | 보안 설정 및 접근 권한 관리 |
| common   | 공통 로직 및 유틸리티       |
| config   | 서버 및 애플리케이션 설정   |

**Data**

| Scope      | 설명                      |
| ---------- | ------------------------- |
| collection | 데이터 수집               |
| preprocess | 데이터 전처리             |
| eda        | 탐색적 데이터 분석        |
| feature    | Feature Engineering       |
| model      | 모델 학습 및 평가         |
| notebook   | Jupyter 및 Colab Notebook |
| pipeline   | 데이터 처리 파이프라인    |

## 2. Branch Convention

브랜치명은 type/이슈키-설명 형식이며, 브랜치명의 이슈 키가 Jira 자동화(개발중 전환)를 일으키므로 반드시 넣는다. 브랜치 전략은 main + develop을 두는 Git Flow를 간소화해 쓴다.

### 2-1. 브랜치 종류

| Branch      | 설명                                                                  | 분기 위치 | 병합 대상     |
| ----------- | --------------------------------------------------------------------- | --------- | ------------- |
| main        | 배포 가능한 안정 상태                                                 | -         | -             |
| develop     | 개발 작업 통합                                                        | main      | -             |
| 작업 브랜치 | feat, fix, refactor, style, docs, test, chore, build, ci, perf, infra | develop   | develop       |
| hotfix      | 배포 후 긴급 오류 수정                                                | main      | main, develop |
| release     | 배포 준비 및 버전 관리                                                | develop   | main, develop |

작업 브랜치의 type은 1-2의 Commit Type과 같은 목록을 쓴다. 기능 브랜치는 feature가 아니라 feat로 쓴다.

### 2-2. 브랜치명 작성 규칙

```
type/이슈키-설명
```

```
feat/VIDI-12-login
fix/VIDI-15-login-validation
refactor/VIDI-20-user-service
docs/VIDI-25-readme-update
test/VIDI-30-login-test
build/VIDI-101-gradle-dependency
ci/VIDI-45-github-actions
infra/VIDI-55-aws-deployment
hotfix/VIDI-90-token-expire-error
```

- Type은 영문 소문자, 이슈 키는 대문자(VIDI-12)로 쓴다.
- 설명은 영문 소문자 kebab-case로 짧게 쓴다.
- Jira 이슈 키는 반드시 넣는다. 예외는 release 브랜치 하나뿐이다.
- Jira 이슈 화면의 Create branch 기능을 쓰면 키가 자동으로 들어간다. 이때도 이 형식에 맞게 이름을 고친다.
- 하나의 브랜치는 하나의 Jira Task만 담당한다.

### 2-3. release 브랜치 (예외)

release 브랜치는 이슈 키 대신 버전 번호를 쓴다.

```
release/1.0.0
```

1. develop에서 release/x.y.z를 분기한다.
2. 버전 수정과 배포 전 버그 수정만 한다. 새 기능은 넣지 않는다.
3. PR로 main에 병합하고 vx.y.z 태그를 단다.
4. main의 변경을 develop에도 반영한다.

### 2-4. hotfix 흐름

1. 긴급 오류도 Jira Task(유형: Bug)를 먼저 만든다.
2. main에서 hotfix/VIDI-90-설명을 분기한다.
3. PR로 main에 병합하고 패치 버전 태그(예: v1.0.1)를 단다.
4. 같은 수정을 develop에도 반영한다.

### 2-5. 운영 규칙

- 새 작업은 최신 develop에서 분기한다.
- main, develop에서 직접 작업하거나 push하지 않는다(브랜치 보호로 막음).
- 작업이 끝나면 PR을 만들어 develop에 병합한다.
- 병합된 작업 브랜치는 자동으로 삭제된다(레포 설정).
- 작업 브랜치는 짧게 유지한다. develop과 멀어지면 주기적으로 최신 develop을 반영한다.

## 3. Jira Issue Convention

모든 개발 작업은 Jira Task를 먼저 만들고 시작하며, Epic → Task → PR → Commit 구조로 추적한다.

```
Epic: 프로젝트 뼈대·설정
├── VIDI-101 [BE] Gradle 의존성 설정
│   └── PR: build(config): Gradle 의존성 설정 [VIDI-101]
├── VIDI-102 [BE] application 환경 설정
├── VIDI-103 [FE] 패키지 및 TypeScript 설정
└── VIDI-104 [CI] GitHub Actions 설정
```

### 3-1. Epic

| Epic                  | 범위                                               |
| --------------------- | -------------------------------------------------- |
| 1. 프로젝트 뼈대·설정 | Backend, Frontend, CI/CD 및 공통 개발 환경 구축    |
| 2. 요청 흐름·인증     | API 요청부터 응답까지의 공통 처리와 인증·인가 흐름 |
| 3. 데이터·UI 기반     | ERD 기반 데이터 계층 및 공통 UI 기반               |

### 3-2. Task 제목

```
[영역] 작업 내용
```

| 영역  | 설명                               |
| ----- | ---------------------------------- |
| BE    | Backend 작업                       |
| FE    | Frontend 작업                      |
| DATA  | 데이터 작업                        |
| CI    | CI/CD 및 자동화                    |
| INFRA | 인프라 작업                        |
| DOCS  | 문서 작업                          |
| 공통  | 여러 영역에 공통으로 적용되는 작업 |

- 제목 접두사와 같은 값을 Jira Component(또는 Label)에도 설정한다. 보드 필터와 통계는 이 필드로 한다.
- BE와 FE에서 모두 작업이 필요하면 Task를 따로 만든다. [BE/FE] 로그인 구현이 아니라 [BE] 로그인 API 구현, [FE] 로그인 화면 및 API 연동으로 나눈다.
- 한 영역에서 처리할 수 없는 공통·협업 작업만 [공통]으로 만든다.

### 3-3. Task 본문

```
목적:
프로젝트의 Backend 개발에 필요한 공통 의존성을 구성한다.

작업 내용:
- Spring Security, JWT, Springdoc, JPA, Flyway
- Kotest, MockK, Kotlin 관련 Plugin

완료 조건:
- build.gradle.kts 의존성 구성 완료
- 프로젝트 정상 빌드 및 테스트 실행 가능
- 팀원 로컬 환경에서 정상 실행 확인

관련 문서:
기능명세서 ID 또는 링크

선행 작업:
없음
```

### 3-4. 작성 규칙

- Task를 만든 뒤 작업을 시작한다.
- 담당자, Component/Label, Epic을 지정한다.
- 목적, 작업 내용, 완료 조건을 구체적으로 쓴다.
- 다른 Task에 의존하면 선행 작업을 적고, Jira의 이슈 링크(blocks / is blocked by)로도 연결한다.
- 하나의 Task에 너무 많은 기능을 넣지 않는다. 목표는 PR 하나로 리뷰할 수 있는 크기다.
- 하나의 Task에는 하나의 PR을 연결한다.

### 3-5. 기능명세서 ID

Jira 이슈 키가 작업의 기본 식별자이고, 기능명세서 ID는 기획 문서와 연결할 때만 선택적으로 쓴다.

| 구분          | 예시                    | 쓰는 곳                        |
| ------------- | ----------------------- | ------------------------------ |
| Jira 이슈 키  | VIDI-42                 | Branch, Commit, PR (필수)      |
| 기능명세서 ID | AUTH-01 (카카오 로그인) | Jira Task 본문, PR 본문 (선택) |

## 4. Jira ↔ GitHub 연동 및 자동화

Jira와 GitHub는 연동되어 있고, Task 상태는 브랜치 생성과 PR 병합에 따라 자동으로 바뀐다. 자동화는 이슈 키로 동작하므로, 키가 빠지거나 틀리면 상태가 바뀌지 않는다.

### 4-1. 상태 자동 전환

| GitHub 이벤트                | Jira 상태 | 조건                                                     |
| ---------------------------- | --------- | -------------------------------------------------------- |
| 이슈 키가 들어간 브랜치 생성 | 개발중    | 브랜치명에 VIDI-12 형태의 키가 있어야 함                 |
| 연결된 PR 병합               | 개발 완료 | PR이 키로 이슈에 연결돼 있어야 함(브랜치명 또는 PR 제목) |

- Jira 상태는 사람이 직접 옮기지 않는다. 자동화가 없는 경우(작업 취소, 보류, 재오픈)만 수동으로 바꾼다.
- 브랜치를 만들었는데 개발중으로 바뀌지 않았다면 브랜치명의 키를 먼저 점검한다.
- PR 하나에 여러 키를 넣지 않는다.
- PR 본문에서 다른 Task를 언급할 때도 주의한다. 연동 방식에 따라 본문의 키도 연결될 수 있다.

### 4-2. 이슈 키가 인식되는 위치

| 위치        | 예시                                   | 역할                                     |
| ----------- | -------------------------------------- | ---------------------------------------- |
| 브랜치명    | feat/VIDI-12-login                     | 개발중 전환, 브랜치의 모든 커밋·PR 연결  |
| 커밋 메시지 | feat(auth): 로그인 기능 구현 [VIDI-12] | 커밋 단위 추적                           |
| PR 제목     | feat(auth): 로그인 기능 구현 [VIDI-12] | PR 연결, Squash 후 develop 커밋에도 남음 |

Jira 이슈의 Development 패널에서 연결된 브랜치, 커밋, PR과 빌드 상태를 확인할 수 있다.

## 5. Pull Request Convention

PR 제목은 커밋 메시지와 같은 형식으로 쓴다. Squash and Merge를 하면 PR 제목이 develop에 남는 커밋 메시지가 되기 때문이다.

### 5-1. PR 제목

```
type(scope): 작업 내용 요약 [Jira 이슈 키]
```

```
feat(auth): 로그인 기능 구현 [VIDI-12]
fix(ui): 로그인 페이지 오류 수정 [VIDI-15]
refactor(user): 사용자 서비스 로직 분리 [VIDI-20]
infra: AWS 배포 환경 구성 [VIDI-35]
```

### 5-2. 작성 규칙

- PR 하나는 Jira Task 하나에 대응한다.
- 변경 사항과 테스트 결과를 쓴다. UI 변경은 스크린샷을 붙인다.
- 담당자(Assignee)와 Label을 설정하고, 리뷰어를 지정해 리뷰를 요청한다.
- 작업 중인 PR은 Draft로 열고, 리뷰 준비가 되면 Ready for review로 바꾼다.

### 5-3. Merge 규칙

- 작성자를 제외한 팀원 전원(현재 2명)을 리뷰어로 지정하고, 2명 모두 Approve해야 병합한다.
- AI 리뷰봇의 리뷰는 참고한다. 승인 수에는 포함하지 않는다.
- Request Changes가 있으면 수정 후 재리뷰를 요청한다.
- CI가 모두 통과해야 병합한다.
- 작업 브랜치 → develop은 **Squash and Merge**만 쓴다. 병합 창의 커밋 제목이 PR 제목과 같은지 확인한다.
- release, hotfix → main은 develop과 히스토리를 맞추기 위해 **Create a merge commit**을 쓴다.
- 병합 후 브랜치는 자동 삭제되고, Jira Task는 자동으로 개발 완료가 된다.

## 6. Code Review Convention

리뷰 코멘트 앞에 우선순위 태그를 붙이고, P1·P2가 하나라도 있으면 Approve하지 않는다.

| Priority | 의미                               | 리뷰 방식       |
| -------- | ---------------------------------- | --------------- |
| P1       | 반드시 수정해야 하는 심각한 문제   | Request Changes |
| P2       | 적극적으로 고려해야 하는 개선 사항 | Request Changes |
| P3       | 웬만하면 반영하는 것이 좋은 사항   | Comment         |
| P4       | 선택적인 개선 사항                 | Approve         |
| P5       | 사소한 의견 및 참고 사항           | Approve         |

표기 예시:

```
[P1] 토큰 만료 시 예외가 처리되지 않아 500이 반환됩니다. 401로 내려주세요.
[P3] 이 로직은 UserService로 분리하면 테스트가 쉬워질 것 같아요.
[P5] 변수명이 직관적이라 읽기 좋네요.
```

- 리뷰는 정중하고 명확하게 쓴다. 문제점과 함께 이유와 개선 방향을 제시한다.
- 단순한 취향 차이는 강요하지 않는다(P4·P5로 표시).
- 포맷팅·린트 지적은 리뷰가 아니라 도구(ktlint, ESLint, Prettier)가 맡는다.
- 리뷰할 내용이 없어도 배운 점이나 좋았던 점을 남길 수 있다.
- 리뷰 요청을 받으면 하루 안에 리뷰를 남기는 것을 원칙으로 한다.

## 7. 전체 작업 흐름

사람이 하는 일은 Task 생성, 개발, PR, 리뷰뿐이고 Jira 상태 변경과 브랜치 삭제는 자동으로 처리된다.

1. Jira Task 생성 (Epic, 담당자, Component 지정)
2. 최신 develop 반영: git switch develop && git pull
3. 이슈 키가 들어간 브랜치 생성 후 push → Jira: 개발중 (자동)
4. 개발 및 Commit (commitlint가 형식 검사)
5. 로컬 테스트
6. PR 생성 (제목 = 커밋 형식, 템플릿 작성, 리뷰어 지정)
7. CI 실행 (빌드, 테스트, PR 제목 검사)
8. 팀원 Code Review → 수정 → Approve 2건
9. Squash and Merge → Jira: 개발 완료 (자동), 브랜치 삭제 (자동)

## 8. 작업 분리 원칙

Backend와 Frontend에서 각각 작업이 필요하면 별도의 Jira Task로 만든다. 실제로 한 영역에서 처리할 수 없는 공통·협업 작업만 [공통] Task로 만든다.

```
Epic: 요청 흐름·인증
├── [BE] ErrorCode / ErrorResponse 구현
├── [BE] SecurityFilterChain 구현
├── [BE] JWT Filter 구현
├── [BE] CORS 설정
├── [BE] 인증 예외 처리
├── [FE] Axios Instance 구현
├── [FE] Token 관리
├── [FE] 401 Refresh 처리
├── [FE] ProtectedRoute 구현
└── [FE] TanStack Query 기본 설정
```

## 9. 데이터 레포지토리 추가 규칙

Google Colab과 Jupyter Notebook을 쓰는 데이터 작업에는 아래 규칙을 추가로 적용한다. Branch, Commit, PR 형식은 다른 레포와 같다.

- 데이터 처리 과정과 실행 방법은 README에 쓴다.
- Notebook 실행 결과(Output)는 지운 뒤 커밋한다. nbstripout을 설치하면 커밋 때 자동으로 지워진다.
- 대용량 원본·가공 데이터는 GitHub에 올리지 않고 별도 스토리지를 쓴다. 데이터 폴더는 .gitignore에 넣는다.
- 개인정보와 외부 공개가 제한된 데이터는 GitHub에 올리지 않는다.
- 전처리·분석 결과가 바뀌면 PR에 주요 변경 사항과 검증 결과를 쓴다.
- 데이터 Task에는 작업 목적과 산출물을 적는다.

```
feat/VIDI-80-data-preprocess
feat(preprocess): 사용자 데이터 전처리 [VIDI-80]
```

## 10. 규칙 자동 강제

규칙은 사람이 기억하는 대신 도구가 막는다. Squash Merge에서는 PR 제목이 최종 커밋이 되므로 PR 제목 검사가 가장 중요하다.

| 도구                         | 검사 대상                                | 적용 레포      |
| ---------------------------- | ---------------------------------------- | -------------- |
| PR 제목 검사 (GitHub Action) | PR 제목 형식과 Jira 키                   | 전체           |
| 브랜치 보호 규칙             | main/develop 직접 push, 승인 수, CI 통과 | 전체           |
| commitlint + husky           | 로컬 커밋 메시지                         | FE (Node 기반) |
| ktlint / ESLint, Prettier    | 코드 스타일                              | BE / FE        |

BE·데이터 레포는 husky를 쓰지 않아도 된다. PR 제목 검사가 develop에 남는 커밋을 보장한다.

### 10-1. GitHub 레포 설정

| 설정 위치                          | 값                                                                     |
| ---------------------------------- | ---------------------------------------------------------------------- |
| Settings → General → Pull Requests | Allow squash merging·Allow merge commits만 켜고, Rebase merging은 끈다 |
| 같은 곳, Squash 기본 메시지        | Default to pull request title                                          |
| 같은 곳                            | Automatically delete head branches 켜기                                |
| Branch protection (main, develop)  | Require a pull request before merging, Required approvals 2            |
| Branch protection (main, develop)  | Require status checks to pass (빌드, 테스트, PR 제목 검사)             |
| Branch protection (main, develop)  | Do not allow bypassing, force push·삭제 금지                           |

### 10-2. PR 제목 검사 워크플로

실제 구현은 각 레포의 `.github/workflows/pr-title.yml`과 `scripts/conventions.cjs`를 기준으로 확인한다.

- 제목 Action과 FE commitlint는 같은 validator를 사용한다.
- scope는 선택이며 breaking change 표기를 허용한다.
- 허용한 type, 120자 제한, 제목 끝 Jira 키 하나를 검사한다.
- 정상·오류 제목 회귀 검사는 `node --test scripts/conventions.test.cjs`로 실행한다.
- workflow의 Action 버전은 검증한 commit SHA로 고정한다.

### 10-3. commitlint 설정 (FE)

FE의 `commitlint.config.js`와 `.husky/commit-msg`를 기준으로 확인한다.

- config-conventional을 확장하고 infra·release를 포함한 type 목록을 공유한다.
- subject-case는 강제하지 않고 Jira 제목 검사를 공통 validator에 맡긴다.
- pre-commit은 lint-staged로 변경 파일을 검사한다.
- commit-msg는 commitlint로 실제 커밋 메시지를 검사한다.
- 구현과 어긋나는 설정 예제를 이 문서에 복제하지 않는다.
