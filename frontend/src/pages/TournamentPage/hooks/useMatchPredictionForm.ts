import { useEffect, useMemo, useReducer, useRef } from 'react';

import type { TournamentMatchPrediction } from '../../../services/predictionsApi';
import type { WinningTeam } from '../TournamentConstants';
import type { PredictActionResult, PredictMatchHandler } from '../types/predictionActions';
import { mapApiWinnerToWinningTeam } from '../utils/predictionWinnerMapper';

type ValidationErrorCode = 'missing_scores' | 'missing_winner' | 'invalid_winner_for_score' | null;
type ValidationResult = {
    errorCode: ValidationErrorCode;
    parsedScores: {
        home: number | null;
        away: number | null;
    };
};
type TouchedState = {
    homeScore: boolean;
    awayScore: boolean;
    winner: boolean;
    submitted: boolean;
};
type BaselineState = {
    homeScore: string;
    awayScore: string;
    // Includes Home/Away winner and Draw outcome.
    winningTeam: WinningTeam | null;
    hasPrediction: boolean;
    baselineSignature: string;
};
type FormState = {
    homeScore: string;
    awayScore: string;
    winningTeam: WinningTeam | null;
    baseline: BaselineState;
    touched: TouchedState;
    inlineError: string | null;
    showSuccessMessage: boolean;
    showSuccessHighlight: boolean;
};
type SyncFromPropsAction = {
    type: 'sync_from_props';
    baseline: BaselineState;
};
type SetHomeScoreAction = {
    type: 'set_home_score';
    value: string;
};
type SetAwayScoreAction = {
    type: 'set_away_score';
    value: string;
};
type SelectWinnerAction = {
    type: 'select_winner';
    winner: WinningTeam;
};
type BlurHomeScoreAction = {
    type: 'blur_home_score';
};
type BlurAwayScoreAction = {
    type: 'blur_away_score';
};
type SubmitAttemptAction = {
    type: 'submit_attempt';
};
type SubmitFailureAction = {
    type: 'submit_failure';
    error: string;
};
type SubmitSuccessAction = {
    type: 'submit_success';
    baseline: BaselineState;
};
type HideSuccessAction = {
    type: 'hide_success_message';
};
type HideSuccessHighlightAction = {
    type: 'hide_success_highlight';
};
type FormAction =
    | SyncFromPropsAction
    | SetHomeScoreAction
    | SetAwayScoreAction
    | SelectWinnerAction
    | BlurHomeScoreAction
    | BlurAwayScoreAction
    | SubmitAttemptAction
    | SubmitFailureAction
    | SubmitSuccessAction
    | HideSuccessAction
    | HideSuccessHighlightAction;

const LOCKED_MATCH_STATUSES = new Set<TournamentMatchPrediction['matchStatus']>(['LIVE', 'IN_PLAY', 'PAUSED', 'FINISHED']);
const SUCCESS_MESSAGE_TIMEOUT_MS = 2200;
const SUCCESS_HIGHLIGHT_TIMEOUT_MS = 1400;

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

function isMatchLocked(match: TournamentMatchPrediction): boolean {
    if (LOCKED_MATCH_STATUSES.has(match.matchStatus)) {
        return true;
    }

    const kickoffTimestamp = Date.parse(match.kickoffTime);
    if (Number.isNaN(kickoffTimestamp)) {
        return false;
    }

    return Date.now() >= kickoffTimestamp;
}

/**
 * Business rule for mixed competition formats:
 * - Users predict normal-time score and a separate match outcome (Home/Away/Draw)
 * - If normal-time is tied, Draw/Home/Away are all valid outcomes
 *   (group-stage draw or knockout winner after penalties/extra time)
 * - If normal-time has a leader, outcome must match that leader
 */
function isWinnerValidForScore(
    homeScore: number,
    awayScore: number,
    winningTeam: WinningTeam
): boolean {
    if (homeScore === awayScore) {
        // Draw scores are valid for regular-season style outcomes.
        // Home/Away on tied normal-time scores are also valid (e.g. penalties).
        return true;
    }

    if (homeScore > awayScore) {
        return winningTeam === 'Home';
    }

    return winningTeam === 'Away';
}

function validatePredictionForm(homeScoreValue: string, awayScoreValue: string, winner: WinningTeam | null): ValidationResult {
    const homeScore = parseScore(homeScoreValue);
    const awayScore = parseScore(awayScoreValue);

    if (homeScore === null || awayScore === null) {
        return { errorCode: 'missing_scores', parsedScores: { home: homeScore, away: awayScore } };
    }

    if (winner === null) {
        return { errorCode: 'missing_winner', parsedScores: { home: homeScore, away: awayScore } };
    }

    if (!isWinnerValidForScore(homeScore, awayScore, winner)) {
        return { errorCode: 'invalid_winner_for_score', parsedScores: { home: homeScore, away: awayScore } };
    }

    return { errorCode: null, parsedScores: { home: homeScore, away: awayScore } };
}

