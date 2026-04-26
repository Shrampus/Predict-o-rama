import { useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';

import type { GroupTournamentResponse } from '../../../services/groupApi';
import { getPredictions } from '../../../services/predictionsApi';
import type { TournamentMatchPrediction } from '../../../services/predictionsApi';
import PredictionResultsCard from '../../../components/predictionResults/PredictionResultsCard';

type LoadedEntry = { matches: TournamentMatchPrediction[]; error: boolean };

type Props = {
  groupId: string;
  tournaments: GroupTournamentResponse[];
  isLoadingTournaments: boolean;
};

export function GroupPredictionResultsSection({ groupId, tournaments, isLoadingTournaments }: Props) {
  const { t } = useTranslation();
  const [loadedData, setLoadedData] = useState<Record<string, LoadedEntry>>({});

  useEffect(() => {
    const playable = tournaments.filter((t) => t.competitionCode);
    playable.forEach((tournament) => {
      const key = `${groupId}:${tournament.id}`;
      getPredictions(tournament.competitionCode!, groupId)
        .then((response) => {
          const completed = response.matches.filter((m) => m.matchStatus === 'COMPLETED');
          setLoadedData((prev) => ({ ...prev, [key]: { matches: completed, error: false } }));
        })
        .catch(() => {
          setLoadedData((prev) => ({ ...prev, [key]: { matches: [], error: true } }));
        });
    });
  }, [groupId, tournaments]);

  const playable = tournaments.filter((t) => t.competitionCode);

  return (
    <section className="bg-white border border-slate-200 rounded-2xl p-5 sm:p-6 shadow-sm space-y-4">
      <h2 className="text-xl font-bold text-slate-900">{t('predictionResults.title')}</h2>

      {isLoadingTournaments && (
        <p className="text-sm text-slate-500">{t('predictionResults.loading')}</p>
      )}

      {!isLoadingTournaments && playable.length === 0 && (
        <p className="text-sm text-slate-500">{t('predictionResults.noTournaments')}</p>
      )}

      {!isLoadingTournaments && playable.map((tournament) => {
        const data = loadedData[`${groupId}:${tournament.id}`];
        return (
          <div key={tournament.id}>
            {data?.error && (
              <p className="text-sm text-red-500">{t('tournament.fetchError')}</p>
            )}
            {!data?.error && !data && (
              <div className="bg-slate-50 rounded-2xl p-6">
                <h4 className="text-base font-black uppercase tracking-tight mb-2">{tournament.name}</h4>
                <p className="text-sm text-slate-400">{t('predictionResults.loading')}</p>
              </div>
            )}
            {!data?.error && data && (
              <PredictionResultsCard matches={data.matches} tournamentName={tournament.name} />
            )}
          </div>
        );
      })}
    </section>
  );
}
