import { useTranslation } from 'react-i18next';
import type { UpcomingMatch } from "../types";
import UpcomingMatchCard from './UpcomingMatchCard';
import { groupMatchesByDate } from '../utils/upcomingMatchUtils';

type UpcomingMatchListProps = {
    matches: UpcomingMatch[];
    isLoading: boolean;
    hasError: boolean;
};

function UpcomingMatchList({ matches, isLoading, hasError }: UpcomingMatchListProps) {
    const { t } = useTranslation();

    if (isLoading) {
        return <p className="text-slate-400 text-sm">{t('upcomingMatches.loading')}</p>;
    }
    if (hasError) {
        return <p className="text-red-500 text-sm">{t('upcomingMatches.error')}</p>;
    }
    if (matches.length === 0) {
        return <p className="text-slate-400 text-sm">{t('upcomingMatches.empty')}</p>;
    }

    const grouped = groupMatchesByDate(matches);

    return (
        <div className="space-y-8">
            {[...grouped.values()].map(({ label, matches: dayMatches }) => (
                <div key={label}>
                    <h3 className="text-sm font-semibold uppercase tracking-widest text-slate-400 mb-3">
                        {label}
                    </h3>
                    <div className="space-y-4">
                        {dayMatches.map((match) => (
                            <UpcomingMatchCard key={match.matchId} match={match} />
                        ))}
                    </div>
                </div>
            ))}
        </div>
    );
}

export default UpcomingMatchList;
