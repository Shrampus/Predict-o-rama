import { useEffect, useState } from "react";
import type { UpcomingMatch } from "../types";
import { matchesApi } from "../../../services/matchesApi";
import { useAuth } from "../../../context/useAuth";

interface UseUpcomingMatchesResult {
    upcomingMatches: UpcomingMatch[];
    isLoading: boolean;
    hasError: boolean;
}

export function useUpcomingMatches(): UseUpcomingMatchesResult {
    const { currentUser, loading: authLoading } = useAuth();
    const [upcomingMatches, setUpcomingMatches] = useState<UpcomingMatch[]>([]);
    const [isLoading, setIsLoading] = useState<boolean>(true);
    const [hasError, setHasError] = useState(false);

    useEffect(() => {
        if (authLoading) return;

        const fetchUpcomingMatches = async () => {
            setIsLoading(true);
            setHasError(false);
            try {
                const matches = currentUser
                    ? await matchesApi.getMyUpcomingMatches()
                    : await matchesApi.getUpcomingMatches();
                setUpcomingMatches(matches);
            } catch {
                setHasError(true);
            } finally {
                setIsLoading(false);
            }
        };

        fetchUpcomingMatches();
    }, [currentUser, authLoading]);

    return { upcomingMatches, isLoading, hasError };
}