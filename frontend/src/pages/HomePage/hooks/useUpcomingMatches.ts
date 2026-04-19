import { useEffect, useState } from "react";
import type { UpcomingMatch } from "../types";
import { matchesApi } from "../../../services/matchesApi";

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
            try {
                const matches = await matchesApi.getUpcomingMatches();
                setUpcomingMatches(matches);
            } catch {
                setHasError(true);
            } finally {
                setIsLoading(false);
            }
        };

        fetchUpcomingMatches();
    }, []);

    return { upcomingMatches, isLoading, hasError };
}