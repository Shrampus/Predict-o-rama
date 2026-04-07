import { useEffect, useState } from 'react';

import type { TournamentMatchPrediction } from '../../../services/predictionsApi';
import { getPredictions } from '../../../services/predictionsApi';

export function useTournamentMatches(competition: string, groupId: string) {
    const [matches, setMatches] = useState<TournamentMatchPrediction[]>([]);
    const [tournamentName, setTournamentName] = useState<string>(competition);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    async function fetchMatches() {
        try {
            setIsLoading(true);
            setError(null);

            const predictions = await getPredictions(competition, groupId);
            setMatches(predictions.matches);
            setTournamentName(predictions.tournamentName ?? competition);
        } catch (error) {
            setError(error instanceof Error ? error.message : 'Failed to fetch matches');
        } finally {
            setIsLoading(false);
        }
    }

    useEffect(() => {
        fetchMatches();
        // eslint-disable-next-line react-hooks/exhaustive-deps -- run when competition/groupId change; fetchMatches is not stable
    }, [competition, groupId]);

    return { matches, tournamentName, isLoading, error, refetch: fetchMatches };
}