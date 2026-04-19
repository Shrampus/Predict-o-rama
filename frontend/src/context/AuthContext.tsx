import { useEffect, useState } from 'react';
import type { ReactNode } from 'react';

import { TOKEN_KEY } from '../lib/apiClient';
import { authApi } from '../services/authApi';
import type { AuthResponse, CurrentUser } from '../services/authApi';
import { AuthContext } from './authContextDef';

export function AuthProvider({ children }: { children: ReactNode }) {
  const [currentUser, setCurrentUser] = useState<CurrentUser | null>(null);
  const [loading, setLoading] = useState(() => !!localStorage.getItem(TOKEN_KEY));
  const [needsOnboarding, setNeedsOnboarding] = useState(false);

  useEffect(() => {
    const token = localStorage.getItem(TOKEN_KEY);
    if (!token) return;
    authApi.me().then(user => {
      if (user) {
        setCurrentUser(user);
        const payload = JSON.parse(atob(token.split('.')[1]));
        setNeedsOnboarding(payload.needsOnboarding === true);
      } else {
        localStorage.removeItem(TOKEN_KEY);
      }
      setLoading(false);
    });
  }, []);

  function applyAuthResponse(response: AuthResponse) {
    localStorage.setItem(TOKEN_KEY, response.token);
    setCurrentUser(response.user);
    const onboarding = response.status === 'NEEDS_ONBOARDING';
    setNeedsOnboarding(onboarding);
    return { needsOnboarding: onboarding };
  }

  async function login(email: string, password: string) {
    const response = await authApi.login(email, password);
    return applyAuthResponse(response);
  }

  async function googleLogin(idToken: string) {
    const response = await authApi.googleLogin(idToken);
    return applyAuthResponse(response);
  }

  async function completeProfile(username: string) {
    const response = await authApi.completeProfile(username);
    applyAuthResponse(response);
  }

  function logout() {
    localStorage.removeItem(TOKEN_KEY);
    setCurrentUser(null);
    setNeedsOnboarding(false);
  }

  return (
    <AuthContext.Provider value={{ currentUser, loading, needsOnboarding, login, googleLogin, completeProfile, logout }}>
      {children}
    </AuthContext.Provider>
  );
}
