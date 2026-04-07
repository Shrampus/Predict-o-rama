function Tabs({ activeTab, setActiveTab }: { activeTab: 'matches' | 'standings'; setActiveTab: (tab: 'matches' | 'standings') => void }) {
    return (
        <div className="flex gap-2 mb-8 bg-slate-100 p-1.5 rounded-full w-fit mx-auto sm:mx-0">
            <button
                onClick={() => setActiveTab('matches')}
                className={`px-8 py-2.5 rounded-full font-bold text-sm transition-all ${activeTab === 'matches'
                    ? 'bg-green-700 text-white shadow-lg'
                    : 'text-slate-500 hover:bg-slate-200'
                    }`}
            >
                Matches
            </button>
            <button
                onClick={() => setActiveTab('standings')}
                className={`px-8 py-2.5 rounded-full font-bold text-sm transition-all ${activeTab === 'standings'
                    ? 'bg-green-700 text-white shadow-lg'
                    : 'text-slate-500 hover:bg-slate-200'
                    }`}
            >
                Standings
            </button>
        </div>
    );
}

export default Tabs;