# Incident Report: 2026-07-03 Production Database Permission Outage

## 1. 개요 (Summary)
* **장애 발생 시각**: 2026-07-03 20:10 (KST)
* **장애 복구 시각**: 2026-07-03 20:50 (KST)
* **총 장애 지속 시간**: 40분
* **영향 범위**: Production API (`api.rogic.io`) 전면 서비스 불가 및 백엔드 502/504 게이트웨이 에러 노출
* **장애 요인**: 신규 EBS 독립 볼륨 부착 후 데이터 디렉터리(`db_data`)의 권한을 Ansible 플레이북에서 `999:999`로 지정함에 따라, `postgres:16-alpine` 이미지 내부의 실제 기동 계정인 `postgres`(UID `70`)가 시스템 맵핑 파일(`global/pg_filenode.map`)을 열지 못해 발생한 `Permission denied` 데이터베이스 접속 불능 장애.

---

## 2. 장애 타임라인 (Timeline)
* **20:10 (KST) - 장애 감지**: Production 배포 실행 직후 백엔드 API 서버가 502 Bad Gateway 응답을 반환하기 시작하며 서비스가 다운됨.
* **20:15 (KST) - 분석 개시**: AWS SSM Session Manager를 통해 실제 Production 호스트 EC2 인스턴스(`i-0dad73fde464e9b97`)의 컨테이너 상태 추적. `nemologic-db` 컨테이너 및 `nemologic-backend-blue` 상태를 분석하기 시작.
* **20:25 (KST) - 근본 원인 특정**: 백엔드 스프링부트 컨테이너 기동 로그에서 Flyway DB 커넥션 획득 실패 및 `Caused by: org.postgresql.util.PSQLException: FATAL: could not open file "global/pg_filenode.map": Permission denied` 예외 감출.
* **20:30 (KST) - 소유권 충돌 판명**: `postgres:16-alpine` 이미지의 기본 구동 UID가 `70`이나, 플레이북의 EBS 볼륨 마운트 권한이 `999`로 덮어씌워지며 데이터베이스 엔진이 내부 시스템 메타 파일에 대한 읽기/쓰기 권한을 상실하여 락이 유발되었음을 인지.
* **20:35 (KST) - 코드 패치 및 수동 완화**: Ansible 플레이북(`playbook.yml`) 내 권한 설정 대상을 `owner: "70", group: "70"`으로 수정. 이와 동시에 Production 및 Staging 서버의 `/opt/nemologic/db_data` 하위 폴더 소유권을 SSM 명령어를 통해 즉시 `70:70`으로 강제 chown 수정 조치하고 DB 서비스를 수동으로 재기동함.
* **20:40 (KST) - 파이프라인 트리거**: Git 패치 커밋 및 원격 푸시를 통해 CI/CD 배포 파이프라인 재트리거.
* **20:44 (KST) - Staging 확인 및 승인**: Staging 빌드 및 Playwright E2E 통합 테스트의 100% 정상 통과를 확인하고 Production Apply를 승인함.
* **20:50 (KST) - 최종 복구**: Production 배포 완료 후 `curl.exe` 검증을 통해 기존 실데이터(Diamond Emblem 등) 정합성과 API 200 OK 복구를 정상 확인하고 인시던트 종료.

---

## 3. 원인 분석 (Root Cause Analysis)
이번 장애는 **PostgreSQL alpine 도커 이미지 내부의 실행 계정 식별자(UID)와 호스트 볼륨 소유권 간의 불일치**에서 기인했습니다.

1. **볼륨 마운트와 소유권 박탈**:
   EBS 독립 볼륨을 `/opt/nemologic/db_data`에 포맷 및 마운트한 이후, 기존 데이터베이스 디렉터리 소유권이 Root 혹은 임의의 UID로 변경되었습니다.
2. **Jinja2 플레이북 하드코딩 오류**:
   플레이북에서 마운트 경로의 사용 권한을 부여할 때, 일반적인 PostgreSQL 배포판 UID인 `999`를 하드코딩하여 반영했습니다:
   ```yaml
   owner: "999"
   group: "999"
   ```
3. **Alpine OS의 UID 구조 불일치**:
   `postgres:16-alpine` 이미지는 경량화를 위해 Alpine 리눅스를 사용하며, 이 이미지 내부의 `postgres` 데몬 프로세스는 **UID `70`**으로 구동됩니다.
4. **결과**:
   디렉터리 및 하위 메타 데이터 파일들의 소유주가 `999`로 강제 변경되면서, UID `70`을 가진 PostgreSQL 프로세스가 시스템 구동용 맵 파일(`global/pg_filenode.map`)에 대한 열기 작업(open)을 수행하지 못하고 아래와 같은 치명적 예외를 뱉으며 기동 실패를 유발했습니다.
   ```
   Caused by: org.postgresql.util.PSQLException: FATAL: could not open file "global/pg_filenode.map": Permission denied
   ```

---

## 4. 해결 방안 (Resolution)
1. **Ansible 플레이북의 소유권 속성을 alpine 규격인 `70`으로 수정**:
   ```diff
   -        owner: "999"
   -        group: "999"
   +        owner: "70"
   +        group: "70"
   ```
2. **서버 인스턴스 긴급 소유권 보정**:
   SSM Session Manager를 통해 Staging 및 Production 호스트에서 아래 명령어를 가동하여 락을 즉각 해제하고 서비스를 정상화했습니다:
   ```bash
   sudo chown -R 70:70 /opt/nemologic/db_data
   sudo docker compose -f /opt/nemologic/docker-compose.prod.yml restart db
   ```

---

## 5. 재발 방지 대책 (Preventative Actions)
* **도커 이미지 UID 사전 명세화 검토**: 향후 데이터 볼륨이나 권한 제어 태스크를 작성할 때, 사용하는 베이스 이미지(Alpine, Debian 등) 내부의 핵심 데몬 프로세스 UID를 사전에 엄밀하게 명세화하고 대조하는 설계 검토 절차를 추가합니다.
* **컨테이너 자가 권한 획득(Self-correction) 검토**: Docker Compose 볼륨 생성 단계 또는 컨테이너 Entrypoint 레벨에서 볼륨 디렉터리 권한을 자동으로 교정할 수 있는 헬퍼 볼륨 구성을 검토하여 외부 형상 도구(Ansible)의 하드코딩 의존성을 축소합니다.
* **Staging 헬스체크 모니터링 강화**: 데이터베이스 컨테이너 자체는 `Up (healthy)`일지라도 백엔드가 502/504 상태인 경우를 감지하여 앤시블 빌드 실패를 명시적으로 조기 유도하는 게이트 파이프라인 단계를 보완합니다.
