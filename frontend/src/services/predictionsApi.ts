export type TournamentMatchPrediction = {
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
};

export type TournamentPredictionsResponse = {
    tournamentName: string;
    matches: TournamentMatchPrediction[];
};

export type SavePredictionRequest = {
    groupId: string;
    matchId: string;
    homeScore: number;
    awayScore: number;
    predictedWinner: 'HOME' | 'AWAY' | 'DRAW';
};

export type PredictionResponse = {
    predictionId: string;
    matchId: string;
    homeScore: number;
    awayScore: number;
    predictedWinner: 'HOME' | 'AWAY' | 'DRAW';
    submittedAt: string;
};

export const winningTeamToApiWinner = {
    Home: 'HOME',
    Away: 'AWAY',
    Draw: 'DRAW',
} as const;

export async function getPredictions(
    competition: string,
    groupId: string
): Promise<TournamentPredictionsResponse> {
    const response = await fetch(
        `/api/predictions?competition=${encodeURIComponent(competition)}&groupId=${groupId}`,
        {
            credentials: 'include',
        }
    );

    if (!response.ok) {
        throw new Error(`Failed to fetch predictions: ${response.status}`);
    }

    return response.json();
}

export async function savePrediction(
    payload: SavePredictionRequest
): Promise<PredictionResponse> {
    const response = await fetch('/api/predictions', {
        method: 'POST',
        credentials: 'include',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(payload),
    });
    if (!response.ok) {
        throw new Error(`Failed to save predictions: ${response.status}`);
    }

    return response.json();
}