import { fireEvent, render, waitFor } from '@testing-library/react';
import { beforeEach, describe, expect, it, vi } from 'vitest';

import { TOKEN_KEY } from '../lib/apiClient';
import { authApi } from '../services/authApi';
import type { AuthResponse, CurrentUser } from '../services/authApi';
import { AuthProvider } from './AuthContext';
import { useAuth } from './useAuth';

vi.mock('../services/authApi', () => ({
  authApi: {
    login: vi.fn(),
    googleLogin: vi.fn(),
    me: vi.fn(),
    completeProfile: vi.fn(),
  },
}));

function makeJwt(payload: object): string {
  const encodedPayload = btoa(JSON.stringify(payload));
  return `header.${encodedPayload}.signature`;
}

function AuthConsumer() {
  const { currentUser, loading, needsOnboarding, login, completeProfile, logout } = useAuth();

  return (
    <div>
      <div data-testid="loading">{String(loading)}</div>
      <div data-testid="needs-onboarding">{String(needsOnboarding)}</div>
      <div data-testid="current-user">{currentUser ? currentUser.email : 'null'}</div>
      <div data-testid="username">{currentUser?.username ?? 'null'}</div>
      <button onClick={() => login('user@example.com', 'password')}>login</button>
      <button onClick={() => completeProfile('alice')}>complete-profile</button>
      <button onClick={logout}>logout</button>
    </div>
  );
}

const mockedAuthApi = vi.mocked(authApi);

function renderAuthProvider() {
  return render(
    <AuthProvider>
      <AuthConsumer />
    </AuthProvider>,
  );
}

const baseUser: CurrentUser = {
  id: 'u1',
  username: null,
  email: 'user@example.com',
  systemRole: 'USER',
};

describe('AuthContext', () => {
  beforeEach(() => {
    localStorage.clear();
    vi.clearAllMocks();
    mockedAuthApi.me.mockResolvedValue(null);
  });

  it('starts unauthenticated when no token is stored', async () => {
    const view = renderAuthProvider();

    await waitFor(() => {
      expect(view.getByTestId('loading')).toHaveTextContent('false');
    });

    expect(mockedAuthApi.me).not.toHaveBeenCalled();
    expect(view.getByTestId('current-user')).toHaveTextContent('null');
    expect(view.getByTestId('needs-onboarding')).toHaveTextContent('false');
  });

  it('login stores token and user in context', async () => {
    const loginResponse: AuthResponse = {
      token: makeJwt({ needsOnboarding: true }),
      status: 'NEEDS_ONBOARDING',
      user: baseUser,
    };
    mockedAuthApi.login.mockResolvedValue(loginResponse);

    const view = renderAuthProvider();
    fireEvent.click(view.getByRole('button', { name: 'login' }));

    await waitFor(() => {
      expect(localStorage.getItem(TOKEN_KEY)).toBe(loginResponse.token);
      expect(view.getByTestId('current-user')).toHaveTextContent('user@example.com');
      expect(view.getByTestId('needs-onboarding')).toHaveTextContent('true');
    });
  });

  it('logout clears token, user, and onboarding flag', async () => {
    const loginResponse: AuthResponse = {
      token: makeJwt({ needsOnboarding: true }),
      status: 'NEEDS_ONBOARDING',
      user: baseUser,
    };
    mockedAuthApi.login.mockResolvedValue(loginResponse);

    const view = renderAuthProvider();
    fireEvent.click(view.getByRole('button', { name: 'login' }));

    await waitFor(() => {
      expect(view.getByTestId('current-user')).toHaveTextContent('user@example.com');
      expect(view.getByTestId('needs-onboarding')).toHaveTextContent('true');
    });

    fireEvent.click(view.getByRole('button', { name: 'logout' }));

    expect(localStorage.getItem(TOKEN_KEY)).toBeNull();
    expect(view.getByTestId('current-user')).toHaveTextContent('null');
    expect(view.getByTestId('needs-onboarding')).toHaveTextContent('false');
  });

  it('hydrates current user and needsOnboarding from stored JWT', async () => {
    localStorage.setItem(TOKEN_KEY, makeJwt({ needsOnboarding: true }));
    mockedAuthApi.me.mockResolvedValue(baseUser);

    const view = renderAuthProvider();

    await waitFor(() => {
      expect(mockedAuthApi.me).toHaveBeenCalledTimes(1);
      expect(view.getByTestId('loading')).toHaveTextContent('false');
      expect(view.getByTestId('current-user')).toHaveTextContent('user@example.com');
      expect(view.getByTestId('needs-onboarding')).toHaveTextContent('true');
    });
  });

  it('completeProfile updates user and clears needsOnboarding', async () => {
    const loginResponse: AuthResponse = {
      token: makeJwt({ needsOnboarding: true }),
      status: 'NEEDS_ONBOARDING',
      user: baseUser,
    };
    const completedUser: CurrentUser = {
      ...baseUser,
      username: 'alice',
    };
    const completeProfileResponse: AuthResponse = {
      token: makeJwt({ needsOnboarding: false }),
      status: 'OK',
      user: completedUser,
    };

    mockedAuthApi.login.mockResolvedValue(loginResponse);
    mockedAuthApi.completeProfile.mockResolvedValue(completeProfileResponse);

    const view = renderAuthProvider();
    fireEvent.click(view.getByRole('button', { name: 'login' }));

    await waitFor(() => {
      expect(view.getByTestId('needs-onboarding')).toHaveTextContent('true');
    });

    fireEvent.click(view.getByRole('button', { name: 'complete-profile' }));

    await waitFor(() => {
      expect(view.getByTestId('username')).toHaveTextContent('alice');
      expect(view.getByTestId('needs-onboarding')).toHaveTextContent('false');
    });
  });
});