function reconcileOutcomeForScores(
    homeScoreValue: string,
    awayScoreValue: string,
    currentOutcome: WinningTeam | null
): WinningTeam | null {
    const homeScore = parseScore(homeScoreValue);
    const awayScore = parseScore(awayScoreValue);
    if (homeScore === null || awayScore === null) {
        return currentOutcome;
    }
    if (homeScore === awayScore) {
        return currentOutcome;
    }

    return homeScore > awayScore ? 'Home' : 'Away';
}

function computeIsDirty(state: FormState): boolean {
    return (
        state.homeScore !== state.baseline.homeScore ||
        state.awayScore !== state.baseline.awayScore ||
        state.winningTeam !== state.baseline.winningTeam
    );
}

function createBaseline(match: TournamentMatchPrediction): BaselineState {
    return {
        homeScore: match.predictedHomeScore === null ? '' : String(match.predictedHomeScore),
        awayScore: match.predictedAwayScore === null ? '' : String(match.predictedAwayScore),
        winningTeam: mapApiWinnerToWinningTeam(match.predictedWinner),
        hasPrediction: match.predictionId !== null,
        baselineSignature: `${match.predictionId ?? ''}:${match.predictedHomeScore ?? ''}:${match.predictedAwayScore ?? ''}:${match.predictedWinner ?? ''}`,
    };
}

function reduceFormState(state: FormState, action: FormAction): FormState {
    switch (action.type) {
        case 'sync_from_props': {
            const isDirty = computeIsDirty(state);
            if (action.baseline.baselineSignature === state.baseline.baselineSignature) {
                return state;
            }

            // Keep unsaved local edits intact while still refreshing the persisted baseline.
            // If the form is clean, fully sync from incoming persisted values.
            return {
                ...state,
                baseline: action.baseline,
                homeScore: isDirty ? state.homeScore : action.baseline.homeScore,
                awayScore: isDirty ? state.awayScore : action.baseline.awayScore,
                winningTeam: isDirty ? state.winningTeam : action.baseline.winningTeam,
                touched: isDirty
                    ? state.touched
                    : {
                        homeScore: false,
                        awayScore: false,
                        winner: false,
                        submitted: false,
                    },
                inlineError: null,
            };
        }
        case 'set_home_score':
            return {
                ...state,
                homeScore: action.value,
                winningTeam: reconcileOutcomeForScores(action.value, state.awayScore, state.winningTeam),
            };
        case 'set_away_score':
            return {
                ...state,
                awayScore: action.value,
                winningTeam: reconcileOutcomeForScores(state.homeScore, action.value, state.winningTeam),
            };
        case 'select_winner':
            return {
                ...state,
                winningTeam: action.winner,
                touched: { ...state.touched, winner: true },
            };
        case 'blur_home_score': {
            const normalizedHomeScore = normalizeScoreInput(state.homeScore);
            return {
                ...state,
                homeScore: normalizedHomeScore,
                winningTeam: reconcileOutcomeForScores(normalizedHomeScore, state.awayScore, state.winningTeam),
                touched: { ...state.touched, homeScore: true },
            };
        }
        case 'blur_away_score': {
            const normalizedAwayScore = normalizeScoreInput(state.awayScore);
            return {
                ...state,
                awayScore: normalizedAwayScore,
                winningTeam: reconcileOutcomeForScores(state.homeScore, normalizedAwayScore, state.winningTeam),
                touched: { ...state.touched, awayScore: true },
            };
        }
        case 'submit_attempt':
            return {
                ...state,
                inlineError: null,
                showSuccessMessage: false,
                showSuccessHighlight: false,
                touched: { ...state.touched, submitted: true },
            };
        case 'submit_failure':
            return { ...state, inlineError: action.error };
        case 'submit_success':
            return {
                ...state,
                baseline: action.baseline,
                homeScore: action.baseline.homeScore,
                awayScore: action.baseline.awayScore,
                winningTeam: action.baseline.winningTeam,
                inlineError: null,
                showSuccessMessage: true,
                showSuccessHighlight: true,
                touched: {
                    homeScore: false,
                    awayScore: false,
                    winner: false,
                    submitted: false,
                },
            };
        case 'hide_success_message':
            return { ...state, showSuccessMessage: false };
        case 'hide_success_highlight':
            return { ...state, showSuccessHighlight: false };
        default:
            return state;
    }
}

type UseMatchPredictionFormParams = {
    match: TournamentMatchPrediction;
    isSaving: boolean;
    onPredict?: PredictMatchHandler;
    defaultSaveErrorMessage: string;
};

