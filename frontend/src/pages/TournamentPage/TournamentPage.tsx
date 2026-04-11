import { useState } from 'react';

import HeroBanner from './components/HeroBanner';
import MatchCard from './components/MatchCard';
import StandingsTable from './components/StandingsTable';
import Tabs from './components/Tabs';
import { useTournamentMatches } from './hooks/useTournamentMatches';
import type { WinningTeam } from './TournamentConstants';
import type { TournamentMatchPrediction } from '../../services/predictionsApi';
import { savePrediction, winningTeamToApiWinner } from '../../services/predictionsApi';

const COMPETITION = 'CL';
const GROUP_ID = 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa';

function TournamentPage() {
    const { matches, tournamentName, isLoading, error, refetch } = useTournamentMatches(
        COMPETITION,
        GROUP_ID
    );

    const [activeTab, setActiveTab] = useState<'matches' | 'standings'>('matches');
    const [savingMatchId, setSavingMatchId] = useState<string | null>(null);
    const [saveError, setSaveError] = useState<string | null>(null);

    const liveMatchCount = matches.filter((match) => match.matchStatus === 'LIVE').length;

    async function handlePredict(
        matchId: string,
        homeScore: number,
        awayScore: number,
        winningTeam: WinningTeam
    ) {
        try {
            setSavingMatchId(matchId);
            setSaveError(null);

            await savePrediction({
                groupId: GROUP_ID,
                matchId,
                homeScore,
                awayScore,
                predictedWinner: winningTeamToApiWinner[winningTeam],
            });

            await refetch();
        } catch (error) {
            setSaveError(error instanceof Error ? error.message : 'Failed to save prediction');
        } finally {
            setSavingMatchId(null);
        }
    }

    return (
        <div className="max-w-7xl mx-auto px-4 sm:px-6 py-8">
            <HeroBanner
                season="Summer 2024 Series"
                name={tournamentName}
                phase="Group Stage Phase"
                liveMatchCount={liveMatchCount}
            />

            <Tabs activeTab={activeTab} setActiveTab={setActiveTab} />

            {activeTab === 'matches' && (
                <div className="grid grid-cols-1 laptop:grid-cols-12 gap-8">
                    {/* Match list */}
                    <div className="laptop:col-span-8 space-y-6">
                        <div className="flex items-center justify-between">
                            <h2 className="text-2xl font-black text-slate-900">
                                ROUND OF 16{' '}
                                <span className="text-slate-400 font-normal ml-2">Upcoming</span>
                            </h2>
                            <span className="text-green-700 font-bold text-sm cursor-pointer hover:underline">
                                View Calendar
                            </span>
                        </div>

                        {isLoading && <p>Loading matches...</p>}
                        {error && <p className="text-red-500">{error}</p>}
                        {saveError && <p className="text-red-500">{saveError}</p>}
                        {!isLoading && !error && matches.length === 0 && <p>No matches available.</p>}

                        {!isLoading &&
                            !error &&
                            matches.map((match: TournamentMatchPrediction) => (
                                <MatchCard
                                    key={`${match.matchId}-${match.predictedHomeScore}-${match.predictedAwayScore}-${match.predictedWinner}`}
                                    match={match}
                                    onPredict={handlePredict}
                                    isSaving={savingMatchId === match.matchId}
                                />
                            ))}
                    </div>

                    {/* Sidebar */}
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