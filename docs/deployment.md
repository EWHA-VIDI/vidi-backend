# 데모 배포

## 배포 위치

EC2 한 대의 Public Subnet에서 Nginx/React와 Spring Boot 컨테이너를 실행한다.
기존 RDS for MySQL은 Private Subnet에서 유지하고 새 RDS를 만들거나 기존 데이터를 초기화하지 않는다.
S3는 비공개 단일 버킷으로 사용하며 EC2는 S3 Gateway Endpoint로 접근한다.
Cloudflare는 DNS/TLS를 담당한다. ALB, NAT Gateway, Auto Scaling, Multi-AZ는 사용하지 않는다.
API·S3 권한 처리, Terraform, Alloy/Sentry/Discord 연동은 각 후속 담당자의 작업이며 이 뼈대에서 가짜 구현을 만들지 않는다.

공통 인프라는 이 레포의 단일 PR에서 관리한다. FE 배포 파일도 `deploy/`에 모아
하나의 Jira Task에 여러 레포 PR이 연결되는 것을 피한다.

## 컨테이너 빌드

BE는 README의 MySQL 기반 검사를 통과한 후 JAR을 만든다.

```bash
./gradlew build test ktlintCheck detekt
docker build -t vidi-backend:local .
docker buildx build --load --build-context infra=./deploy \
  -f deploy/frontend.Dockerfile -t vidi-frontend:local ../vidi-frontend
```

FE 빌드 context는 FE 체크아웃이며 Nginx 설정만 named context로 전달한다.
FE는 `/api`만 사용하고 API 주소나 비밀번호를 이미지에 넣지 않는다.
TLS 검사 프록시가 있는 클라우드에서는 FE 빌드에 `--secret id=proxy_ca,src=인증서경로`를 추가한다.
인증서 검증은 끄지 않으며 인증서는 image layer에 복사하지 않는다.

## GitHub Actions와 AWS 설정

`Deploy demo`는 main에서 수동으로만 실행한다. 입력은 리뷰된 FE 전체 SHA이다.
BE/FE CI와 팀원 2인 승인이 완료된 main 커밋만 배포한다.
OIDC로 임시 IAM Role을 받아 ECR에 SHA 태그 이미지를 push하고 SSM Run Command로 배포한다.
장기 AWS access key를 GitHub Secrets에 저장하지 않는다.
현재 두 레포는 public이므로 교차 checkout에 별도 토큰을 사용하지 않는다.
private으로 전환하면 FE 읽기 전용 GitHub App 토큰과 해당 설정을 별도로 준비한다.

GitHub production Environment에 다음 Variables를 실제 리소스 값으로 등록한다.

| 이름                    | 용도                         |
| ----------------------- | ---------------------------- |
| AWS_REGION              | 리소스가 위치한 AWS 리전     |
| AWS_DEPLOY_ROLE_ARN     | OIDC로 수임하는 IAM Role ARN |
| ECR_BACKEND_REPOSITORY  | 기존 BE ECR repository 이름  |
| ECR_FRONTEND_REPOSITORY | 기존 FE ECR repository 이름  |
| EC2_INSTANCE_ID         | 배포 대상 SSM 관리 인스턴스  |
| SSM_PARAMETER_PREFIX    | 예: /vidi/prod/backend       |

현재 배포에 필요한 GitHub Secret은 없다. AWS 인증은 OIDC, DB 비밀값은 SSM을 사용한다.
추후 Sentry/Discord 등을 연결할 때도 브라우저에 노출되는 VITE 값과 서버 비밀값을 구분한다.
이 환경의 토큰은 Secrets/브랜치 보호 API에 403을 반환하므로 등록·보호 설정 완료로 표시하지 않는다.

SSM prefix 하위에 DB_URL, DB_USERNAME, DB_PASSWORD를 SecureString으로 등록한다.
DB_URL에는 TLS 검증과 UTC 세션 설정을 포함한다. RDS CA는 Java truststore와 호환되게 관리한다.
비밀값은 채팅, workflow, .env.example에 넣지 않는다.

OIDC trust는 해당 저장소의 production Environment subject와 sts.amazonaws.com audience로 제한한다.
Actions Role은 해당 두 ECR 저장소 push, 해당 인스턴스의 AWS-RunShellScript 실행/결과 조회만 허용한다.
EC2 Instance Role은 ECR pull, 해당 SSM prefix 읽기와 필요한 KMS decrypt만 허용한다.
앱의 S3 권한은 별도 prefix별 최소 권한으로 추가한다.

## EC2 준비와 운영

Docker Engine, Compose 2.30 이상, AWS CLI, Python 3, curl, SSM Agent를 준비한다.
EC2 보안 그룹은 관리자가 정한 Cloudflare origin 경로만 허용하고 DB는 앱 보안 그룹에서만 접근하게 한다.
Compose는 host loopback의 8088(FE), 8080(BE)만 공개한다.
호스트 TLS reverse proxy를 443에 두고 Cloudflare Full (strict)와 유효한 origin 인증서를 설정한다.
호스트 proxy는 127.0.0.1:8088로 전달하고 Host 및 X-Forwarded-Proto를 직접 설정한다.
Cloudflare와 EC2 origin TLS가 검증되기 전에는 공개 배포 완료로 표시하지 않는다.

배포 스크립트는 SSM 값을 권한 0600의 backend.env로 받고 prod 프로필로 실행한다.
DB 비밀값은 VITE 환경변수나 Docker build argument로 전달하지 않는다.
Nginx는 SPA deep link에 index.html을 제공하되 누락 asset과 API 오류는 그대로 반환한다.
SSM 배포 성공은 BE health UP과 FE healthz를 확인한 뒤에만 기록한다.
실패한 배포는 이전의 검증된 두 SHA 태그로 재실행한다. 자동 rollback은 구현하지 않는다.
Flyway 변경이 있는 릴리스는 백업·역호환성을 확인하며 이미 실행한 migration을 되돌리지 않는다.

## 미완료 외부 조건

실제 AWS 변수·IAM·TLS·RDS 연결은 이 환경에 제공되지 않아 확인하지 않았다.
production Environment 보호와 main/develop 승인 2명·필수 CI·직접 push 금지를 저장소 관리자가 적용한다.
팀원 로컬 실행과 리뷰를 확인한 뒤 배포한다.
