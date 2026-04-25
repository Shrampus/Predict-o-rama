import { useEffect, useState } from 'react';

import { getRuleset, saveRuleset, type RulesetResponse } from '../../../services/rulesetApi';

export type RuleConfig = { name: string; enabled: boolean; points: number };

const RULE_ORDER = ['CORRECT_WINNER', 'EXACT_SCORE', 'CORRECT_GOAL_DIFFERENCE'];

function responseToRules(response: RulesetResponse): RuleConfig[] {
  const merged: RuleConfig[] = [
    ...Object.entries(response.activeRules).map(([name, points]) => ({
      name,
      enabled: true,
      points,
    })),
    ...Object.entries(response.disabledRules).map(([name, points]) => ({
      name,
      enabled: false,
      points,
    })),
  ];
  return merged.sort((a, b) => {
    const ai = RULE_ORDER.indexOf(a.name);
    const bi = RULE_ORDER.indexOf(b.name);
    return (ai === -1 ? 99 : ai) - (bi === -1 ? 99 : bi);
  });
}

type UseTournamentRulesetReturn = {
  rules: RuleConfig[];
  isLoading: boolean;
  isSaving: boolean;
  error: string;
  successMessage: string;
  handleToggle: (name: string) => void;
  handlePointsChange: (name: string, points: number) => void;
  handleSave: () => Promise<void>;
};

export function useTournamentRuleset(
  groupId: string,
  tournamentId: string,
  onRulesSaved: () => void,
): UseTournamentRulesetReturn {
  const [rules, setRules] = useState<RuleConfig[]>([]);
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
        if (isMounted) setRules(responseToRules(data));
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

  function handlePointsChange(name: string, points: number) {
    setRules((current) => current.map((r) => (r.name === name ? { ...r, points } : r)));
  }

  async function handleSave() {
    setIsSaving(true);
    setError('');
    setSuccessMessage('');
    try {
      const rulePoints = Object.fromEntries(
        rules.filter((r) => r.enabled).map((r) => [r.name, r.points]),
      );
      const saved = await saveRuleset(groupId, tournamentId, { rulePoints });
      setRules(responseToRules(saved));
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

  return { rules, isLoading, isSaving, error, successMessage, handleToggle, handlePointsChange, handleSave };
}
