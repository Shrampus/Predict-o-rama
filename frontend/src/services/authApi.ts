import { apiFetch } from '../lib/apiClient';

const BASE = '/api/auth';

export interface CurrentUser {
  id: string;
  username: string | null;
  email: string;
  systemRole: 'ADMIN' | 'USER';
}

export interface AuthResponse {
  token: string;
  status: 'OK' | 'NEEDS_ONBOARDING';
  user: CurrentUser;
}

export const authApi = {
  login: (email: string, password: string): Promise<AuthResponse> =>
    fetch(`${BASE}/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email, password }),
    }).then(res => {
      if (!res.ok) throw new Error('Invalid credentials');
      return res.json();
    }),

  googleLogin: (idToken: string): Promise<AuthResponse> =>
    fetch(`${BASE}/google`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ idToken }),
    }).then(res => {
      if (!res.ok) throw new Error('Google login failed');
      return res.json();
    }),

  me: (): Promise<CurrentUser | null> =>
    apiFetch(`${BASE}/me`).then(res => (res.ok ? res.json() : null)),

  completeProfile: (username: string): Promise<AuthResponse> =>
    apiFetch(`${BASE}/complete-profile`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username }),
    }).then(res => {
      if (!res.ok) throw new Error('Failed to complete profile');
      return res.json();
    }),
};
