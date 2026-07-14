# 스테이징 서버 AMI 업데이트로 인한 배포 실패 보고서

## 1. 개요
* **장애 발생 시각**<br>
  2026년 7월 14일 21시 59분
* **장애 복구 시각**<br>
  2026년 7월 14일 22시 15분 (약 16분 소요)
* **장애 영향 범위**<br>
  Staging 환경 인프라 배포 (CI/CD 파이프라인 중단)
* **장애 원인 요약**<br>
  AWS Canonical의 Ubuntu 최신 AMI 릴리즈로 인해 데이터 소스가 업데이트되어 Staging EC2 인스턴스의 강제 재생성(Destroy & Recreate)이 시도되었으나, `prevent_destroy = true` 설정과의 충돌로 인해 배포 파이프라인이 실패함.

## 2. 장애 타임라인
* **2026-07-14 21:59**<br>
  Staging CI/CD 파이프라인에서 Terraform 실행 도중 `Error: Instance cannot be destroyed` 에러와 함께 빌드 실패 감지.
* **2026-07-14 22:01**<br>
  [main.tf](../../infra/terraform/envs/staging/main.tf)의 `aws_instance.nemologic_staging_server` 리소스에 적용된 `prevent_destroy = true`와 AMI 변경으로 인한 인스턴스 강제 재생성 계획 간의 충돌 원인 분석 완료.
* **2026-07-14 22:12**<br>
  해결 방안 수립 및 계획 승인 완료.
* **2026-07-14 22:13**<br>
  `ignore_changes = [ami]` 1차 코드 적용.
* **2026-07-14 22:18**<br>
  파이프라인 실행 중 `terraform fmt` 정렬 위반(`exit code 3`)으로 인한 빌드 실패 추가 감지 및 `terraform fmt` 실행을 통한 스타일 수정 완료.
* **2026-07-14 22:19**<br>
  최종 수정본 원격 반영 완료.

## 3. 원인 분석
* **상세 분석**<br>
  AWS Staging 환경에서 사용하는 Ubuntu 22.04 LTS AMI는 [main.tf](../../infra/terraform/envs/staging/main.tf#L113-L124)에서 `most_recent = true` 옵션을 설정하여 `data.aws_ami.ubuntu`를 통해 동적으로 최신 이미지를 탐색하도록 설정되어 있었습니다.
  AWS 측에서 새로운 패치 버전의 AMI를 릴리즈함에 따라 `data.aws_ami.ubuntu.id` 값이 변경되었고, 이에 따라 Terraform은 인스턴스 재생성을 계획했습니다.
  하지만 인스턴스에는 예상치 못한 삭제를 방지하기 위해 `prevent_destroy = true` 설정이 추가되어 있었고, 리소스를 파괴하지 못해 배포가 중단되었습니다.

  | 환경 구분 | 영향도 | 리소스 교체 정책 |
  | :--- | :--- | :--- |
  | 단일 인스턴스 (현재 Staging) | 인스턴스 삭제 시 서비스 중단(Downtime) 발생 및 로컬 데이터 소실 | In-place OS 패치가 안전함 (Ignore AMI changes) |
  | 고가용성 환경 (ASG + ALB) | 롤링 배포를 통한 무중단 업데이트 가능 | 신규 AMI 출시 시 인스턴스 재생성이 권장됨 |

## 4. 해결 방안
* **수정 내용**<br>
  [main.tf](../../infra/terraform/envs/staging/main.tf#L237-L243) 파일의 `lifecycle` 블록에 `ignore_changes = [ami]`를 추가하여, AMI가 업데이트되더라도 기존 단일 인스턴스를 파괴하고 재생성하지 않도록 예외 처리했습니다.
  보안 패치 등은 인스턴스를 재생성하지 않고 OS 내부의 패키지 관리자(`apt`) 또는 Ansible 플레이북을 활용해 점진적으로 업데이트하도록 우회했습니다.

## 5. 재발 방지 대책
* **조치 계획**<br>
  단일 EC2 인스턴스로 운용되는 환경(Staging 등)에서는 Terraform의 `aws_instance` 리소스에 AMI 변경으로 인한 강제 재생성이 일어날 수 있는 속성에 항상 `ignore_changes` 설정을 부여하여 안정성을 확보합니다.
  보안 취약점 패치 및 시스템 커널 업데이트는 Ansible Playbook의 OS 업데이트 모듈을 활용하여 무중단 혹은 유지보수 시간에 점진적으로 배포할 수 있는 내부 자동화 프로세스를 구축합니다.
