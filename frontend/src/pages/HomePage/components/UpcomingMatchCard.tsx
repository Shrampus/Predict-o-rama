import { Link } from "react-router-dom";
import TeamDisplay from "../../../components/ui/TeamDisplay";
import TimeBadge from "../../../components/ui/TimeBadge";
import { formatKickoffTime,deriveTimeStyle } from "../../TournamentPage/utils/matchCardUtils";
import type { UpcomingMatch } from "../types";

type UpcomingMatchCardProps = {
    match : UpcomingMatch;
};

function UpcomingMatchCard({ match }: UpcomingMatchCardProps) {
  return (
    <div className="relative overflow-hidden rounded-xl bg-white p-6 shadow-sm group flex flex-col sm:flex-row items-center gap-8">
      <div className="absolute top-0 left-0 w-1 h-full bg-green-700 opacity-0 group-hover:opacity-100 transition-opacity" />

      <TeamDisplay
        imageUrl={match.homeTeamImage}
        label=""
        name={match.homeTeamName}
        align="right"
      />

      <div className="flex flex-col items-center gap-3 bg-slate-50 rounded-2xl p-4 min-w-[160px]">
        <span className="text-slate-400 font-bold">VS</span>
        <div className="flex flex-col gap-2 w-full">
          {match.groups.map((group) => (
            <Link
              key={group.groupId}
              to={`/predictions?groupId=${group.groupId}&competition=${group.competitionId}`}
              className="w-full text-center px-4 py-1.5 rounded-full bg-orange-600 text-white text-xs font-bold uppercase tracking-widest hover:bg-orange-700 transition-colors"
            >
              Predict · {group.groupName}
            </Link>
          ))}
        </div>
      </div>

      <TeamDisplay
        imageUrl={match.awayTeamImage}
        label=""
        name={match.awayTeamName}
        align="left"
      />

      <TimeBadge
        time={formatKickoffTime(match.kickoffTime)}
        timeStyle={deriveTimeStyle('SCHEDULED')}
      />
    </div>
  );
}

export default UpcomingMatchCard;