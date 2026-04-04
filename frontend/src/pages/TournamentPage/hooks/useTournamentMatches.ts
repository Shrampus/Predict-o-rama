import { useEffect, useState } from "react";
import { getPredictions } from "../../../services/predictionsApi";
import type { PredictionPageMatch } from "../../../services/predictionsApi";

export function useTournamentMatches(competition: string, userId: string, groupId: string) {
    const [matches, setMatches] = useState<PredictionPageMatch[]>([]);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        async function fetchMatches() {
            try {
                const predictions = await getPredictions(competition, userId, groupId);
                setMatches(predictions.matches);
            } catch (err) {
                setError("Failed to fetch matches");
            } finally {
                setIsLoading(false);
            }
        }

        fetchMatches();
    }, [competition, userId, groupId]);

    return { matches, isLoading, error };
}