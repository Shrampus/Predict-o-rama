import { useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';

import PredictionResultsCard from '../../components/predictionResults/PredictionResultsCard';
import { getMyGroups, getGroupTournaments } from '../../services/groupApi';
import type { MyGroupsResponse, GroupTournamentResponse } from '../../services/groupApi';
import { getPredictions } from '../../services/predictionsApi';
import type { TournamentMatchPrediction } from '../../services/predictionsApi';

type TournamentState = {
  tournament: GroupTournamentResponse;
  matches: TournamentMatchPrediction[] | null;
};

type GroupState = {
  group: MyGroupsResponse;
  tournaments: TournamentState[] | null;
};

function HomePage() {
  const { t } = useTranslation();
  const [groupStates, setGroupStates] = useState<GroupState[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    getMyGroups()
      .then((groups) => {
        setGroupStates(groups.map((group) => ({ group, tournaments: null })));

        groups.forEach((group) => {
          getGroupTournaments(group.groupId).then((tournaments) => {
            const playable = tournaments.filter((t) => t.competitionCode);

            setGroupStates((prev) =>
              prev.map((gs) =>
                gs.group.groupId === group.groupId
                  ? { ...gs, tournaments: playable.map((t) => ({ tournament: t, matches: null })) }
                  : gs,
              ),
            );

            playable.forEach((tournament) => {
              getPredictions(tournament.competitionCode!, group.groupId)
                .then((response) => {
                  const completed = response.matches.filter((m) => m.matchStatus === 'COMPLETED');
                  setGroupStates((prev) =>
                    prev.map((gs) =>
                      gs.group.groupId === group.groupId
                        ? {
                            ...gs,
                            tournaments:
                              gs.tournaments?.map((ts) =>
                                ts.tournament.id === tournament.id ? { ...ts, matches: completed } : ts,
                              ) ?? null,
                          }
                        : gs,
                    ),
                  );
                })
                .catch(() => {
                  setGroupStates((prev) =>
                    prev.map((gs) =>
                      gs.group.groupId === group.groupId
                        ? {
                            ...gs,
                            tournaments:
                              gs.tournaments?.map((ts) =>
                                ts.tournament.id === tournament.id ? { ...ts, matches: [] } : ts,
                              ) ?? null,
                          }
                        : gs,
                    ),
                  );
                });
            });
          });
        });
      })
      .finally(() => setLoading(false));
  }, []);

  if (loading) {
    return <div className="p-6 text-slate-500 text-sm">{t('predictionResults.loading')}</div>;
  }

  if (groupStates.length === 0) {
    return <div className="p-6 text-slate-500 text-sm">{t('predictionResults.noGroups')}</div>;
  }

  return (
    <div className="p-6 space-y-8 max-w-2xl mx-auto">
      <h1 className="text-2xl font-black uppercase tracking-tight">{t('predictionResults.title')}</h1>

      {groupStates.map(({ group, tournaments }) => (
        <div key={group.groupId} className="space-y-4">
          <h2 className="text-lg font-bold text-slate-700 border-b border-slate-200 pb-2">
            {group.name}
          </h2>

          {tournaments === null ? (
            <p className="text-slate-400 text-sm">{t('predictionResults.loading')}</p>
          ) : tournaments.length === 0 ? (
            <p className="text-slate-500 text-sm">{t('predictionResults.noTournaments')}</p>
          ) : (
            tournaments.map(({ tournament, matches }) =>
              matches === null ? (
                <div key={tournament.id} className="bg-slate-50 rounded-2xl p-6">
                  <h3 className="text-base font-black uppercase tracking-tight mb-2">
                    {tournament.name}
                  </h3>
                  <p className="text-slate-400 text-sm">{t('predictionResults.loading')}</p>
                </div>
              ) : (
                <PredictionResultsCard
                  key={tournament.id}
                  matches={matches}
                  tournamentName={tournament.name}
                />
              ),
            )
          )}
        </div>
      ))}
    </div>
  );
}

export default HomePage;
