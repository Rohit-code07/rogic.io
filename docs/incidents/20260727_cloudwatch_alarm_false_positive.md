# 20260727 - CloudWatch False Positive Alarm (nemologic-server-error-alarm)

**개요 (Summary)**<br>
  * 발생 일시: 2026-07-27 12:27 UTC ~ 12:45 UTC (총 18분)
  * 영향 범위: 실제 서비스 다운타임 없음. (운영 환경 오탐 알람 발송)
  * 주요 원인: CloudWatch Log Metric Filter 정규식 패턴 오탐으로 인해 Nginx 정상 응답 바이트 수(500 bytes)를 HTTP 500 에러로 잘못 감지.

**장애 타임라인 (Timeline)**<br>
  * **12:27:00 UTC**: Nginx Access Log에서 응답 바이트가 정확히 500바이트인 200 OK 요청(`... HTTP/1.1" 200 500 ...`)이 `/aws/ec2/nemologic` 로그 그룹에 수집됨.
  * **12:32:35 UTC**: CloudWatch Metric Alarm `nemologic-server-error-alarm`이 `ServerErrorCount >= 1` 조건을 충족하여 ALARM 상태로 전환 및 SNS 알림 발송.
  * **12:38:00 UTC**: 알람 인지 후 AWS CLI를 통한 로그 분석 개시.
  * **12:42:00 UTC**: 에러 필터 패턴(`?" 500 "`)이 바이트 수와 일치하여 발생한 오탐임을 확인.
  * **12:45:00 UTC**: 원인 규명 완료 및 해결 방안(인프라 수정 계획) 수립.

**원인 분석 (Root Cause Analysis)**<br>
  * `infra/terraform/envs/production/main.tf`에 정의된 CloudWatch Metric Filter 패턴은 `?ERROR ?" 500 " ?"Internal Server Error"`로 설정되어 있었습니다.
  * CloudWatch Filter의 공백 구분자 특성상 `" 500 "`은 공백으로 감싸진 500이라는 문자열과 일치합니다.
  * Nginx Access Log의 포맷이 `"$request" $status $body_bytes_sent` 형태로 기록됨에 따라, `200 500`과 같이 바이트 수가 500인 정상 요청이 필터에 매칭되어 에러 카운트를 1 증가시켰습니다.

**해결 방안 (Resolution)**<br>
  * Production 환경의 Metric Filter 패턴을 Nginx Access Log의 상태 코드 부분만 정확히 타겟팅할 수 있도록 `?ERROR ?\"\\\" 500 \" ?\"Internal Server Error\"`로 수정하여 오탐을 방지했습니다. (`" 500 ` 형태 매칭)
  * 또한 아키텍처 규칙 상의 환경 동기화 원칙(Parity)에 따라, 기존에 Staging 환경(`infra/terraform/envs/staging/main.tf`)에 누락되어 있던 동일한 Metric Filter와 Alarm 구성을 보완하여 인프라 형상을 일치시켰습니다.

**재발 방지 대책 (Preventative Actions)**<br>
  * **패턴 검증 강화**: 향후 모니터링 알람에서 상태 코드, 포트 번호 등 순수 숫자를 매칭해야 하는 경우 주변의 문맥(따옴표, HTTP 키워드 등)을 필터 패턴에 포함하여 다른 로깅 필드(바이트 수, PID 등)와의 혼동(False Positive)을 원천 차단합니다.
  * **Staging 인프라 선행 적용**: 알람 및 모니터링 구성 변경 시에도 반드시 Staging 환경에 먼저 적용하여, 실제 트래픽과 로그 상에서 오작동이 없는지 관찰한 후 Production에 반영하는 프로세스를 엄수합니다.
