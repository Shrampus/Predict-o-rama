import { useEffect, useState } from 'react';

import { getMemberPredictions } from '../../../services/groupApi';
import type { TournamentMatchPrediction } from '../../../services/predictionsApi';

export function useUserPredictions(groupId: string, userId: string, competitionCode: string) {
  const [matches, setMatches] = useState<TournamentMatchPrediction[] | null>(null);
  const [hasError, setHasError] = useState(false);

  useEffect(() => {
    async function load() {
      try {
        const response = await getMemberPredictions(groupId, userId, competitionCode);
        setMatches(response.matches.filter((m) => m.matchStatus === 'COMPLETED'));
      } catch {
        setHasError(true);
      }
    }
    void load();
  }, [groupId, userId, competitionCode]);

  return { matches, hasError };
}
