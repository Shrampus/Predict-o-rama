import { useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';

import type { TournamentMatchPrediction } from '../../../services/predictionsApi';
import { getPredictions } from '../../../services/predictionsApi';

type UseTournamentMatchesResult = {
    matches: TournamentMatchPrediction[];
    tournamentName: string;
    seasonLabel: string;
    phaseLabel: string;
    isLoading: boolean;
    error: string | null;
    refetch: () => Promise<void>;
};

export function useTournamentMatches(competition: string, groupId: string): UseTournamentMatchesResult {
    const { t } = useTranslation();
    const [matches, setMatches] = useState<TournamentMatchPrediction[]>([]);
    const [tournamentName, setTournamentName] = useState<string>(competition);
    const [seasonLabel, setSeasonLabel] = useState<string>('');
    const [phaseLabel, setPhaseLabel] = useState<string>('');
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    async function fetchMatches() {
        if (!competition || !groupId) {
            setMatches([]);
            setTournamentName(competition);
            setSeasonLabel('');
            setPhaseLabel('');
            setError(null);
            setIsLoading(false);
            return;
        }

        try {
            setIsLoading(true);
            setError(null);

            const predictions = await getPredictions(competition, groupId);
            setMatches(predictions.matches);
            setTournamentName(predictions.tournamentName ?? competition);
            setSeasonLabel(predictions.seasonLabel ?? '');
            setPhaseLabel(predictions.phaseLabel ?? '');
        } catch (error) {
            setError(error instanceof Error ? error.message : t('tournament.fetchError'));
        } finally {
            setIsLoading(false);
        }
    }

    useEffect(() => {
        fetchMatches();
        // eslint-disable-next-line react-hooks/exhaustive-deps -- run when competition/groupId change; fetchMatches is not stable
    }, [competition, groupId]);

    return { matches, tournamentName, seasonLabel, phaseLabel, isLoading, error, refetch: fetchMatches };
}