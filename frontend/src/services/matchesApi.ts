import type { UpcomingMatch } from '../pages/HomePage/types';

interface UpcomingMatchResponse {
    matchId: string;
    homeTeamName: string;
    homeTeamImage: string;
    awayTeamName: string;
    awayTeamImage: string;
    kickoffTime: string;
    groups: { groupId: string; groupName: string; competitionId: string }[];
}

async function getUpcomingMatches(): Promise<UpcomingMatch[]> {
    const res = await fetch('/api/matches/upcoming');
    if (!res.ok) throw new Error('Failed to fetch upcoming matches');
    const data: UpcomingMatchResponse[] = await res.json();
    return data.map(m => ({
        matchId: m.matchId,
        externalMatchId: '',
        homeTeamName: m.homeTeamName,
        homeTeamImage: m.homeTeamImage,
        awayTeamName: m.awayTeamName,
        awayTeamImage: m.awayTeamImage,
        kickoffTime: m.kickoffTime,
        groups: m.groups,
    }));
}

export const matchesApi = { getUpcomingMatches };
