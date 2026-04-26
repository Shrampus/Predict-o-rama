import { beforeEach, describe, expect, it, vi } from 'vitest';

import { getRuleset, saveRuleset } from './rulesetApi';

vi.mock('../lib/apiClient', () => ({
  apiFetch: vi.fn(),
}));

vi.mock('./apiConfig', () => ({
  API_PREFIX: '/api/v1',
  getApiUrl: (path: string) => `http://localhost${path}`,
}));

import { apiFetch } from '../lib/apiClient';

const mockRulesetResponse = {
  id: 'ruleset-1',
  activeRules: { CORRECT_WINNER: 1, EXACT_SCORE: 3 },
  disabledRules: { CORRECT_GOAL_DIFFERENCE: 2 },
};

function mockFetch(status: number, body?: unknown) {
  vi.mocked(apiFetch).mockResolvedValue({
    status,
    ok: status >= 200 && status < 300,
    json: () => Promise.resolve(body),
  } as Response);
}

describe('getRuleset', () => {
  beforeEach(() => vi.clearAllMocks());

  it('returns parsed ruleset on 200', async () => {
    mockFetch(200, mockRulesetResponse);

    const result = await getRuleset('g1', 't1');

    expect(result).toEqual(mockRulesetResponse);
    expect(apiFetch).toHaveBeenCalledWith(
      'http://localhost/api/v1/groups/g1/tournaments/t1/ruleset',
    );
  });

  it('throws on 401', async () => {
    mockFetch(401);

    await expect(getRuleset('g1', 't1')).rejects.toThrow('You must be logged in to view the ruleset.');
  });

  it('throws on 403', async () => {
    mockFetch(403);

    await expect(getRuleset('g1', 't1')).rejects.toThrow('You are not a member of this group.');
  });

  it('throws with status code on other errors', async () => {
    mockFetch(500);

    await expect(getRuleset('g1', 't1')).rejects.toThrow('Failed to load ruleset (500)');
  });
});

describe('saveRuleset', () => {
  beforeEach(() => vi.clearAllMocks());

  it('returns updated ruleset on 200', async () => {
    mockFetch(200, mockRulesetResponse);

    const result = await saveRuleset('g1', 't1', { rulePoints: { CORRECT_WINNER: 2 } });

    expect(result).toEqual(mockRulesetResponse);
    expect(apiFetch).toHaveBeenCalledWith(
      'http://localhost/api/v1/groups/g1/tournaments/t1/ruleset',
      expect.objectContaining({
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ rulePoints: { CORRECT_WINNER: 2 } }),
      }),
    );
  });

  it('throws on 401', async () => {
    mockFetch(401);

    await expect(saveRuleset('g1', 't1', { rulePoints: {} })).rejects.toThrow(
      'You must be logged in to update the ruleset.',
    );
  });

  it('throws on 403', async () => {
    mockFetch(403);

    await expect(saveRuleset('g1', 't1', { rulePoints: {} })).rejects.toThrow(
      'Only group admins can update the ruleset.',
    );
  });

  it('throws with status code on other errors', async () => {
    mockFetch(500);

    await expect(saveRuleset('g1', 't1', { rulePoints: {} })).rejects.toThrow(
      'Failed to save ruleset (500)',
    );
  });
});
