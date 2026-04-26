import { useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';

import type { GroupTournamentResponse } from '../../../services/groupApi';
import { getPredictions } from '../../../services/predictionsApi';
import type { TournamentMatchPrediction } from '../../../services/predictionsApi';
import PredictionResultsCard from '../../../components/predictionResults/PredictionResultsCard';

type TournamentResult = {
  tournament: GroupTournamentResponse;
  matches: TournamentMatchPrediction[] | null;
  error: boolean;
};

type Props = {
  groupId: string;
  tournaments: GroupTournamentResponse[];
  isLoadingTournaments: boolean;
};

export function GroupPredictionResultsSection({ groupId, tournaments, isLoadingTournaments }: Props) {
  const { t } = useTranslation();
  const [results, setResults] = useState<TournamentResult[]>([]);

  useEffect(() => {
    const playable = tournaments.filter((t) => t.competitionCode);
    setResults(playable.map((tournament) => ({ tournament, matches: null, error: false })));

    playable.forEach((tournament) => {
      getPredictions(tournament.competitionCode!, groupId)
        .then((response) => {
          const completed = response.matches.filter((m) => m.matchStatus === 'COMPLETED');
          setResults((prev) =>
            prev.map((r) =>
              r.tournament.id === tournament.id ? { ...r, matches: completed } : r,
            ),
          );
        })
        .catch(() => {
          setResults((prev) =>
            prev.map((r) =>
              r.tournament.id === tournament.id ? { ...r, matches: [], error: true } : r,
            ),
          );
        });
    });
  }, [groupId, tournaments]);

  const playableCount = tournaments.filter((t) => t.competitionCode).length;

  return (
    <section className="bg-white border border-slate-200 rounded-2xl p-5 sm:p-6 shadow-sm space-y-4">
      <h2 className="text-xl font-bold text-slate-900">{t('predictionResults.title')}</h2>

      {isLoadingTournaments && (
        <p className="text-sm text-slate-500">{t('predictionResults.loading')}</p>
      )}

      {!isLoadingTournaments && playableCount === 0 && (
        <p className="text-sm text-slate-500">{t('predictionResults.noTournaments')}</p>
      )}

      {!isLoadingTournaments && results.map(({ tournament, matches, error }) => (
        <div key={tournament.id}>
          {error && (
            <p className="text-sm text-red-500">{t('tournament.fetchError')}</p>
          )}
          {!error && matches === null && (
            <div className="bg-slate-50 rounded-2xl p-6">
              <h4 className="text-base font-black uppercase tracking-tight mb-2">{tournament.name}</h4>
              <p className="text-sm text-slate-400">{t('predictionResults.loading')}</p>
            </div>
          )}
          {!error && matches !== null && (
            <PredictionResultsCard matches={matches} tournamentName={tournament.name} />
          )}
        </div>
      ))}
    </section>
  );
}
