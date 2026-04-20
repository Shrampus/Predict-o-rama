import { useCallback, useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';

import type { TournamentMatchPrediction } from '../../../services/predictionsApi';
import { getPredictions } from '../../../services/predictionsApi';
import type { SavedPredictionPayload } from './usePredictionSaver';

type UseTournamentMatchesResult = {
    matches: TournamentMatchPrediction[];
    tournamentName: string;
    seasonLabel: string;
    phaseLabel: string;
    isLoading: boolean;
    error: string | null;
    refetch: () => Promise<void>;
    updateMatchPrediction: (prediction: SavedPredictionPayload) => void;
};

export function useTournamentMatches(competition: string, groupId: string): UseTournamentMatchesResult {
    const { t } = useTranslation();
    const [matches, setMatches] = useState<TournamentMatchPrediction[]>([]);
    const [tournamentName, setTournamentName] = useState<string>(competition);
    const [seasonLabel, setSeasonLabel] = useState<string>('');
    const [phaseLabel, setPhaseLabel] = useState<string>('');
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    const fetchMatches = useCallback(async () => {
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
    }, [competition, groupId, t]);

    useEffect(() => {
        fetchMatches();
    }, [fetchMatches]);

    const updateMatchPrediction = useCallback((prediction: SavedPredictionPayload) => {
        setMatches((currentMatches) =>
            currentMatches.map((match) => {
                if (match.matchId !== prediction.matchId) {
                    return match;
                }

                return {
                    ...match,
                    predictionId: prediction.predictionId,
                    predictedHomeScore: prediction.homeScore,
                    predictedAwayScore: prediction.awayScore,
                    predictedWinner: prediction.predictedWinner,
                };
            })
        );
    }, []);

    return {
        matches,
        tournamentName,
        seasonLabel,
        phaseLabel,
        isLoading,
        error,
        refetch: fetchMatches,
        updateMatchPrediction,
    };
}