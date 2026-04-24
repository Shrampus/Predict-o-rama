import { useTranslation } from 'react-i18next';
import type { DayRange, GroupOption } from '../hooks/useUpcomingMatchFilters';

type UpcomingMatchFiltersProps = {
    tournamentOptions: string[];
    groupOptions: GroupOption[];
    selectedTournament: string | null;
    selectedGroupId: string | null;
    selectedDays: DayRange | null;
    onTournamentChange: (t: string | null) => void;
    onGroupChange: (g: string | null) => void;
    onDaysChange: (d: DayRange | null) => void;
};

const DAY_RANGES: DayRange[] = ['today', 7, 14, 28];

function FilterPill({
    label,
    isActive,
    onClick,
}: {
    label: string;
    isActive: boolean;
    onClick: () => void;
}) {
    return (
        <button
            onClick={onClick}
            className={`px-3 py-1 rounded-full text-xs font-semibold uppercase tracking-wide transition-colors ${
                isActive
                    ? 'bg-green-700 text-white'
                    : 'bg-slate-100 text-slate-600 hover:bg-slate-200'
            }`}
        >
            {label}
        </button>
    );
}

function UpcomingMatchFilters({
    tournamentOptions,
    groupOptions,
    selectedTournament,
    selectedGroupId,
    selectedDays,
    onTournamentChange,
    onGroupChange,
    onDaysChange,
}: UpcomingMatchFiltersProps) {
    const { t } = useTranslation();

    const hasCompetitionFilter = tournamentOptions.length > 0;
    const hasGroupFilter = groupOptions.length > 0;

    return (
        <div className="flex flex-col gap-3">
            {hasCompetitionFilter && (
                <div className="flex flex-wrap gap-2 items-center">
                    <span className="text-xs font-semibold text-slate-400 uppercase tracking-wide w-20 shrink-0">
                        {t('upcomingMatches.filters.competition')}
                    </span>
                    <FilterPill
                        label={t('upcomingMatches.filters.all')}
                        isActive={selectedTournament === null}
                        onClick={() => onTournamentChange(null)}
                    />
                    {tournamentOptions.map(tournament => (
                        <FilterPill
                            key={tournament}
                            label={tournament}
                            isActive={selectedTournament === tournament}
                            onClick={() => onTournamentChange(tournament)}
                        />
                    ))}
                </div>
            )}

            {hasGroupFilter && (
                <div className="flex flex-wrap gap-2 items-center">
                    <span className="text-xs font-semibold text-slate-400 uppercase tracking-wide w-20 shrink-0">
                        {t('upcomingMatches.filters.group')}
                    </span>
                    <FilterPill
                        label={t('upcomingMatches.filters.all')}
                        isActive={selectedGroupId === null}
                        onClick={() => onGroupChange(null)}
                    />
                    {groupOptions.map(group => (
                        <FilterPill
                            key={group.groupId}
                            label={group.groupName}
                            isActive={selectedGroupId === group.groupId}
                            onClick={() => onGroupChange(group.groupId)}
                        />
                    ))}
                </div>
            )}

            <div className="flex flex-wrap gap-2 items-center">
                <span className="text-xs font-semibold text-slate-400 uppercase tracking-wide w-20 shrink-0">
                    {t('upcomingMatches.filters.dateRange')}
                </span>
                <FilterPill
                    label={t('upcomingMatches.filters.all')}
                    isActive={selectedDays === null}
                    onClick={() => onDaysChange(null)}
                />
                {DAY_RANGES.map(days => (
                    <FilterPill
                        key={days}
                        label={days === 'today' ? t('upcomingMatches.filters.today') : t('upcomingMatches.filters.nextDays', { count: days })}
                        isActive={selectedDays === days}
                        onClick={() => onDaysChange(days)}
                    />
                ))}
            </div>
        </div>
    );
}

export default UpcomingMatchFilters;
