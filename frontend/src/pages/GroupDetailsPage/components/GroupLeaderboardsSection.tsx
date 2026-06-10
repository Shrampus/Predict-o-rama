import { useTranslation } from 'react-i18next';

import type { GroupLeaderboardResponse } from '../../../services/groupApi';
import { useLeaderboardModal } from '../hooks/useLeaderboardModal';
import UserPredictionsModal from './UserPredictionsModal';

type GroupLeaderboardsSectionProps = {
  groupId: string;
  leaderboards: GroupLeaderboardResponse[];
  isLoadingLeaderboards: boolean;
  leaderboardsError: string;
};

export function GroupLeaderboardsSection({
  groupId,
  leaderboards,
  isLoadingLeaderboards,
  leaderboardsError,
}: GroupLeaderboardsSectionProps) {
  const { t } = useTranslation();
  const { selectedUser, selectUser, clearSelectedUser } = useLeaderboardModal();

  return (
    <section className="bg-white border border-slate-200 rounded-2xl p-5 sm:p-6 shadow-sm space-y-4">
      <h2 className="text-xl font-bold text-slate-900">{t('groups.leaderboardsSection.title')}</h2>
      {isLoadingLeaderboards && (
        <p className="text-sm text-slate-500">
          {t('groups.leaderboardsSection.loadingLeaderboards')}
        </p>
      )}
      {leaderboardsError && <p className="text-sm text-red-600">{leaderboardsError}</p>}
      {!isLoadingLeaderboards && !leaderboardsError && leaderboards.length === 0 && (
        <p className="text-sm text-slate-500">
          {t('groups.leaderboardsSection.emptyLeaderboards')}
        </p>
      )}
      {!isLoadingLeaderboards && !leaderboardsError && leaderboards.length > 0 && (
        <div className="grid gap-4 lg:grid-cols-2">
          {leaderboards.map((leaderboard) => (
            <article
              key={leaderboard.tournamentId}
              className="border border-slate-200 rounded-xl bg-slate-50/70 overflow-hidden"
            >
              <div className="px-4 py-3 border-b border-slate-200 bg-white">
                <h3 className="text-sm font-bold text-slate-900">{leaderboard.tournamentName}</h3>
              </div>
              <ol className="divide-y divide-slate-200">
                {leaderboard.entries.map((entry, index) => (
                  <li
                    key={entry.userId}
                    className="grid grid-cols-[2.25rem_minmax(0,1fr)_auto] items-center gap-3 px-4 py-3 text-sm cursor-pointer hover:bg-slate-100 focus:bg-slate-100 focus:outline-none transition-colors"
                    role="button"
                    tabIndex={leaderboard.competitionCode ? 0 : -1}
                    onClick={() =>
                      leaderboard.competitionCode &&
                      selectUser({
                        userId: entry.userId,
                        userName: entry.name,
                        competitionCode: leaderboard.competitionCode!,
                        tournamentName: leaderboard.tournamentName,
                      })
                    }
                    onKeyDown={(e) => {
                      if ((e.key === 'Enter' || e.key === ' ') && leaderboard.competitionCode) {
                        e.preventDefault();
                        selectUser({
                          userId: entry.userId,
                          userName: entry.name,
                          competitionCode: leaderboard.competitionCode,
                          tournamentName: leaderboard.tournamentName,
                        });
                      }
                    }}
                  >
                    <span className="flex h-7 w-7 items-center justify-center rounded-full bg-white border border-slate-200 text-xs font-bold text-slate-600">
                      {index + 1}
                    </span>
                    <div className="min-w-0">
                      <p className="truncate font-semibold text-slate-800">{entry.name}</p>
                      <p className="text-xs text-slate-500">
                        {t('groups.leaderboardsSection.scoredPredictions', {
                          scored: entry.scoredPredictions,
                          total: entry.totalPredictions,
                        })}
                      </p>
                    </div>
                    <div className="text-right">
                      <p className="text-lg font-black text-green-700">{entry.totalScore}</p>
                      <p className="text-[11px] font-bold uppercase tracking-wide text-slate-500">
                        {t('groups.leaderboardsSection.points')}
                      </p>
                    </div>
                  </li>
                ))}
              </ol>
            </article>
          ))}
        </div>
      )}

      {selectedUser && (
        <UserPredictionsModal
          groupId={groupId}
          userId={selectedUser.userId}
          userName={selectedUser.userName}
          competitionCode={selectedUser.competitionCode}
          tournamentName={selectedUser.tournamentName}
          onClose={clearSelectedUser}
        />
      )}
    </section>
  );
}
