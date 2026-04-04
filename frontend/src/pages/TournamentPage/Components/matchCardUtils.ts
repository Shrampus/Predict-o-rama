import type { WinningTeam, Prediction } from '../TournamentConstants';

export function deriveWinner(homeScore: number, awayScore: number): WinningTeam {
    if (homeScore > awayScore) return 'Home';
    if (homeScore < awayScore) return 'Away';
    return 'Draw';
}

export const DEFAULT_PREDICTION: Prediction = { home: 0, away: 0, winningTeam: 'Draw', saved: false };
