import axios from 'axios';
import { getOrRefreshToken } from './cognito';

const apiClient = axios.create();

apiClient.interceptors.request.use(
  async (config) => {
    // 1. Bypass administrative endpoints
    if (config.url && config.url.includes('/api/admin/')) {
      return config;
    }

    // 2. Preserve existing Authorization headers if explicitly set
    if (config.headers && config.headers.Authorization) {
      return config;
    }

    // 3. Silently fetch or refresh the active Cognito token
    try {
      const token = await getOrRefreshToken();
      if (token) {
        if (!config.headers) {
          config.headers = {} as any;
        }
        config.headers.Authorization = `Bearer ${token}`;
      }
    } catch (error) {
      console.error('Interceptor failed to refresh token:', error);
    }

    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

export default apiClient;
