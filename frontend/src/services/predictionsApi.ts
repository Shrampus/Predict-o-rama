export type PredictionPageMatch = {
    matchId: string;
    externalMatchId: string;
    homeTeamName: string;
    awayTeamName: string;
    homeTeamImage: string;
    awayTeamImage: string;
    kickoffTime: string;
    matchStatus: string;
    predictionId: string | null;
    predictedHomeScore: number | null;
    predictedAwayScore: number | null;
    predictedWinner: 'HOME' | 'AWAY' | 'DRAW' | null;
}

export type PredictionPageResponse = {
    matches: PredictionPageMatch[];
}

export async function getPredictions(competition: string): Promise<PredictionPageResponse> {
    const response = await fetch('/api/predictions?competition=' + competition);
    return response.json();
}   