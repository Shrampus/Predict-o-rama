import { standings } from '../TournamentConstants';

function StandingsTable() {
    return (
        <div className="bg-slate-50 rounded-2xl p-6">
            <h3 className="text-xl font-black mb-6 uppercase tracking-tight">Group A Standings</h3>
            <div className="space-y-3">
                {standings.map((team) => (
                    <div
                        key={team.rank}
                        className={`p-4 rounded-xl flex items-center justify-between ${team.active
                            ? 'bg-green-200 text-green-900 shadow-md'
                            : team.danger
                                ? 'bg-white border-l-4 border-red-300'
                                : 'bg-white'
                            }`}
                    >
                        <div className="flex items-center gap-3">
                            <span className={`font-black text-lg ${!team.active ? 'text-slate-400' : ''}`}>
                                {team.rank}
                            </span>
                            <span className="text-2xl">{team.flag}</span>
                            <span className="font-bold">{team.name}</span>
                        </div>
                        <div className="flex gap-4 items-center">
                            <span className="text-slate-400 text-sm">{team.pts}pts</span>
                            <span className={`font-black text-sm ${team.danger ? 'text-red-600' : ''}`}>
                                {team.gd}
                            </span>
                        </div>
                    </div>
                ))}
            </div>
            <button className="w-full mt-6 text-green-700 font-bold text-sm uppercase tracking-widest py-2 hover:bg-white rounded-lg transition-colors">
                View Detailed Table
            </button>
        </div>
    );
}

export default StandingsTable;