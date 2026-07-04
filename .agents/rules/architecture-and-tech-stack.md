---
trigger: always_on
---

# [ARCHITECTURE & TECH STACK CONSTRAINTS]

## 1. Directory Structure
- Maintain strict separation of directories within a single repository: frontend/, backend/, infra/, and docs/.
- Never modify files in multiple main directories simultaneously within a single task.

## 2. Frontend
- Build the client application using Vue.js (Vite-based environment preferred).
- The puzzle grid MUST be rendered utilizing the HTML5 <canvas> API. Do NOT use virtual DOM elements (e.g., div, table) for rendering the grid cells.
- Keep the core game engine (array manipulation, hint calculation, validation) as pure script modules, completely decoupled from Vue's reactivity system (Ref, Reactive) or component lifecycles.

## 3. Backend
- Build the REST API server using Java / Spring Boot.

## 4. Infrastructure & IaC
- Target environment is AWS. Enforce strict division of responsibilities:
  - Provisioning (Resource Creation): Managed declaratively via Terraform.
  - Configuration Management & Deployment: Automated via Ansible Playbooks.
- Enforce strict environment isolation and sequential deployment order:
  - You MUST modify and apply infrastructure configurations (Terraform) and configuration management (Ansible) to the Staging environment first.
  - Verification of the Staging changes must be completed before modifying and deploying the Production environment.
  - Do NOT modify or apply Staging and Production infrastructure configurations simultaneously within a single task.