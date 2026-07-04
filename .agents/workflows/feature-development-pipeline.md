---
description: WORKFLOW: FEATURE DEVELOPMENT PIPELINE
---

## Trigger Conditions
- Activating when the user requests a new feature, logic implementation, or bug fix.

## Execution Steps
1. **Context Assessment:**
   - Read `docs/progress_state.md` and `docs/api_spec.md` to check current dependencies before writing any code.
2. **Harness Generation (TDD):**
   - Identify the target file and create/modify the corresponding unit test file first.
   - Run the test suite to ensure the baseline fails or remains consistent.
3. **Implementation:**
   - Write the minimal implementation code in `frontend/` or `backend/` to satisfy the newly written test cases.
   - Run the tests again to ensure a 100% pass rate.
4. **State Synchronization:**
   - Update `docs/progress_state.md` with the completed items, current status, and next technical steps.
   - Present the final diff and test results concisely to the user.