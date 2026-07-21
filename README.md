# 0. rogic.io

**rogic.io** is an intelligent web-based puzzle game that automatically generates and serves daily logic puzzles using Vue 3, Spring Boot, and AI (Gemini). The project is built with a strong emphasis on performance optimization, robust infrastructure architecture, and engineering best practices.

![License](https://img.shields.io/badge/license-MIT-blue.svg)
![Vue](https://img.shields.io/badge/vue-3.x-4FC08D.svg)
![Spring Boot](https://img.shields.io/badge/spring_boot-3.x-6DB33F.svg)
![GraalVM](https://img.shields.io/badge/GraalVM-Native_Image-FF813F.svg)

## 0.1. Engineering Constraints & Principles
#### Architecture Principles
<p align="center">
  <img src="./docs/assets/engineering_principles.png" width="100%" alt="rogic.io Engineering Principles & Constraints" />
</p>

## 0.2. Game Concept
#### Core Game Differentiators
<p align="center">
  <img src="./docs/assets/game_differentiators.png" width="100%" alt="rogic.io Core Game Differentiators" />
</p>

#### Gameplay Video Demonstration
<p align="center">
  <video src="./docs/assets/rogic_gameplay_intro.mp4" width="100%" autoplay loop muted playsinline></video>
</p>

## 0.3. Contributing
We welcome contributions from the community! Whether it's a bug fix, new feature, or documentation update, your help is highly appreciated.
Please read our [Contributing Guidelines](./CONTRIBUTING.md) to get started with your local development environment, testing, and pull request workflow. By participating in this project, you agree to abide by our [Code of Conduct](./CODE_OF_CONDUCT.md).

---

# 1. Infrastructure

## 1.1. System Architecture
```mermaid
C4Context
    title System Context Diagram for rogic.io (Level 1: System Context)

    Person(player, "Player / User", "Accesses the puzzle game through a web browser.")
    
    System_Boundary(dns_cdn, "Global Edge Delivery") {
        System_Ext(route53, "Route 53", "DNS management mapping domains to CloudFront & EC2.")
        System_Ext(cloudfront, "Amazon CloudFront", "CDN distributing static web assets globally.")
        System(s3, "Amazon S3 Bucket", "Stores Vite-built Vue static compilation files.")
    }

    System_Boundary(identity_provider, "Identity & Access Management") {
        System_Ext(cognito, "AWS Cognito User Pool", "Managed identity pool handling Federated Sign-In & token issuing.")
        System_Ext(google_oauth, "Google Identity Provider", "Handles third-party authentication via Google accounts.")
    }

    System_Boundary(backend, "Core API Server") {
        System(api, "rogic.io REST API (EC2)", "Spring Boot backend verifying JWT signatures and exposing secured game services.")
        SystemDb(postgres, "PostgreSQL DB", "Relational database storing user profiles, history logs, and stage metadata.")
    }

    Rel(player, route53, "Queries DNS for rogic.io / api.rogic.io", "DNS Protocol")
    Rel(player, cloudfront, "Requests static assets", "HTTPS / Port 443")
    Rel(cloudfront, s3, "Pulls origin static files", "S3 Protocol")
    Rel(player, cognito, "Initiates federated login & exchanges codes via PKCE", "HTTPS / Port 443")
    Rel(cognito, google_oauth, "Delegates user identity lookup", "OIDC / OAuth 2.0")
    Rel(player, api, "Calls REST API services with ID Token JWT", "HTTPS / Port 443")
    Rel(api, cognito, "Downloads JSON Web Key Sets (JWKS) to verify JWT signature", "HTTPS / Port 443")
    Rel(api, postgres, "Reads/Writes game state", "JDBC & JPA / Port 5432")
```
## 1.2. Component Specifications
### 1.2.1. Compute
#### Instance Type
AWS EC2 `t3a.nano` (1 vCPU, 0.5 GiB Memory)

#### Runtime Engine
Docker / Docker Compose

#### Application Stack
Spring Boot (Compiled with GraalVM Native Image for minimal footprint, target memory consumption constrained to 30MB)

### 1.2.2. Network & Traffic
#### DNS Resolution
Route 53 Hosted Zone (Domain A record mapping)

#### IPv4 Address
AWS Elastic IP (EIP) statically allocated

#### Reverse Proxy
Docker Nginx Proxy (80/443 SSL termination and backend request forwarding)

### 1.2.3. Storage & CDN
#### Static Content
Amazon S3 (Hosting Vite/Vue static build artifacts)

#### Content Delivery Network
Amazon CloudFront (Secured with Origin Access Control (OAC))

#### Persistent Volume
AWS EBS gp3 (10 GiB storage volume)

### 1.2.4. Database
#### Database Engine
PostgreSQL 16 (Docker Container)

#### Backup Storage
S3 Backup Bucket (Database snapshots dumped periodically every 3 hours)

### 1.2.5. Staging Environment
#### Instance Lifecycle
On-Demand setup (Automatically starts during deployments and E2E test runs)

#### Resource Cleanup
Automated nightly stop workflow based on Cron schedule (`staging-cleanup.yml`)

## 1.3. Observability

### 1.3.1. Metrics & Telemetry
```mermaid
C4Container
    title Telemetry Diagram for rogic.io (Level 3: Observability & Alerting)

    System_Boundary(host, "AWS EC2 Instance (Target Host)") {
        Container(nginx, "Nginx Reverse Proxy", "Docker", "Bearer Token Authentication Endpoint.")
        Container(spring, "Spring Boot Backend", "Docker (GraalVM)", "Exposes Prometheus Actuator Metrics.")
        Container(node_exporter, "Node Exporter", "Docker", "Exposes host hardware and OS metrics.")
        Rel(nginx, spring, "Forwards application scraping requests", "Port 8080")
        Rel(nginx, node_exporter, "Forwards host scraping requests", "Port 9100")
    }

    System_Boundary(grafana_cloud, "Grafana Cloud Platform") {
        Container(grafana, "Grafana Dashboards", "SaaS Dashboard", "Visualizes SLA metrics, CPU, Memory, and log groups.")
        Container(prometheus, "Prometheus / Mimir", "SaaS TSDB", "Scrapes metrics via Agentless Pull architecture.")
        Rel(grafana, prometheus, "Queries metrics data")
    }

    System_Boundary(observability, "AWS Management & Alerting") {
        Container(cw, "Amazon CloudWatch", "AWS Logging", "Collects application stdout log streams via awslogs driver.")
        Container(sns, "AWS SNS Topic", "AWS Alerting", "Triggers notifications based on metric filter threshold alarms.")
        Person(sre, "SRE Developer", "Receives real-time incident warning emails.")
        
        Rel(cw, sns, "Metric Filter Threshold Alarmed")
        Rel(sns, sre, "Sends warning email notification")
    }

    Rel(prometheus, nginx, "Scrapes metrics (Agentless Pull)", "HTTPS Bearer Auth / Port 443")
    Rel(spring, cw, "Streams application logs", "awslogs driver")
```

#### Collection Architecture
Agentless Pull (Scrapes metrics directly from Spring Actuator and Node Exporter via Nginx proxy, omitting the Grafana Agent daemon)

#### Access Security
Bearer Token verification and virtual path mapping enforced at the Nginx reverse proxy layer

#### Performance Overhead
Host resource overhead (CPU/Memory) converges close to 0%

### 1.3.2. Log Aggregation & Storage
#### Shipping Driver
`awslogs` Docker Logging Driver (aggregates live stdout console streams)

#### Storage Target
Amazon CloudWatch Logs (eliminating disk usage on the host)

#### Log Filtration
Access logging is turned off for Nginx routes `/actuator/*` and `/node-metrics`

### 1.3.3. Alerting & SLO Visualization
#### Synthetic Probes
Grafana Cloud Synthetic Monitoring (multi-region health checks across Singapore, Sydney, and Tokyo)

#### Incident Alarm
CloudWatch Logs Metric Filter alarms immediately push real-time alerts through AWS SNS to email channels

#### SLO Dashboard
Grafana API-integrated Dashboard (visualizes Availability SLA, Incidents, MTTR, MTBF KPIs. Public link: [Grafana Live Public Dashboard](https://grandwalrus3189.grafana.net/public-dashboards/ec9e06b0d1ea4540b97af6b56abb1380). Detailed PromQL specifications are documented in [docs/appendices.md](./docs/appendices.md#2-promql-query-formulations-slo-metrics))

---

## 1.4. Disaster Recovery

### 1.4.1. DR Recovery Flow
```mermaid
stateDiagram-v2
    state "Normal Operation" as Normal
    state "Hardware / Instance Failure" as HardFail
    state "Data / Volume Corruption" as DataFail

    state "AWS Auto Recovery" as AutoRec {
        [*] --> Detect : Status Check Failed (1 min)
        Detect --> TerminateAndStart : Trigger CloudWatch Alarm
        TerminateAndStart --> CompleteAutoRec : Re-attach EBS & Re-bind EIP
    }

    state "GitHub Actions DR Restore" as ManualRec {
        [*] --> TriggerWorkflow : Dispatch db-restore.yml
        TriggerWorkflow --> FetchS3 : Pull latest pg_dump from S3
        FetchS3 --> DockerRestore : Exec pg_restore & Restart Stack
    }

    [*] --> Normal
    Normal --> HardFail : Host Hardware Crash
    Normal --> DataFail : DB dropped / Volume Corrupted

    HardFail --> AutoRec : Trigger Alarm
    AutoRec --> Normal : Complete Auto Recovery (RTO: 1~2 min)

    DataFail --> ManualRec : Run Restore Pipeline
    ManualRec --> Normal : Complete DB Restoration (RTO: 37~360 sec)
```

#### Auto Recovery RTO
1 to 2 minutes (CloudWatch Alarm monitors Status Check Failures and triggers immediate host physical recovery)

#### Manual Recovery RTO
37 to 360 seconds (utilizes GitHub Actions `db-restore.yml` one-click restore pipeline and SSM SendCommand automation)

### 1.4.2. Storage & Backup Design
#### Persistent Volume
AWS EBS gp3 (10 GiB volume detached from OS lifecycle, guarded with `prevent_destroy` to prevent accidental loss)

#### Volume Binding
Host Bind Mount (`/opt/nemologic/db_data` path prevents Docker named volume lifecycle data loss)

#### Database Backup
Automated database snapshot dump using `pg_dump` scheduled every 3 hours and pushed to a remote, isolated S3 backup bucket

#### Lifecycle Policy
S3 Lifecycle rules automatically delete backups older than 30 days

---

# 2. Security

## 2.1. Identity & Access Management
### 2.1.1. Host Access Control
#### Session Manager
Connections established via Systems Manager Session Manager (Inbound port 22 SSH completely disabled on the host)

#### Ansible SSM Tunnel
SSH ProxyCommand encapsulated via `aws ssm start-session` mapped with local PEM file verification (detailed setups in [docs/appendices.md](./docs/appendices.md#13-aws-ssm-session-manager-setup))

### 2.1.2. Pipeline Authentication
#### OIDC Keyless Auth
GitHub Actions federates with AWS OIDC to assume temporary STS role credentials, completely eliminating persistent static Secret Keys

#### Least Privilege Policy
Distinct Custom IAM Policies allocated to Staging and Production to isolate and block access to unauthorized AWS services

### 2.1.3. IAM Least Privilege Design

```mermaid
C4Component
    title Component Diagram for Identity & Access Management (Level 3: Security & IAM)

    Container(runner, "GitHub Actions Runner", "GitHub Cloud", "Deploys infra/app using temporal credentials.")
    Container(ec2, "EC2 App Server", "AWS EC2", "Runs application stack and background helpers.")

    System_Boundary(iam, "AWS IAM (Identity & Access Management)") {
        Component(oidc, "OIDC Provider", "token.actions.githubusercontent.com", "Verifies GitHub Actions runner token.")
        Component(run_role, "CI/CD Runner IAM Role", "IAM Role", "Assumed via OIDC federation.")
        Component(host_role, "EC2 Host IAM Role", "IAM Role (Instance Profile)", "Attached to EC2 hosting profile.")
        
        Component(tf_policy, "Terraform & Deploy Policy", "Customer Managed Policy", "Allows EC2, VPC, S3, DynamoDB, Route 53, CloudFront management.")
        Component(ssm_policy, "SSM Managed Policy", "AWS Managed Policy", "Allows SSM Systems Manager connectivity.")
        Component(cw_policy, "CloudWatch Log Policy", "Customer Managed Policy", "Allows log groups/streams push operations.")
        Component(s3_back_policy, "S3 Backup Write Policy", "Customer Managed Policy", "Allows database dump upload.")
    }

    System_Boundary(aws_resources, "AWS Resources Boundary") {
        System(s3_tf, "S3 tfstate & deploy Bucket", "Object Storage")
        System(ddb_lock, "DynamoDB tfstate lock Table", "NoSQL Database")
        System(cf_cdn, "CloudFront CDN / Route 53", "Edge Routing")
        System(cw_logs, "CloudWatch Logs", "Telemetry Store")
        System(s3_back, "S3 Backup Bucket", "Object Storage")
    }

    Rel(runner, oidc, "1. Authenticates", "OIDC Web Identity Token")
    Rel(oidc, run_role, "2. Issues short-term session", "AssumeRoleWithWebIdentity")
    Rel(run_role, tf_policy, "3. Binds permissions")
    
    Rel_D(tf_policy, ec2, "Manage VPC & Host", "AWS API")
    Rel_D(tf_policy, s3_tf, "Read/Write tfstate & deploy site", "AWS API")
    Rel_D(tf_policy, ddb_lock, "Acquire/Release Lock", "AWS API")
    Rel_D(tf_policy, cf_cdn, "Invalidate cache / Update DNS", "AWS API")

    Rel(ec2, host_role, "4. Obtains profile context", "Instance Metadata Service (IMDS)")
    Rel(host_role, ssm_policy, "5. Binds permissions")
    Rel(host_role, cw_policy, "5. Binds permissions")
    Rel(host_role, s3_back_policy, "5. Binds permissions")

    Rel_D(ssm_policy, ec2, "Establish secure tunnel", "SSM Tunnel")
    Rel_D(cw_policy, cw_logs, "Push application stdout", "CloudWatch API")
    Rel_D(s3_back_policy, s3_back, "Upload daily DB dump", "S3 API")
```

| Principal | Auth Type | Linked IAM Policies & Permissions | Key Role |
| :--- | :--- | :--- | :--- |
| **EC2 Host Role** | Instance Profile | `AmazonSSMManagedInstanceCore`<br>Staging: `CloudWatchAgentServerPolicy` (Managed)<br>Production: `nemologic-cloudwatch-log-policy` (Custom)<br>`s3_backup_policy` (Custom) | Activates SSM secure tunneling, forwards application logs to CloudWatch (differentiated policy limits between Staging and Production), controls S3 upload permissions for backups. |
| **CI/CD Runner (GitHub)** | AWS OIDC (Keyless) | `nemologic-staging-github-policy`<br>`nemologic-production-github-policy` (Custom) | Acquires one-time short-term credentials via `sts:AssumeRoleWithWebIdentity` to perform Terraform modifications and deployments without static credentials. |

### 2.1.4. User Authentication & Authorization
* **OAuth 2.0 PKCE Flow**: Employs cryptographic validation (Code Verifier & Challenge) via Hosted UI redirection to defend against authorization code interception.
* **Stateless JWT Security**: Backend utilizes Spring Security stateless validation. JWT signatures (`RS256`) are verified dynamically using AWS Cognito JWKS URI public keys.
* **Environment Redirection**: Dynamically resolves callback/sign-out URLs matching `window.location.origin` for seamless multi-environment Cognito Client integration.
* **Token Lifetime**: Restricts Access/ID Token lifetime to 5 minutes, paired with a 30-day Refresh Token limit.
* **Token Rotation**: Enforces Refresh Token rotation, invalidating and replacing the active refresh token upon every renewal query (one-time use configuration).
* **Token Revocation**: Integrates user logout triggers with Cognito Revocation endpoints to invalidate current sessions.
* **Solve Verification**: Verifies puzzle clears at the backend layer (`/api/stages/{id}/verify`), matching completion metrics before issuing an HMAC-SHA256 encrypted `proofToken` signature.
* **Tamper-proof Migration**: Verifies the guest history signature (`proofToken`) at login redirection to prevent arbitrary profile alteration or fraudulent XP acquisition.
* **Symmetric Key Cryptography**: Employs symmetric key signatures for guest clear validation to minimize CPU footprint (under 0.1ms computation latency per token check).

---

## 2.2. Infrastructure Protection
```mermaid
C4Container
    title Container Diagram for rogic.io (Level 2: Network & Containers)

    Person(player, "Player / User", "Accesses the puzzle game through a web browser.")
    Person(sre, "SRE / QA (CI/CD)", "Deploys and tests the staging application.")

    System_Boundary(aws, "AWS Cloud (ap-northeast-2)") {
        
        System_Boundary(vpc_prod, "Production VPC (10.0.0.0/16)") {
            System_Boundary(fnet_prod, "frontend-net (Docker Bridge)") {
                Container(nginx, "Nginx Reverse Proxy", "Docker Container", "SSL/TLS termination (Non-root / Port 8443), API routing, and Bearer token auth validation.")
            }
            
            System_Boundary(bnet_prod, "backend-net (Docker Bridge)") {
                ContainerDb(postgres, "PostgreSQL Database", "Docker Container", "Persists puzzle templates, user logs, clear history, and user stats.")
            }
            
            Container(spring, "Spring Boot App", "Docker Container (GraalVM) [frontend-net & backend-net]", "Handles business logic, daily puzzle scheduling, rating, and XP leaderboard.")
            
            Rel(nginx, spring, "Proxy API requests", "HTTP / Port 8080 [frontend-net]")
            Rel(spring, postgres, "Reads/Writes state", "JPA & JDBC / Port 5432 [backend-net]")
        }
        
        System_Boundary(vpc_stage, "Staging VPC (10.1.0.0/16)") {
            System_Boundary(fnet_stg, "frontend-net (Stage Bridge)") {
                Container(nginx_stg, "Nginx Reverse Proxy (Stage)", "Docker Container", "Staging SSL/TLS termination (Non-root / Port 8443) and API routing.")
            }
            
            System_Boundary(bnet_stg, "backend-net (Stage Bridge)") {
                ContainerDb(postgres_stg, "PostgreSQL Database (Stage)", "Docker Container", "Persists isolated staging state.")
            }
            
            Container(spring_stg, "Spring Boot App (Stage)", "Docker Container (JVM) [frontend-net & backend-net]", "Staging application runtime environment.")
            
            Rel(nginx_stg, spring_stg, "Proxy API requests", "HTTP / Port 8080 [frontend-net]")
            Rel(spring_stg, postgres_stg, "Reads/Writes state", "JPA & JDBC / Port 5432 [backend-net]")
        }

        Container(cloudfront, "Amazon CloudFront", "AWS CDN", "Distributes static web assets with low latency.")
        Container(s3, "Amazon S3", "AWS Bucket Storage", "Hosts Vite/Vue built static files (HTML, JS, CSS).")
    }

    Rel(player, cloudfront, "Fetches static web pages", "HTTPS / Port 443")
    Rel(cloudfront, s3, "Refreshes cache from origin", "S3 Protocol")
    Rel(player, nginx, "Calls API endpoints", "HTTPS / Port 443 (Forwarded to 8443)")
    Rel(sre, nginx_stg, "Calls API endpoints (Stage) during tests", "HTTPS / Port 443 (Forwarded to 8443)")
```

### 2.2.1. Network & Host Security
#### VPC Isolation
Blocks cross-environment network access by partitioning configurations into distinct subnet boundaries for Staging VPC (`10.1.0.0/16`) and Production VPC (`10.0.0.0/16`).

#### Port Restriction
Nginx exposes ports 80 and 443 globally, while inbound ports for SSH (22), API (8080), and dev tools remain locked to external traffic.

#### Scraping Proxy
Excludes direct access to Actuator endpoints. Prometheus scraper queries must pass token verification at the Nginx reverse proxy layer before loopback delivery to Port 8080.

### 2.2.2. Container Security
#### Network Partitioning
Isolates multi-tier components utilizing separate Docker bridge subnets: `frontend-net` (Nginx/API proxying) and `backend-net` (API/Database state operations).

#### Database Isolation
Configures the `backend-net` bridge with `internal: true` to prevent container outbound access to the public internet.

#### Non-root Execution
Employs `nginx-unprivileged:alpine` image configurations to force web server execution under a non-root account profile (UID 101).

#### Read-Only rootfs
Applies `read_only: true` on containers, isolating writable operations to temp memory mounts (`tmpfs` bound to `/tmp`).

#### Safe Backup
Encapsulates database snapshots via standardized stdout streams (`docker exec pg_dump`), preventing database credentials from leaking in backup script logs.

### 2.2.3. Security Group Configuration
#### Ingress Control
Minimizes inbound boundary open ports (only 80 and 443 are exposed).

| Port | Protocol | Source | Purpose / Service |
| :---: | :---: | :---: | :--- |
| 80 | TCP | `0.0.0.0/0` | HTTP Web server (redirects traffic to HTTPS 443) |
| 443 | TCP | `0.0.0.0/0` | Secure HTTPS Web Services, REST APIs, and scraper collection routing |

#### Egress Control
Controls outbound paths for OS upgrades and S3 updates.

| Port | Protocol | Destination | Purpose / Service |
| :---: | :---: | :---: | :--- |
| All | All | `0.0.0.0/0` | Package updates, external API requests, and DB backup synchronization to S3 |

---

## 2.3. Data Protection
#### SSL Certification
Let's Encrypt SSL certificates (port 443 encryption) managed with automated renewal triggers via Certbot hooks.

#### State Lock Management
Infrastructure configuration state files are stored encrypted on S3, coupled with DynamoDB table bindings (`LockID`) to prevent state corruption during concurrent pipeline runs.

---

# 3. CI/CD

## 3.1. Pipeline Workflow
```mermaid
stateDiagram-v2
    direction LR
    [*] --> CI : Git Push to main
    
    state "1. Continuous Integration (CI)" as CI {
        direction TB
        state "Backend: Gradle Tests" as UnitB
        state "Frontend: Vitest Tests" as UnitF
        state "Infra: Ansible Lint" as Lint
        
        [*] --> UnitB
        [*] --> UnitF
        [*] --> Lint
    }

    state "2. Continuous Delivery: Staging" as Staging {
        direction TB
        state "Build Backend (GHCR)" as BuildB
        state "Build & S3 Sync Frontend" as BuildF
        state "Terraform Apply Staging" as TFA_S
        state "Deploy Backend via Ansible" as Deploy_S
        state "Run Playwright E2E Tests" as E2E
        
        [*] --> BuildB
        [*] --> BuildF
        BuildB --> TFA_S
        BuildF --> TFA_S
        TFA_S --> Deploy_S
        Deploy_S --> E2E
    }

    state "3. Approval Gate" as Gate {
        state "Pause for Admin Manual Approval" as Approve
        [*] --> Approve
    }

    state "4. Continuous Deployment: Production" as Production {
        direction TB
        state "Terraform Apply Production" as TFA_P
        state "Deploy Production via Ansible" as Deploy_P
        state "Auto-SemVer Tag & Release" as Release
        
        [*] --> TFA_P
        TFA_P --> Deploy_P
        Deploy_P --> Release
    }

    CI --> Staging : Validations Pass
    Staging --> Gate : Playwright E2E Pass
    Gate --> Production : Approved
    Production --> [*] : Production Release Complete
```

### 3.1.1. Trigger Optimization
#### Path Filtering
Skips build triggers for pure documentation modifications (`*.md`) or changes to local configuration scripts, saving Cloud runner execution minutes.

#### Concurrency Limit
Instantly aborts outdated runs when a new push is merged during an active staging build (`cancel-in-progress: true`).

---

## 3.2. Artifact & Release Management
#### Compute Offloading
Heavy compilations and GraalVM native image generation tasks are completely offloaded to GitHub Actions runner agents (refer to [1.2.1. Compute](#121-compute)).

#### Static Asset Delivery
Frontend build bundles are synchronized to S3 via `aws s3 sync`, followed by an edge invalidation command to distribute updates via CloudFront.

#### Versioning Automation
Changelogs and Semantic Versioning tags are automatically generated by parsing standardized conventional commit logs.

---

## 3.3. Continuous Validation
### 3.3.1. Verification Gates
#### Static Analysis
Performs parallel verification checks (Ansible Lint, Gradle tests, and Vitest suite) immediately upon PR creation.

#### Trivy Vulnerability Scan
Performs SCA dependency audits, IaC static rule reviews, and container image scans before registry publication.

#### Playwright E2E Test
Initiates automated browser integration tests (`staging.spec.ts`) as soon as the staging environment deployment is complete.

### 3.3.2. Delivery Gates
#### Manual Approval
Enforces manual gate check triggers. Deployments pause after passing Staging, requiring explicit administrator approval to promote code to Production.

#### Automated DR Gate
DB restore processes (`db-restore.yml`) are executed via SSM command pipelines, wrapping AWS Systems Manager APIs for secure recovery.

---

# 4. AI Engineering

## 4.1. LLM Generation Pipeline
#### Generation Engine
Automated daily stage generation utilizing a hybrid pipeline (`gemini-3-flash` for grids, `gemini-3.5-flash` for themes), scheduled nightly at 04:17 KST.

#### Rate Limit Defense
Configured with a 15-second interval sleep and 3-stage exponential backoff logic to respect 5 RPM limits.

#### FIFO Buffer Store
A FIFO database table maintains a buffer of at least 5 ready-to-play puzzles for each size (5x5 to 20x20) to ensure continuous gameplay.

---

## 4.2. Automated Quality Guardrails
#### Logic Verification
Every generated grid is evaluated by a Java DFS backtracker (`NonogramSolver`) to ensure a single, logical, unique solution exists.

#### Data Filtering
Puzzles with multiple solutions or invalid layouts are immediately discarded before database persistence.

---

## 4.3. AI Governance & Human-in-the-Loop
#### HITL Feedback Loop
Aggregates thumbs up/down user feedback to adjust prompt structures and tune generation metrics.

#### Backoffice Hard Delete
Enforces an administrator control panel that enables hard deleting bad or deformed stages directly from the database.

#### Co-Engineering Stack
Pair programming environment centered around the **Antigravity IDE**:
* **Gemini 3.5 Flash**: IDE assistant and Chrome DevTools subagent (writes code, analyzes logs, and checks UI elements).
* **Claude Sonnet 4.6**: Analyzes code structures and reviews test case coverage.
* **Claude Opus 4.6**: High-level architectural validation and repository integrity reviews.

#### Agent Governance Rules
This repository maintains active rules defining security, style, and programming conventions for AI coding agents:

| Rule File | Description | Version Tracking |
| :--- | :--- | :--- |
| [architecture-and-tech-stack.md](.agents/rules/architecture-and-tech-stack.md) | Enforces directory separations, prevents simultaneous multi-tier modifications, blocks Vue reactive model leaks, and validates sequence order. | `Git Tracked` |
| [documentation-guidelines.md](.agents/rules/documentation-guidelines.md) | Mandates relative referencing (no absolute file schema links), defines markdown spacing rules, and requires tables for comparative metrics. | `Git Tracked` |
| [git-and-commit-guidelines.md](.agents/rules/git-and-commit-guidelines.md) | Enforces Conventional Commit rules and local commit/push configurations. | `Git Tracked (Force Added)` |
| [workflow-and-tdd.md](.agents/rules/workflow-and-tdd.md) | Enforces TDD priority for all logic layers and updates progress contexts. | `Git Tracked` |
| [safety-and-communication.md](.agents/rules/safety-and-communication.md) | Mandates halting changes for ambiguous requirements (no guessing) and awaits human approval. | `Git Tracked` |
| [incident-reporting.md](.agents/rules/incident-reporting.md) | Outlines incident report structures using postmortem analyses. | `Git Tracked` |

---

# 5. Performance & Cost Analysis
## 5.1. Operational Cost Metrics
#### Billing Period: 2026.06.01 ~ 2026.06.30

| AWS Service | Monthly Charges |
| :--- | :---: |
| **Elastic Compute Cloud (EC2 & EBS)** | USD 6.05 |
| **Virtual Private Cloud (Public IPv4)** | USD 4.07 |
| **Route 53 (Hosted Zone & Queries)** | USD 1.14 |
| **Data Transfer** | USD 0.15 |
| **Relational Database & S3** | USD 0.04 |
| **Total** | **USD 11.45** |

> **Note**: The costs above represent the minimum baseline required to run 1 Production environment. **If the Staging server is running 24/7**, EC2 (t3a.nano) instance usage, Public IPv4 allocation, and EBS storage costs will double, **increasing the estimated monthly cost to approximately $24**. To optimize costs, we recommend stopping the Staging server when it is not in use.

## 5.2. SLO Targets vs Actual Performance
#### Measurement Period: 2026.06.25 ~ 2026.07.02

| Metric KPI | SLA Actual Outcome |
| :--- | :--- |
| **Availability** | **99.98%** |
| **API Latency** | **123.4 ms** |
| **MTTR** | **11.76 Min** |
| **RPO** | **Max 3 Hours** |
| **RTO** | **Within 3 Minutes** |

![Grafana SLA Dashboard](./docs/assets/grafana_sla_snapshot.png)

## 5.3. Security Vulnerability Metrics
#### Vulnerability Target Limits
Trivy security scan limits compared against final automated deployment blocking thresholds.

| Scan Target | Component Details | Severity | Target Limit | Actual Scan Result |
| :--- | :--- | :---: | :---: | :---: |
| **SCA (Dependencies)** | Backend (Gradle), Frontend (npm) | CRITICAL / HIGH / MEDIUM | **0** / **0** / Minimize | **0** / **0** / 0 (Clean) |
| **IaC (Infrastructure as Code)** | Terraform, Ansible, Dockerfile | CRITICAL / HIGH / MEDIUM | **0** / **0** / Minimize | **0** / **0** / 0 (Clean) |
| **Container (Backend Image)** | ghcr.io/devdoyen/nemologic-backend | CRITICAL / HIGH | **0** / **0** | **0** / **0** (Clean) |

## 5.4. User & System Traffic Metrics
#### Traffic Accumulation
Measurement Period: 2026.06.25 ~ 2026.07.02

| Metric KPI | SLA Actual Outcome |
| :--- | :--- |
| **Active Users** | 39 |
| **Total Events** | 535 |
| **Average Engagement** | 2m 53s |
| **Daily Generated** | 60+ stages |

![Google Analytics 4 User Report](./docs/assets/ga4_report.png)

---

# 6. Troubleshooting & Incidents

## 6.1. Host Memory Exhaustion Incident
#### Symptom
Host memory depletion, OOM events, and disk I/O thrashing occurred on the t3a.nano (512MB RAM) instance due to monitoring agent resource limits.

#### Root Cause
The collection agent (Grafana Alloy) consumed excessive memory (100MB+), causing resource exhaustion when coupled with high I/O spikes during container image extractions.

#### Mitigation
* **Agentless Architecture**: Discarded the host collection agent. Replaced it with an agentless pull structure where Grafana Mimir scrapes metrics directly from Node Exporter/Spring Actuator via Nginx proxy paths.
* **GraalVM Native Image**: Compiled the Spring Boot API server into a native binary, reducing active memory consumption to under 30MB.
* **Memory Buffer**: Added a 2GB SWAP space and configured automatic garbage collection for Docker images.

#### Retrospective
Proven that low-spec hardware limits can be overcome by tracking resources using low-level tools (`top`, `vmstat`) and optimizing compilers (GraalVM native AOT) coupled with virtual memory buffers.

---

## 6.2. Deployment Pipeline Conflict
#### Symptom
1. Access denied errors occurred on the site because DNS switches occurred before S3 asset replication completed.
2. Deployment runs were aborted during hotfixes, causing SSL certificate failures.

#### Root Cause
1. Development and Staging infra configurations were coupled in the same Terraform workspace, failing to isolate the blast radius.
2. The `cancel-in-progress: true` option was misconfigured, killing runs during active certificate provisioning.

#### Mitigation
* **Workspace Isolation**: Partitioned Terraform state configurations using distinct directories for Staging and Production.
* **Fine-Tuned Concurrency**: Enforced `cancel-in-progress: false` for production stages to prevent transaction interruptions.
* **Manual Verification Gate**: Added a manual approval gate before DNS routing switches.

#### Retrospective
Stateful transactions must be protected against sudden abort signals, and critical updates should be protected by manual approvals.

---

## 6.3. AI Puzzle Generation Parsing Incident
#### Symptom
The daily stage generation pipeline crashed with a serialization exception (`JsonParseException`).

#### Root Cause
Under heavy 30x30 calculations, the LLM returned shorthand JavaScript code (e.g. `Array(30).fill(0)`) instead of raw, valid 2D JSON array structures.

#### Mitigation
* **Prompt Engineering**: Enforced the `MUST be a literal 2D JSON array` constraint in prompt definitions.
* **Buffer Safety**: Reduced the daily generation batch size from 5 concurrent puzzles to 2.

#### Retrospective
When parsing non-deterministic LLM outputs in batch services, raw schema validation boundaries should be enforced at the entry point.

---

## 6.4. Production Database Initialization
#### Symptom
The production database (user profiles, clears, custom stages) was wiped during an IAM update deployment that forced an EC2 recreation.

#### Root Cause
* **Coupled Lifecycle**<br>
  The database was mounted on Docker named volumes within the EC2 host lifecycle. When the instance was destroyed, the data was lost.
* **Silent Backup Failure**<br>
  The backup script failed silently due to a missing S3 list permission (`s3:ListAllMyBuckets`). Standard error routing to `/dev/null` masked the failure for months.

#### Mitigation
* **Lifecycle Protections (`prevent_destroy`)**<br>
  Enforced `prevent_destroy = true` for the EC2 host instances in [main.tf](./infra/terraform/envs/production/main.tf).
* **EBS Storage Isolation**<br>
  Created a separate AWS EBS gp3 volume (10GB) and migrated database mounts to host directory bindings (`/opt/nemologic/db_data`) to decouple data from instance lifecycles.
* **Silent Error Correction**<br>
  Replaced S3 bucket query calls in the backup script with direct Ansible variable injections, avoiding access checks. Removed error redirect suppression to capture Cron failures in log streams.
* **One-Click Disaster Recovery**<br>
  Deployed restore scripts ([restore_db.sh.j2](./infra/ansible/templates/restore_db.sh.j2)) and a workflow (`db-restore.yml`) using SSM commands. Verified a recovery time (RTO) of 37~38 seconds.

#### Retrospective
Stateful storage must be decoupled from compute instances, and recovery workflows must be regularly tested to ensure RTO metrics are met.
