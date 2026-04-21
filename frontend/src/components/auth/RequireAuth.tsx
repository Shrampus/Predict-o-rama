import type { ReactNode } from 'react';
import { Navigate, useLocation } from 'react-router-dom';

import { ROUTE_PATHS } from '../../app/routePaths';
import { useAuth } from '../../context/useAuth';

export function RequireAuth({ children }: { children: ReactNode }) {
  const { currentUser, loading, needsOnboarding } = useAuth();
  const location = useLocation();

  if (loading) return null;
  if (!currentUser) return <Navigate to={ROUTE_PATHS.login} replace />;
  if (needsOnboarding && location.pathname !== ROUTE_PATHS.onboarding) {
    return <Navigate to={ROUTE_PATHS.onboarding} replace />;
  }
  return <>{children}</>;
}
