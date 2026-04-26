import { useTranslation } from 'react-i18next';
import { Link } from 'react-router-dom';

import { ROUTE_PATHS } from '../../app/routePaths';
import { useInvitePage } from './hooks/useInvitePage';

export default function InvitePage() {
  const { t } = useTranslation();
  const { pageState, isJoining, joinError, isLoggedIn, handleJoin, handleLoginToJoin } =
    useInvitePage();

  if (pageState.status === 'loading') {
    return (
      <div className="flex min-h-screen items-center justify-center">
        <p className="text-gray-500">{t('invite.loading')}</p>
      </div>
    );
  }

  if (pageState.status === 'not_found') {
    return (
      <div className="flex min-h-screen items-center justify-center">
        <div className="text-center">
          <h1 className="mb-2 text-xl font-semibold text-gray-800">{t('invite.invalidLink')}</h1>
          <p className="text-sm text-gray-500">{t('invite.invalidLinkDesc')}</p>
        </div>
      </div>
    );
  }

  if (pageState.status === 'load_error') {
    return (
      <div className="flex min-h-screen items-center justify-center">
        <div className="text-center">
          <h1 className="mb-2 text-xl font-semibold text-gray-800">{t('invite.loadError')}</h1>
          <p className="text-sm text-gray-500">{t('invite.loadErrorDesc')}</p>
        </div>
      </div>
    );
  }

  if (pageState.status === 'already_member') {
    return (
      <div className="flex min-h-screen items-center justify-center">
        <div className="text-center">
          <p className="mb-4 text-gray-700">{t('invite.alreadyMember')}</p>
          <Link
            to={ROUTE_PATHS.groups}
            className="rounded-lg bg-green-700 px-5 py-2 text-sm font-semibold text-white hover:bg-green-800"
          >
            {t('invite.goToGroups')}
          </Link>
        </div>
      </div>
    );
  }

  if (pageState.status === 'joined') {
    return null;
  }

  const { preview } = pageState;

  return (
    <div className="flex min-h-screen items-start justify-center bg-gray-50 px-4 py-16">
      <div className="w-full max-w-md overflow-hidden rounded-2xl shadow-lg">
        <header className="relative overflow-hidden">
          <div className="absolute inset-0 bg-gradient-to-r from-green-900 via-green-800 to-emerald-700" />
          <div className="relative z-10 px-6 py-7 sm:px-7">
            <p className="text-xs font-bold tracking-[0.14em] text-green-200">
              {t('invite.admin')}: {preview.adminName}
            </p>
            <h1 className="mt-2 text-3xl font-black tracking-tight text-white sm:text-4xl">
              {preview.name}
            </h1>
          </div>
        </header>

        <div className="bg-white px-6 py-5 sm:px-7">
          <h2 className="mb-2 text-xs font-bold uppercase tracking-[0.14em] text-gray-500">
            {t('invite.tournaments')}
          </h2>
          {preview.tournamentNames.length === 0 ? (
            <p className="text-sm text-gray-400">{t('invite.noTournaments')}</p>
          ) : (
            <ul className="space-y-1">
              {preview.tournamentNames.map(name => (
                <li key={name} className="text-sm text-gray-700">
                  {name}
                </li>
              ))}
            </ul>
          )}
        </div>

        <div className="border-t border-gray-100 bg-white px-6 py-5 sm:px-7">
          {joinError && <p className="mb-3 text-sm text-red-600">{t(joinError)}</p>}
          {isLoggedIn ? (
            <button
              onClick={handleJoin}
              disabled={isJoining}
              className="w-full rounded-lg bg-green-700 px-5 py-2 text-sm font-semibold text-white hover:bg-green-800 disabled:opacity-50"
            >
              {isJoining ? t('invite.joining') : t('invite.joinGroup')}
            </button>
          ) : (
            <button
              onClick={handleLoginToJoin}
              className="w-full rounded-lg bg-green-700 px-5 py-2 text-sm font-semibold text-white hover:bg-green-800"
            >
              {t('invite.loginToJoin')}
            </button>
          )}
        </div>
      </div>
    </div>
  );
}
