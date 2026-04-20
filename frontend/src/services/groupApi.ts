import { apiFetch } from '../lib/apiClient';
import { getApiUrl } from './apiConfig';

/** Request/response types matching the backend DTOs. */

export interface CreateGroupRequest {
  name: string;
  description: string;
}

export interface GroupResponse {
  id: string;
  ownerId: string;
  inviteCode: string;
  name: string;
  description: string;
}

export interface JoinGroupRequest {
  inviteCode: string;
}

export interface GroupMemberResponse {
  id: string;
  groupId: string;
  userId: string;
  name: string;
  memberRole: 'ADMIN' | 'USER';
  status: 'ACTIVE' | 'INACTIVE';
}

export interface MyGroupsResponse {
  groupId: string;
  inviteCode: string;
  name: string;
  description: string;
  groupMemberRole: 'ADMIN' | 'USER';
}

export interface GroupDetailsResponse {
  id: string;
  name: string;
  description: string;
  currentUserRole: 'ADMIN' | 'USER';
}

export interface AddGroupMemberRequest {
  email: string;
}

export interface GroupTournamentResponse {
  id: string;
  competitionCode: string | null;
  name: string;
  description: string;
  sport: string;
}

export interface AddGroupTournamentRequest {
  tournamentId: string;
}

export interface TournamentOption {
  id: string;
  competitionCode: string | null;
  name: string;
  description: string;
  sport: string;
}

type MyGroupsApiResponse = {
  groupId?: string;
  id?: string;
  inviteCode: string;
  name: string;
  description: string;
  groupMemberRole: 'ADMIN' | 'USER';
};

/** Create a new prediction group. */
export async function createGroup(request: CreateGroupRequest): Promise<GroupResponse> {
  const response = await apiFetch(getApiUrl('/api/groups'), {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(request),
  });

  if (response.status === 401) {
    throw new Error('You must be logged in to create a group.');
  }

  if (!response.ok) {
    throw new Error(`Failed to create group (${response.status})`);
  }

  return response.json();
}

/** Join an existing group via invite code. */
export async function joinGroup(request: JoinGroupRequest): Promise<GroupMemberResponse> {
  const response = await apiFetch(getApiUrl('/api/groups/join'), {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(request),
  });

  if (response.status === 401) {
    throw new Error('You must be logged in to join a group.');
  }

  if (response.status === 404) {
    throw new Error('Invite code not found. Please check and try again.');
  }

  if (response.status === 409) {
    throw new Error('You are already a member of this group.');
  }

  if (!response.ok) {
    throw new Error(`Failed to join group (${response.status})`);
  }

  return response.json();
}

/** Leave a group. */
export async function leaveGroup(groupId: string): Promise<void> {
  const response = await apiFetch(getApiUrl(`/api/groups/${groupId}/leave`), {
    method: 'DELETE',
  });

  if (response.status === 401) {
    throw new Error('You must be logged in to leave a group.');
  }

  if (!response.ok) {
    throw new Error(`Failed to leave group (${response.status})`);
  }
}

/** Fetch all groups the current user is a member of. */
export async function getMyGroups(): Promise<MyGroupsResponse[]> {
  const response = await apiFetch(getApiUrl('/api/groups/my'));

  if (response.status === 401) {
    throw new Error('You must be logged in to view your groups.');
  }

  if (!response.ok) {
    throw new Error(`Failed to fetch groups (${response.status})`);
  }

  const data = (await response.json()) as MyGroupsApiResponse[];
  return data
    .map((group) => ({
      groupId: group.groupId ?? group.id ?? '',
      inviteCode: group.inviteCode,
      name: group.name,
      description: group.description,
      groupMemberRole: group.groupMemberRole,
    }))
    .filter((group) => group.groupId.length > 0);
}

/** Fetch details for one group. */
export async function getGroupDetails(groupId: string): Promise<GroupDetailsResponse> {
  const response = await apiFetch(getApiUrl(`/api/groups/${groupId}`));

  if (response.status === 401) {
    throw new Error('You must be logged in to view group details.');
  }

  if (response.status === 403) {
    throw new Error('You are not a member of this group.');
  }

  if (response.status === 404) {
    throw new Error('Group not found.');
  }

  if (!response.ok) {
    throw new Error(`Failed to fetch group details (${response.status})`);
  }

  return response.json();
}

