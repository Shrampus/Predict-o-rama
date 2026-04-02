import { useState } from 'react';
import TimeBadgeFunction from './TimeBadge';
import WinnerButton from './WinnerButton';
import type { WinningTeam, Prediction, Match } from '../TournamentConstants';
import { getTeamLabel } from '../TournamentConstants';

function deriveWinner(home: number, away: number): WinningTeam {
    if (home > away) return 'Home';
    if (home < away) return 'Away';
    return 'Draw';
}

const DEFAULT_PREDICTION: Prediction = { home: 0, away: 0, winningTeam: 'Draw', saved: false };

function MatchCard({
    match,
    prediction = DEFAULT_PREDICTION,
    onPredict,
}: {
    match: Match;
    prediction?: Prediction;
    onPredict: (id: number, home: number, away: number, winningTeam: WinningTeam) => void;
}) {
    const [home, setHome] = useState(prediction.home);
    const [away, setAway] = useState(prediction.away);
    const [winningTeam, setWinningTeam] = useState<WinningTeam>(deriveWinner(prediction.home, prediction.away));

    function handleHomeChange(val: number) {
        setHome(val);
        setWinningTeam(deriveWinner(val, away));
    }

    function handleAwayChange(val: number) {
        setAway(val);
        setWinningTeam(deriveWinner(home, val));
    }

    return (
        <div className="relative overflow-hidden rounded-xl bg-white p-6 shadow-sm group flex flex-col sm:flex-row items-center gap-8">
            <div className="absolute top-0 left-0 w-1 h-full bg-green-700 opacity-0 group-hover:opacity-100 transition-opacity" />

            {/* Home team */}
            <div className="flex-1 flex flex-col items-center sm:items-end gap-2 text-center sm:text-right">
                <span className="text-5xl">{match.homeTeam.flag}</span>
                <div>
                    <p className="text-xs font-bold uppercase tracking-widest text-slate-400 mb-1">{getTeamLabel(match.homeTeam.name, match.homeTeam.isHost)}</p>
                    <h3 className="text-xl font-black tracking-tight">{match.homeTeam.name}</h3>
                </div>
            </div>

            {/* Score inputs and winner selector */}
            <div className="flex flex-col items-center gap-4 bg-slate-50 rounded-2xl p-4 min-w-[50px]">
                {/* {Score} */}
                <div className="flex items-center gap-4">
                    <input
                        className="w-14 h-14 bg-white rounded-xl text-center text-2xl font-black border border-slate-200 focus:outline-none focus:ring-2 focus:ring-green-400"
                        type="number"
                        min={0}
                        value={home}
                        onChange={(e) => handleHomeChange(Number(e.target.value))}
                    />
                    <span className="text-slate-400 font-bold">VS</span>
                    <input
                        className="w-14 h-14 bg-white rounded-xl text-center text-2xl font-black border border-slate-200 focus:outline-none focus:ring-2 focus:ring-green-400"
                        type="number"
                        min={0}
                        value={away}
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
                    onClick={() => onPredict(match.id, home, away, winningTeam)}
                    className={`w-full px-6 py-2 rounded-full font-bold text-xs uppercase tracking-widest transition-transform active:scale-95 ${prediction.saved
                        ? 'bg-green-700 text-white'
                        : 'bg-orange-600 text-white hover:bg-orange-700'
                        }`}
                >
                    {prediction.saved ? '✓ Saved' : 'Predict Now'}
                </button>
            </div>

            {/* Away team */}
            <div className="flex-1 flex flex-col items-center sm:items-start gap-2 text-center sm:text-left">
                <span className="text-5xl">{match.awayTeam.flag}</span>
                <div>
                    <p className="text-xs font-bold uppercase tracking-widest text-slate-400 mb-1">{getTeamLabel(match.awayTeam.name)}</p>
                    <h3 className="text-xl font-black tracking-tight">{match.awayTeam.name}</h3>
                </div>
            </div>

            <TimeBadgeFunction time={match.time} timeStyle={match.timeStyle} />

        </div>
    );
}

export default MatchCard;