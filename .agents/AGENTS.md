# Workspace Customization Rules (AGENTS.md)

This file consolidates all workspace-specific rules and conventions to ensure they are parsed and loaded by the agent.

---

## 1. Architecture & Tech Stack Constraints
Ref: [architecture-and-tech-stack.md](./rules/architecture-and-tech-stack.md)

### Directory Structure
- Maintain strict separation of directories within a single repository: `frontend/`, `backend/`, `infra/`, and `docs/`.
- Never modify files in multiple main directories simultaneously within a single task.

### Frontend
- Build the client application using Vue.js (Vite-based environment preferred).
- The puzzle grid MUST be rendered utilizing the HTML5 `<canvas>` API. Do NOT use virtual DOM elements (e.g., `div`, `table`) for rendering the grid cells.
- Keep the core game engine (array manipulation, hint calculation, validation) as pure script modules, completely decoupled from Vue's reactivity system (`Ref`, `Reactive`) or component lifecycles.

### Backend
- Build the REST API server using Java / Spring Boot.

### Infrastructure & IaC
- Target environment is AWS. Enforce strict division of responsibilities:
  - Provisioning (Resource Creation): Managed declaratively via Terraform.
  - Configuration Management & Deployment: Automated via Ansible Playbooks.
- Enforce strict environment isolation and sequential deployment order:
  - You MUST modify and apply infrastructure configurations (Terraform) and configuration management (Ansible) to the Staging environment first.
  - Verification of the Staging changes must be completed before modifying and deploying the Production environment.
  - Do NOT modify or apply Staging and Production infrastructure configurations simultaneously within a single task.

---

## 2. Safety & Communication Guardrails
Ref: [safety-and-communication.md](./rules/safety-and-communication.md)

### No Guessing (Prevent Hallucination)
- Do not make assumptions regarding ambiguous requirements, edge-case logic, or API specifications.
- Halt implementation immediately and ask the user for clarification whenever uncertainty arises.

### Output & Communication Style
- Absolutely NO flattery, compliments, or unnecessary pleasantries (e.g., "Great job", "You worked hard") anywhere in the response.
- Skip introductions, greetings, and conclusions. Deliver ONLY structured technical explanations, architectural analysis, and precise code diffs in a concise manner.

---

## 3. Workflow & Test-Driven Development
Ref: [workflow-and-tdd.md](./rules/workflow-and-tdd.md)

### Mandatory TDD (Test-Driven Development)
- Before implementing any core business logic (e.g., Validator, HintCalculator), you MUST write the corresponding unit test cases first.
- Implementation code is only acceptable if it perfectly passes the evaluation harness (tests) you have established.

### Progress State Synchronization
- Upon finishing any task or modifying code, you MUST read and update the `docs/progress_state.md` file to log the current progress and define the next goals.
- Ensure the latest development context is thoroughly documented to maintain contextual consistency.

---

## 4. Git & Commit Conventions
Ref: [git-and-commit-guidelines.md](./rules/git-and-commit-guidelines.md)

### Commit Message Convention
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

### Issue & Pull Request Workflow Governance
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
- **GitHub CLI를 통한 이슈/PR 등록 자동화**:
  - 사용자 환경에 GitHub CLI(`gh` CLI)가 설치되어 있고 인증 자격 증명이 활성화되어 있는 경우, 에이전트는 이슈 생성 요구 시 로컬 파일 작성에 그치지 않고 `gh issue create` 명령을 통해 실제 GitHub 원격 리포지토리에 이슈를 자동으로 등록해야 합니다. PR의 경우에도 동일하게 `gh pr create` 명령을 적극적으로 활용하여 초안(Draft) 또는 일반 PR을 자동 생성해야 합니다.

---

## 5. Documentation & Formatting Guidelines
Ref: [documentation-guidelines.md](./rules/documentation-guidelines.md)

### Markdown Formatting for Technical Context & Troubleshooting
- 마크다운 문서(예: `README.md`, `docs/` 내의 가이드 문서)에서 상세 기술 내역이나 트러블슈팅 사례를 기술할 때, 볼드 태그 소제목(예: `**배경**`, `**해결 방안**`, `**개발자 회고**`) 뒤에 콜론(`:`)을 붙이고 한 줄로 내용을 이어서 나열하지 않습니다.
- 마크다운의 파서에 따른 줄바꿈 병합 현상을 방지하고 시각적인 가독성을 보장하기 위해, 각 소제목 뒤에는 반드시 **`<br>` 태그**를 붙여 개행을 명시한 뒤 아랫줄에 들여쓰기를 적용하여 본문 내용을 기술해야 합니다.

