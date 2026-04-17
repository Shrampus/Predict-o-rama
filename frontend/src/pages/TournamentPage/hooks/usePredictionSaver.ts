import { useState } from 'react';

import { savePrediction, winningTeamToApiWinner } from '../../../services/predictionsApi';
import type { WinningTeam } from '../TournamentConstants';

type UsePredictionSaverReturn = {
  savingMatchId: string | null;
  saveError: string | null;
  saveMatchPrediction: (
    groupId: string,
    matchId: string,
    homeScore: number,
    awayScore: number,
    winningTeam: WinningTeam,
  ) => Promise<boolean>;
};

export function usePredictionSaver(): UsePredictionSaverReturn {
  const [savingMatchId, setSavingMatchId] = useState<string | null>(null);
  const [saveError, setSaveError] = useState<string | null>(null);

  async function saveMatchPrediction(
    groupId: string,
    matchId: string,
    homeScore: number,
    awayScore: number,
    winningTeam: WinningTeam,
  ): Promise<boolean> {
    try {
      setSavingMatchId(matchId);
      setSaveError(null);

      await savePrediction({
        groupId,
        matchId,
        homeScore,
        awayScore,
        predictedWinner: winningTeamToApiWinner[winningTeam],
      });

      return true;
    } catch (error) {
      setSaveError(error instanceof Error ? error.message : 'Failed to save prediction');
      return false;
    } finally {
      setSavingMatchId(null);
    }
  }

  return { savingMatchId, saveError, saveMatchPrediction };
}
