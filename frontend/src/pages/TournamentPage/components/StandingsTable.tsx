import { standings } from '../TournamentConstants';

const groups = Array.from(new Set(standings.map((t) => t.group)));

function StandingsTable() {
    return (
        <div className="space-y-6">
            {groups.map((group) => (
                <div key={group} className="bg-slate-50 rounded-2xl p-6">
                    <h3 className="text-xl font-black mb-4 uppercase tracking-tight">
                        Group {group}
                    </h3>
                    <div className="space-y-3">
                        {standings
                            .filter((t) => t.group === group)
                            .map((team) => (
                                <div
                                    key={team.name}
                                    className={`p-4 rounded-xl flex items-center justify-between ${
                                        team.rank === 1
                                            ? 'bg-green-200 text-green-900 shadow-md'
                                            : team.danger
                                            ? 'bg-white border-l-4 border-red-300'
                                            : 'bg-white'
                                    }`}
                                >
                                    <div className="flex items-center gap-3">
                                        <span className={`font-black text-lg ${team.rank !== 1 ? 'text-slate-400' : ''}`}>
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
                </div>
            ))}
        </div>
    );
}

export default StandingsTable;