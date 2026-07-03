import axios from 'axios';
import { getAuthHeader } from './auth';

export interface User {
  id: number;
  username: string;
  xp: number;
  level: number;
  uuid?: string;
  email?: string;
  profileImageUrl?: string;
}

export interface HistoryResponse {
  id: number;
  userId: number;
  stageId: number;
  stageName: string;
  clearedAt: string;
  xpEarned: number;
  elapsedTime: number;
}

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL
  ? `${import.meta.env.VITE_API_BASE_URL}/api/users`
  : (import.meta.env.PROD ? '/api/users' : 'http://localhost:8080/api/users');

export async function fetchRanking(): Promise<User[]> {
  const response = await axios.get<User[]>(`${API_BASE_URL}/ranking`);
  return response.data;
}

export async function clearStage(userId: number, difficulty: string, stageId?: number, elapsedTime?: number): Promise<User> {
  const params: any = { difficulty };
  if (stageId !== undefined) {
    params.stageId = stageId;
  }
  if (elapsedTime !== undefined) {
    params.elapsedTime = elapsedTime;
  }
  const response = await axios.post<User>(`${API_BASE_URL}/${userId}/clear`, null, {
    params,
    headers: getAuthHeader(),
  });
  return response.data;
}

export async function fetchUserHistory(userId: number): Promise<HistoryResponse[]> {
  const response = await axios.get<HistoryResponse[]>(`${API_BASE_URL}/${userId}/history`, {
    headers: getAuthHeader(),
  });
  return response.data;
}

export async function fetchMeFromServer(): Promise<User> {
  const authBaseUrl = import.meta.env.VITE_API_BASE_URL
    ? `${import.meta.env.VITE_API_BASE_URL}/api/auth`
    : (import.meta.env.PROD ? '/api/auth' : 'http://localhost:8080/api/auth');
  const response = await axios.post<User>(`${authBaseUrl}/me`, null, {
    headers: getAuthHeader(),
  });
  return response.data;
}


