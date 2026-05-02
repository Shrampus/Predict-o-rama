import { useTranslation } from 'react-i18next';

import type { TournamentMatchPrediction } from '../../services/predictionsApi';

interface Props {
  matches: TournamentMatchPrediction[];
  tournamentName: string;
}

function PointsBadge({ points }: { points: number | null }) {
  const { t } = useTranslation();

  if (points === null) {
    return (
      <span className="text-slate-400 text-sm font-medium">
        {t('predictionResults.pending')}
      </span>
    );
  }

  const colour =
    points === 3
      ? 'bg-green-100 text-green-800'
      : points === 1
        ? 'bg-yellow-100 text-yellow-800'
        : 'bg-slate-100 text-slate-500';

  return (
    <span className={`text-xs font-bold px-2 py-0.5 rounded-full ${colour}`}>
      {points} {t('predictionResults.points')}
    </span>
  );
}

function WinnerLabel({ winner }: { winner: 'HOME' | 'AWAY' | 'DRAW' | null }) {
  const { t } = useTranslation();
  if (!winner) return null;
  if (winner === 'DRAW') return <span className="text-slate-500 text-xs">{t('matchCard.draw')}</span>;
  if (winner === 'HOME') return <span className="text-slate-500 text-xs">{t('matchCard.home')}</span>;
  return <span className="text-slate-500 text-xs">{t('matchCard.away')}</span>;
}

function PredictionResultsCard({ matches, tournamentName }: Props) {
  const { t } = useTranslation();

  if (matches.length === 0) {
    return (
      <div className="bg-slate-50 rounded-2xl p-6">
        <h2 className="text-xl font-black mb-2 uppercase tracking-tight">{tournamentName}</h2>
        <p className="text-slate-500 text-sm">{t('predictionResults.noResults')}</p>
      </div>
    );
  }

  return (
    <div className="bg-slate-50 rounded-2xl p-6">
      <h2 className="text-xl font-black mb-4 uppercase tracking-tight">{tournamentName}</h2>
      <div className="space-y-3">
        {matches.map((match) => (
          <div key={match.matchId} className="bg-white rounded-xl p-4">
            <div className="flex items-center justify-between gap-4">
              <div className="flex items-center gap-2 flex-1 min-w-0">
                {match.homeTeamImage && (
                  <img src={match.homeTeamImage} alt={match.homeTeamName} className="w-6 h-6 object-contain shrink-0" />
                )}
                <span className="font-semibold text-sm truncate">{match.homeTeamName}</span>
              </div>

              <div className="flex flex-col items-center gap-1 shrink-0">
                <span className="text-slate-400 text-xs uppercase tracking-wide">
                  {t('predictionResults.actual')}
                </span>
                <span className="font-black text-lg tabular-nums">
                  {match.actualHomeScore ?? '?'} {t('predictionResults.scoreSeparator')} {match.actualAwayScore ?? '?'}
                </span>
              </div>

              <div className="flex items-center gap-2 flex-1 min-w-0 justify-end">
                <span className="font-semibold text-sm truncate text-right">{match.awayTeamName}</span>
                {match.awayTeamImage && (
                  <img src={match.awayTeamImage} alt={match.awayTeamName} className="w-6 h-6 object-contain shrink-0" />
                )}
              </div>
            </div>

            <div className="mt-3 pt-3 border-t border-slate-100 flex items-center justify-between gap-2">
              <div className="flex items-center gap-2 text-slate-500 text-sm">
                <span className="text-xs text-slate-400">{t('predictionResults.yourPrediction')}:</span>
                {match.predictionId ? (
                  <>
                    <span className="font-medium tabular-nums">
                      {match.predictedHomeScore ?? '?'} {t('predictionResults.scoreSeparator')} {match.predictedAwayScore ?? '?'}
                    </span>
                    <WinnerLabel winner={match.predictedWinner} />
                  </>
                ) : (
                  <span className="italic text-slate-400 text-xs">{t('predictionResults.pending')}</span>
                )}
              </div>
              <PointsBadge points={match.predictionResult} />
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}

export default PredictionResultsCard;
