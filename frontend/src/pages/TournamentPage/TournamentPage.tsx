import { useState } from 'react';
import { matches } from './TournamentConstants';
import type { Prediction, WinningTeam } from './TournamentConstants';
import HeroBanner from './Components/HeroBanner';
import Tabs from './Components/Tabs';
import MatchCard from './Components/MatchCard';
import StandingsTable from './Components/StandingsTable';
import BentoBoxes from './Components/BentoBoxes';

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
            <HeroBanner />

            <Tabs activeTab={activeTab} setActiveTab={setActiveTab} />

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

                    {matches.map((match) => (
                        <MatchCard
                            key={match.id}
                            match={match}
                            prediction={predictions[match.id]}
                            onPredict={handlePredict}
                        />
                    ))}
                </div>

                {/* Sidebar */}
                <div className="laptop:col-span-4 space-y-6">
                    <StandingsTable />

                    <BentoBoxes />
                </div>
            </div>
        </div>
    );
}

export default TournamentPage;
