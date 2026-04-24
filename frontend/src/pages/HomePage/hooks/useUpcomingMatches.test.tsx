import { renderHook, waitFor } from '@testing-library/react';
import { beforeEach, describe, expect, it, vi } from 'vitest';

import type { UpcomingMatch } from '../types';
import { matchesApi } from '../../../services/matchesApi';
import { useAuth } from '../../../context/useAuth';
import { useUpcomingMatches } from './useUpcomingMatches';

vi.mock('../../../services/matchesApi', () => ({
  matchesApi: {
    getUpcomingMatches: vi.fn(),
    getMyUpcomingMatches: vi.fn(),
  },
}));

vi.mock('../../../context/useAuth', () => ({
  useAuth: vi.fn(),
}));

function makeUpcomingMatch(overrides: Partial<UpcomingMatch> = {}): UpcomingMatch {
  return {
    matchId: 'match-1',
    homeTeamName: 'Home FC',
    awayTeamName: 'Away FC',
    homeTeamImage: 'home.png',
    awayTeamImage: 'away.png',
    kickoffTime: '2026-04-24T12:00:00.000Z',
    tournamentName: 'Champions Cup',
    groups: [],
    ...overrides,
  };
}

describe('useUpcomingMatches', () => {
  const getUpcomingMatchesMock = vi.mocked(matchesApi.getUpcomingMatches);
  const getMyUpcomingMatchesMock = vi.mocked(matchesApi.getMyUpcomingMatches);
  const useAuthMock = vi.mocked(useAuth);

  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('loads generic upcoming matches when unauthenticated', async () => {
    const matches = [makeUpcomingMatch()];
    getUpcomingMatchesMock.mockResolvedValue(matches);
    useAuthMock.mockReturnValue({ currentUser: null, loading: false });

    const { result } = renderHook(() => useUpcomingMatches());

    expect(result.current.isLoading).toBe(true);
    expect(result.current.hasError).toBe(false);

    await waitFor(() => {
      expect(result.current.isLoading).toBe(false);
    });

    expect(result.current.upcomingMatches).toEqual(matches);
    expect(getUpcomingMatchesMock).toHaveBeenCalledTimes(1);
    expect(getMyUpcomingMatchesMock).not.toHaveBeenCalled();
  });

  it('loads my upcoming matches when authenticated', async () => {
    const matches = [makeUpcomingMatch({ matchId: 'my-match-1' })];
    getMyUpcomingMatchesMock.mockResolvedValue(matches);
    useAuthMock.mockReturnValue({
      currentUser: {
        id: 'user-1',
        username: 'alice',
        email: 'alice@example.com',
        systemRole: 'USER',
      },
      loading: false,
    });

    const { result } = renderHook(() => useUpcomingMatches());

    await waitFor(() => {
      expect(result.current.isLoading).toBe(false);
    });

    expect(result.current.upcomingMatches).toEqual(matches);
    expect(getMyUpcomingMatchesMock).toHaveBeenCalledTimes(1);
    expect(getUpcomingMatchesMock).not.toHaveBeenCalled();
  });

  it('sets error state when request fails', async () => {
    getUpcomingMatchesMock.mockRejectedValue(new Error('boom'));
    useAuthMock.mockReturnValue({ currentUser: null, loading: false });

    const { result } = renderHook(() => useUpcomingMatches());

    await waitFor(() => {
      expect(result.current.isLoading).toBe(false);
    });

    expect(result.current.hasError).toBe(true);
    expect(result.current.upcomingMatches).toEqual([]);
  });

  it('does not call APIs while auth state is loading', () => {
    useAuthMock.mockReturnValue({ currentUser: null, loading: true });

    const { result } = renderHook(() => useUpcomingMatches());

    expect(result.current.isLoading).toBe(true);
    expect(result.current.hasError).toBe(false);
    expect(result.current.upcomingMatches).toEqual([]);
    expect(getUpcomingMatchesMock).not.toHaveBeenCalled();
    expect(getMyUpcomingMatchesMock).not.toHaveBeenCalled();
  });
});
