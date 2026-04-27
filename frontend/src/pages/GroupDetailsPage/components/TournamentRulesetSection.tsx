import { useTranslation } from 'react-i18next';

import { useTournamentRuleset } from '../hooks/useTournamentRuleset';

type TournamentRulesetSectionProps = {
  groupId: string;
  tournamentId: string;
  isAdmin: boolean;
  onRulesSaved: () => void;
};

export function TournamentRulesetSection({
  groupId,
  tournamentId,
  isAdmin,
  onRulesSaved,
}: TournamentRulesetSectionProps) {
  const { t } = useTranslation();
  const { rules, isLoading, isSaving, isDirty, error, successMessage, handleToggle, handlePointsChange, handlePointsBlur, handleSave } =
    useTournamentRuleset(groupId, tournamentId, onRulesSaved);

  return (
    <div className="mt-1 bg-slate-50 border border-slate-200 rounded-xl px-3.5 py-3 space-y-2.5">
      <p className="text-[11px] font-semibold text-slate-400 uppercase tracking-wide">
        {t('groups.ruleset.title')}
      </p>

      {isLoading && <p className="text-sm text-slate-500">{t('groups.ruleset.loading')}</p>}
      {!isLoading && error && rules.length === 0 && <p className="text-sm text-red-600">{error}</p>}

      {!isLoading && rules.length > 0 && (
        <div className="space-y-2">
          {rules.map((rule) => (
            <div key={rule.name} className="flex items-center gap-3">
              <input
                type="checkbox"
                id={`rule-${tournamentId}-${rule.name}`}
                checked={rule.enabled}
                onChange={() => {
                  handleToggle(rule.name);
                }}
                disabled={!isAdmin || isSaving}
                className="h-4 w-4 rounded border-slate-300 accent-slate-700 disabled:opacity-50"
              />
              <label
                htmlFor={`rule-${tournamentId}-${rule.name}`}
                className={`flex-1 text-sm ${rule.enabled ? 'text-slate-700' : 'text-slate-400'} ${isAdmin ? 'cursor-pointer' : ''}`}
              >
                {t(`groups.ruleset.rules.${rule.name}`)}
              </label>
              {rule.enabled && (
                <>
                  <input
                    type="number"
                    value={rule.points}
                    min={1}
                    onChange={(e) => handlePointsChange(rule.name, e.target.value)}
                    onBlur={() => handlePointsBlur(rule.name)}
                    disabled={!isAdmin || isSaving}
                    className="w-14 border border-slate-300 rounded-lg px-2 py-1 text-sm text-center bg-white focus:outline-none focus:ring-2 focus:ring-green-600/20 focus:border-green-600 disabled:bg-slate-100 disabled:text-slate-400"
                  />
                  <span className="text-xs text-slate-500 w-5">{t('groups.ruleset.points')}</span>
                </>
              )}
            </div>
          ))}
        </div>
      )}

      {isAdmin && !isLoading && rules.length > 0 && (
        <div className="pt-2 border-t border-slate-100 flex items-center justify-between gap-3">
          <div className="min-h-[1.25rem]">
            {error && <p className="text-sm text-red-600">{error}</p>}
            {!error && successMessage && <p className="text-sm text-emerald-700">{successMessage}</p>}
          </div>
          <button
            type="button"
            onClick={() => void handleSave()}
            disabled={isSaving || rules.length === 0 || (!isDirty && !error) || !!error}
            className={`rounded-lg text-white px-3 py-1.5 text-xs font-semibold transition-colors disabled:opacity-50 ${
              error
                ? 'bg-red-600 disabled:hover:bg-red-600'
                : isDirty
                  ? 'bg-green-700 hover:bg-green-800 disabled:hover:bg-green-700'
                  : 'bg-green-600 disabled:hover:bg-green-600'
            }`}
          >
            {isSaving ? t('groups.ruleset.saving') : isDirty ? t('groups.ruleset.saveButton') : t('groups.ruleset.saved')}
          </button>
        </div>
      )}

      {!isAdmin && (
        <p className="text-xs text-slate-400 pt-1 border-t border-slate-100">
          {t('groups.ruleset.readOnly')}
        </p>
      )}
    </div>
  );
}
