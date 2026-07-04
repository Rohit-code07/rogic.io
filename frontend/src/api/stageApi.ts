import axios from 'axios';

export interface StageSummary {
  id: number;
  name: string;
  width: number;
  height: number;
  totalAttempts?: number;
  totalClears?: number;
  averageElapsedTime?: number;
  upvotes?: number;
  downvotes?: number;
}

export interface StageDetails extends StageSummary {
  solutionGrid: number[][];
}

export interface PageResponse<T> {
  content: T[];
  totalPages: number;
  totalElements: number;
  size: number;
  number: number;
}

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL
  ? `${import.meta.env.VITE_API_BASE_URL}/api/stages`
  : (import.meta.env.PROD ? '/api/stages' : 'http://localhost:8080/api/stages');

export async function fetchStages(page?: number, size?: number, width?: number): Promise<StageSummary[] | PageResponse<StageSummary>> {
  const params: any = {};
  if (page !== undefined) params.page = page;
  if (size !== undefined) params.size = size;
  if (width !== undefined) params.width = width;
  
  const hasParams = Object.keys(params).length > 0;
  if (hasParams) {
    const response = await axios.get<StageSummary[] | PageResponse<StageSummary>>(API_BASE_URL, { params });
    return response.data;
  } else {
    const response = await axios.get<StageSummary[] | PageResponse<StageSummary>>(API_BASE_URL);
    return response.data;
  }
}

export async function fetchStageById(id: number): Promise<StageDetails> {
  const response = await axios.get<StageDetails>(`${API_BASE_URL}/${id}`);
  return response.data;
}

export async function fetchAiStages(): Promise<StageSummary[]> {
  const response = await axios.get<StageSummary[]>(API_BASE_URL);
  return response.data;
}

export async function fetchNextReleaseDelaySeconds(): Promise<number> {
  const response = await axios.get<number>(`${API_BASE_URL}/next-release-delay`);
  return response.data;
}

export async function startStage(id: number): Promise<void> {
  await axios.post(`${API_BASE_URL}/${id}/start`);
}

export async function likeStage(id: number): Promise<StageDetails> {
  const response = await axios.post<StageDetails>(`${API_BASE_URL}/${id}/like`);
  return response.data;
}

export async function dislikeStage(id: number): Promise<StageDetails> {
  const response = await axios.post<StageDetails>(`${API_BASE_URL}/${id}/dislike`);
  return response.data;
}
