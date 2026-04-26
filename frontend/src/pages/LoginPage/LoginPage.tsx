import { GoogleLogin } from '@react-oauth/google';
import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { Navigate, useLocation, useNavigate } from 'react-router-dom';

import { ROUTE_PATHS } from '../../app/routePaths';
import { useAuth } from '../../context/useAuth';

type TestUser = {
  email: string;
  password: string;
  role: 'ADMIN' | 'USER';
};

const TEST_USERS: TestUser[] = [
  { email: 'alice@test.com', password: '***', role: 'ADMIN' },
  { email: 'bob@test.com', password: '***', role: 'USER' },
  { email: 'carol@test.com', password: '***', role: 'USER' },
  { email: 'dave@test.com', password: '***', role: 'USER' },
];

export default function LoginPage() {
  const { currentUser, login, googleLogin, needsOnboarding } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const { t } = useTranslation();
  const from = (location.state as { from?: string } | null)?.from ?? null;
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      const { needsOnboarding } = await login(email, password);
      navigate(needsOnboarding ? ROUTE_PATHS.onboarding : (from ?? ROUTE_PATHS.home));
    } catch {
      setError(t('login.error'));
    } finally {
      setLoading(false);
    }
  }

  async function handleGoogleSuccess(idToken: string) {
    setError('');
    setLoading(true);
    try {
      const { needsOnboarding } = await googleLogin(idToken);
      navigate(needsOnboarding ? ROUTE_PATHS.onboarding : (from ?? ROUTE_PATHS.home));
    } catch {
      setError(t('login.googleError'));
    } finally {
      setLoading(false);
    }
  }

  function fillUser(userEmail: string) {
    setEmail(userEmail);
    setPassword('predictorama123');
    setError('');
  }

  if (currentUser) {
    return <Navigate to={needsOnboarding ? ROUTE_PATHS.onboarding : (from ?? ROUTE_PATHS.home)} replace />;
  }

  return (
    <div className="mx-auto w-full max-w-md px-4 py-10">
      <div className="w-full">
        <h1 className="mb-6 text-center text-2xl font-semibold">{t('nav.appName')}</h1>

        <form onSubmit={handleSubmit} className="mb-4 space-y-4">
          <div>
            <label className="mb-1 block text-sm font-medium text-gray-700" htmlFor="email">
              {t('login.email')}
            </label>
            <input
              id="email"
              type="email"
              required
              value={email}
              onChange={e => setEmail(e.target.value)}
              className="w-full rounded border border-gray-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
          </div>

          <div>
            <label className="mb-1 block text-sm font-medium text-gray-700" htmlFor="password">
              {t('login.password')}
            </label>
            <input
              id="password"
              type="password"
              required
              value={password}
              onChange={e => setPassword(e.target.value)}
              className="w-full rounded border border-gray-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
          </div>

          {error && <p className="text-sm text-red-600">{error}</p>}

          <button
            type="submit"
            disabled={loading}
            className="w-full rounded bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700 disabled:opacity-50"
          >
            {loading ? t('login.signingIn') : t('login.signIn')}
          </button>
        </form>

        <div className="mb-6 flex items-center gap-3">
          <div className="h-px flex-1 bg-gray-200" />
          <span className="text-xs text-gray-400">{t('login.or')}</span>
          <div className="h-px flex-1 bg-gray-200" />
        </div>

        <div className="mb-6 flex justify-center">
          <GoogleLogin
            onSuccess={(response: { credential?: string }) => {
              if (response.credential) handleGoogleSuccess(response.credential);
            }}
            onError={() => setError(t('login.googleError'))}
          />
        </div>

        <div className="rounded border border-gray-200 bg-white p-4">
          <p className="mb-2 text-xs font-semibold uppercase tracking-wide text-gray-500">
            {t('login.devAccounts')}
          </p>
          <table className="w-full text-xs">
            <thead>
              <tr className="text-left text-gray-400">
                <th className="pb-1 pr-2">{t('login.email')}</th>
                <th className="pb-1 pr-2">{t('login.password')}</th>
                <th className="pb-1">{t('login.role')}</th>
              </tr>
            </thead>
            <tbody>
              {TEST_USERS.map(u => (
                <tr
                  key={u.email}
                  className="cursor-pointer hover:bg-gray-50"
                  onClick={() => fillUser(u.email)}
                >
                  <td className="py-0.5 pr-2 text-blue-600">{u.email}</td>
                  <td className="py-0.5 pr-2 text-gray-500">{u.password}</td>
                  <td className="py-0.5 text-gray-500">{u.role}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
