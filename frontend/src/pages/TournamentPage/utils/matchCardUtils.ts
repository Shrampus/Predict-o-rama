import type { TournamentMatchPrediction } from '../../../services/predictionsApi';
import type { WinningTeam, Prediction } from '../TournamentConstants';

type PredictedWinner = NonNullable<TournamentMatchPrediction['predictedWinner']>;

const winnerMap = {
    HOME: 'Home',
    AWAY: 'Away',
    DRAW: 'Draw',
} as const satisfies Record<PredictedWinner, WinningTeam>;

function isPredictedWinner(value: TournamentMatchPrediction['predictedWinner']): value is PredictedWinner {
    return value !== null && value in winnerMap;
}

export function deriveWinner(homeScore: number, awayScore: number): WinningTeam {
    if (homeScore > awayScore) return 'Home';
    if (homeScore < awayScore) return 'Away';
    return 'Draw';
}

export function formatKickoffTime(kickoffTime: string): string {
    const date = new Date(kickoffTime);

    return date.toLocaleString(undefined, {
        day: 'numeric',
        month: 'short',
        hour: '2-digit',
        minute: '2-digit',
    });
}

export function deriveTimeStyle(matchStatus: string): string {
    return matchStatus === 'LIVE' ? 'urgent' : 'default';
}

export const DEFAULT_PREDICTION: Prediction = { home: 0, away: 0, winningTeam: 'Draw', saved: false };

export function buildPrediction(match: TournamentMatchPrediction): Prediction {
    if (match.predictedHomeScore === null || match.predictedAwayScore === null) {
        return DEFAULT_PREDICTION;
    }

    return {
        home: match.predictedHomeScore,
        away: match.predictedAwayScore,
        winningTeam: isPredictedWinner(match.predictedWinner)
            ? winnerMap[match.predictedWinner]
            : deriveWinner(match.predictedHomeScore, match.predictedAwayScore),
        saved: match.predictionId !== null,
    };
}