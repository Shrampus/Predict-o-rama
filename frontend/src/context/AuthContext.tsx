import { useEffect, useState } from 'react';
import type { ReactNode } from 'react';

import { AuthContext } from './authContextDef';
import { authApi } from '../services/authApi';
import type { CurrentUser } from '../services/authApi';

export function AuthProvider({ children }: { children: ReactNode }) {
  const [currentUser, setCurrentUser] = useState<CurrentUser | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    authApi.me().then(user => {
      setCurrentUser(user);
      setLoading(false);
    });
  }, []);

  async function login(email: string, password: string) {
    const user = await authApi.login(email, password);
    setCurrentUser(user);
  }

  async function logout() {
    await authApi.logout();
    setCurrentUser(null);
  }

  return (
    <AuthContext.Provider value={{ currentUser, loading, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
}
