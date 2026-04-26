import { apiFetch } from '../lib/apiClient';
import { API_PREFIX, getApiUrl } from './apiConfig';

export interface RulesetResponse {
  id: string;
  activeRules: Record<string, number>;
  disabledRules: Record<string, number>;
}

export interface RulesetUpdateRequest {
  rulePoints: Record<string, number>;
}

export async function getRuleset(groupId: string, tournamentId: string): Promise<RulesetResponse> {
  const response = await apiFetch(
    getApiUrl(`${API_PREFIX}/groups/${groupId}/tournaments/${tournamentId}/ruleset`),
  );

  if (response.status === 401) throw new Error('You must be logged in to view the ruleset.');
  if (response.status === 403) throw new Error('You are not a member of this group.');
  if (!response.ok) throw new Error(`Failed to load ruleset (${response.status})`);

  return response.json();
}

export async function saveRuleset(
  groupId: string,
  tournamentId: string,
  request: RulesetUpdateRequest,
): Promise<RulesetResponse> {
  const response = await apiFetch(
    getApiUrl(`${API_PREFIX}/groups/${groupId}/tournaments/${tournamentId}/ruleset`),
    {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(request),
    },
  );

  if (response.status === 401) throw new Error('You must be logged in to update the ruleset.');
  if (response.status === 403) throw new Error('Only group admins can update the ruleset.');
  if (!response.ok) throw new Error(`Failed to save ruleset (${response.status})`);

  return response.json();
}
