---
trigger: always_on
---

# [WORKFLOW & TEST-DRIVEN DEVELOPMENT]

## 1. Mandatory TDD (Test-Driven Development)
- Before implementing any core business logic (e.g., Validator, HintCalculator), you MUST write the corresponding unit test cases first.
- Implementation code is only acceptable if it perfectly passes the evaluation harness (tests) you have established.

## 2. Progress State Synchronization
- Upon finishing any task or modifying code, you MUST read and update the `docs/progress_state.md` file to log the current progress and define the next goals.
- Ensure the latest development context is thoroughly documented to maintain contextual consistency.