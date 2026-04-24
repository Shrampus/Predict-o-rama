import { useTranslation } from 'react-i18next';
import { NavLink, useNavigate } from 'react-router-dom';

import { ROUTE_PATHS } from '../../app/routePaths';
import { useAuth } from '../../context/useAuth';

const LANGUAGES = [
  { code: 'en', label: 'EN' },
  { code: 'et', label: 'ET' },
  { code: 'ru', label: 'RU' },
];

export function AppNavbar() {
  const { currentUser, logout } = useAuth();
  const navigate = useNavigate();
  const { t, i18n } = useTranslation();

  async function handleLogout() {
    await logout();
    navigate(ROUTE_PATHS.home);
  }

  return (
    <header>
      <div className="mx-auto flex max-w-5xl flex-wrap items-center justify-between gap-3 px-4 py-3">
        <h1 className="text-lg font-semibold">
          <NavLink to={ROUTE_PATHS.home} end className="transition-opacity hover:opacity-80">
            {t('nav.appName')}
          </NavLink>
        </h1>

        <nav aria-label="Primary" className="flex items-center gap-2">
          <NavLink
            to={ROUTE_PATHS.home}
            end
            className={({ isActive }) =>
              `nav-link ${isActive ? 'nav-link-active' : 'nav-link-default'}`
            }
          >
            {t('nav.home')}
          </NavLink>

          {currentUser ? (
            <NavLink
              to={ROUTE_PATHS.groups}
              className={({ isActive }) =>
                `nav-link ${isActive ? 'nav-link-active' : 'nav-link-default'}`
              }
            >
              {t('nav.groups')}
            </NavLink>
          ) : null}
        </nav>

        <div className="flex items-center gap-3">
          <div className="flex items-center gap-1">
            {LANGUAGES.map(lang => (
              <button
                key={lang.code}
                onClick={() => i18n.changeLanguage(lang.code)}
                className={`rounded px-2 py-1 text-xs font-bold transition-colors ${
                  i18n.language === lang.code
                    ? 'bg-blue-600 text-white'
                    : 'text-gray-500 hover:text-gray-800'
                }`}
              >
                {lang.label}
              </button>
            ))}
          </div>

          {currentUser ? (
            <>
              <span className="text-sm text-gray-600">{currentUser.username}</span>
              <button
                type="button"
                onClick={handleLogout}
                className="rounded border border-gray-300 px-3 py-1 text-sm hover:bg-gray-100"
              >
                {t('nav.signOut')}
              </button>
            </>
          ) : (
            <NavLink
              to={ROUTE_PATHS.login}
              className={({ isActive }) =>
                `rounded border border-gray-300 px-3 py-1 text-sm hover:bg-gray-100 ${
                  isActive ? 'bg-gray-100' : ''
                }`
              }
            >
              {t('nav.login')}
            </NavLink>
          )}
        </div>
      </div>
    </header>
  );
}
