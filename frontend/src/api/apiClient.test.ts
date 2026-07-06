import { describe, it, expect, vi, beforeEach } from 'vitest';
import apiClient from './apiClient';
import * as cognito from './cognito';

vi.mock('./cognito', () => ({
  getOrRefreshToken: vi.fn(),
}));

describe('apiClient.ts Request Interceptor Tests (TDD)', () => {
  let requestHandler: any;

  beforeEach(() => {
    vi.clearAllMocks();
    
    // Extract the request interceptor handler to test it in isolation
    const handlers = (apiClient.interceptors.request as any).handlers;
    expect(handlers.length).toBeGreaterThan(0);
    requestHandler = handlers[0].fulfilled;
  });

  it('should not attach Authorization header in Guest Mode (no token)', async () => {
    vi.mocked(cognito.getOrRefreshToken).mockResolvedValue(null);

    const config = {
      headers: {} as any,
      url: '/api/users/5/history',
    };

    const result = await requestHandler(config);

    expect(result.headers.Authorization).toBeUndefined();
    expect(cognito.getOrRefreshToken).toHaveBeenCalled();
  });

  it('should automatically attach Authorization header if valid token exists', async () => {
    vi.mocked(cognito.getOrRefreshToken).mockResolvedValue('valid_mock_token_123');

    const config = {
      headers: {} as any,
      url: '/api/users/5/history',
    };

    const result = await requestHandler(config);

    expect(result.headers.Authorization).toBe('Bearer valid_mock_token_123');
    expect(cognito.getOrRefreshToken).toHaveBeenCalled();
  });

  it('should preserve existing Authorization header if already present', async () => {
    vi.mocked(cognito.getOrRefreshToken).mockResolvedValue('valid_mock_token_123');

    const config = {
      headers: {
        Authorization: 'Bearer user_explicit_token',
      } as any,
      url: '/api/users/5/history',
    };

    const result = await requestHandler(config);

    expect(result.headers.Authorization).toBe('Bearer user_explicit_token');
    expect(cognito.getOrRefreshToken).not.toHaveBeenCalled();
  });

  it('should bypass Cognito token fetching for administrative admin endpoints', async () => {
    const config = {
      headers: {} as any,
      url: '/api/admin/stages',
    };

    const result = await requestHandler(config);

    expect(result.headers.Authorization).toBeUndefined();
    expect(cognito.getOrRefreshToken).not.toHaveBeenCalled();
  });
});
