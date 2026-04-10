import { useEffect, useState } from "react";
import { MOCK_UPCOMING_MATCHES } from "../mockData";
import type { UpcomingMatch } from "../types";

interface UseUpcomingMatchesResult {
    upcomingMatches: UpcomingMatch[];
    isLoading: boolean;
    hasError: boolean;
}

export function useUpcomingMatches(): UseUpcomingMatchesResult {
    const [upcomingMatches, setUpcomingMatches] = useState<UpcomingMatch[]>([]);
    const [isLoading, setIsLoading] = useState<boolean>(true);
    const [hasError, setHasError] = useState(false);

    useEffect(() => {
        const fetchUpcomingMatches = async () => {
            setIsLoading(true);
            setHasError(false);

        // TODO: replace with real API call once PRs #49, #50, #51 are merged
        // and backend adds `competition` to GET /api/groups/my response.
        // Real implementation will:
        //   1. Call getMyGroups() from groupApi.ts
        //   2. For each group call getPredictions(group.competition, group.groupId)
        //   3. Deduplicate by externalMatchId, collecting groups per match
        //   4. Filter to matches where predictionId === null

            try {
                // Simulate an API call delay
                await simulateDelay();
                setUpcomingMatches(MOCK_UPCOMING_MATCHES);
            } catch (error) {
                setHasError(true);
            } finally {
                setIsLoading(false);
            }
        };

        fetchUpcomingMatches();
    }, []);

    return { upcomingMatches, isLoading, hasError };
}

function simulateDelay(): Promise<void> {
  return new Promise((resolve) => setTimeout(resolve, 500));
}