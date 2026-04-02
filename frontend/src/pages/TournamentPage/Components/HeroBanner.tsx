type HeroBannerProps = {
    season: string;
    name: string;
    phase: string;
    liveMatchCount: number;
};

function HeroBanner({ season, name, phase, liveMatchCount }: HeroBannerProps) {
    return (
        <section className="relative h-48 sm:h-64 rounded-xl overflow-hidden mb-8 shadow-2xl">
            <div className="absolute inset-0 bg-gradient-to-r from-green-900 to-green-600" />
            <div className="relative z-10 h-full flex flex-col justify-center px-8">
                <span className="text-green-300 font-bold tracking-widest text-xs uppercase mb-2">
                    {season}
                </span>
                <h1 className="text-white text-4xl sm:text-6xl font-black tracking-tighter leading-none mb-4">
                    {name}
                </h1>
                <div className="flex gap-3 flex-wrap">
                    <span className="bg-white/20 backdrop-blur-md text-white px-3 py-1 rounded-full text-sm font-medium">
                        {phase}
                    </span>
                    <span className="bg-orange-500 text-white px-3 py-1 rounded-full text-sm font-bold">
                        {liveMatchCount} Matches Live
                    </span>
                </div>
            </div>
        </section>
    );
}

export default HeroBanner;