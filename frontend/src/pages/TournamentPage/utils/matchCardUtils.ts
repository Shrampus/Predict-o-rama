import type { WinningTeam, Prediction } from '../TournamentConstants';

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
