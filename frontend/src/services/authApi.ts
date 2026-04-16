const BASE = '/api/auth';

export interface CurrentUser {
  id: string;
  username: string;
  email: string;
  systemRole: 'ADMIN' | 'USER';
}

export const authApi = {
  login: (email: string, password: string): Promise<CurrentUser> =>
    fetch(`${BASE}/login`, {
      method: 'POST',
      credentials: 'include',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email, password }),
    }).then(res => {
      if (!res.ok) throw new Error('Invalid credentials');
      return res.json();
    }),

  me: (): Promise<CurrentUser | null> =>
    fetch(`${BASE}/me`, { credentials: 'include' }).then(res => (res.ok ? res.json() : null)),

  logout: (): Promise<void> =>
    fetch(`${BASE}/logout`, { method: 'POST', credentials: 'include' }).then(() => {}),
};
