import { createContext } from 'react';

import type { CurrentUser } from '../services/authApi';

export interface AuthContextValue {
  currentUser: CurrentUser | null;
  loading: boolean;
  login: (email: string, password: string) => Promise<void>;
  logout: () => Promise<void>;
}

export const AuthContext = createContext<AuthContextValue | null>(null);
