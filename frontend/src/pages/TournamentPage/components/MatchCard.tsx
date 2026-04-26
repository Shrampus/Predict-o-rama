import { useTranslation } from 'react-i18next';

import type { TournamentMatchPrediction } from '../../../services/predictionsApi';
import { useMatchPredictionForm } from '../hooks/useMatchPredictionForm';
import type { PredictMatchHandler } from '../types/predictionActions';
import TeamDisplay from '../../../components/ui/TeamDisplay';
import TimeBadge from '../../../components/ui/TimeBadge';
import WinnerButton from './WinnerButton';
import { deriveTimeStyle, formatKickoffTime } from '../utils/matchCardUtils';

type MatchCardProps = {
    match: TournamentMatchPrediction;
    onPredict?: PredictMatchHandler;
    isSaving?: boolean;
};

function getValidationKey(code: 'missing_scores' | 'missing_winner' | 'invalid_winner_for_score'): string {
    return {
        missing_scores: 'matchCard.enterBothScores',
        missing_winner: 'matchCard.selectWinner',
        invalid_winner_for_score: 'matchCard.invalidWinnerForScore',
    }[code];
}

function MatchCard({ match, onPredict, isSaving = false }: MatchCardProps) {
    const { t } = useTranslation();
    const {
        homeScore,
        awayScore,
        winningTeam,
        isLocked,
        isDirty,
        hasExistingPrediction,
        showSuccessMessage,
        showSuccessHighlight,
        inlineError,
        validationErrorCode,
        canSubmit,
        setHomeScore,
        setAwayScore,
        selectWinner,
        blurHomeScore,
        blurAwayScore,
        submit,
    } = useMatchPredictionForm({
        match,
        isSaving,
        onPredict,
        defaultSaveErrorMessage: t('matchCard.saveFailed'),
    });
    const validationMessage = validationErrorCode ? t(getValidationKey(validationErrorCode)) : null;
    const helperText = isLocked ? t('matchCard.predictionClosedHelper') : t('matchCard.editUntilKickoff');
    const ctaLabel = isLocked
        ? t('matchCard.predictionClosed')
        : isSaving
            ? t('matchCard.saving')
            : hasExistingPrediction && !isDirty
                ? t('matchCard.saved')
                : hasExistingPrediction
                    ? t('matchCard.updatePrediction')
                    : t('matchCard.savePrediction');
    const ctaTone = isLocked
        ? 'bg-slate-400 text-white'
        : hasExistingPrediction && !isDirty
            ? 'bg-green-700 text-white'
            : 'bg-orange-600 text-white hover:bg-orange-700';
    const shouldShowGroupIdentifier =
        Boolean(match.groupIdentifier) && !match.roundIdentifier?.includes(match.groupIdentifier ?? '');

    return (
        <div className="relative overflow-hidden rounded-xl bg-white p-6 shadow-sm group flex flex-col sm:flex-row items-center gap-8">
            <div className="absolute top-0 left-0 w-1 h-full bg-green-700 opacity-0 group-hover:opacity-100 transition-opacity" />

            <TeamDisplay
                imageUrl={match.homeTeamImage}
                name={match.homeTeamName}
                align="right"
            />

            <div
                className={`flex w-full max-w-sm flex-col items-center gap-3 rounded-2xl p-4 transition-colors ${showSuccessHighlight ? 'bg-green-100' : 'bg-slate-50'}`}
            >
                {match.roundIdentifier ? (
                    <p className="text-[11px] font-bold uppercase tracking-widest text-slate-500">
                        {match.roundIdentifier}
                    </p>
                ) : null}
                {shouldShowGroupIdentifier ? (
                    <p className="text-[11px] text-slate-400">
                        {match.groupIdentifier}
                    </p>
                ) : null}
                <div className="flex w-full items-center justify-center gap-3">
                    <input
                        className="h-14 w-16 rounded-xl border border-slate-200 bg-white text-center text-2xl font-black focus:outline-none focus:ring-2 focus:ring-green-400 disabled:cursor-not-allowed disabled:bg-slate-100 disabled:text-slate-400"
                        type="number"
                        min={0}
                        inputMode="numeric"
                        value={homeScore}
                        onChange={(e) => setHomeScore(e.target.value)}
                        onBlur={blurHomeScore}
                        aria-label={t('matchCard.scoreInputAria', { team: match.homeTeamName })}
                        aria-invalid={Boolean(validationMessage)}
                        disabled={isSaving || isLocked}
                    />
                    <span
                        className="text-slate-400 font-bold text-2xl"
                        aria-hidden="true"
                    >
                        -
                    </span>
                    <input
                        className="h-14 w-16 rounded-xl border border-slate-200 bg-white text-center text-2xl font-black focus:outline-none focus:ring-2 focus:ring-green-400 disabled:cursor-not-allowed disabled:bg-slate-100 disabled:text-slate-400"
                        type="number"
                        min={0}
                        inputMode="numeric"
                        value={awayScore}
                        onChange={(e) => setAwayScore(e.target.value)}
                        onBlur={blurAwayScore}
                        aria-label={t('matchCard.scoreInputAria', { team: match.awayTeamName })}
                        aria-invalid={Boolean(validationMessage)}
                        disabled={isSaving || isLocked}
                    />
                </div>

                <div className="flex w-full gap-2">
                    <WinnerButton
                        isActive={winningTeam === 'Home'}
                        onClick={() => selectWinner('Home')}
                        disabled={isSaving || isLocked}
                    >
                        {match.homeTeamName}
                    </WinnerButton>
                    <WinnerButton
                        isActive={winningTeam === 'Draw'}
                        onClick={() => selectWinner('Draw')}
                        disabled={isSaving || isLocked}
                    >
                        {t('matchCard.draw')}
                    </WinnerButton>
                    <WinnerButton
                        isActive={winningTeam === 'Away'}
                        onClick={() => selectWinner('Away')}
                        disabled={isSaving || isLocked}
                    >
                        {match.awayTeamName}
                    </WinnerButton>
                </div>

                {inlineError ? (
                    <p className="w-full text-center text-xs text-red-600" role="alert">
                        {inlineError}
                    </p>
                ) : validationMessage ? (
                    <p className="w-full text-center text-xs text-red-600" role="alert">
                        {validationMessage}
                    </p>
                ) : showSuccessMessage && !isDirty ? (
                    <p className="w-full text-center text-xs text-green-700">{t('matchCard.savedInline')}</p>
                ) : (
                    <p className="w-full text-center text-xs text-slate-500">{helperText}</p>
                )}

                <button
                    type="button"
                    onClick={submit}
                    disabled={!canSubmit}
                    className={`w-full rounded-full px-6 py-2 text-xs font-bold uppercase tracking-widest transition-transform active:scale-95 ${ctaTone} ${!canSubmit ? 'cursor-not-allowed opacity-60' : ''}`}
                >
                    {ctaLabel}
                </button>
            </div>

            <TeamDisplay
                imageUrl={match.awayTeamImage}
                name={match.awayTeamName}
                align="left"
            />

            <TimeBadge time={formatKickoffTime(match.kickoffTime)} timeStyle={deriveTimeStyle(match.matchStatus)} />
        </div>
    );
}

export default MatchCard;
