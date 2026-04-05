import { useState } from 'react';

import HeroBanner from './Components/HeroBanner';
import MatchCard from './Components/MatchCard';
import StandingsTable from './Components/StandingsTable';
import Tabs from './Components/Tabs';
import { matches } from './TournamentConstants';
import type { Prediction, WinningTeam } from './TournamentConstants';

const groups = Array.from(new Set(matches.map((m) => m.group)));
const matchesByGroup = groups.map((group) => ({
    group,
    matches: matches
        .filter((m) => m.group === group)
        .sort((a, b) => a.datetime.localeCompare(b.datetime)),
}));



function TournamentPage() {
    const [activeTab, setActiveTab] = useState<'matches' | 'standings'>('matches');
    const [predictions, setPredictions] = useState<Record<number, Prediction>>({
        1: { home: 0, away: 0, winningTeam: 'Draw', saved: false },
        2: { home: 1, away: 2, winningTeam: 'Away', saved: false },
    });

    function handlePredict(id: number, home: number, away: number, winningTeam: WinningTeam) {
        setPredictions((prev) => ({ ...prev, [id]: { home, away, saved: true, winningTeam } }));
    }

    return (
        <div className="max-w-7xl mx-auto px-4 sm:px-6 py-8">
            <HeroBanner
                season="Summer 2024 Series"
                name="EURO CHAMPIONS CUP"
                phase="Group Stage Phase"
                liveMatchCount={128}
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

                        {matchesByGroup.map(({ group, matches: groupMatches }) => (
                            <div key={group}>
                                <h3 className="text-sm font-bold uppercase tracking-widest text-slate-400 mb-3">
                                    Group {group}
                                </h3>
                                <div className="space-y-4">
                                    {groupMatches.map((match) => (
                                        <MatchCard
                                            key={match.id}
                                            match={match}
                                            prediction={predictions[match.id]}
                                            onPredict={handlePredict}
                                        />
                                    ))}
                                </div>
                            </div>
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
