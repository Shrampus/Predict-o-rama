import { act, renderHook, waitFor } from '@testing-library/react';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { useLocation, useNavigate, useParams } from 'react-router-dom';

import { useAuth } from '../../../context/useAuth';
import { getGroupPreview, joinGroup } from '../../../services/groupApi';
import type { GroupPreviewResponse } from '../../../services/groupApi';
import { useInvitePage } from './useInvitePage';

vi.mock('react-router-dom', () => ({
  useParams: vi.fn(),
  useNavigate: vi.fn(),
  useLocation: vi.fn(),
}));

vi.mock('../../../context/useAuth', () => ({
  useAuth: vi.fn(),
}));

vi.mock('../../../services/groupApi', () => ({
  getGroupPreview: vi.fn(),
  joinGroup: vi.fn(),
}));

const mockNavigate = vi.fn();

function makePreview(overrides: Partial<GroupPreviewResponse> = {}): GroupPreviewResponse {
  return {
    name: 'Legends',
    adminName: 'alice',
    tournamentNames: ['Champions League'],
    ...overrides,
  };
}

describe('useInvitePage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    vi.mocked(useNavigate).mockReturnValue(mockNavigate);
    vi.mocked(useLocation).mockReturnValue({ pathname: '/invite/abc123' } as ReturnType<typeof useLocation>);
    vi.mocked(useAuth).mockReturnValue({ currentUser: null } as ReturnType<typeof useAuth>);
  });

  it('fetches preview on mount and transitions to loaded state', async () => {
    const preview = makePreview();
    vi.mocked(useParams).mockReturnValue({ inviteCode: 'abc123' });
    vi.mocked(getGroupPreview).mockResolvedValue(preview);

    const { result } = renderHook(() => useInvitePage());

    expect(result.current.pageState.status).toBe('loading');

    await waitFor(() => {
      expect(result.current.pageState.status).toBe('loaded');
    });

    expect(getGroupPreview).toHaveBeenCalledWith('abc123');
    expect(result.current.pageState).toEqual({ status: 'loaded', preview });
  });

  it('transitions to not_found on INVITE_NOT_FOUND error', async () => {
    vi.mocked(useParams).mockReturnValue({ inviteCode: 'bad-code' });
    vi.mocked(getGroupPreview).mockRejectedValue(new Error('INVITE_NOT_FOUND'));

    const { result } = renderHook(() => useInvitePage());

    await waitFor(() => {
      expect(result.current.pageState.status).toBe('not_found');
    });
  });

  it('transitions to load_error on network or server failures', async () => {
    vi.mocked(useParams).mockReturnValue({ inviteCode: 'abc123' });
    vi.mocked(getGroupPreview).mockRejectedValue(new Error('Failed to fetch group preview (500)'));

    const { result } = renderHook(() => useInvitePage());

    await waitFor(() => {
      expect(result.current.pageState.status).toBe('load_error');
    });
  });

  it('transitions to not_found immediately when inviteCode is missing', async () => {
    vi.mocked(useParams).mockReturnValue({});

    const { result } = renderHook(() => useInvitePage());

    await waitFor(() => {
      expect(result.current.pageState.status).toBe('not_found');
    });

    expect(getGroupPreview).not.toHaveBeenCalled();
  });

  it('reflects isLoggedIn based on currentUser', () => {
    vi.mocked(useParams).mockReturnValue({ inviteCode: 'abc123' });
    vi.mocked(getGroupPreview).mockResolvedValue(makePreview());
    vi.mocked(useAuth).mockReturnValue({
      currentUser: { id: 'u1', username: 'alice', email: 'alice@example.com', systemRole: 'USER' },
    } as ReturnType<typeof useAuth>);

    const { result } = renderHook(() => useInvitePage());

    expect(result.current.isLoggedIn).toBe(true);
  });

  it('handleJoin navigates to groups on success', async () => {
    vi.mocked(useParams).mockReturnValue({ inviteCode: 'abc123' });
    vi.mocked(getGroupPreview).mockResolvedValue(makePreview());
    vi.mocked(joinGroup).mockResolvedValue({} as Awaited<ReturnType<typeof joinGroup>>);

    const { result } = renderHook(() => useInvitePage());

    await act(async () => {
      await result.current.handleJoin();
    });

    expect(joinGroup).toHaveBeenCalledWith({ inviteCode: 'abc123' });
    expect(mockNavigate).toHaveBeenCalledWith('/groups');
  });

  it('handleJoin transitions to already_member when already in group', async () => {
    vi.mocked(useParams).mockReturnValue({ inviteCode: 'abc123' });
    vi.mocked(getGroupPreview).mockResolvedValue(makePreview());
    vi.mocked(joinGroup).mockRejectedValue(new Error('You are already a member of this group.'));

    const { result } = renderHook(() => useInvitePage());

    await act(async () => {
      await result.current.handleJoin();
    });

    expect(result.current.pageState.status).toBe('already_member');
    expect(mockNavigate).not.toHaveBeenCalled();
  });

  it('handleJoin sets joinError on other failures', async () => {
    vi.mocked(useParams).mockReturnValue({ inviteCode: 'abc123' });
    vi.mocked(getGroupPreview).mockResolvedValue(makePreview());
    vi.mocked(joinGroup).mockRejectedValue(new Error('server error'));

    const { result } = renderHook(() => useInvitePage());

    await act(async () => {
      await result.current.handleJoin();
    });

    expect(result.current.joinError).toBe('invite.joinError');
    expect(result.current.pageState.status).not.toBe('already_member');
    expect(mockNavigate).not.toHaveBeenCalled();
  });

  it('handleLoginToJoin navigates to login with from state', () => {
    vi.mocked(useParams).mockReturnValue({ inviteCode: 'abc123' });
    vi.mocked(getGroupPreview).mockResolvedValue(makePreview());

    const { result } = renderHook(() => useInvitePage());

    act(() => {
      result.current.handleLoginToJoin();
    });

    expect(mockNavigate).toHaveBeenCalledWith('/login', { state: { from: '/invite/abc123' } });
  });
});
