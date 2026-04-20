/**
 * Resolves URLs for backend API calls.
 *
 * When `VITE_API_BASE_URL` is empty, requests stay relative (for example
 * `/api/...`). In local development, Vite proxies those requests to the
 * backend. In same-origin deployments, the frontend server / reverse proxy
 * can serve them directly.
 *
 * Set `VITE_API_BASE_URL` (for example `https://api.example.com`) when the
 * frontend and backend are deployed on different origins.
 */
const RAW_BASE = import.meta.env.VITE_API_BASE_URL ?? '';
const API_BASE_URL = RAW_BASE.replace(/\/+$/, '');

export function getApiUrl(path: string): string {
  const normalizedPath = path.startsWith('/') ? path : `/${path}`;
  return `${API_BASE_URL}${normalizedPath}`;
}
