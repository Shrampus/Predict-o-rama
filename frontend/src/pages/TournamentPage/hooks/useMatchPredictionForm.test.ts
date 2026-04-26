import { act, renderHook } from '@testing-library/react';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';

import type { TournamentMatchPrediction } from '../../../services/predictionsApi';
import { useMatchPredictionForm } from './useMatchPredictionForm';

function makeMatch(overrides: Partial<TournamentMatchPrediction> = {}): TournamentMatchPrediction {
  return {
    matchId: 'match-1',
    externalMatchId: 'ext-1',
    homeTeamName: 'Home FC',
    awayTeamName: 'Away FC',
    homeTeamImage: 'home.png',
    awayTeamImage: 'away.png',
    kickoffTime: '2026-04-24T12:00:00.000Z',
    matchStatus: 'SCHEDULED',
    roundIdentifier: null,
    groupIdentifier: 'group-1',
    matchdayIdentifier: 1,
    predictionId: null,
    predictedHomeScore: null,
    predictedAwayScore: null,
    predictedWinner: null,
    ...overrides,
  };
}

describe('useMatchPredictionForm', () => {
  beforeEach(() => {
    vi.useFakeTimers();
    vi.setSystemTime(new Date('2026-04-24T11:00:00.000Z'));
  });

  afterEach(() => {
    vi.useRealTimers();
  });

  it('is editable before kickoff and submits parsed prediction', async () => {
    const onPredict = vi.fn().mockResolvedValue({ ok: true });
    const match = makeMatch({ kickoffTime: '2026-04-24T12:00:00.000Z', matchStatus: 'SCHEDULED' });
    const { result } = renderHook(() =>
      useMatchPredictionForm({
        match,
        isSaving: false,
        onPredict,
        defaultSaveErrorMessage: 'failed',
      }),
    );

    expect(result.current.isLocked).toBe(false);

    act(() => {
      result.current.setHomeScore('2');
      result.current.setAwayScore('1');
      result.current.selectWinner('Home');
    });

    expect(result.current.canSubmit).toBe(true);

    await act(async () => {
      await result.current.submit();
    });

    expect(onPredict).toHaveBeenCalledWith('match-1', 2, 1, 'Home');
  });

  it('does not call onPredict when isSaving is true', async () => {
    const onPredict = vi.fn().mockResolvedValue({ ok: true });
    const match = makeMatch({ kickoffTime: '2026-04-24T12:00:00.000Z', matchStatus: 'SCHEDULED' });
    const { result, rerender } = renderHook(
      (props: { isSaving: boolean }) =>
        useMatchPredictionForm({
          match,
          isSaving: props.isSaving,
          onPredict,
          defaultSaveErrorMessage: 'failed',
        }),
      { initialProps: { isSaving: false } },
    );

    act(() => {
      result.current.setHomeScore('2');
      result.current.setAwayScore('1');
      result.current.selectWinner('Home');
    });

    rerender({ isSaving: true });
    expect(result.current.canSubmit).toBe(false);

    await act(async () => {
      await result.current.submit();
    });

    expect(onPredict).not.toHaveBeenCalled();
  });

  it('is locked after kickoff even when status is scheduled', async () => {
    const onPredict = vi.fn().mockResolvedValue({ ok: true });
    const match = makeMatch({ kickoffTime: '2026-04-24T10:00:00.000Z', matchStatus: 'SCHEDULED' });
    const { result } = renderHook(() =>
      useMatchPredictionForm({
        match,
        isSaving: false,
        onPredict,
        defaultSaveErrorMessage: 'failed',
      }),
    );

    act(() => {
      result.current.setHomeScore('2');
      result.current.setAwayScore('1');
      result.current.selectWinner('Home');
    });

    expect(result.current.isLocked).toBe(true);
    expect(result.current.canSubmit).toBe(false);

    await act(async () => {
      await result.current.submit();
    });

    expect(onPredict).not.toHaveBeenCalled();
  });

  it('is locked when match status is LIVE', async () => {
    const onPredict = vi.fn().mockResolvedValue({ ok: true });
    const match = makeMatch({ matchStatus: 'LIVE', kickoffTime: '2026-04-24T12:00:00.000Z' });
    const { result } = renderHook(() =>
      useMatchPredictionForm({
        match,
        isSaving: false,
        onPredict,
        defaultSaveErrorMessage: 'failed',
      }),
    );

    expect(result.current.isLocked).toBe(true);
    expect(result.current.canSubmit).toBe(false);

    await act(async () => {
      await result.current.submit();
    });

    expect(onPredict).not.toHaveBeenCalled();
  });

  it('shows invalid winner error for non-draw score mismatch', async () => {
    const match = makeMatch();
    const { result } = renderHook(() =>
      useMatchPredictionForm({
        match,
        isSaving: false,
        defaultSaveErrorMessage: 'failed',
      }),
    );

    act(() => {
      result.current.setHomeScore('2');
      result.current.setAwayScore('1');
      result.current.selectWinner('Away');
    });

    expect(result.current.validationErrorCode).toBe('invalid_winner_for_score');

    await act(async () => {
      await result.current.submit();
    });

    expect(result.current.validationErrorCode).toBe('invalid_winner_for_score');
  });

  it('reconciles winner to Home when scores move to home lead', () => {
    const match = makeMatch();
    const { result } = renderHook(() =>
      useMatchPredictionForm({
        match,
        isSaving: false,
        defaultSaveErrorMessage: 'failed',
      }),
    );

    act(() => {
      result.current.setAwayScore('1');
      result.current.setHomeScore('2');
    });

    expect(result.current.winningTeam).toBe('Home');
  });
});
