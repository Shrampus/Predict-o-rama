import { useState } from 'react';
import { useTournamentMatches } from './hooks/useTournamentMatches';
import HeroBanner from './Components/HeroBanner';
import MatchCard from './Components/MatchCard';
import StandingsTable from './Components/StandingsTable';
import Tabs from './Components/Tabs';
import { buildPrediction } from './utils/matchCardUtils';





function TournamentPage() {
    const { matches, isLoading, error } = useTournamentMatches('CL', '11111111-1111-1111-1111-111111111111', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa');
    const tournamentName = 'UEFA_EURO_2024'.replace(/_/g, ' ');
    const [activeTab, setActiveTab] = useState<'matches' | 'standings'>('matches');
    const liveMatchCount = matches.filter( m => m.matchStatus === 'LIVE').length; 




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
                        {!isLoading && !error && matches.length === 0 && <p>No matches available.</p>}
                        {!isLoading && !error && matches.map((match) => (
                            <MatchCard key={match.matchId} match={match} prediction={buildPrediction(match)} />
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
