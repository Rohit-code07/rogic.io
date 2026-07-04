# [GIT & COMMIT CONVENTIONS]

## 1. Commit Message Convention
- 커밋 메시지는 Conventional Commits 사상을 기반으로 명확하고 일관된 영문 소문자 prefix(태그)를 사용하여 기술합니다.
- 대표적인 Prefix 목록:
  - `feat`: 신규 기능 구현
  - `fix`: 버그 해결 및 수정
  - `refactor`: 코드 구조 개선 (기능 변화 없음)
  - `docs`: README.md, progress_state.md 등 마크다운 문서 및 주석 변경
  - `infra`: Terraform 구성, Grafana 대시보드 등 인프라 형상 및 모니터링 변경
  - `ci`: GitHub Actions 워크플로우 등 CI/CD 파이프라인 설정 변경
  - `test`: 단위 테스트 및 검증 테스트 케이스 추가/수정
  - `chore`: 빌드 스크립트 수정, 패키지 설정 변경 등 단순 보조 업무
- 형식 예시:
  - `infra: convert SLA dashboard metrics to dynamic global range picker`
  - `ci: add terraform path filter to prevent deployment pipeline skips`

## 2. Issue & Pull Request Workflow Governance
- **작업 브랜치(Branch) 생성 규칙**:
  - 에이전트는 신규 작업 시작 시, `main` 브랜치에서 직접 개발하지 않고 항상 피처 브랜치를 생성하여 작업해야 합니다.
  - 브랜치 네이밍 컨벤션:
    - 이슈 기반 작업 시: `feat/#<issue_number>-<brief_description>` 또는 `fix/#<issue_number>-<brief_description>`
    - 일반 작업 시: `feat/agent-<brief_description>` 또는 `fix/agent-<brief_description>`
- **자동 커밋 및 푸시(Push) 정책**:
  - 에이전트는 기능 개발 또는 인프라 작업 완료 후, 변경된 파일들을 스테이징하고 정의된 컨벤션에 의거해 로컬 커밋(`git commit`)을 수행합니다.
  - 작업 브랜치(`feat/*` 또는 `fix/*`)에 한해서, 에이전트는 원격 저장소(`origin`)로 직접 `git push`를 자동으로 수행하여 변경 내역을 반영할 수 있습니다. (메인 브랜치 `main`으로의 직접 푸시는 엄격히 금지됩니다.)
- **풀 리퀘스트(PR) 생성 및 제출**:
  - 피처 브랜치에 코드를 푸시한 후, 에이전트는 해당 브랜치에서 `main` 브랜치로의 PR 초안(Draft) 또는 PR을 생성하거나, 사용자가 GitHub에서 바로 PR을 열 수 있도록 작성 명세(Title, Description, Checklist)를 제공해야 합니다.
  - 생성된 PR은 사용자의 승인(Approve) 및 머지(Merge)를 거쳐 메인 브랜치와 Staging 환경에 최종 반영됩니다.
- **GitHub CLI를 통한 이슈/PR 등록 및 구현 계획서 연동 자동화**:
  - 사용자 환경에 GitHub CLI(`gh` CLI)가 설치되어 있고 인증 자격 증명이 활성화되어 있는 경우, 에이전트는 기획 단계(Planning Mode)에서 수립한 **구현 계획서(`implementation_plan.md`)**의 내용을 `gh issue create --title "[feat/fix]: <brief_description>" --body-file "implementation_plan.md"` 명령을 통해 원격 GitHub 저장소에 **이슈로 선제 등록**해야 합니다.
  - 이슈 생성 후 반환된 이슈 번호(`#<issue_number>`)를 작업 브랜치 생성(`feat/#<issue_number>-...` 또는 `fix/#<issue_number>-...`)에 활용합니다.
  - 작업 완료 시 PR 생성 역시 `gh pr create` 명령을 활용하여 초안(Draft) 또는 일반 PR을 자동 생성하고 이슈 번호와 연동시킵니다.
