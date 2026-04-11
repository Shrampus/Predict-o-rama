import { useState } from 'react';
import { useNavigate } from 'react-router-dom';

import { ROUTE_PATHS } from '../../app/routePaths';
import { useAuth } from '../../context/useAuth';

const TEST_USERS = [
  { email: 'alice@test.com', password: '***', role: 'ADMIN' },
  { email: 'bob@test.com', password: '***', role: 'USER' },
  { email: 'carol@test.com', password: '***', role: 'USER' },
  { email: 'dave@test.com', password: '***', role: 'USER' },
]

export default function LoginPage() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      await login(email, password);
      navigate(ROUTE_PATHS.home);
    } catch {
      setError('Invalid email or password.');
    } finally {
      setLoading(false);
    }
  }

  function fillUser(userEmail: string) {
    setEmail(userEmail);
    setPassword('predictorama123');
    setError('');
  }

  return (
    <div className="flex min-h-screen items-center justify-center bg-gray-50 px-4">
      <div className="w-full max-w-sm">
        <h1 className="mb-6 text-center text-2xl font-semibold">Predict-o-rama</h1>

        <form onSubmit={handleSubmit} className="mb-6 space-y-4">
          <div>
            <label className="mb-1 block text-sm font-medium text-gray-700" htmlFor="email">
              Email
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
              Password
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
            {loading ? 'Signing in…' : 'Sign in'}
          </button>
        </form>

        <div className="rounded border border-gray-200 bg-white p-4">
          <p className="mb-2 text-xs font-semibold uppercase tracking-wide text-gray-500">
            Dev accounts — click to fill
          </p>
          <table className="w-full text-xs">
            <thead>
              <tr className="text-left text-gray-400">
                <th className="pb-1 pr-2">Email</th>
                <th className="pb-1 pr-2">Password</th>
                <th className="pb-1">Role</th>
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
