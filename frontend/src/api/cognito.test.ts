import { describe, it, expect, vi, beforeEach } from 'vitest';
import axios from 'axios';
import { handleCallback, logout, getOrRefreshToken, isTokenExpired } from './cognito';

vi.mock('axios');

describe('cognito.ts Session Retention Tests (TDD)', () => {
  const ID_KEY = 'nemologic_id_token';
  const REFRESH_KEY = 'nemologic_refresh_token';

  beforeEach(() => {
    localStorage.clear();
    sessionStorage.clear();
    vi.clearAllMocks();
  });

  // Helper to generate a dummy JWT token with custom expiration
  const createMockToken = (expInSeconds: number): string => {
    const header = btoa(JSON.stringify({ alg: 'RS256', typ: 'JWT' }));
    const payload = btoa(JSON.stringify({ sub: 'user123', exp: expInSeconds }));
    return `${header}.${payload}.signature`;
  };

  describe('handleCallback', () => {
    it('should exchange code and store both id_token and refresh_token', async () => {
      localStorage.setItem('nemologic_code_verifier', 'verifier_xyz');
      const mockResponse = {
        data: {
          id_token: 'new_id_token_xyz',
          refresh_token: 'new_refresh_token_xyz',
        },
      };
      vi.mocked(axios.post).mockResolvedValue(mockResponse);

      const token = await handleCallback('auth_code_123');

      expect(token).toBe('new_id_token_xyz');
      expect(localStorage.getItem(ID_KEY)).toBe('new_id_token_xyz');
      expect(localStorage.getItem(REFRESH_KEY)).toBe('new_refresh_token_xyz');
      expect(localStorage.getItem('nemologic_code_verifier')).toBeNull();
    });

    it('should throw error if id_token is missing in response', async () => {
      localStorage.setItem('nemologic_code_verifier', 'verifier_xyz');
      vi.mocked(axios.post).mockResolvedValue({ data: {} });

      await expect(handleCallback('auth_code_123')).rejects.toThrow('No id_token returned');
    });
  });

  describe('logout', () => {
    it('should clear both id_token and refresh_token from localStorage', () => {
      localStorage.setItem(ID_KEY, 'some_id_token');
      localStorage.setItem(REFRESH_KEY, 'some_refresh_token');

      vi.stubGlobal('location', { href: '' });

      logout();

      expect(localStorage.getItem(ID_KEY)).toBeNull();
      expect(localStorage.getItem(REFRESH_KEY)).toBeNull();

      vi.unstubAllGlobals();
    });
  });

  describe('getOrRefreshToken', () => {
    it('should return stored token immediately if it is not expired', async () => {
      const validTime = Math.floor(Date.now() / 1000) + 3600; // 1 hour in future
      const token = createMockToken(validTime);
      localStorage.setItem(ID_KEY, token);

      const returnedToken = await getOrRefreshToken();

      expect(returnedToken).toBe(token);
      expect(axios.post).not.toHaveBeenCalled();
    });

    it('should refresh and return a new token if id_token is expired but refresh_token exists', async () => {
      const expiredTime = Math.floor(Date.now() / 1000) - 60; // 1 minute in past
      const expiredToken = createMockToken(expiredTime);
      localStorage.setItem(ID_KEY, expiredToken);
      localStorage.setItem(REFRESH_KEY, 'valid_refresh_token_123');

      const mockResponse = {
        data: {
          id_token: 'refreshed_id_token_999',
          refresh_token: 'new_refresh_token_999',
        },
      };
      vi.mocked(axios.post).mockResolvedValue(mockResponse);

      const returnedToken = await getOrRefreshToken();

      expect(returnedToken).toBe('refreshed_id_token_999');
      expect(localStorage.getItem(ID_KEY)).toBe('refreshed_id_token_999');
      expect(localStorage.getItem(REFRESH_KEY)).toBe('new_refresh_token_999');
      expect(axios.post).toHaveBeenCalledTimes(1);
    });

    it('should clear all tokens and return null if id_token is expired and refresh_token is missing', async () => {
      const expiredTime = Math.floor(Date.now() / 1000) - 60;
      const expiredToken = createMockToken(expiredTime);
      localStorage.setItem(ID_KEY, expiredToken);

      const returnedToken = await getOrRefreshToken();

      expect(returnedToken).toBeNull();
      expect(localStorage.getItem(ID_KEY)).toBeNull();
      expect(localStorage.getItem(REFRESH_KEY)).toBeNull();
    });

    it('should clear all tokens and return null if token refresh network call fails', async () => {
      const expiredTime = Math.floor(Date.now() / 1000) - 60;
      const expiredToken = createMockToken(expiredTime);
      localStorage.setItem(ID_KEY, expiredToken);
      localStorage.setItem(REFRESH_KEY, 'some_refresh_token');

      vi.mocked(axios.post).mockRejectedValue(new Error('Network error'));

      const returnedToken = await getOrRefreshToken();

      expect(returnedToken).toBeNull();
      expect(localStorage.getItem(ID_KEY)).toBeNull();
      expect(localStorage.getItem(REFRESH_KEY)).toBeNull();
    });
  });

  describe('isTokenExpired helper', () => {
    it('should return true for invalid token formats', () => {
      expect(isTokenExpired('invalid_token')).toBe(true);
      expect(isTokenExpired('a.b')).toBe(true);
    });

    it('should return true for expired payload', () => {
      const expiredToken = createMockToken(Math.floor(Date.now() / 1000) - 5);
      expect(isTokenExpired(expiredToken)).toBe(true);
    });

    it('should return false for active payload', () => {
      const activeToken = createMockToken(Math.floor(Date.now() / 1000) + 120);
      expect(isTokenExpired(activeToken)).toBe(false);
    });
  });
});
