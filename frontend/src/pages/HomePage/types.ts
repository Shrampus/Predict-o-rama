export interface GroupReference {
    groupId: string;
    groupName: string;
    competitionId: string;
}

export interface UpcomingMatch {
    matchId: string;
    externalMatchId: string;
    homeTeamName: string;
    awayTeamName: string;
    homeTeamImage: string;
    awayTeamImage: string;
    kickoffTime: string;
    groups: GroupReference[];

}