# VIDI Backend

유튜브 댓글의 계정·시간·문장 관계를 분석해 조직적 바이럴 의심 패턴을 탐지하는 VIDI의 백엔드입니다.

## 요구 환경

- JDK 21
- Docker Desktop 또는 Docker Engine

## 로컬 실행

```bash
cp .env.example .env
docker compose up -d
./gradlew bootRun
```

- API: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- Health check: `http://localhost:8080/actuator/health`

## 검증

```bash
./scripts/verify.sh quick
./scripts/verify.sh full
```

Windows PowerShell에서는 다음 명령을 사용합니다.

```powershell
.\scripts\verify.ps1 quick
.\scripts\verify.ps1 full
```

작업 전 `AGENTS.md`와 관련 `docs/rules` 파일을 확인합니다.
