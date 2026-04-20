import type { TournamentMatchPrediction } from '../../../services/predictionsApi';
import type { WinningTeam } from '../TournamentConstants';

/**
 * Maps API winner enum to UI winner selection.
 */
export function mapApiWinnerToWinningTeam(
    predictedWinner: TournamentMatchPrediction['predictedWinner']
): WinningTeam | null {
    if (predictedWinner === 'HOME') {
        return 'Home';
    }
    if (predictedWinner === 'AWAY') {
        return 'Away';
    }
    if (predictedWinner === 'DRAW') {
        return 'Draw';
    }
    // Unknown values are treated as no selection.
    return null;
}
