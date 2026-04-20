import { createContext } from 'react';

import type { CurrentUser } from '../services/authApi';

export interface AuthContextValue {
  currentUser: CurrentUser | null;
  loading: boolean;
  needsOnboarding: boolean;
  login: (email: string, password: string) => Promise<{ needsOnboarding: boolean }>;
  googleLogin: (idToken: string) => Promise<{ needsOnboarding: boolean }>;
  completeProfile: (username: string) => Promise<void>;
  logout: () => void;
}

export const AuthContext = createContext<AuthContextValue | null>(null);
