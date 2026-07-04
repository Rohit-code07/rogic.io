---
trigger: always_on
---

# [WORKFLOW & TEST-DRIVEN DEVELOPMENT]

## 1. Mandatory TDD (Test-Driven Development)
- Before implementing any core business logic (e.g., Validator, HintCalculator), you MUST write the corresponding unit test cases first.
- Implementation code is only acceptable if it perfectly passes the evaluation harness (tests) you have established.

## 2. Progress Context Synchronization via Git Logs
- 에이전트는 기획 진행 상태 수집을 위해 `docs/progress_state.md` 파일을 작성 및 갱신하지 않으며, 해당 파일은 프로젝트에서 완전히 제거됩니다.
- **Git 로그 기반 최근 맥락 분석**: 에이전트는 세션 시작(초기화) 시점에 항상 `git log -n 15 --oneline` 명령어를 실행하여 최근 머지된 기능, 작업 순서, 수정 이력 등의 최신 개발 진행 맥락을 반드시 스스로 분석하고 인지해야 합니다.
- **Git 히스토리 및 PR 기반 이력 추적**: 모든 작업 이력 추적 및 작업 상태 관리는 Conventional Commits 규칙이 적용된 Git 커밋 로그 및 GitHub Pull Request 내용(제목, 본문, 체크리스트)에만 100% 의존하여 관리합니다.