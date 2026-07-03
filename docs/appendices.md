# Appendices

To run `rogic.io` on your local workstation, select one of the options below:

## 1. Local Development Setup

### 1.1. Docker Compose Stack Deployment
전체 애플리케이션 스택(Database, Backend, Frontend)을 한 번에 빌드하고 기동하려는 경우 아래 옵션을 선택합니다.

```bash
# In the project root, compile, build and start all container services
docker compose up --build
```

#### Frontend Web Client
`http://localhost:5173`

#### Backend REST API
`http://localhost:8080`

#### Prerequisites
Docker & Docker Compose 설치 필요

---

### 1.2. Local and Container Hybrid Run
코드 수정 시 즉각적인 라이브 반영 및 핫 리로딩(Vite dev server)을 원하는 경우 아래 단계별로 서비스를 기동합니다.

#### Step 1: PostgreSQL 데이터베이스 기동
```bash
# Start only the database container in the background
docker compose up -d db
```

#### Step 2: 백엔드 API 서버 실행
```bash
cd backend
./gradlew bootRun
```
* API Server 구동 주소: `http://localhost:8080`
* **Prerequisites**: Java 17 JDK 설치 필요

#### Step 3: 프론트엔드 클라이언트 실행
```bash
cd frontend
npm install
npm run dev
```
* Frontend Client 구동 주소: `http://localhost:5173`
* **Prerequisites**: Node.js 20+ 설치 필요

---

### 1.3. AWS SSM Session Manager Setup
보안 그룹 22번 포트 폐쇄 환경 하에서 원격 EC2 인스턴스 터미널에 접속하거나 Ansible 터널을 설정하는 방법입니다.

