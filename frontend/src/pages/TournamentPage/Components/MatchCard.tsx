import { useState } from 'react';

import TimeBadge from './TimeBadge';
import WinnerButton from './WinnerButton';
import TeamDisplay from './TeamDisplay';
import { deriveWinner, DEFAULT_PREDICTION } from './matchCardUtils';
import { getTeamLabel } from '../TournamentConstants';
import type { WinningTeam, Prediction, Match } from '../TournamentConstants';

function MatchCard({
    match,
    prediction = DEFAULT_PREDICTION,
    onPredict,
}: {
    match: Match;
    prediction?: Prediction;
    onPredict: (id: number, home: number, away: number, winningTeam: WinningTeam) => void;
}) {
    const [homeScore, setHomeScore] = useState(prediction.home);
    const [awayScore, setAwayScore] = useState(prediction.away);
    const [winningTeam, setWinningTeam] = useState<WinningTeam>(deriveWinner(prediction.home, prediction.away));

    function handleHomeChange(val: number) {
        setHomeScore(val);
        setWinningTeam(deriveWinner(val, awayScore));
    }

    function handleAwayChange(val: number) {
        setAwayScore(val);
        setWinningTeam(deriveWinner(homeScore, val));
    }

    return (
        <div className="relative overflow-hidden rounded-xl bg-white p-6 shadow-sm group flex flex-col sm:flex-row items-center gap-8">
            <div className="absolute top-0 left-0 w-1 h-full bg-green-700 opacity-0 group-hover:opacity-100 transition-opacity" />

            <TeamDisplay
                flag={match.homeTeam.flag}
                label={getTeamLabel(match.homeTeam.name, match.homeTeam.isHost)}
                name={match.homeTeam.name}
                align="right"
            />

            {/* Score inputs and winner selector */}
            <div className="flex flex-col items-center gap-4 bg-slate-50 rounded-2xl p-4 min-w-[50px]">
                <div className="flex items-center gap-4">
                    <input
                        className="w-14 h-14 bg-white rounded-xl text-center text-2xl font-black border border-slate-200 focus:outline-none focus:ring-2 focus:ring-green-400"
                        type="number"
                        min={0}
                        value={homeScore}
                        onChange={(e) => handleHomeChange(Number(e.target.value))}
                    />
                    <span className="text-slate-400 font-bold">VS</span>
                    <input
                        className="w-14 h-14 bg-white rounded-xl text-center text-2xl font-black border border-slate-200 focus:outline-none focus:ring-2 focus:ring-green-400"
                        type="number"
                        min={0}
                        value={awayScore}
                        onChange={(e) => handleAwayChange(Number(e.target.value))}
                    />
                </div>

                {/* Winner selector */}
                <div className="flex w-full gap-10">
                    <WinnerButton
                        isActive={winningTeam === 'Home'}
                        onClick={() => setWinningTeam('Home')}
                    >
                        {match.homeTeam.name}
                    </WinnerButton>
                    <WinnerButton
                        isActive={winningTeam === 'Draw'}
                        onClick={() => setWinningTeam('Draw')}
                    >
                        Draw
                    </WinnerButton>
                    <WinnerButton
                        isActive={winningTeam === 'Away'}
                        onClick={() => setWinningTeam('Away')}
                    >
                        {match.awayTeam.name}
                    </WinnerButton>
                </div>

                {/* Submit */}
                <button
                    onClick={() => onPredict(match.id, homeScore, awayScore, winningTeam)}
                    className={`w-full px-6 py-2 rounded-full font-bold text-xs uppercase tracking-widest transition-transform active:scale-95 ${
                        prediction.saved
                            ? 'bg-green-700 text-white'
                            : 'bg-orange-600 text-white hover:bg-orange-700'
                    }`}
                >
                    {prediction.saved ? '✓ Saved' : 'Predict Now'}
                </button>
            </div>

            <TeamDisplay
                flag={match.awayTeam.flag}
                label={getTeamLabel(match.awayTeam.name)}
                name={match.awayTeam.name}
                align="left"
            />

            <TimeBadge time={match.time} timeStyle={match.timeStyle} />
        </div>
    );
}

export default MatchCard;
