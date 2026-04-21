export interface GroupReference {
    groupId: string;
    groupName: string;
    competitionId: string;
    hasPrediction: boolean;
}

export interface UpcomingMatch {
    matchId: string;
    homeTeamName: string;
    awayTeamName: string;
    homeTeamImage: string;
    awayTeamImage: string;
    kickoffTime: string;
    tournamentName: string | null;
    groups: GroupReference[];
}