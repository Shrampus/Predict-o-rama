import { useEffect, useState } from 'react';

import { getRuleset, saveRuleset, type RulesetResponse } from '../../../services/rulesetApi';

export type RuleConfig = { name: string; enabled: boolean; points: string };

const RULE_ORDER = ['CORRECT_WINNER', 'EXACT_SCORE', 'CORRECT_GOAL_DIFFERENCE'];

function responseToRules(response: RulesetResponse): RuleConfig[] {
  const merged: RuleConfig[] = [
    ...Object.entries(response.activeRules).map(([name, points]) => ({
      name,
      enabled: true,
      points: String(points),
    })),
    ...Object.entries(response.disabledRules).map(([name, points]) => ({
      name,
      enabled: false,
      points: String(points),
    })),
  ];
  return merged.sort((a, b) => {
    const ai = RULE_ORDER.indexOf(a.name);
    const bi = RULE_ORDER.indexOf(b.name);
    return (ai === -1 ? 99 : ai) - (bi === -1 ? 99 : bi);
  });
}

function rulesSignature(rules: RuleConfig[]): string {
  return rules.map((r) => `${r.name}:${r.enabled}:${r.points}`).join(',');
}

type UseTournamentRulesetReturn = {
  rules: RuleConfig[];
  isLoading: boolean;
  isSaving: boolean;
  isDirty: boolean;
  error: string;
  successMessage: string;
  handleToggle: (name: string) => void;
  handlePointsChange: (name: string, value: string) => void;
  handlePointsBlur: (name: string) => void;
  handleSave: () => Promise<void>;
};

export function useTournamentRuleset(
  groupId: string,
  tournamentId: string,
  onRulesSaved: () => void,
): UseTournamentRulesetReturn {
  const [rules, setRules] = useState<RuleConfig[]>([]);
  const [baselineRules, setBaselineRules] = useState<RuleConfig[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [isSaving, setIsSaving] = useState(false);
  const [error, setError] = useState('');
  const [successMessage, setSuccessMessage] = useState('');

  useEffect(() => {
    let isMounted = true;

    async function fetchRuleset() {
      setIsLoading(true);
      setError('');
      try {
        const data = await getRuleset(groupId, tournamentId);
        if (isMounted) {
          const loaded = responseToRules(data);
          setRules(loaded);
          setBaselineRules(loaded);
        }
      } catch (err) {
        if (isMounted) setError(err instanceof Error ? err.message : 'Failed to load rules.');
      } finally {
        if (isMounted) setIsLoading(false);
      }
    }

    void fetchRuleset();

    return () => {
      isMounted = false;
    };
  }, [groupId, tournamentId]);

  function handleToggle(name: string) {
    setRules((current) => current.map((r) => (r.name === name ? { ...r, enabled: !r.enabled } : r)));
  }

  function handlePointsChange(name: string, value: string) {
    if (!/^\d*$/.test(value)) return;
    setRules((current) => current.map((r) => (r.name === name ? { ...r, points: value } : r)));
  }

  function handlePointsBlur(name: string) {
    setRules((current) =>
      current.map((r) => {
        if (r.name !== name) return r;
        const n = parseInt(r.points, 10);
        return { ...r, points: !isNaN(n) && n >= 1 ? String(n) : '1' };
      }),
    );
  }

  async function handleSave() {
    setIsSaving(true);
    setError('');
    setSuccessMessage('');
    try {
      const rulePoints = Object.fromEntries(
        rules.filter((r) => r.enabled).map((r) => [r.name, parseInt(r.points, 10)]),
      );
      const saved = await saveRuleset(groupId, tournamentId, { rulePoints });
      const savedRules = responseToRules(saved);
      setRules(savedRules);
      setBaselineRules(savedRules);
      setSuccessMessage('Rules saved. Leaderboard updating…');
      setTimeout(() => {
        onRulesSaved();
      }, 3000);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to save rules.');
    } finally {
      setIsSaving(false);
    }
  }

  const isDirty = rulesSignature(rules) !== rulesSignature(baselineRules);

  return { rules, isLoading, isSaving, isDirty, error, successMessage, handleToggle, handlePointsChange, handlePointsBlur, handleSave };
}
