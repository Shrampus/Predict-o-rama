import { apiFetch } from '../lib/apiClient';
import { API_PREFIX, getApiUrl } from './apiConfig';
import type { UpcomingMatch } from '../pages/HomePage/types';

interface UpcomingMatchResponse {
    matchId: string;
    homeTeamName: string;
    homeTeamImage: string;
    awayTeamName: string;
    awayTeamImage: string;
    kickoffTime: string;
    tournamentName: string | null;
    groups: { groupId: string; groupName: string; competitionId: string; hasPrediction: boolean }[];
}

function mapUpcomingMatchesResponse(data: UpcomingMatchResponse[]): UpcomingMatch[] {
    return data.map(m => ({
        matchId: m.matchId,
        homeTeamName: m.homeTeamName,
        homeTeamImage: m.homeTeamImage,
        awayTeamName: m.awayTeamName,
        awayTeamImage: m.awayTeamImage,
        kickoffTime: m.kickoffTime,
        tournamentName: m.tournamentName,
        groups: m.groups,
    }));
}

async function getUpcomingMatches(): Promise<UpcomingMatch[]> {
    const res = await apiFetch(getApiUrl(`${API_PREFIX}/matches/upcoming`));
    if (!res.ok) throw new Error('Failed to fetch upcoming matches');
    return mapUpcomingMatchesResponse(await res.json());
}

async function getMyUpcomingMatches(): Promise<UpcomingMatch[]> {
    const res = await apiFetch(getApiUrl(`${API_PREFIX}/matches/upcoming/my`));
    if (!res.ok) throw new Error('Failed to fetch upcoming matches');
    return mapUpcomingMatchesResponse(await res.json());
}

export const matchesApi = { getUpcomingMatches, getMyUpcomingMatches };
