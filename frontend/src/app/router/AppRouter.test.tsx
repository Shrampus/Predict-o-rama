import { render } from '@testing-library/react';
import { describe, expect, it, vi } from 'vitest';
import { MemoryRouter, Outlet } from 'react-router-dom';

import { AuthContext } from '../../context/authContextDef';
import type { AuthContextValue } from '../../context/authContextDef';
import type { CurrentUser } from '../../services/authApi';
import { AppRouter } from './AppRouter';

vi.mock('../../components/layout/AppShell', () => ({
  AppShell: () => <Outlet />,
}));

vi.mock('../../components/layout/MainLayout', () => ({
  MainLayout: () => <Outlet />,
}));

vi.mock('../../pages/HomePage/HomePage', () => ({
  default: () => <div>HOME</div>,
}));

vi.mock('../../pages/LoginPage/LoginPage', () => ({
  default: () => <div>LOGIN</div>,
}));

vi.mock('../../pages/OnboardingPage/OnboardingPage', () => ({
  default: () => <div>ONBOARDING</div>,
}));

vi.mock('../../pages/GroupPage/GroupsPage', () => ({
  default: () => <div>GROUPS</div>,
}));

vi.mock('../../pages/GroupDetailsPage/GroupDetailsPage', () => ({
  default: () => <div>GROUP_DETAILS</div>,
}));

vi.mock('../../pages/TournamentPage/TournamentPage', () => ({
  default: () => <div>TOURNAMENT</div>,
}));

const baseUser: CurrentUser = {
  id: 'u1',
  username: null,
  email: 'user@example.com',
  systemRole: 'USER',
};

function renderWithAuth(path: string, authOverrides: Partial<AuthContextValue> = {}) {
  const authValue: AuthContextValue = {
    currentUser: null,
    loading: false,
    needsOnboarding: false,
    login: async () => ({ needsOnboarding: false }),
    googleLogin: async () => ({ needsOnboarding: false }),
    completeProfile: async () => {},
    logout: () => {},
    ...authOverrides,
  };

  return render(
    <AuthContext.Provider value={authValue}>
      <MemoryRouter initialEntries={[path]}>
        <AppRouter />
      </MemoryRouter>
    </AuthContext.Provider>,
  );
}

describe('AppRouter', () => {
  it('renders /login', () => {
    const view = renderWithAuth('/login');

    expect(view.getByText('LOGIN')).toBeInTheDocument();
  });

  it('redirects protected routes to / when unauthenticated', () => {
    const view = renderWithAuth('/groups', { currentUser: null, needsOnboarding: false });

    expect(view.getByText('HOME')).toBeInTheDocument();
    expect(view.queryByText('GROUPS')).not.toBeInTheDocument();
  });

  it('renders protected route for authenticated users who completed onboarding', () => {
    const view = renderWithAuth('/groups', {
      currentUser: baseUser,
      needsOnboarding: false,
    });

    expect(view.getByText('GROUPS')).toBeInTheDocument();
  });

  it('redirects authenticated users needing onboarding to /onboarding', () => {
    const view = renderWithAuth('/groups', { currentUser: baseUser, needsOnboarding: true });

    expect(view.getByText('ONBOARDING')).toBeInTheDocument();
    expect(view.queryByText('GROUPS')).not.toBeInTheDocument();
  });

  it('allows authenticated users needing onboarding to access /onboarding', () => {
    const view = renderWithAuth('/onboarding', {
      currentUser: baseUser,
      needsOnboarding: true,
    });

    expect(view.getByText('ONBOARDING')).toBeInTheDocument();
  });
});