#### AWS CLI 및 Session Manager Plugin 설치
로컬 기기에 AWS CLI를 최신 상태로 유지하고, SSH 터널링을 지원하기 위해 AWS 공식 [session-manager-plugin](https://docs.aws.amazon.com/systems-manager/latest/userguide/session-manager-working-with-install-plugin.html)을 설치합니다.

#### 로컬 SSH Config 설정 (~/.ssh/config)
보안 그룹에서 SSH(22) 포트가 폐쇄되었더라도 호스트의 SSM 에이전트를 프록시로 삼아 SSH 터널을 수립할 수 있도록 아래 설정을 로컬 SSH 환경 파일에 등록합니다.
```ssh
# SSH over SSM Tunnel Configuration
Host i-* mi-*
    ProxyCommand aws ssm start-session --target %h --document-name AWS-StartSSHSession --parameters portNumber=%p
```

#### EC2 Host Connection Command
인스턴스 ID와 기존 SSH 인증 키를 사용해 22포트 방화벽 차단을 우회하여 쉘 세션을 안전하게 수립합니다.
```bash
ssh -i ~/.ssh/nemologic-key.pem ubuntu@i-xxxxxxxxxxxxxxxxx
```

#### Ansible SSM SSH Tunneling Configuration (hosts.ini)
22번 포트 차단 상태에서 Ansible Playbook 가동을 위해 호스트의 SSM 에이전트를 프록시 터널로 삼아 연결할 수 있도록 아래와 같이 `hosts.ini` 설정을 구성하여 SSH 연결을 캡슐화합니다.
```ini
[nemologic_servers]
nemologic-app-server ansible_host=<EC2_Instance_ID> ansible_user=ubuntu ansible_ssh_private_key_file=<PEM_File_Path> ansible_ssh_common_args='-o ProxyCommand="aws ssm start-session --target %h --document-name AWS-StartSSHSession --parameters portNumber=%p"'
```

---

### 1.4. Cognito Authentication Configuration (Local .env)
로컬 개발 환경에서 구글 소셜 로그인 기능 및 회원 프로필 저장을 정상 가동하기 위해 아래와 같이 환경변수 및 인증 연동 주소를 구성합니다.

#### Frontend Local Environment (`frontend/.env.local` 생성)
```env
VITE_COGNITO_DOMAIN=https://nemologic-stage-auth-ey12fmas.auth.ap-northeast-2.amazoncognito.com
VITE_COGNITO_CLIENT_ID=539c98pgejrm7vi5sm3j82b53p
```
로컬 Vite 개발 서버(`http://localhost:5173`) 실행 시 위 Cognito Staging 도메인으로 리다이렉트되어 연동이 진행됩니다. `VITE_APP_URL`을 지정하지 않으면 런타임 origin인 `http://localhost:5173/`이 콜백 주소로 자동 지정됩니다.

#### Backend Local Environment (`backend/src/main/resources/application-local.yml` 또는 `.env`)
```env
COGNITO_JWK_SET_URI=https://cognito-idp.ap-northeast-2.amazonaws.com/ap-northeast-2_ey12fmas/.well-known/jwks.json
```
백엔드 REST API 구동 시, 전달된 ID Token(JWT) 서명의 무결성을 Cognito 공개키 세트(JWKS)로 검증하기 위해 주입하는 키 서버 엔드포인트 정보입니다.

---

## 2. PromQL Query Formulations (SLO Metrics)
> [!NOTE]
> 수식 내 기호 정의: $P_t \in \{0, 1\}$는 특정 측정 시점 $t$의 API 헬스체크 가용 성공 여부(`probe_success`)를 의미합니다. 초기 수집 시점에 가용 상태가 0(장애)으로 시작하는 경우, 첫 번째 변화(0 → 1)가 장애 복구임에도 홀수 변화 횟수가 반환되어 나눗셈 결과에 소수점이 발생할 수 있으므로 쿼리에서는 정수 나눗셈(내림) 처리를 적용합니다.

#### API Health Status
$$\text{API Health} = \sum P_t$$

```promql
sum(probe_success{job="nemologic-api-health", instance="https://rogic.io/actuator/health"})
```

#### Dynamic Service Availability
$$\text{Availability (\%)} = \text{avg}_{t \in \text{range}}(P_t) \times 100$$

```promql
avg_over_time(probe_success{job="nemologic-api-health", instance="https://rogic.io/actuator/health"}[$__range]) * 100
```

#### Dynamic Incident Count
$$\text{Incident Count} = \left\lfloor \frac{\text{changes}(P_t)}{2} \right\rfloor$$

```promql
floor(changes(probe_success{job="nemologic-api-health", instance="https://rogic.io/actuator/health"}[$__range]) / 2)
```

#### Dynamic MTTR (Mean Time To Recovery)
$$\text{MTTR (sec)} = \frac{\left(\text{count}_{t \in \text{range}}(P_t) - \sum_{t \in \text{range}} P_t\right) \times 60}{\text{clamp}_{\text{min}}\left(\frac{\text{changes}(P_t)}{2}, 1\right)}$$

```promql
((count_over_time(probe_success{job="nemologic-api-health", instance="https://rogic.io/actuator/health"}[$__range]) - sum_over_time(probe_success{job="nemologic-api-health", instance="https://rogic.io/actuator/health"}[$__range])) * 60) / clamp_min(changes(probe_success{job="nemologic-api-health", instance="https://rogic.io/actuator/health"}[$__range]) / 2, 1)
```

#### Dynamic MTBF (Mean Time Between Failures)
$$\text{MTBF (sec)} = \frac{\sum_{t \in \text{range}} P_t \times 60}{\text{clamp}_{\text{min}}\left(\frac{\text{changes}(P_t)}{2}, 1\right)}$$

```promql
(sum_over_time(probe_success{job="nemologic-api-health", instance="https://rogic.io/actuator/health"}[$__range]) * 60) / clamp_min(changes(probe_success{job="nemologic-api-health", instance="https://rogic.io/actuator/health"}[$__range]) / 2, 1)
```

* $\text{clamp}_{\text{min}}(x, d) = \max(x, d)$을 의미하며, 측정 대상 기간 중 장애/복구 전환 이벤트가 0회 발생할 경우 발생하는 분모 0 오류(Zero-division) 방지를 위해 PromQL 함수로 보정한 것입니다.
