import type { UpcomingMatch } from '../types';

export function groupMatchesByTournament(
    matches: UpcomingMatch[]
): Map<string, { tournamentName: string; matches: UpcomingMatch[] }> {
    const grouped = new Map<string, { tournamentName: string; matches: UpcomingMatch[] }>();
    for (const match of matches) {
        if (match.groups.length === 0) continue;
        const key = match.tournamentName ?? '';
        if (!grouped.has(key)) {
            grouped.set(key, { tournamentName: match.tournamentName ?? '', matches: [] });
        }
        grouped.get(key)!.matches.push(match);
    }
    return grouped;
}
