# 데모 배포 기본 설정

## 이번 범위

배포 방식, BE Dockerfile, FE의 SPA 배포 설정, 필요한 환경변수를 정리한다.
실제 배포 workflow와 서버 실행 스크립트는 별도 배포 작업에서 각 레포가 관리한다.
FE SHA를 BE workflow에 입력해 두 앱을 함께 배포하지 않는다.
기존 자동화 구현은 Git 이력에 보존하며 배포 시점에 요구 사항을 다시 검토한다.

## 서버와 데이터

- EC2 한 대를 Public Subnet에 두고 Nginx/React와 Spring Boot를 실행한다.
- 기존 RDS for MySQL은 Private Subnet에 유지하며 데이터를 초기화하지 않는다.
- S3는 비공개 단일 버킷을 사용하고 EC2는 S3 Gateway Endpoint로 접근한다.
- Cloudflare는 DNS/TLS를 담당하며 origin에는 유효한 인증서를 설정한다.
- ALB, NAT Gateway, Auto Scaling, Multi-AZ는 사용하지 않는다.

## 레포별 이미지

BE는 JDK 21 빌드 결과를 JRE 21 이미지에 넣는다.

```bash
./gradlew build
docker build -t vidi-backend:local .
```

FE는 [FE 레포](https://github.com/EWHA-VIDI/vidi-frontend)의 Dockerfile과 nginx.conf로
이미지를 직접 관리한다. FE 빌드에 BE 체크아웃이나 named context는 필요하지 않다.
FE Nginx는 SPA fallback과 `/api/` 프록시, Docker DNS의 백엔드 주소 갱신을 담당한다.
같은 Docker 네트워크에서 백엔드 호스트 이름을 `backend`, 포트를 `8080`으로 연결한다.
TLS reverse proxy는 FE로 요청을 전달하며 Host와 X-Forwarded-Proto를 설정한다.

## 환경변수와 비밀값

| 위치           | 값과 용도                                                                                        |
| -------------- | ------------------------------------------------------------------------------------------------ |
| BE 실행 환경   | SPRING_PROFILES_ACTIVE=prod, DB_URL, DB_USERNAME, DB_PASSWORD                                    |
| FE 빌드 공개값 | VITE_API_BASE_URL=/api                                                                           |
| 향후 배포 설정 | AWS_REGION, AWS_DEPLOY_ROLE_ARN, 각 레포의 ECR repository, EC2_INSTANCE_ID, SSM_PARAMETER_PREFIX |

로컬 DB 값은 `.env.example`을 참고하고 실제 비밀값은 커밋하지 않는다.
운영 DB 값은 SSM Parameter Store SecureString으로 관리하고 서버에서 주입한다.
DB_URL에는 TLS 검증과 UTC 세션 설정을 포함하고 RDS CA를 신뢰하도록 구성한다.
FE의 VITE 환경변수와 Docker build argument에 서버 비밀값을 넣지 않는다.

배포 자동화 도입 시 GitHub Actions OIDC로 임시 IAM Role을 받고 ECR·SSM을 사용한다.
장기 AWS access key는 GitHub Secrets에 저장하지 않는다.
각 레포의 배포 권한은 해당 ECR 저장소와 배포 대상에 필요한 범위로 제한한다.
현재 PR에서는 AWS 리소스나 배포 권한을 생성·변경하지 않는다.

## 확인 사항

- production Environment 보호와 main/develop 승인 2명·필수 CI·직접 push 금지는 팀에서 적용한 설정을 사용한다.
- 변경이 필요하면 저장소 관리자와 확인하며 이 PR에서 보호 규칙을 다시 변경하지 않는다.
- 실제 AWS 변수·IAM·origin TLS·RDS 연결은 배포 단계에서 검증한다.
- 배포 시 BE health UP, Nginx를 거친 `/healthz/backend`의 UP, FE healthz를 함께 확인한다.
- Flyway 변경은 백업과 역호환성을 확인하고 이미 적용한 migration을 되돌리지 않는다.
