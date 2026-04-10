import type { UpcomingMatch } from "../types";
import UpcomingMatchCard from './UpcomingMatchCard';


type UpcomingMatchListProps = {
    matches: UpcomingMatch[];
    isLoading: boolean;
    hasError: boolean;
};

function UpcomingMatchList({ matches, isLoading, hasError }: UpcomingMatchListProps) {
    if (isLoading) {
        return <p className="text-slate-400 text-sm">Loading upcoming matches...</p>;
    }
    if (hasError) {
        return <p className="text-red-500 text-sm">Failed to load upcoming matches.</p>;
    }
    if (matches.length === 0) {
        return <p className="text-slate-400 text-sm">No upcoming matches to predict.</p>;
    }

    return (
        <div className="space-y-4">
            {matches.map((match) => (
                <UpcomingMatchCard key={match.matchId} match={match} />
            ))}
        </div>
    );
}

export default UpcomingMatchList;