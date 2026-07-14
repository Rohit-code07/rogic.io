# 스테이징 및 프로덕션 서버 AMI 업데이트로 인한 배포 실패 보고서

## 1. 개요
* **장애 발생 시각**<br>
  2026년 7월 14일 21시 59분
* **장애 복구 시각**<br>
  2026년 7월 14일 22시 35분 (약 36분 소요)
* **장애 영향 범위**<br>
  Staging 및 Production 환경 인프라 배포 (CI/CD 파이프라인 중단)
* **장애 원인 요약**<br>
  AWS Canonical의 최신 Ubuntu AMI 릴리즈로 인해 데이터 소스가 업데이트되면서 Staging 및 Production EC2 인스턴스의 강제 재생성(Destroy & Recreate)이 계획되었으나, `prevent_destroy = true` 설정과의 충돌로 배포가 실패함. 추가적으로 로컬에서의 비정상적인 plan 중단으로 인해 Staging 환경의 State Lock 잠김 현상이 발생했으며, PR 검증 단계에 plan을 추가한 후 OIDC 신뢰 조건 누락으로 인한 인증 예외가 동반됨.

## 2. 장애 타임라인
* **2026-07-14 21:59**<br>
  Staging CI/CD 파이프라인에서 Terraform 실행 도중 `Error: Instance cannot be destroyed` 에러와 함께 1차 빌드 실패 감지.
* **2026-07-14 22:01**<br>
  [main.tf](../../infra/terraform/envs/staging/main.tf)의 `aws_instance.nemologic_staging_server` 리소스에 적용된 `prevent_destroy = true`와 AMI 변경으로 인한 인스턴스 강제 재생성 계획 간의 충돌 원인 분석 완료.
* **2026-07-14 22:12**<br>
  Staging 해결 방안 수립 및 계획 승인 완료.
* **2026-07-14 22:13**<br>
  Staging 환경의 `ignore_changes = [ami]` 적용 완료.
* **2026-07-14 22:18**<br>
  코드 포맷 검증 단계(`terraform fmt -check`) 실패 확인 및 포맷 수정 후 커밋 반영 완료.
* **2026-07-14 22:20**<br>
  Staging PR 머지 후 배포 과정에서 Staging `DynamoDB State Lock` 충돌 에러 및 Production 배포 단계의 `aws_instance.nemologic_server` 재생성 에러 발생.
* **2026-07-14 22:27**<br>
  2차 원인 분석(로컬 plan 중단으로 인한 락 누수 및 Production AMI 무시 누락) 완료 및 2차 해결 계획 승인.
* **2026-07-14 22:28**<br>
  Staging 환경 강제 락 해제(`terraform force-unlock`) 완료 및 Production [main.tf](../../infra/terraform/envs/production/main.tf) 수정/검증 완료.
* **2026-07-14 22:30**<br>
  PR 검증 단계에 Terraform Dry-Run(Plan) 추가를 위한 [ci-cd.yml](../../.github/workflows/ci-cd.yml) 수정 진행 중 OIDC 수임 권한 오류(`sts:AssumeRoleWithWebIdentity`) 추가 감지.
* **2026-07-14 22:34**<br>
  Production OIDC Role Trust Policy 설정 누락 해결 방안 수립 및 최종 계획 승인 완료.
* **2026-07-14 22:35**<br>
  최종 수정본 원격 반영 완료.

