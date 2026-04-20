import { useRef, useState } from 'react';

import { savePrediction, winningTeamToApiWinner } from '../../../services/predictionsApi';
import type { PredictionResponse } from '../../../services/predictionsApi';
import type { WinningTeam } from '../TournamentConstants';

export type SavedPredictionPayload = {
  matchId: string;
  predictionId: string;
  homeScore: number;
  awayScore: number;
  predictedWinner: PredictionResponse['predictedWinner'];
};

export type SavePredictionResult =
  | { ok: true; prediction: SavedPredictionPayload }
  | { ok: false; skipped?: true; error?: string };

type UsePredictionSaverReturn = {
  isSavingMatch: (matchId: string) => boolean;
  saveMatchPrediction: (
    groupId: string,
    matchId: string,
    homeScore: number,
    awayScore: number,
    winningTeam: WinningTeam,
  ) => Promise<SavePredictionResult>;
};

export function usePredictionSaver(): UsePredictionSaverReturn {
  // Ref guards overlap synchronously between calls; state drives reactive UI rendering.
  const [savingMatchIds, setSavingMatchIds] = useState<Set<string>>(new Set());
  const activeSaveMatchIdsRef = useRef<Set<string>>(new Set());

  async function saveMatchPrediction(
    groupId: string,
    matchId: string,
    homeScore: number,
    awayScore: number,
    winningTeam: WinningTeam,
  ): Promise<SavePredictionResult> {
    if (activeSaveMatchIdsRef.current.has(matchId)) {
      return { ok: false, skipped: true };
    }

    try {
      activeSaveMatchIdsRef.current.add(matchId);
      setSavingMatchIds((previous) => {
        const next = new Set(previous);
        next.add(matchId);
        return next;
      });

      const response = await savePrediction({
        groupId,
        matchId,
        homeScore,
        awayScore,
        predictedWinner: winningTeamToApiWinner[winningTeam],
      });

      return {
        ok: true,
        prediction: {
          matchId: response.matchId,
          predictionId: response.predictionId,
          homeScore: response.homeScore,
          awayScore: response.awayScore,
          predictedWinner: response.predictedWinner,
        },
      };
    } catch (error) {
      return {
        ok: false,
        error: error instanceof Error ? error.message : undefined,
      };
    } finally {
      activeSaveMatchIdsRef.current.delete(matchId);
      setSavingMatchIds((previous) => {
        if (!previous.has(matchId)) {
          return previous;
        }
        const next = new Set(previous);
        next.delete(matchId);
        return next;
      });
    }
  }

  return {
    isSavingMatch: (matchId: string) => savingMatchIds.has(matchId),
    saveMatchPrediction,
  };
}
