import { useUpcomingMatches } from "./hooks/useUpcomingMatches"
import UpcomingMatchList from "./components/UpcomingMatchList"

function HomePage() {
  const { upcomingMatches, isLoading, hasError } = useUpcomingMatches();

  return (
    <div className="max-w-2xl mx-auto px-4 py-8 flex flex-col gap-6">
      <h2 className="text-xl font-bold text-slate-800">Upcoming Matches</h2>
      <UpcomingMatchList
        matches={upcomingMatches}
        isLoading={isLoading}
        hasError={hasError}
      />
    </div>
  );
}

export default HomePage;