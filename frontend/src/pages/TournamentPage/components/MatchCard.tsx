import { useState } from 'react';
import { useTranslation } from 'react-i18next';

import type { TournamentMatchPrediction } from '../../../services/predictionsApi';
import type { WinningTeam } from '../TournamentConstants';
import TeamDisplay from './TeamDisplay';
import TimeBadge from './TimeBadge';
import WinnerButton from './WinnerButton';
import { buildPrediction, deriveTimeStyle, formatKickoffTime, } from '../utils/matchCardUtils';

type MatchCardProps = {
    match: TournamentMatchPrediction;
    onPredict?: (id: string, home: number, away: number, winningTeam: WinningTeam) => void | Promise<void>;
    isSaving?: boolean;
};

function isScoreInput(value: string): boolean {
    return /^\d*$/.test(value);
}

function parseScore(value: string): number | null {
    if (!/^\d+$/.test(value)) {
        return null;
    }

    const parsed = Number.parseInt(value, 10);
    return Number.isNaN(parsed) ? null : parsed;
}

function normalizeScoreInput(value: string): string {
    const parsedScore = parseScore(value);
    return parsedScore === null ? '' : String(parsedScore);
}

function MatchCard({ match, onPredict, isSaving = false, }: MatchCardProps) {
    const prediction = buildPrediction(match);
    const { t } = useTranslation();

    const [homeScore, setHomeScore] = useState(match.predictedHomeScore === null ? '' : String(match.predictedHomeScore));
    const [awayScore, setAwayScore] = useState(match.predictedAwayScore === null ? '' : String(match.predictedAwayScore));
    const [winningTeam, setWinningTeam] = useState<WinningTeam>(prediction.winningTeam);
    const [isHomeScoreTouched, setIsHomeScoreTouched] = useState(false);
    const [isAwayScoreTouched, setIsAwayScoreTouched] = useState(false);

    const parsedHomeScore = parseScore(homeScore);
    const parsedAwayScore = parseScore(awayScore);
    const canSubmitPrediction = parsedHomeScore !== null && parsedAwayScore !== null;
    const shouldShowScoreError = (isHomeScoreTouched || isAwayScoreTouched) && !canSubmitPrediction;

    function handlePredictClick() {
        const home = parseScore(homeScore);
        const away = parseScore(awayScore);
        if (home === null || away === null) {
            return;
        }

        onPredict?.(match.matchId, home, away, winningTeam);
    }

    return (
        <div className="relative overflow-hidden rounded-xl bg-white p-6 shadow-sm group flex flex-col sm:flex-row items-center gap-8">
            <div className="absolute top-0 left-0 w-1 h-full bg-green-700 opacity-0 group-hover:opacity-100 transition-opacity" />

            <TeamDisplay
                imageUrl={match.homeTeamImage}
                label=""
                name={match.homeTeamName}
                align="right"
            />

            {/* Score inputs and winner selector */}
            <div className="flex flex-col items-center gap-4 bg-slate-50 rounded-2xl p-4 min-w-[50px]">
                <div className="flex items-center gap-4">
                    <input
                        className="w-14 h-14 bg-white rounded-xl text-center text-2xl font-black border border-slate-200 focus:outline-none focus:ring-2 focus:ring-green-400"
                        type="number"
                        min={0}
                        inputMode="numeric"
                        value={homeScore}
                        onChange={(e) => {
                            const value = e.target.value;
                            if (!isScoreInput(value)) {
                                return;
                            }
                            setHomeScore(value);
                        }}
                        onBlur={() => {
                            setIsHomeScoreTouched(true);
                            setHomeScore((currentValue) => normalizeScoreInput(currentValue));
                        }}
                        aria-invalid={shouldShowScoreError}
                        disabled={isSaving}
                    />
                    <span className="text-slate-400 font-bold">{t('matchCard.vs')}</span>
                    <input
                        className="w-14 h-14 bg-white rounded-xl text-center text-2xl font-black border border-slate-200 focus:outline-none focus:ring-2 focus:ring-green-400"
                        type="number"
                        min={0}
                        inputMode="numeric"
                        value={awayScore}
                        onChange={(e) => {
                            const value = e.target.value;
                            if (!isScoreInput(value)) {
                                return;
                            }
                            setAwayScore(value);
                        }}
                        onBlur={() => {
                            setIsAwayScoreTouched(true);
                            setAwayScore((currentValue) => normalizeScoreInput(currentValue));
                        }}
                        aria-invalid={shouldShowScoreError}
                        disabled={isSaving}
                    />
                </div>
                {shouldShowScoreError && (
                    <p className="text-xs text-red-600">{t('matchCard.enterValidScore')}</p>
                )}

                {/* Winner selector */}
                <div className="flex w-full gap-10">
                    <WinnerButton
                        isActive={winningTeam === 'Home'}
                        onClick={() => setWinningTeam('Home')}
                    >
                        {match.homeTeamName}
                    </WinnerButton>
                    <WinnerButton
                        isActive={winningTeam === 'Draw'}
                        onClick={() => setWinningTeam('Draw')}
                    >
                        {t('matchCard.draw')}
                    </WinnerButton>
                    <WinnerButton
                        isActive={winningTeam === 'Away'}
                        onClick={() => setWinningTeam('Away')}
                    >
                        {match.awayTeamName}
                    </WinnerButton>
                </div>

                {/* Submit */}
                <button
                    onClick={handlePredictClick}
                    disabled={isSaving || !canSubmitPrediction}
                    className={`w-full px-6 py-2 rounded-full font-bold text-xs uppercase tracking-widest transition-transform active:scale-95 ${prediction.saved
                        ? 'bg-green-700 text-white'
                        : 'bg-orange-600 text-white hover:bg-orange-700'
                        } ${isSaving || !canSubmitPrediction ? 'opacity-60 cursor-not-allowed' : ''}`}
                >
                    {isSaving ? t('matchCard.saving') : prediction.saved ? t('matchCard.saved') : t('matchCard.predictNow')}
                </button>
            </div>

            <TeamDisplay
                imageUrl={match.awayTeamImage}
                label=""
                name={match.awayTeamName}
                align="left"
            />

            <TimeBadge time={formatKickoffTime(match.kickoffTime)} timeStyle={deriveTimeStyle(match.matchStatus)} />
        </div>
    );
}

export default MatchCard;
