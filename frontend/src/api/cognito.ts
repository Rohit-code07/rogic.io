import axios from 'axios';

const COGNITO_DOMAIN = import.meta.env.VITE_COGNITO_DOMAIN || 'https://nemologic-stage-auth.auth.ap-northeast-2.amazoncognito.com';
const CLIENT_ID = import.meta.env.VITE_COGNITO_CLIENT_ID || '';
const APP_URL = import.meta.env.VITE_APP_URL || (typeof window !== 'undefined' ? window.location.origin : 'http://localhost:5173');

const TOKEN_KEY = 'nemologic_id_token';
const REFRESH_KEY = 'nemologic_refresh_token';
const VERIFIER_KEY = 'nemologic_code_verifier';

// PKCE helper: Generate random string
function dec2hex(dec: number): string {
  return dec.toString(16).padStart(2, '0');
}

function generateCodeVerifier(): string {
  const array = new Uint32Array(56);
  window.crypto.getRandomValues(array);
  return Array.from(array, dec2hex).join('');
}

// PKCE helper: SHA-256 base64url hash
async function sha256(plain: string): Promise<ArrayBuffer> {
  const encoder = new TextEncoder();
  const data = encoder.encode(plain);
  return window.crypto.subtle.digest('SHA-256', data);
}

function base64urlencode(a: ArrayBuffer): string {
  let str = '';
  const bytes = new Uint8Array(a);
  const len = bytes.byteLength;
  for (let i = 0; i < len; i++) {
    str += String.fromCharCode(bytes[i]);
  }
  return btoa(str)
    .replace(/\+/g, '-')
    .replace(/\//g, '_')
    .replace(/=+$/, '');
}

async function generateCodeChallenge(v: string): Promise<string> {
  const hashed = await sha256(v);
  return base64urlencode(hashed);
}

// --- Exported Auth Methods ---

export async function loginWithGoogle(): Promise<void> {
  const verifier = generateCodeVerifier();
  sessionStorage.setItem(VERIFIER_KEY, verifier);

  const challenge = await generateCodeChallenge(verifier);
  const redirectUri = `${APP_URL}/`;
  
  const hostedUiUrl = `${COGNITO_DOMAIN}/oauth2/authorize?identity_provider=Google&redirect_uri=${encodeURIComponent(redirectUri)}&response_type=code&client_id=${CLIENT_ID}&code_challenge=${challenge}&code_challenge_method=S256`;
  
  window.location.href = hostedUiUrl;
}

export async function handleCallback(code: string): Promise<string> {
  const verifier = sessionStorage.getItem(VERIFIER_KEY);
  if (!verifier) {
    throw new Error('PKCE code verifier not found in session');
  }

  const redirectUri = `${APP_URL}/`;
  const params = new URLSearchParams();
  params.append('grant_type', 'authorization_code');
  params.append('client_id', CLIENT_ID);
  params.append('code', code);
  params.append('code_verifier', verifier);
  params.append('redirect_uri', redirectUri);

  const tokenUrl = `${COGNITO_DOMAIN}/oauth2/token`;
  const response = await axios.post(tokenUrl, params.toString(), {
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded',
    },
  });

  const idToken = response.data.id_token;
  const refreshToken = response.data.refresh_token;
  if (!idToken) {
    throw new Error('No id_token returned from token endpoint');
  }

  localStorage.setItem(TOKEN_KEY, idToken);
  if (refreshToken) {
    localStorage.setItem(REFRESH_KEY, refreshToken);
  }
  sessionStorage.removeItem(VERIFIER_KEY);
  return idToken;
}

export function logout(): void {
  localStorage.removeItem(TOKEN_KEY);
  localStorage.removeItem(REFRESH_KEY);
  const redirectUri = `${APP_URL}/`;
  const logoutUrl = `${COGNITO_DOMAIN}/logout?client_id=${CLIENT_ID}&logout_uri=${encodeURIComponent(redirectUri)}`;
  window.location.href = logoutUrl;
}

export async function getOrRefreshToken(): Promise<string | null> {
  const token = localStorage.getItem(TOKEN_KEY);
  if (token && !isTokenExpired(token)) {
    return token;
  }

  const refreshToken = localStorage.getItem(REFRESH_KEY);
  if (!refreshToken) {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(REFRESH_KEY);
    return null;
  }

  try {
    const params = new URLSearchParams();
    params.append('grant_type', 'refresh_token');
    params.append('client_id', CLIENT_ID);
    params.append('refresh_token', refreshToken);

    const tokenUrl = `${COGNITO_DOMAIN}/oauth2/token`;
    const response = await axios.post(tokenUrl, params.toString(), {
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded',
      },
    });

    const newIdToken = response.data.id_token;
    const newRefreshToken = response.data.refresh_token;

    if (!newIdToken) {
      throw new Error('No id_token returned during refresh');
    }

    localStorage.setItem(TOKEN_KEY, newIdToken);
    if (newRefreshToken) {
      localStorage.setItem(REFRESH_KEY, newRefreshToken);
    }
    return newIdToken;
  } catch (error) {
    console.error('Failed to refresh Cognito token:', error);
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(REFRESH_KEY);
    return null;
  }
}

export function getStoredToken(): string | null {
  const token = localStorage.getItem(TOKEN_KEY);
  if (!token) return null;

  if (isTokenExpired(token)) {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(REFRESH_KEY);
    return null;
  }
  return token;
}

export function isTokenExpired(token: string): boolean {
  try {
    const parts = token.split('.');
    if (parts.length !== 3) return true;
    const payload = JSON.parse(atob(parts[1].replace(/-/g, '+').replace(/_/g, '/')));
    const exp = payload.exp;
    if (!exp) return true;
    // Current time in seconds. Buffer of 10 seconds.
    return (Date.now() / 1000) >= (exp - 10);
  } catch (error) {
    return true;
  }
}
