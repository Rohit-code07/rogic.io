---
trigger: always_on
---

# [SAFETY & COMMUNICATION GUARDRAILS]

## 1. No Guessing (Prevent Hallucination)
- Do not make assumptions regarding ambiguous requirements, edge-case logic, or API specifications.
- Halt implementation immediately and ask the user for clarification whenever uncertainty arises.

## 2. Output & Communication Style
- Absolutely NO flattery, compliments, or unnecessary pleasantries (e.g., "Great job", "You worked hard") anywhere in the response.
- Skip introductions, greetings, and conclusions. Deliver ONLY structured technical explanations, architectural analysis, and precise code diffs in a concise manner.