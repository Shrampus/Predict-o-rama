import { createContext, useContext, useEffect, useState } from 'react';
import type { ReactNode } from 'react';
import { authApi } from '../services/authApi';
import type { CurrentUser } from '../services/authApi';

interface AuthContextValue {
  currentUser: CurrentUser | null;
  loading: boolean;
  login: (email: string, password: string) => Promise<void>;
  logout: () => Promise<void>;
}

const AuthContext = createContext<AuthContextValue | null>(null);

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

export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used inside AuthProvider');
  return ctx;
}
