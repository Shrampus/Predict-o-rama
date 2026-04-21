import { useMemo, useState } from 'react';
import type { UpcomingMatch } from '../types';

export type DayRange = 7 | 14 | 28;

export interface GroupOption {
    groupId: string;
    groupName: string;
}

interface UseUpcomingMatchFiltersResult {
    filteredMatches: UpcomingMatch[];
    tournamentOptions: string[];
    groupOptions: GroupOption[];
    selectedTournament: string | null;
    selectedGroupId: string | null;
    selectedDays: DayRange | null;
    setSelectedTournament: (t: string | null) => void;
    setSelectedGroupId: (g: string | null) => void;
    setSelectedDays: (d: DayRange | null) => void;
}

export function useUpcomingMatchFilters(matches: UpcomingMatch[]): UseUpcomingMatchFiltersResult {
    const [selectedTournament, setSelectedTournament] = useState<string | null>(null);
    const [selectedGroupId, setSelectedGroupId] = useState<string | null>(null);
    const [selectedDays, setSelectedDays] = useState<DayRange | null>(null);

    const tournamentOptions = useMemo(() => {
        const seen = new Set<string>();
        for (const match of matches) {
            if (match.tournamentName) seen.add(match.tournamentName);
        }
        return [...seen];
    }, [matches]);

    const groupOptions = useMemo(() => {
        const seen = new Map<string, GroupOption>();
        for (const match of matches) {
            for (const group of match.groups) {
                if (!seen.has(group.groupId)) {
                    seen.set(group.groupId, { groupId: group.groupId, groupName: group.groupName });
                }
            }
        }
        return [...seen.values()];
    }, [matches]);

    const filteredMatches = useMemo(() => {
        const cutoff = selectedDays
            ? new Date(Date.now() + selectedDays * 24 * 60 * 60 * 1000)
            : null;

        return matches.filter(match => {
            if (selectedTournament && match.tournamentName !== selectedTournament) return false;
            if (selectedGroupId && !match.groups.some(g => g.groupId === selectedGroupId)) return false;
            if (cutoff && new Date(match.kickoffTime) > cutoff) return false;
            return true;
        });
    }, [matches, selectedTournament, selectedGroupId, selectedDays]);

    return {
        filteredMatches,
        tournamentOptions,
        groupOptions,
        selectedTournament,
        selectedGroupId,
        selectedDays,
        setSelectedTournament,
        setSelectedGroupId,
        setSelectedDays,
    };
}
