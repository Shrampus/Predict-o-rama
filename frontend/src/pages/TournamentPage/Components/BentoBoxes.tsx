function BentoBoxes() {
    return (
        <div className="grid grid-cols-2 gap-4">
            <div className="bg-orange-500 text-white p-4 rounded-2xl flex flex-col justify-between aspect-square">
                <span className="text-3xl">🏆</span>
                <div>
                    <p className="text-[10px] font-bold uppercase opacity-80">Top Scorer</p>
                    <p className="text-lg font-black leading-tight">Mbappé</p>
                    <p className="text-xs">5 Goals</p>
                </div>
            </div>
            <div className="bg-blue-100 text-slate-800 p-4 rounded-2xl flex flex-col justify-between aspect-square">
                <span className="text-3xl">📈</span>
                <div>
                    <p className="text-[10px] font-bold uppercase opacity-80">My Rank</p>
                    <p className="text-lg font-black leading-tight">#1,402</p>
                    <p className="text-xs text-green-700 font-bold">Top 5%</p>
                </div>
            </div>
        </div>
    );
}

export default BentoBoxes;