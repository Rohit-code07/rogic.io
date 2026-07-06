import http from 'k6/http';
import { check, sleep } from 'k6';

// Read target configurations from environment variables passed by the runner
const TARGET_HOST = __ENV.TARGET_HOST || 'http://localhost:8080';
const COGNITO_TOKEN = __ENV.K6_COGNITO_TOKEN || '';

export const options = {
  stages: [
    { duration: '30s', target: 5 },  // ramp-up
    { duration: '1m', target: 20 },  // steady load
    { duration: '30s', target: 0 },  // ramp-down
  ],
  thresholds: {
    http_req_failed: ['rate<0.05'], // Http errors must be below 5%
    http_req_duration: ['p(95)<800'], // 95% of requests should be below 800ms
  },
};

export default function () {
  const headers = { 'Content-Type': 'application/json' };
  let authHeaders = { 'Content-Type': 'application/json' };

  if (COGNITO_TOKEN) {
    authHeaders['Authorization'] = `Bearer ${COGNITO_TOKEN}`;
  }

  // 1. Fetch Puzzles (Public GET)
  const getStagesRes = http.get(`${TARGET_HOST}/api/stages`, { headers });
  check(getStagesRes, {
    'GET /api/stages status is 200': (r) => r.status === 200,
  });

  let selectedStage = null;
  if (getStagesRes.status === 200) {
    try {
      const stages = JSON.parse(getStagesRes.body);
      if (Array.isArray(stages) && stages.length > 0) {
        // Randomly select a stage from the fetched list
        selectedStage = stages[Math.floor(Math.random() * stages.length)];
      }
    } catch (e) {
      // JSON parse error handling
    }
  }

  sleep(1);

  // 2. High-CPU Solver Verification (Public POST)
  if (selectedStage && selectedStage.id) {
    const payload = JSON.stringify({
      rotationSteps: 0,
      gridState: selectedStage.solutionGrid || [[0]],
      elapsedTime: 120,
    });

    const verifyRes = http.post(
      `${TARGET_HOST}/api/stages/${selectedStage.id}/verify`,
      payload,
      { headers }
    );

    check(verifyRes, {
      'POST /verify status is 200 or 400': (r) => r.status === 200 || r.status === 400,
    });
  }

  sleep(1);

  // 3. User Context Routing (Authenticated request - only if Token is active)
  if (COGNITO_TOKEN) {
    const meRes = http.post(`${TARGET_HOST}/api/auth/me`, null, { headers: authHeaders });
    const isMeOk = check(meRes, {
      'POST /api/auth/me status is 200': (r) => r.status === 200,
    });

    if (isMeOk && meRes.status === 200) {
      try {
        const user = JSON.parse(meRes.body);
        if (user && user.id) {
          // Fetch User Clear History (Authenticated GET)
          const historyRes = http.get(`${TARGET_HOST}/api/users/${user.id}/history`, { headers: authHeaders });
          check(historyRes, {
            'GET /api/users/{id}/history status is 200': (r) => r.status === 200,
          });
        }
      } catch (e) {
        // JSON parse error handling
      }
    }
    sleep(1);
  }
}
