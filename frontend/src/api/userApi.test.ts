import { describe, it, expect, vi } from 'vitest';
import apiClient from './apiClient';
import { fetchRanking, clearStage, fetchMeFromServer, fetchUserHistory } from './userApi';

vi.mock('./apiClient');

describe('userApi TDD Red Phase', () => {
  it('fetchRanking should call GET /ranking and return user ranking list', async () => {
    const mockRankings = [
      { id: 3, username: 'Player3', xp: 1000, level: 5 },
      { id: 2, username: 'Player2', xp: 500, level: 3 },
      { id: 1, username: 'Player1', xp: 200, level: 2 }
    ];

    vi.mocked(apiClient.get).mockResolvedValue({ data: mockRankings });

    const result = await fetchRanking();
    expect(apiClient.get).toHaveBeenCalledWith('http://localhost:8080/api/users/ranking');
    expect(result).toEqual(mockRankings);
  });

  it('clearStage should call POST /{id}/clear with difficulty query param', async () => {
    const mockUpdatedUser = { id: 1, username: 'Player1', xp: 250, level: 2 };
    vi.mocked(apiClient.post).mockResolvedValue({ data: mockUpdatedUser });

    const result = await clearStage(1, 'EASY');
    expect(apiClient.post).toHaveBeenCalledWith('http://localhost:8080/api/users/1/clear', null, {
      params: { difficulty: 'EASY' }
    });
    expect(result).toEqual(mockUpdatedUser);
  });

  it('clearStage should call POST /{id}/clear with difficulty, stageId and elapsedTime query params', async () => {
    const mockUpdatedUser = { id: 1, username: 'Player1', xp: 250, level: 2 };
    vi.mocked(apiClient.post).mockResolvedValue({ data: mockUpdatedUser });

    const result = await clearStage(1, 'EASY', 2, 120);
    expect(apiClient.post).toHaveBeenCalledWith('http://localhost:8080/api/users/1/clear', null, {
      params: { difficulty: 'EASY', stageId: 2, elapsedTime: 120 }
    });
    expect(result).toEqual(mockUpdatedUser);
  });

  it('fetchUserHistory should call GET /{id}/history and return list of user clear history', async () => {
    const mockHistory = [
      { id: 1, userId: 1, stageId: 2, stageName: 'Heart Shape', clearedAt: '2026-06-08T22:40:40', xpEarned: 50, elapsedTime: 120 }
    ];
    vi.mocked(apiClient.get).mockResolvedValue({ data: mockHistory });

    const result = await fetchUserHistory(1);
    expect(apiClient.get).toHaveBeenCalledWith('http://localhost:8080/api/users/1/history', {
      params: { _t: expect.any(Number) }
    });
    expect(result).toEqual(mockHistory);
  });

  it('fetchMeFromServer should call POST /auth/me and return logged-in user profile', async () => {
    const mockUser = { id: 4, username: 'GoogleUser', xp: 120, level: 2, email: 'user@example.com', profileImageUrl: 'https://example.com' };
    vi.mocked(apiClient.post).mockResolvedValue({ data: mockUser });

    const result = await fetchMeFromServer();
    expect(apiClient.post).toHaveBeenCalledWith('http://localhost:8080/api/auth/me', null);
    expect(result).toEqual(mockUser);
  });
});


