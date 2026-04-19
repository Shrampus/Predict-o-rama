import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { Navigate, useLocation, useParams } from 'react-router-dom';

import HeroBanner from './components/HeroBanner';
import MatchCard from './components/MatchCard';
import StandingsTable from './components/StandingsTable';
import Tabs from './components/Tabs';
import { usePredictionSaver } from './hooks/usePredictionSaver';
import { useTournamentMatches } from './hooks/useTournamentMatches';
import type { WinningTeam } from './TournamentConstants';
import type { PredictActionResult } from './types/predictionActions';
import { ROUTE_PATHS } from '../../app/routePaths';
import type { TournamentMatchPrediction } from '../../services/predictionsApi';

type TournamentRouteState = {
    tournamentName?: string;
    tournamentDescription?: string;
    tournamentPhase?: string;
};

function isTournamentRouteState(value: unknown): value is TournamentRouteState {
    if (!value || typeof value !== 'object') {
        return false;
    }

    const routeState = value as Record<string, unknown>;
    return (
        (routeState.tournamentName === undefined || typeof routeState.tournamentName === 'string') &&
        (routeState.tournamentDescription === undefined || typeof routeState.tournamentDescription === 'string') &&
        (routeState.tournamentPhase === undefined || typeof routeState.tournamentPhase === 'string')
    );
}

function TournamentPage() {
    const { groupId, tournament } = useParams<{ groupId: string; tournament: string }>();
    const location = useLocation();
    const resolvedGroupId = groupId ?? '';
    const resolvedTournament = tournament ?? '';
    const routeState = isTournamentRouteState(location.state) ? location.state : null;

    const {
        matches,
        tournamentName,
        seasonLabel: responseSeasonLabel,
        phaseLabel: responsePhaseLabel,
        isLoading,
        error,
        updateMatchPrediction,
    } =
        useTournamentMatches(resolvedTournament, resolvedGroupId);
    const { t } = useTranslation();

    const [activeTab, setActiveTab] = useState<'matches' | 'standings'>('matches');
    const { savingMatchId, saveMatchPrediction } = usePredictionSaver();

    if (!groupId || !tournament) {
        return <Navigate to={ROUTE_PATHS.groups} replace />;
    }

    const liveMatchCount = matches.filter((match) => match.matchStatus === 'LIVE').length;
    const seasonLabel =
        responseSeasonLabel.trim() ||
        routeState?.tournamentDescription?.trim() ||
        routeState?.tournamentName?.trim() ||
        tournamentName;
    const phaseLabel =
        routeState?.tournamentPhase?.trim() ||
        responsePhaseLabel.trim() ||
        t('tournament.phaseFallback');

    async function handlePredict(
        matchId: string,
        homeScore: number,
        awayScore: number,
        winningTeam: WinningTeam
    ): Promise<PredictActionResult> {
        const saveResult = await saveMatchPrediction(
            resolvedGroupId,
            matchId,
            homeScore,
            awayScore,
            winningTeam
        );
        if (saveResult.ok) {
            updateMatchPrediction(saveResult.prediction);
            return { ok: true };
        }

        return { ok: false, error: saveResult.error };
    }

    return (
        <div className="max-w-7xl mx-auto px-4 sm:px-6 py-8">
            <HeroBanner
                season={seasonLabel}
                name={tournamentName}
                phase={phaseLabel}
                liveMatchCount={liveMatchCount}
            />

            <Tabs activeTab={activeTab} setActiveTab={setActiveTab} />

            {activeTab === 'matches' && (
                <div className="grid grid-cols-1 laptop:grid-cols-12 gap-8">
                    <div className="laptop:col-span-8 space-y-6">
                        <div className="flex items-center justify-between">
                            <h2 className="text-2xl font-black text-slate-900">
                                {phaseLabel}{' '}
                                <span className="text-slate-400 font-normal ml-2">{t('tournament.upcoming')}</span>
                            </h2>
                            {/* <span className="text-green-700 font-bold text-sm cursor-pointer hover:underline">
                                {t('tournament.viewCalendar')}
                            </span> */}
                        </div>

                        {isLoading && <p>{t('tournament.loadingMatches')}</p>}
                        {error && <p className="text-red-500">{error}</p>}
                        {!isLoading && !error && matches.length === 0 && <p>{t('tournament.noMatches')}</p>}

                        {!isLoading &&
                            !error &&
                            matches.map((match: TournamentMatchPrediction) => (
                                <MatchCard
                                    key={match.matchId}
                                    match={match}
                                    onPredict={handlePredict}
                                    isSaving={savingMatchId === match.matchId}
                                />
                            ))}
                    </div>

                    <div className="laptop:col-span-4 space-y-6">
                        {/* <BentoBoxes /> */}
                    </div>
                </div>
            )}

            {activeTab === 'standings' && <StandingsTable />}
        </div>
    );
}

export default TournamentPage;
