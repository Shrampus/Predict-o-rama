import type { ReactNode } from 'react';
import { Navigate } from 'react-router-dom';

import { useAuth } from '../../context/useAuth';

export function RequireAuth({ children }: { children: ReactNode }) {
  const { currentUser, loading } = useAuth();
  if (loading) return null;
  if (!currentUser) return <Navigate to="/login" replace />;
  return <>{children}</>;
}
