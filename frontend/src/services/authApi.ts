import { getApiUrl } from './apiConfig';

export interface CurrentUser {
  id: string;
  username: string;
  email: string;
  systemRole: 'ADMIN' | 'USER';
}

export const authApi = {
  login: (email: string, password: string): Promise<CurrentUser> =>
    fetch(getApiUrl('/api/auth/login'), {
      method: 'POST',
      credentials: 'include',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email, password }),
    }).then(res => {
      if (!res.ok) throw new Error('Invalid credentials');
      return res.json();
    }),

  me: (): Promise<CurrentUser | null> =>
    fetch(getApiUrl('/api/auth/me'), { credentials: 'include' }).then(res =>
      res.ok ? res.json() : null,
    ),

  logout: (): Promise<void> =>
    fetch(getApiUrl('/api/auth/logout'), { method: 'POST', credentials: 'include' }).then(
      () => {},
    ),
};
