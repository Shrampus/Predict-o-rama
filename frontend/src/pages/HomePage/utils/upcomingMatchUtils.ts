import type { UpcomingMatch } from '../types';

export function groupMatchesByDate(
    matches: UpcomingMatch[]
): Map<string, { label: string; matches: UpcomingMatch[] }> {
    const grouped = new Map<string, { label: string; matches: UpcomingMatch[] }>();
    for (const match of matches) {
        const date = new Date(match.kickoffTime);
        const key = date.toISOString().slice(0, 10);
        const label = date.toLocaleDateString(undefined, { weekday: 'long', day: 'numeric', month: 'long' });
        if (!grouped.has(key)) {
            grouped.set(key, { label, matches: [] });
        }
        grouped.get(key)!.matches.push(match);
    }
    return grouped;
}
