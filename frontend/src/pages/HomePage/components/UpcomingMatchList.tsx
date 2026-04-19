import type { UpcomingMatch } from "../types";
import UpcomingMatchCard from './UpcomingMatchCard';
import { groupMatchesByGroup } from '../utils/upcomingMatchUtils';

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
        return <p className="text-slate-400 text-sm">No upcoming matches.</p>;
    }

    const hasGroups = matches.some(m => m.groups.length > 0);

    if (!hasGroups) {
        return (
            <div className="space-y-4">
                {matches.map((match) => (
                    <UpcomingMatchCard key={match.matchId} match={match} />
                ))}
            </div>
        );
    }

    const grouped = groupMatchesByGroup(matches);

    return (
        <div className="space-y-8">
            {[...grouped.values()].map(({ group, matches: groupMatches }) => (
                <div key={group.groupId}>
                    <h3 className="text-sm font-semibold uppercase tracking-widest text-slate-400 mb-3">
                        {group.groupName}
                    </h3>
                    <div className="space-y-4">
                        {groupMatches.map((match) => (
                            <UpcomingMatchCard key={match.matchId} match={match} />
                        ))}
                    </div>
                </div>
            ))}
        </div>
    );
}

export default UpcomingMatchList;