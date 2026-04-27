import type { FormEvent } from 'react';
import { Fragment, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { useNavigate } from 'react-router-dom';

import { ROUTE_PATHS } from '../../../app/routePaths';
import type { GroupTournamentResponse, TournamentOption } from '../../../services/groupApi';
import { TournamentRulesetSection } from './TournamentRulesetSection';

type GroupTournamentsSectionProps = {
  groupId: string;
  tournaments: GroupTournamentResponse[];
  isLoadingTournaments: boolean;
  tournamentsError: string;
  isAdmin: boolean;
  removingTournamentId: string | null;
  selectableTournaments: TournamentOption[];
  selectedTournamentId: string;
  isLoadingAvailableTournaments: boolean;
  availableTournamentsError: string;
  isAddingTournament: boolean;
  tournamentActionMessage: string;
  tournamentActionError: string;
  onAddTournament: (event: FormEvent<HTMLFormElement>) => void;
  onSelectTournament: (value: string) => void;
  onResetTournamentFeedback: () => void;
  onRemoveTournament: (tournament: GroupTournamentResponse) => void;
  onRulesSaved: () => void;
};

export function GroupTournamentsSection({
  groupId,
  tournaments,
  isLoadingTournaments,
  tournamentsError,
  isAdmin,
  removingTournamentId,
  selectableTournaments,
  selectedTournamentId,
  isLoadingAvailableTournaments,
  availableTournamentsError,
  isAddingTournament,
  tournamentActionMessage,
  tournamentActionError,
  onAddTournament,
  onSelectTournament,
  onResetTournamentFeedback,
  onRemoveTournament,
  onRulesSaved,
}: GroupTournamentsSectionProps) {
  const navigate = useNavigate();
  const { t } = useTranslation();
  const [openRulesetTournamentId, setOpenRulesetTournamentId] = useState<string | null>(null);

  function toggleRuleset(tournamentId: string) {
    setOpenRulesetTournamentId((current) => (current === tournamentId ? null : tournamentId));
  }

  function getTournamentDetailsPath(competitionCode: string) {
    return ROUTE_PATHS.groupTournamentDetails
      .replace(':groupId', groupId)
      .replace(':tournament', competitionCode);
  }

  function openTournamentDetails(
    competitionCode: string | null,
    tournamentName?: string,
    seasonLabel?: string | null,
  ) {
    if (!competitionCode) {
      return;
    }
    navigate(getTournamentDetailsPath(competitionCode), {
      state: {
        tournamentName,
        seasonLabel,
      },
    });
  }

  function shouldIgnoreRowNavigation(target: EventTarget | null): boolean {
    if (!(target instanceof HTMLElement)) {
      return false;
    }

    return target.closest('button, a') !== null;
  }

  return (
    <section className="bg-white border border-slate-200 rounded-2xl p-5 sm:p-6 shadow-sm space-y-4">
      <h2 className="text-xl font-bold text-slate-900">{t('groups.tournamentsSection.title')}</h2>
      {isLoadingTournaments && (
        <p className="text-sm text-slate-500">{t('groups.tournamentsSection.loadingTournaments')}</p>
      )}
      {tournamentsError && <p className="text-sm text-red-600">{tournamentsError}</p>}
      {!isLoadingTournaments && !tournamentsError && tournaments.length === 0 && (
        <p className="text-sm text-slate-500">{t('groups.tournamentsSection.emptyTournaments')}</p>
      )}
      {!isLoadingTournaments && !tournamentsError && tournaments.length > 0 && (
        <ul className="space-y-2.5">
          {tournaments.map((tournament) => (
            <Fragment key={tournament.id}>
            <li
              onClick={(event) => {
                if (shouldIgnoreRowNavigation(event.target)) {
                  return;
                }
                openTournamentDetails(
                  tournament.competitionCode,
                  tournament.name,
                  tournament.seasonLabel,
                );
              }}
              onKeyDown={(event) => {
                if (shouldIgnoreRowNavigation(event.target)) {
                  return;
                }
                if (event.key === 'Enter' || event.key === ' ') {
                  event.preventDefault();
                  openTournamentDetails(
                    tournament.competitionCode,
                    tournament.name,
                    tournament.seasonLabel,
                  );
                }
              }}
              role="button"
              tabIndex={tournament.competitionCode ? 0 : -1}
              aria-disabled={!tournament.competitionCode}
              className={`text-sm text-slate-700 flex items-center justify-between bg-slate-50/70 border border-slate-200 rounded-xl px-3.5 py-2.5 gap-3 ${
                tournament.competitionCode
                  ? 'cursor-pointer hover:bg-slate-100 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-green-600/40'
                  : 'cursor-default'
              }`}
            >
              <div className="min-w-0">
                <p className="font-bold text-slate-800">{tournament.name}</p>
                {tournament.seasonLabel && <p className="text-xs text-slate-500">{tournament.seasonLabel}</p>}
              </div>
              <div className="flex items-center gap-2">
                {tournament.competitionCode ? (
                  <button
                    type="button"
                    onClick={() =>
                      openTournamentDetails(
                        tournament.competitionCode,
                        tournament.name,
                        tournament.seasonLabel,
                      )
                    }
                    className="rounded-lg border border-green-200 bg-green-50 px-2.5 py-1 text-[11px] font-semibold uppercase tracking-wide text-green-700 hover:bg-green-100"
                  >
                    {t('groups.tournamentsSection.open')}
                  </button>
                ) : (
                  <span className="rounded-lg border border-slate-200 bg-slate-100 px-2.5 py-1 text-[11px] font-semibold uppercase tracking-wide text-slate-500">
                    {t('groups.tournamentsSection.unavailable')}
                  </span>
                )}
                <button
                  type="button"
                  onClick={() => toggleRuleset(tournament.id)}
                  className={`rounded-lg border px-2.5 py-1 text-[11px] font-semibold uppercase tracking-wide transition-colors ${
                    openRulesetTournamentId === tournament.id
                      ? 'border-slate-300 bg-slate-200 text-slate-700 hover:bg-slate-300'
                      : 'border-slate-200 bg-slate-100 text-slate-600 hover:bg-slate-200'
                  }`}
                >
                  {t('groups.ruleset.title')}
                </button>
                {isAdmin && (
                  <button
                    type="button"
                    onClick={() => onRemoveTournament(tournament)}
                    disabled={removingTournamentId === tournament.id}
                    className="rounded-lg border border-red-200 bg-red-50 px-2.5 py-1 text-[11px] font-semibold uppercase tracking-wide text-red-700 hover:bg-red-100 disabled:opacity-60 disabled:hover:bg-red-50"
                  >
                    {removingTournamentId === tournament.id
                      ? t('groups.tournamentsSection.removing')
                      : t('groups.tournamentsSection.remove')}
                  </button>
                )}
              </div>
            </li>
            {openRulesetTournamentId === tournament.id && (
              <TournamentRulesetSection
                groupId={groupId}
                tournamentId={tournament.id}
                isAdmin={isAdmin}
                onRulesSaved={onRulesSaved}
              />
            )}
            </Fragment>
          ))}
        </ul>
      )}

      {isAdmin ? (
        <form onSubmit={onAddTournament} className="pt-4 border-t border-slate-100 space-y-2.5">
          <label htmlFor="tournament-id" className="block text-sm font-semibold text-slate-700">
            {t('groups.tournamentsSection.addTournament')}
          </label>
          <div className="flex gap-2">
            <select
              id="tournament-id"
              value={selectedTournamentId}
              onChange={(event) => {
                onSelectTournament(event.target.value);
                onResetTournamentFeedback();
              }}
              disabled={isAddingTournament || isLoadingAvailableTournaments}
              className="flex-1 border border-slate-300 rounded-lg px-3 py-2 text-sm bg-white text-slate-800 focus:outline-none focus:ring-2 focus:ring-green-600/20 focus:border-green-600"
            >
              <option value="">{t('groups.tournamentsSection.selectTournament')}</option>
              {selectableTournaments.map((tournament) => (
                <option key={tournament.id} value={tournament.id}>
                  {tournament.name}
                </option>
              ))}
            </select>
            <button
              type="submit"
              disabled={
                isAddingTournament || selectedTournamentId.length === 0 || isLoadingAvailableTournaments
              }
              className="rounded-lg bg-green-700 hover:bg-green-800 text-white px-4 py-2 text-sm font-semibold transition-colors disabled:opacity-50 disabled:hover:bg-green-700"
            >
              {isAddingTournament
                ? t('groups.tournamentsSection.adding')
                : t('groups.tournamentsSection.add')}
            </button>
          </div>
          {isLoadingAvailableTournaments && (
            <p className="text-sm text-slate-500">
              {t('groups.tournamentsSection.loadingOptions')}
            </p>
          )}
          {!isLoadingAvailableTournaments && !availableTournamentsError && selectableTournaments.length === 0 && (
            <p className="text-sm text-slate-500">
              {t('groups.tournamentsSection.emptyOptions')}
            </p>
          )}
          {availableTournamentsError && <p className="text-sm text-red-600">{availableTournamentsError}</p>}
          {tournamentActionMessage && <p className="text-sm text-emerald-700">{tournamentActionMessage}</p>}
          {tournamentActionError && <p className="text-sm text-red-600">{tournamentActionError}</p>}
        </form>
      ) : (
        <p className="text-xs text-slate-500 pt-4 border-t border-slate-100">
          {t('groups.tournamentsSection.adminOnlyAction')}
        </p>
      )}
    </section>
  );
}
