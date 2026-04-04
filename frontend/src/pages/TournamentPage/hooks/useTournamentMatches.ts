import { useEffect, useState } from "react";
import { getPredictions } from "../../../services/predictionsApi";
import type { PredictionPageMatch } from "../../../services/predictionsApi";

export function useTournamentMatches(competitionId: string) {
    const [matches, setMatches] = useState<PredictionPageMatch[]>([]);
    const [isLoading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        async function fetchMatches() {
            try {
                const predictions = await getPredictions(competitionId);
                setMatches(predictions.matches);
            } catch (err) {
                setError("Failed to fetch matches");
            } finally {
                setLoading(false);
            }
        }

        fetchMatches();
    }, [competitionId]);

    return { matches, isLoading, error };
}