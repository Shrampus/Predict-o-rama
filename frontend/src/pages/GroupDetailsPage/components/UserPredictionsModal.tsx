import { useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';

import PredictionResultsCard from '../../../components/predictionResults/PredictionResultsCard';
import { getMemberPredictions } from '../../../services/groupApi';
import type { TournamentMatchPrediction } from '../../../services/predictionsApi';

interface Props {
  groupId: string;
  userId: string;
  userName: string;
  competitionCode: string;
  tournamentName: string;
  onClose: () => void;
}

function UserPredictionsModal({ groupId, userId, userName, competitionCode, tournamentName, onClose }: Props) {
  const { t } = useTranslation();
  const [matches, setMatches] = useState<TournamentMatchPrediction[] | null>(null);
  const [hasError, setHasError] = useState(false);

  useEffect(() => {
    getMemberPredictions(groupId, userId, competitionCode)
      .then((response) => {
        setMatches(response.matches.filter((m) => m.matchStatus === 'COMPLETED'));
      })
      .catch(() => setHasError(true));
  }, [groupId, userId, competitionCode]);

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50"
      onClick={onClose}
    >
      <div
        className="bg-white rounded-2xl shadow-xl w-full max-w-lg max-h-[85vh] flex flex-col"
        onClick={(e) => e.stopPropagation()}
      >
        <div className="flex items-center justify-between px-6 py-4 border-b border-slate-200 shrink-0">
          <h2 className="font-bold text-slate-900 truncate">
            {t('groups.leaderboardsSection.viewPredictions', { name: userName })}
          </h2>
          <button
            onClick={onClose}
            className="ml-4 shrink-0 text-slate-400 hover:text-slate-600 text-xl font-bold leading-none"
            aria-label={t('common.close')}
          >
            {t('common.closeIcon')}
          </button>
        </div>

        <div className="overflow-y-auto p-4">
          {hasError && (
            <p className="text-sm text-red-500 p-2">{t('tournament.fetchError')}</p>
          )}
          {!hasError && matches === null && (
            <p className="text-sm text-slate-400 p-2">{t('predictionResults.loading')}</p>
          )}
          {!hasError && matches !== null && (
            <PredictionResultsCard matches={matches} tournamentName={tournamentName} />
          )}
        </div>
      </div>
    </div>
  );
}

export default UserPredictionsModal;
