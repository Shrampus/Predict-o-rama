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

/** Create a new prediction group. */
export async function createGroup(request: CreateGroupRequest): Promise<GroupResponse> {
  const response = await fetch('/api/groups', {
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
  const response = await fetch('/api/groups/join', {
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
  const response = await fetch(`/api/groups/${groupId}/leave`, {
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
  const response = await fetch('/api/groups/my');

  if (response.status === 401) {
    throw new Error('You must be logged in to view your groups.');
  }

  if (!response.ok) {
    throw new Error(`Failed to fetch groups (${response.status})`);
  }

  return response.json();
}
