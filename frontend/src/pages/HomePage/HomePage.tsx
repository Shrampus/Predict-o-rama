import { useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';

import { useUpcomingMatches } from './hooks/useUpcomingMatches';
import { useUpcomingMatchFilters } from './hooks/useUpcomingMatchFilters';
import UpcomingMatchList from './components/UpcomingMatchList';
import UpcomingMatchFilters from './components/UpcomingMatchFilters';
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

  const { upcomingMatches, isLoading, hasError } = useUpcomingMatches();
  const {
    filteredMatches,
    tournamentOptions,
    groupOptions,
    selectedTournament,
    selectedGroupId,
    selectedDays,
    setSelectedTournament,
    setSelectedGroupId,
    setSelectedDays,
  } = useUpcomingMatchFilters(upcomingMatches);

  const [groupStates, setGroupStates] = useState<GroupState[]>([]);
  const [resultsLoading, setResultsLoading] = useState(true);

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
      .finally(() => setResultsLoading(false));
  }, []);

  return (
    <div className="max-w-2xl mx-auto px-4 py-8 flex flex-col gap-10">
      <section className="flex flex-col gap-6">
        <h2 className="text-xl font-bold text-slate-800">{t('upcomingMatches.title')}</h2>
        <UpcomingMatchFilters
          tournamentOptions={tournamentOptions}
          groupOptions={groupOptions}
          selectedTournament={selectedTournament}
          selectedGroupId={selectedGroupId}
          selectedDays={selectedDays}
          onTournamentChange={setSelectedTournament}
          onGroupChange={setSelectedGroupId}
          onDaysChange={setSelectedDays}
        />
        <UpcomingMatchList matches={filteredMatches} isLoading={isLoading} hasError={hasError} />
      </section>

      <section className="flex flex-col gap-6">
        <h2 className="text-xl font-bold text-slate-800">{t('predictionResults.title')}</h2>

        {resultsLoading ? (
          <p className="text-slate-400 text-sm">{t('predictionResults.loading')}</p>
        ) : groupStates.length === 0 ? (
          <p className="text-slate-500 text-sm">{t('predictionResults.noGroups')}</p>
        ) : (
          groupStates.map(({ group, tournaments }) => (
            <div key={group.groupId} className="flex flex-col gap-4">
              <h3 className="text-lg font-bold text-slate-700 border-b border-slate-200 pb-2">
                {group.name}
              </h3>

              {tournaments === null ? (
                <p className="text-slate-400 text-sm">{t('predictionResults.loading')}</p>
              ) : tournaments.length === 0 ? (
                <p className="text-slate-500 text-sm">{t('predictionResults.noTournaments')}</p>
              ) : (
                tournaments.map(({ tournament, matches }) =>
                  matches === null ? (
                    <div key={tournament.id} className="bg-slate-50 rounded-2xl p-6">
                      <h4 className="text-base font-black uppercase tracking-tight mb-2">
                        {tournament.name}
                      </h4>
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
          ))
        )}
      </section>
    </div>
  );
}

export default HomePage;