/** Fetch all members in a group. */
export async function getGroupMembers(groupId: string): Promise<GroupMemberResponse[]> {
  const response = await apiFetch(getApiUrl(`/api/groups/${groupId}/members`));

  if (response.status === 401) {
    throw new Error('You must be logged in to view group members.');
  }

  if (response.status === 403) {
    throw new Error('You are not allowed to view this group.');
  }

  if (response.status === 404) {
    throw new Error('Group not found.');
  }

  if (!response.ok) {
    throw new Error(`Failed to fetch group members (${response.status})`);
  }

  return response.json();
}

/** Add a member to a group by email. Admin only. */
export async function addGroupMember(
  groupId: string,
  request: AddGroupMemberRequest,
): Promise<GroupMemberResponse> {
  const response = await apiFetch(getApiUrl(`/api/groups/${groupId}/members`), {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(request),
  });

  if (response.status === 401) {
    throw new Error('You must be logged in to add members.');
  }

  if (response.status === 403) {
    throw new Error('Only group admins can add members.');
  }

  if (response.status === 404) {
    throw new Error('User or group not found.');
  }

  if (response.status === 409) {
    throw new Error('That user is already a member of this group.');
  }

  if (!response.ok) {
    throw new Error(`Failed to add member (${response.status})`);
  }

  return response.json();
}

/** Remove a member from a group. Admin only. */
export async function removeGroupMember(groupId: string, memberUserId: string): Promise<void> {
  const response = await apiFetch(getApiUrl(`/api/groups/${groupId}/members/${memberUserId}`), {
    method: 'DELETE',
  });

  if (response.status === 401) {
    throw new Error('You must be logged in to remove members.');
  }

  if (response.status === 403) {
    throw new Error('Only group admins can remove members.');
  }

  if (response.status === 404) {
    throw new Error('Group or member not found in this group.');
  }

  if (response.status === 400) {
    throw new Error(
      'That member cannot be removed (for example, removing yourself or another admin is not allowed).',
    );
  }

  if (!response.ok) {
    throw new Error(`Failed to remove member (${response.status})`);
  }
}

/** Fetch tournaments linked to a group. */
export async function getGroupTournaments(groupId: string): Promise<GroupTournamentResponse[]> {
  const response = await apiFetch(getApiUrl(`/api/groups/${groupId}/tournaments`));

  if (response.status === 401) {
    throw new Error('You must be logged in to view group tournaments.');
  }

  if (response.status === 403) {
    throw new Error('You are not allowed to view this group.');
  }

  if (response.status === 404) {
    throw new Error('Group not found.');
  }

  if (!response.ok) {
    throw new Error(`Failed to fetch group tournaments (${response.status})`);
  }

  return response.json();
}

/** Link a tournament to a group. Admin only. */
export async function addGroupTournament(
  groupId: string,
  request: AddGroupTournamentRequest,
): Promise<void> {
  const response = await apiFetch(getApiUrl(`/api/groups/${groupId}/tournaments`), {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(request),
  });

  if (response.status === 401) {
    throw new Error('You must be logged in to add tournaments.');
  }

  if (response.status === 403) {
    throw new Error('Only group admins can add tournaments.');
  }

  if (response.status === 404) {
    throw new Error('Group or tournament not found.');
  }

  if (response.status === 409) {
    throw new Error('That tournament is already linked to this group.');
  }

  if (!response.ok) {
    throw new Error(`Failed to add tournament (${response.status})`);
  }
}

/** Remove a tournament link from a group. Admin only. */
export async function removeGroupTournament(groupId: string, tournamentId: string): Promise<void> {
  const response = await apiFetch(
    getApiUrl(`/api/groups/${groupId}/tournaments/${tournamentId}`),
    {
      method: 'DELETE',
    },
  );

  if (response.status === 401) {
    throw new Error('You must be logged in to remove tournaments.');
  }

  if (response.status === 403) {
    throw new Error('Only group admins can remove tournaments.');
  }

  if (response.status === 404) {
    throw new Error('Group or tournament link not found.');
  }

  if (!response.ok) {
    throw new Error(`Failed to remove tournament (${response.status})`);
  }
}

/** Fetch available tournaments for selection. */
export async function getTournaments(): Promise<TournamentOption[]> {
  const response = await apiFetch(getApiUrl('/api/tournaments'));

  if (response.status === 401) {
    throw new Error('You must be logged in to view tournaments.');
  }

  if (!response.ok) {
    throw new Error(`Failed to fetch tournaments (${response.status})`);
  }

  return response.json();
}
