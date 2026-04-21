import { Link } from "react-router-dom";
import { useTranslation } from "react-i18next";
import TeamDisplay from "../../../components/ui/TeamDisplay";
import TimeBadge from "../../../components/ui/TimeBadge";
import { formatKickoffTime, deriveTimeStyle } from "../../TournamentPage/utils/matchCardUtils";
import type { UpcomingMatch } from "../types";

type UpcomingMatchCardProps = {
    match: UpcomingMatch;
};

function UpcomingMatchCard({ match }: UpcomingMatchCardProps) {
    const { t } = useTranslation();

    return (
        <div className="relative overflow-hidden rounded-xl bg-white p-6 shadow-sm group flex flex-col sm:flex-row items-center gap-8">
            <div className="absolute top-0 left-0 w-1 h-full bg-green-700 opacity-0 group-hover:opacity-100 transition-opacity" />

            <TeamDisplay
                imageUrl={match.homeTeamImage}
                label=""
                name={match.homeTeamName}
                align="right"
            />

            <div className="flex flex-col items-center gap-3 bg-slate-50 rounded-2xl p-4 min-w-40">
                <span className="text-slate-400 font-bold">{t('matchCard.vs')}</span>
                <div className="flex flex-col gap-2 w-full">
                    {match.groups.map((group) => (
                        <Link
                            key={group.groupId}
                            to={`/groups/${group.groupId}/tournaments/${group.competitionId}`}
                            className={`w-full text-center px-4 py-1.5 rounded-full text-white text-xs font-bold uppercase tracking-widest transition-colors ${
                                group.hasPrediction
                                    ? 'bg-green-700 hover:bg-green-800'
                                    : 'bg-orange-600 hover:bg-orange-700'
                            }`}
                        >
                            {group.hasPrediction
                                ? t('upcomingMatches.predicted', { groupName: group.groupName })
                                : t('upcomingMatches.predict', { groupName: group.groupName })}
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