## 3. 원인 분석
* **상세 분석**<br>
  AWS Staging 환경에서 사용하는 Ubuntu 22.04 LTS AMI는 [main.tf](../../infra/terraform/envs/staging/main.tf#L113-L124)에서 `most_recent = true` 옵션을 설정하여 `data.aws_ami.ubuntu`를 통해 동적으로 최신 이미지를 탐색하도록 설정되어 있었습니다.
  AWS 측에서 새로운 패치 버전의 AMI를 릴리즈함에 따라 `data.aws_ami.ubuntu.id` 값이 변경되었고, 이에 따라 Terraform은 인스턴스 재생성을 계획했습니다.
  하지만 인스턴스에는 예상치 못한 삭제를 방지하기 위해 `prevent_destroy = true` 설정이 추가되어 있었고, 리소스를 파괴하지 못해 배포가 중단되었습니다.
  마찬가지로 Production 환경의 [main.tf](../../infra/terraform/envs/production/main.tf#L217-L242)에서도 동일한 AMI 참조 및 `prevent_destroy = true` 설정이 작동하고 있었기 때문에 Production 배포 파이프라인 역시 동일한 예외를 트리거했습니다.
  또한, 로컬에서 배포 파이프라인 디버깅 과정 중 실행했던 `terraform plan` 명령어가 강제 종료되면서 원격 DynamoDB State Lock 릴리즈가 누수되어 Staging 배포 파이프라인이 추가적으로 중단되는 2차 장애가 발생했습니다.
  추가적으로, PR 검증 단계에 Terraform Dry-Run(Plan) 검증을 추가한 이후, Pull Request 이벤트 기반 OIDC 주체(`repo:devdoyen/rogic.io:pull_request`)가 Production IAM 역할의 신뢰 정책에 허용되어 있지 않아 `sts:AssumeRoleWithWebIdentity` 에러와 함께 파이프라인 OIDC 인증이 거부되는 추가적인 장애가 동반되었습니다.

  | 환경 구분 | 영향도 | 리소스 교체 정책 |
  | :--- | :--- | :--- |
  | 단일 인스턴스 (현재 Staging & Production) | 인스턴스 삭제 시 서비스 중단(Downtime) 발생 및 로컬 데이터 소실 | In-place OS 패치가 안전함 (Ignore AMI changes) |
  | 고가용성 환경 (ASG + ALB) | 롤링 배포를 통한 무중단 업데이트 가능 | 신규 AMI 출시 시 인스턴스 재생성이 권장됨 |

## 4. 해결 방안
* **수정 내용**<br>
  Staging 및 Production 환경의 `main.tf` 파일의 `lifecycle` 블록에 `ignore_changes = [ami]`를 추가하여, AMI가 업데이트되더라도 기존 단일 인스턴스를 파괴하고 재생성하지 않도록 예외 처리했습니다.
  원격에 락이 걸려 중단되던 Staging 환경에 대해서는 로컬에서 `terraform force-unlock` 명령어를 통해 명시적으로 락을 해제하여 배포 정상 작동을 보장했습니다.
  추가적으로, 미래에 동일한 배포 차단 문제를 사전에 모니터링하기 위해 [ci-cd.yml](../../.github/workflows/ci-cd.yml#L136-L191) 파일 내 `infra-plan-staging` 및 `infra-plan-production` 작업(Dry-Run)의 실행 조건에 `pull_request` 이벤트를 포함하여 PR 단계에서 Terraform 예외를 사전 감지하도록 보강했습니다.
  이에 따른 OIDC 인증 실패를 해소하고자 Production [main.tf](../../infra/terraform/envs/production/main.tf#L488-L494) OIDC Role Trust Policy 설정에 `repo:devdoyen/rogic.io:pull_request` 주체를 추가함으로써 PR 검사 시에도 안전하게 AWS 자격 증명을 획득할 수 있도록 수정 조치했습니다.

## 5. 재발 방지 대책
* **조치 계획**<br>
  단일 EC2 인스턴스로 운용되는 환경(Staging, Production 등)에서는 Terraform의 `aws_instance` 리소스에 AMI 변경으로 인한 강제 재생성이 일어날 수 있는 속성에 항상 `ignore_changes` 설정을 부여하여 안정성을 확보합니다.
  보안 취약점 패치 및 시스템 커널 업데이트는 Ansible Playbook의 OS 업데이트 모듈을 활용하여 무중단 혹은 유지보수 시간에 점진적으로 배포할 수 있는 내부 자동화 프로세스를 구축합니다.
  * **CI/CD PR 검증 강화**<br>
    리소스 삭제 방지(`prevent_destroy`)와 리소스 강제 재생성이 충돌하는 문제를 배포(Merge) 전에 탐지하기 위해, PR 생성 및 업데이트 시점에 항상 `terraform plan` (Dry-Run) 검증 단계가 강제로 통과되도록 파이프라인의 안전망을 구축하여 운영합니다.
