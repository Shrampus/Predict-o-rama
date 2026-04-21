import type { WinningTeam } from '../TournamentConstants';

export type PredictActionResult =
    | { ok: true }
    | { ok: false; skipped?: true; error?: string };

export type PredictMatchHandler = (
    id: string,
    home: number,
    away: number,
    winningTeam: WinningTeam
) => PredictActionResult | Promise<PredictActionResult>;
