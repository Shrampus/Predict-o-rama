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
  | { ok: false; error?: string };

type UsePredictionSaverReturn = {
  savingMatchId: string | null;
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
  const [savingMatchId, setSavingMatchId] = useState<string | null>(null);
  const activeSaveMatchIdRef = useRef<string | null>(null);

  async function saveMatchPrediction(
    groupId: string,
    matchId: string,
    homeScore: number,
    awayScore: number,
    winningTeam: WinningTeam,
  ): Promise<SavePredictionResult> {
    if (activeSaveMatchIdRef.current !== null) {
      return {
        ok: false,
        error: undefined,
      };
    }

    try {
      activeSaveMatchIdRef.current = matchId;
      setSavingMatchId(matchId);

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
      if (activeSaveMatchIdRef.current === matchId) {
        activeSaveMatchIdRef.current = null;
      }
      setSavingMatchId(null);
    }
  }

  return { savingMatchId, saveMatchPrediction };
}
