import type { GroupReference, UpcomingMatch } from '../types';

export function groupMatchesByGroup(
    matches: UpcomingMatch[]
): Map<string, { group: GroupReference; matches: UpcomingMatch[] }> {
    const grouped = new Map<string, { group: GroupReference; matches: UpcomingMatch[] }>();
    for (const match of matches) {
        for (const group of match.groups) {
            if (!grouped.has(group.groupId)) {
                grouped.set(group.groupId, { group, matches: [] });
            }
            grouped.get(group.groupId)!.matches.push(match);
        }
    }
    return grouped;
}
