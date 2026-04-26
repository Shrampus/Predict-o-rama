import { useTranslation } from 'react-i18next';

import { useUpcomingMatches } from './hooks/useUpcomingMatches';
import { useUpcomingMatchFilters } from './hooks/useUpcomingMatchFilters';
import UpcomingMatchList from './components/UpcomingMatchList';
import UpcomingMatchFilters from './components/UpcomingMatchFilters';

function HomePage() {
  const { t } = useTranslation();

  const { upcomingMatches, isLoading, hasError } = useUpcomingMatches();
  const {
    filteredMatches,
    tournamentOptions,
    groupOptions,
    selectedTournament,
    selectedGroupId,
    selectedDays,
    setSelectedTournament,
    setSelectedGroupId,
    setSelectedDays,
  } = useUpcomingMatchFilters(upcomingMatches);

  return (
    <div className="max-w-2xl mx-auto px-4 py-8 flex flex-col gap-10">
      <section className="flex flex-col gap-6">
        <h2 className="text-xl font-bold text-slate-800">{t('upcomingMatches.title')}</h2>
        <UpcomingMatchFilters
          tournamentOptions={tournamentOptions}
          groupOptions={groupOptions}
          selectedTournament={selectedTournament}
          selectedGroupId={selectedGroupId}
          selectedDays={selectedDays}
          onTournamentChange={setSelectedTournament}
          onGroupChange={setSelectedGroupId}
          onDaysChange={setSelectedDays}
        />
        <UpcomingMatchList matches={filteredMatches} isLoading={isLoading} hasError={hasError} />
      </section>
    </div>
  );
}

export default HomePage;
