import { act, renderHook, waitFor } from '@testing-library/react';
import { beforeEach, describe, expect, it, vi } from 'vitest';

import { getRuleset, saveRuleset } from '../../../services/rulesetApi';
import type { RulesetResponse } from '../../../services/rulesetApi';
import { useTournamentRuleset } from './useTournamentRuleset';

vi.mock('react-i18next', () => ({
  useTranslation: () => ({ t: (key: string) => key }),
}));

vi.mock('../../../services/rulesetApi', () => ({
  getRuleset: vi.fn(),
  saveRuleset: vi.fn(),
}));

function makeResponse(overrides: Partial<RulesetResponse> = {}): RulesetResponse {
  return {
    id: 'ruleset-1',
    activeRules: { CORRECT_WINNER: 1, EXACT_SCORE: 3 },
    disabledRules: { CORRECT_GOAL_DIFFERENCE: 2 },
    ...overrides,
  };
}

describe('useTournamentRuleset', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    vi.useRealTimers();
  });

  it('loads ruleset on mount and transitions out of loading state', async () => {
    vi.mocked(getRuleset).mockResolvedValue(makeResponse());

    const { result } = renderHook(() => useTournamentRuleset('g1', 't1', vi.fn()));

    expect(result.current.isLoading).toBe(true);

    await waitFor(() => {
      expect(result.current.isLoading).toBe(false);
    });

    expect(getRuleset).toHaveBeenCalledWith('g1', 't1');
    expect(result.current.rules).toHaveLength(3);
  });

  it('orders rules by RULE_ORDER: CORRECT_WINNER, EXACT_SCORE, CORRECT_GOAL_DIFFERENCE', async () => {
    vi.mocked(getRuleset).mockResolvedValue(makeResponse());

    const { result } = renderHook(() => useTournamentRuleset('g1', 't1', vi.fn()));

    await waitFor(() => expect(result.current.isLoading).toBe(false));

    expect(result.current.rules.map((r) => r.name)).toEqual([
      'CORRECT_WINNER',
      'EXACT_SCORE',
      'CORRECT_GOAL_DIFFERENCE',
    ]);
  });

  it('marks active rules as enabled and disabled rules as disabled', async () => {
    vi.mocked(getRuleset).mockResolvedValue(makeResponse({
      activeRules: { CORRECT_WINNER: 1 },
      disabledRules: { EXACT_SCORE: 3, CORRECT_GOAL_DIFFERENCE: 2 },
    }));

    const { result } = renderHook(() => useTournamentRuleset('g1', 't1', vi.fn()));

    await waitFor(() => expect(result.current.isLoading).toBe(false));

    const winner = result.current.rules.find((r) => r.name === 'CORRECT_WINNER');
    const exact = result.current.rules.find((r) => r.name === 'EXACT_SCORE');
    expect(winner?.enabled).toBe(true);
    expect(exact?.enabled).toBe(false);
  });

  it('sets error with translation key on load failure when error is not an Error instance', async () => {
    vi.mocked(getRuleset).mockRejectedValue('unexpected failure');

    const { result } = renderHook(() => useTournamentRuleset('g1', 't1', vi.fn()));

    await waitFor(() => expect(result.current.isLoading).toBe(false));

    expect(result.current.error).toBe('groups.ruleset.loadFailed');
  });

  it('handleToggle flips the enabled state of a rule', async () => {
    vi.mocked(getRuleset).mockResolvedValue(makeResponse());

    const { result } = renderHook(() => useTournamentRuleset('g1', 't1', vi.fn()));
    await waitFor(() => expect(result.current.isLoading).toBe(false));

    act(() => result.current.handleToggle('CORRECT_WINNER'));

    expect(result.current.rules.find((r) => r.name === 'CORRECT_WINNER')?.enabled).toBe(false);
  });

  it('handlePointsChange accepts digit strings and ignores non-digits', async () => {
    vi.mocked(getRuleset).mockResolvedValue(makeResponse());

    const { result } = renderHook(() => useTournamentRuleset('g1', 't1', vi.fn()));
    await waitFor(() => expect(result.current.isLoading).toBe(false));

    act(() => result.current.handlePointsChange('EXACT_SCORE', '7'));
    expect(result.current.rules.find((r) => r.name === 'EXACT_SCORE')?.points).toBe('7');

    act(() => result.current.handlePointsChange('EXACT_SCORE', 'abc'));
    expect(result.current.rules.find((r) => r.name === 'EXACT_SCORE')?.points).toBe('7');
  });

  it('handlePointsBlur clamps empty or zero value to 1', async () => {
    vi.mocked(getRuleset).mockResolvedValue(makeResponse());

    const { result } = renderHook(() => useTournamentRuleset('g1', 't1', vi.fn()));
    await waitFor(() => expect(result.current.isLoading).toBe(false));

    act(() => result.current.handlePointsChange('EXACT_SCORE', ''));
    act(() => result.current.handlePointsBlur('EXACT_SCORE'));

    expect(result.current.rules.find((r) => r.name === 'EXACT_SCORE')?.points).toBe('1');
  });

  it('isDirty is false initially and true after a change', async () => {
    vi.mocked(getRuleset).mockResolvedValue(makeResponse());

    const { result } = renderHook(() => useTournamentRuleset('g1', 't1', vi.fn()));
    await waitFor(() => expect(result.current.isLoading).toBe(false));

    expect(result.current.isDirty).toBe(false);

    act(() => result.current.handleToggle('CORRECT_WINNER'));

    expect(result.current.isDirty).toBe(true);
  });

  it('handleSave sends only enabled rules, sets success message, and calls onRulesSaved after delay', async () => {
    vi.mocked(getRuleset).mockResolvedValue(makeResponse());
    const savedResponse = makeResponse({ activeRules: { CORRECT_WINNER: 1, EXACT_SCORE: 3 } });
    vi.mocked(saveRuleset).mockResolvedValue(savedResponse);
    const onRulesSaved = vi.fn();

    const { result } = renderHook(() => useTournamentRuleset('g1', 't1', onRulesSaved));
    await waitFor(() => expect(result.current.isLoading).toBe(false));

    vi.useFakeTimers();

    await act(async () => {
      await result.current.handleSave();
    });

    expect(saveRuleset).toHaveBeenCalledWith('g1', 't1', {
      rulePoints: { CORRECT_WINNER: 1, EXACT_SCORE: 3 },
    });
    expect(result.current.successMessage).toBe('groups.ruleset.saveSuccess');
    expect(onRulesSaved).not.toHaveBeenCalled();

    act(() => vi.advanceTimersByTime(3000));
    expect(onRulesSaved).toHaveBeenCalledOnce();
  });

  it('handleSave resets isDirty after a successful save', async () => {
    vi.mocked(getRuleset).mockResolvedValue(makeResponse());
    vi.mocked(saveRuleset).mockResolvedValue(makeResponse());

    const { result } = renderHook(() => useTournamentRuleset('g1', 't1', vi.fn()));
    await waitFor(() => expect(result.current.isLoading).toBe(false));

    act(() => result.current.handleToggle('CORRECT_WINNER'));
    expect(result.current.isDirty).toBe(true);

    await act(async () => {
      await result.current.handleSave();
    });

    expect(result.current.isDirty).toBe(false);
  });

  it('handleSave sets error with translation key on save failure when error is not an Error instance', async () => {
    vi.mocked(getRuleset).mockResolvedValue(makeResponse());
    vi.mocked(saveRuleset).mockRejectedValue('unexpected server failure');

    const { result } = renderHook(() => useTournamentRuleset('g1', 't1', vi.fn()));
    await waitFor(() => expect(result.current.isLoading).toBe(false));

    await act(async () => {
      await result.current.handleSave();
    });

    expect(result.current.error).toBe('groups.ruleset.saveFailed');
    expect(result.current.successMessage).toBe('');
  });
});