### Relative Path Specification for File Links
- 프로젝트 내부 문서나 소스 코드 내에서 리포지토리의 다른 파일들을 마크다운 링크로 참조할 때, 로컬 개발 환경에 의존적인 절대 경로(예: `file:///c:/Users/...` 또는 `file:///` 스키마)를 절대 사용하지 않습니다.
- 깃허브(GitHub) 등 원격 플랫폼 상에서 웹뷰 상호 링크 호환과 다른 개발자 환경으로의 이식성을 완전 보장하기 위해 반드시 **리포지토리 루트 기준의 상대 경로(Relative Path, 예: `./docs/incidents/` 또는 `../.agents/rules/`)**로 참조 링크를 지정해야 합니다.

### Data Representation for Numerical & Performance Comparison
- 비용(Billing), 시스템 사양, 벤치마크 지표, 처리량(Throughput) 등 기존 아키텍처 대비 개선 사항을 대조하여 설득력을 제시해야 하는 수치형 데이터는 괄호 및 문장을 이용한 일반 나열식 포맷을 지양합니다.
- 독자가 최적화 전후의 리소스와 비용 증감을 명확하고 직관적으로 인지할 수 있도록 마크다운 표(Table) 형식의 비교 테이블을 작성하여 구조화해야 합니다.
- 비교 테이블 내 기존 구성의 비용이나 수치를 측정하지 않은 경우, 의미가 불명확한 `-` 기호 대신 반드시 `N/A` 또는 `미측정`과 같은 명시적인 표기를 사용하고, 필요 시 각주(footnote)로 이유를 보충 기술합니다.

### Heading Concision & Table of Contents Omission
- 마크다운 문서 내의 모든 대제목/소제목(Heading)을 설계할 때 다중 명사를 엔드 기호(`&`)로 길게 열거하여 가독성을 저해하는 장황한 형태를 지양합니다. 핵심을 관통하는 명확하고 간결한 단일 명사나 약어로 대표화하거나, 분할이 필요한 경우 계층적 하위 캡슐화(Subheading)를 수행하여 직관성을 극대화합니다.
- GitHub 마크다운 렌더러가 문서 제목들을 기반으로 아웃라인 목차(TOC)를 자동 파싱하여 제공하므로, `README.md` 등 주요 문서 상단에 불필요하고 유지보수가 번거로운 수동 링크식 목차 구문을 명시하지 않고 완전히 제거하여 관리합니다.

### Heading Depth Limit & Flat Bold List
- 마크다운 헤딩 계층은 **최대 H4(`####`)까지만** 허용합니다. H5(`#####`) 이상의 깊이는 앵커 링크 없이는 탐색 자체가 불가능하여 독자 경험을 심각하게 저해합니다.
- H5 이상이 필요한 세부 항목은 반드시 볼드 리스트(`* **항목명**<br>`) 형식으로 평탄화하여 본문 내에 인라인으로 배치합니다. 이 방식은 TOC 추적이 필요 없는 세부 명세에 적합하며, 구조적 가독성을 유지합니다.

### Content Deduplication & Cross-Reference
- 동일한 기술 내용(예: GraalVM 메모리 최적화, Agentless Pull 구조, ALB 제거 근거)이 문서 내 여러 섹션에 분산 배치되는 중복 서술을 엄격히 금지합니다.
- 핵심 설명은 가장 적합한 섹션에 **단 한 곳**에만 서술하고, 나머지 위치에서는 반드시 마크다운 앵커 링크(예: `[1.3.1. Build Resource Constraints](#131-build-resource-constraints)`)를 통해 해당 섹션으로 참조 유도합니다.

### Heading Language Consistency
- 하나의 문서 내에서 헤딩(Heading) 언어는 영어 또는 한국어 중 하나로 일관되게 통일합니다.
- 기술 문서(예: `README.md`)에서는 영어 헤딩을 기본으로 채택하며, 한국어 본문과 혼용하는 경우 헤딩만큼은 영어로 단일화합니다. 한국어 헤딩 혼입은 탐색 일관성을 무너뜨리고 앵커 링크 참조 오류를 유발할 수 있습니다.

---

## 6. Incident Reporting Guidelines
Ref: [incident-reporting.md](./rules/incident-reporting.md)

### Trigger
Whenever a critical server error, production/stage deployment failure, database migration exception, or major system outage occurs and is resolved during developer operations.

### Instruction
You MUST generate a Postmortem (Incident Report) document inside the `docs/incidents/` directory.

### Filename Format
`YYYYMMDD_short_description.md` (e.g., `20260629_flyway_migration_failure.md`)

### Required Document Sections
1. **개요 (Summary):** Incident start/end time, total duration, scope of impact, and high-level cause.
2. **장애 타임라인 (Timeline):** Chronological sequence of events (detection, analysis, mitigation, validation, resolution) with timestamps.
3. **원인 분석 (Root Cause Analysis):** Detailed technical breakdown of why the failure occurred, including code snippets or SQL errors.
4. **해결 방안 (Resolution):** Description of the fix applied to resolve the incident, including diffs or query modifications.
5. **재발 방지 대책 (Preventative Actions):** Actionable steps to prevent similar failures in the future (architecture changes, validation procedures, testing strategy).
