import { useMemo, useState } from 'react';
import type { DayRange, GroupOption, UpcomingMatch } from '../types';

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
        let cutoff: Date | null = null;
        let todayStart: Date | null = null;

        if (selectedDays === 'today') {
            todayStart = new Date();
            todayStart.setHours(0, 0, 0, 0);
            cutoff = new Date();
            cutoff.setHours(23, 59, 59, 999);
        } else if (selectedDays) {
            cutoff = new Date();
            cutoff.setDate(cutoff.getDate() + selectedDays);
        }

        return matches.filter(match => {
            if (selectedTournament && match.tournamentName !== selectedTournament) return false;
            if (selectedGroupId && !match.groups.some(g => g.groupId === selectedGroupId)) return false;
            const kickoff = new Date(match.kickoffTime);
            if (todayStart && kickoff < todayStart) return false;
            if (cutoff && kickoff > cutoff) return false;
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