export type MatchPredictionFormViewModel = {
    homeScore: string;
    awayScore: string;
    winningTeam: WinningTeam | null;
    isLocked: boolean;
    isDirty: boolean;
    hasExistingPrediction: boolean;
    showSuccessMessage: boolean;
    showSuccessHighlight: boolean;
    inlineError: string | null;
    validationErrorCode: ValidationErrorCode;
    canSubmit: boolean;
    setHomeScore: (value: string) => void;
    setAwayScore: (value: string) => void;
    selectWinner: (winner: WinningTeam) => void;
    blurHomeScore: () => void;
    blurAwayScore: () => void;
    submit: () => Promise<void>;
};

export function useMatchPredictionForm({
    match,
    isSaving,
    onPredict,
    defaultSaveErrorMessage,
}: UseMatchPredictionFormParams): MatchPredictionFormViewModel {
    const baselineFromProps = useMemo(() => createBaseline(match), [match]);

    const [state, dispatch] = useReducer(
        reduceFormState,
        baselineFromProps,
        (baseline): FormState => ({
            homeScore: baseline.homeScore,
            awayScore: baseline.awayScore,
            winningTeam: baseline.winningTeam,
            baseline,
            touched: {
                homeScore: false,
                awayScore: false,
                winner: false,
                submitted: false,
            },
            inlineError: null,
            showSuccessMessage: false,
            showSuccessHighlight: false,
        })
    );

    useEffect(() => {
        dispatch({ type: 'sync_from_props', baseline: baselineFromProps });
    }, [baselineFromProps]);

    useEffect(() => {
        if (!state.showSuccessMessage && !state.showSuccessHighlight) {
            return;
        }

        const hideMessageTimeout = window.setTimeout(
            () => dispatch({ type: 'hide_success_message' }),
            SUCCESS_MESSAGE_TIMEOUT_MS
        );
        const hideHighlightTimeout = window.setTimeout(
            () => dispatch({ type: 'hide_success_highlight' }),
            SUCCESS_HIGHLIGHT_TIMEOUT_MS
        );

        return () => {
            window.clearTimeout(hideMessageTimeout);
            window.clearTimeout(hideHighlightTimeout);
        };
    }, [state.showSuccessHighlight, state.showSuccessMessage]);

    const isSubmittingRef = useRef(false);

    const isLocked = isMatchLocked(match);
    const isDirty = computeIsDirty(state);
    const hasExistingPrediction = state.baseline.hasPrediction;
    const validationResult = validatePredictionForm(state.homeScore, state.awayScore, state.winningTeam);
    const shouldValidate =
        state.touched.submitted ||
        state.touched.homeScore ||
        state.touched.awayScore ||
        state.touched.winner;
    const validationErrorCode = shouldValidate && !isLocked ? validationResult.errorCode : null;
    const canSubmit =
        !isLocked &&
        !isSaving &&
        validationResult.errorCode === null &&
        (!hasExistingPrediction || isDirty);

    async function submit() {
        if (isSubmittingRef.current) {
            return;
        }

        dispatch({ type: 'submit_attempt' });

        if (validationResult.errorCode !== null) {
            return;
        }

        if (!onPredict) {
            return;
        }
        if (validationResult.parsedScores.home === null || validationResult.parsedScores.away === null) {
            return;
        }
        if (state.winningTeam === null) {
            return;
        }

        isSubmittingRef.current = true;
        try {
            const result: PredictActionResult = await onPredict(
                match.matchId,
                validationResult.parsedScores.home,
                validationResult.parsedScores.away,
                state.winningTeam
            );

            if (!result.ok) {
                if (result.skipped) {
                    return;
                }
                dispatch({
                    type: 'submit_failure',
                    error: result.error || defaultSaveErrorMessage,
                });
                return;
            }

            dispatch({
                type: 'submit_success',
                baseline: {
                    homeScore: String(validationResult.parsedScores.home),
                    awayScore: String(validationResult.parsedScores.away),
                    winningTeam: state.winningTeam,
                    hasPrediction: true,
                    baselineSignature: `local:${match.matchId}:${validationResult.parsedScores.home}:${validationResult.parsedScores.away}:${state.winningTeam}`,
                },
            });
        } finally {
            isSubmittingRef.current = false;
        }
    }

    return {
        homeScore: state.homeScore,
        awayScore: state.awayScore,
        winningTeam: state.winningTeam,
        isLocked,
        isDirty,
        hasExistingPrediction,
        showSuccessMessage: state.showSuccessMessage,
        showSuccessHighlight: state.showSuccessHighlight,
        inlineError: state.inlineError,
        validationErrorCode,
        canSubmit,
        setHomeScore: (value) => {
            if (!isScoreInput(value)) {
                return;
            }
            dispatch({ type: 'set_home_score', value });
        },
        setAwayScore: (value) => {
            if (!isScoreInput(value)) {
                return;
            }
            dispatch({ type: 'set_away_score', value });
        },
        selectWinner: (winner) => dispatch({ type: 'select_winner', winner }),
        blurHomeScore: () => dispatch({ type: 'blur_home_score' }),
        blurAwayScore: () => dispatch({ type: 'blur_away_score' }),
        submit,
    };
}
