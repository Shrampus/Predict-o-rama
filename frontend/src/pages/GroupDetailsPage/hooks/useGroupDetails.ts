import { useEffect, useState } from 'react';
import type { FormEvent } from 'react';
import { useTranslation } from 'react-i18next';

import {
  addGroupMember,
  addGroupTournament,
  getGroupDetails,
  getGroupLeaderboards,
  getGroupMembers,
  getGroupTournaments,
  getTournaments,
  removeGroupMember,
  removeGroupTournament,
  type GroupDetailsResponse,
  type GroupLeaderboardResponse,
  type GroupMemberResponse,
  type GroupTournamentResponse,
  type TournamentOption,
} from '../../../services/groupApi';

type UseGroupDetailsReturn = {
  group: GroupDetailsResponse | null;
  groupError: string;
  isLoadingGroup: boolean;
  members: GroupMemberResponse[];
  membersError: string;
  isLoadingMembers: boolean;
  tournaments: GroupTournamentResponse[];
  tournamentsError: string;
  isLoadingTournaments: boolean;
  leaderboards: GroupLeaderboardResponse[];
  leaderboardsError: string;
  isLoadingLeaderboards: boolean;
  memberEmail: string;
  handleMemberEmailChange: (value: string) => void;
  memberActionMessage: string;
  memberActionError: string;
  resetMemberFeedback: () => void;
  isAddingMember: boolean;
  removingMemberUserId: string | null;
  availableTournaments: TournamentOption[];
  selectedTournamentId: string;
  handleTournamentSelectionChange: (value: string) => void;
  availableTournamentsError: string;
  isLoadingAvailableTournaments: boolean;
  tournamentActionMessage: string;
  tournamentActionError: string;
  resetTournamentFeedback: () => void;
  isAddingTournament: boolean;
  removingTournamentId: string | null;
  isAdmin: boolean;
  handleAddMember: (event: FormEvent<HTMLFormElement>) => Promise<void>;
  handleAddTournament: (event: FormEvent<HTMLFormElement>) => Promise<void>;
  handleRemoveMember: (member: GroupMemberResponse) => Promise<void>;
  handleRemoveTournament: (tournament: GroupTournamentResponse) => Promise<void>;
};

export function useGroupDetails(groupId: string | undefined): UseGroupDetailsReturn {
  const { t } = useTranslation();

  const [group, setGroup] = useState<GroupDetailsResponse | null>(null);
  const [groupError, setGroupError] = useState('');
  const [isLoadingGroup, setIsLoadingGroup] = useState(false);

  const [members, setMembers] = useState<GroupMemberResponse[]>([]);
  const [membersError, setMembersError] = useState('');
  const [isLoadingMembers, setIsLoadingMembers] = useState(false);

  const [tournaments, setTournaments] = useState<GroupTournamentResponse[]>([]);
  const [tournamentsError, setTournamentsError] = useState('');
  const [isLoadingTournaments, setIsLoadingTournaments] = useState(false);

  const [leaderboards, setLeaderboards] = useState<GroupLeaderboardResponse[]>([]);
  const [leaderboardsError, setLeaderboardsError] = useState('');
  const [isLoadingLeaderboards, setIsLoadingLeaderboards] = useState(false);

  const [memberEmail, setMemberEmail] = useState('');
  const [memberActionMessage, setMemberActionMessage] = useState('');
  const [memberActionError, setMemberActionError] = useState('');
  const [isAddingMember, setIsAddingMember] = useState(false);
  const [removingMemberUserId, setRemovingMemberUserId] = useState<string | null>(null);

  const [availableTournaments, setAvailableTournaments] = useState<TournamentOption[]>([]);
  const [selectedTournamentId, setSelectedTournamentId] = useState('');
  const [availableTournamentsError, setAvailableTournamentsError] = useState('');
  const [isLoadingAvailableTournaments, setIsLoadingAvailableTournaments] = useState(false);
  const [tournamentActionMessage, setTournamentActionMessage] = useState('');
  const [tournamentActionError, setTournamentActionError] = useState('');
  const [isAddingTournament, setIsAddingTournament] = useState(false);
  const [removingTournamentId, setRemovingTournamentId] = useState<string | null>(null);

  const isAdmin = group?.currentUserRole === 'ADMIN';

  function resetMemberFeedback() {
    setMemberActionMessage('');
    setMemberActionError('');
  }

  function handleMemberEmailChange(value: string) {
    setMemberEmail(value);
    resetMemberFeedback();
  }

  function resetTournamentFeedback() {
    setTournamentActionMessage('');
    setTournamentActionError('');
  }

  function handleTournamentSelectionChange(value: string) {
    setSelectedTournamentId(value);
    resetTournamentFeedback();
  }

  useEffect(() => {
    setGroup(null);
    setGroupError('');
    setMembers([]);
    setMembersError('');
    setTournaments([]);
    setTournamentsError('');
    setLeaderboards([]);
    setLeaderboardsError('');
    setAvailableTournaments([]);
    setAvailableTournamentsError('');
    setSelectedTournamentId('');
    setMemberActionMessage('');
    setMemberActionError('');
    setTournamentActionMessage('');
    setTournamentActionError('');
    setRemovingMemberUserId(null);
    setRemovingTournamentId(null);

    if (!groupId) {
      setIsLoadingGroup(false);
      setIsLoadingMembers(false);
      setIsLoadingTournaments(false);
      setIsLoadingLeaderboards(false);
      setIsLoadingAvailableTournaments(false);
      return;
    }

    let isMounted = true;

    async function loadGroupDetails() {
      setIsLoadingGroup(true);
      setGroupError('');
      try {
        // @ts-expect-error groupId is guarded before this async function runs
        const details = await getGroupDetails(groupId);
        if (isMounted) {
          setGroup(details);
        }
      } catch (err) {
        if (isMounted) {
          setGroupError(
            err instanceof Error ? err.message : t('groups.groupErrors.failedToLoadGroupDetails'),
          );
        }
      } finally {
        if (isMounted) {
          setIsLoadingGroup(false);
        }
      }
    }

    async function loadMembers() {
      setIsLoadingMembers(true);
      setMembersError('');
      try {
        // @ts-expect-error groupId is guarded before this async function runs
        const data = await getGroupMembers(groupId);
        if (isMounted) {
          setMembers(data);
        }
      } catch (err) {
        if (isMounted) {
          setMembersError(
            err instanceof Error ? err.message : t('groups.groupErrors.failedToLoadMembers'),
          );
        }
      } finally {
        if (isMounted) {
          setIsLoadingMembers(false);
        }
      }
    }

    async function loadTournaments() {
      setIsLoadingTournaments(true);
      setTournamentsError('');
      try {
        // @ts-expect-error groupId is guarded before this async function runs
        const data = await getGroupTournaments(groupId);
        if (isMounted) {
          setTournaments(data);
        }
      } catch (err) {
        if (isMounted) {
          setTournamentsError(
            err instanceof Error ? err.message : t('groups.groupErrors.failedToLoadTournaments'),
          );
        }
      } finally {
        if (isMounted) {
          setIsLoadingTournaments(false);
        }
      }
    }

    async function loadLeaderboards() {
      setIsLoadingLeaderboards(true);
      setLeaderboardsError('');
      try {
        // @ts-expect-error groupId is guarded before this async function runs
        const data = await getGroupLeaderboards(groupId);
        if (isMounted) {
          setLeaderboards(data);
        }
      } catch (err) {
        if (isMounted) {
          setLeaderboardsError(
            err instanceof Error ? err.message : t('groups.groupErrors.failedToLoadLeaderboards'),
          );
        }
      } finally {
        if (isMounted) {
          setIsLoadingLeaderboards(false);
        }
      }
    }

    void loadGroupDetails();
    void loadMembers();
    void loadTournaments();
    void loadLeaderboards();

    return () => {
      isMounted = false;
    };
  }, [groupId, t]);

  useEffect(() => {
    if (!groupId || !isAdmin) {
      setAvailableTournaments([]);
      setAvailableTournamentsError('');
      setSelectedTournamentId('');
      setIsLoadingAvailableTournaments(false);
      return;
    }

    let isMounted = true;

    async function loadAvailableTournaments() {
      setIsLoadingAvailableTournaments(true);
      setAvailableTournamentsError('');
      try {
        const options = await getTournaments();
        if (isMounted) {
          setAvailableTournaments(options);
        }
      } catch (err) {
        if (isMounted) {
          setAvailableTournamentsError(
            err instanceof Error
              ? err.message
              : t('groups.groupErrors.failedToLoadTournamentOptions'),
          );
        }
      } finally {
        if (isMounted) {
          setIsLoadingAvailableTournaments(false);
        }
      }
    }

    void loadAvailableTournaments();

    return () => {
      isMounted = false;
    };
  }, [groupId, isAdmin, t]);

  async function handleAddMember(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!groupId || group?.id !== groupId || memberEmail.trim().length === 0) {
      return;
    }

    setIsAddingMember(true);
    setMemberActionMessage('');
    setMemberActionError('');

    try {
      const addedMember = await addGroupMember(groupId, { email: memberEmail.trim() });
      const latestLeaderboards = await getGroupLeaderboards(groupId);
      setMembers((currentMembers) =>
        currentMembers.some((member) => member.id === addedMember.id)
          ? currentMembers
          : [...currentMembers, addedMember],
      );
      setLeaderboards(latestLeaderboards);
      setMemberEmail('');
      setMemberActionMessage(t('groups.memberAction.memberAdded'));
    } catch (err) {
      setMemberActionError(err instanceof Error ? err.message : t('groups.memberAction.addFailed'));
    } finally {
      setIsAddingMember(false);
    }
  }

  async function handleAddTournament(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!groupId || group?.id !== groupId || selectedTournamentId.length === 0) {
      return;
    }

    setIsAddingTournament(true);
    setTournamentActionMessage('');
    setTournamentActionError('');

    try {
      await addGroupTournament(groupId, { tournamentId: selectedTournamentId });
      const latestTournaments = await getGroupTournaments(groupId);
      const latestLeaderboards = await getGroupLeaderboards(groupId);
      setTournaments(latestTournaments);
      setLeaderboards(latestLeaderboards);
      setSelectedTournamentId('');
      setTournamentActionMessage(t('groups.tournamentAction.tournamentLinked'));
    } catch (err) {
      setTournamentActionError(
        err instanceof Error ? err.message : t('groups.tournamentAction.addFailed'),
      );
    } finally {
      setIsAddingTournament(false);
    }
  }

  async function handleRemoveMember(member: GroupMemberResponse) {
    if (!groupId || group?.id !== groupId) {
      return;
    }

    const hasConfirmed = window.confirm(
      t('groups.memberAction.removeConfirmation', { name: member.name }),
    );
    if (!hasConfirmed) {
      return;
    }

    setRemovingMemberUserId(member.userId);
    setMemberActionMessage('');
    setMemberActionError('');

    try {
      await removeGroupMember(groupId, member.userId);
      setMembers((currentMembers) =>
        currentMembers.filter((currentMember) => currentMember.userId !== member.userId),
      );
      setLeaderboards((currentLeaderboards) =>
        currentLeaderboards.map((leaderboard) => ({
          ...leaderboard,
          entries: leaderboard.entries.filter((entry) => entry.userId !== member.userId),
        })),
      );
      setMemberActionMessage(t('groups.memberAction.memberRemoved'));
    } catch (err) {
      setMemberActionError(
        err instanceof Error ? err.message : t('groups.memberAction.removeFailed'),
      );
    } finally {
      setRemovingMemberUserId(null);
    }
  }

  async function handleRemoveTournament(tournament: GroupTournamentResponse) {
    if (!groupId || group?.id !== groupId) {
      return;
    }

    const hasConfirmed = window.confirm(
      t('groups.tournamentAction.removeConfirmation', { name: tournament.name }),
    );
    if (!hasConfirmed) {
      return;
    }

    setRemovingTournamentId(tournament.id);
    setTournamentActionMessage('');
    setTournamentActionError('');

    try {
      await removeGroupTournament(groupId, tournament.id);
      setTournaments((currentTournaments) =>
        currentTournaments.filter((currentTournament) => currentTournament.id !== tournament.id),
      );
      setLeaderboards((currentLeaderboards) =>
        currentLeaderboards.filter((leaderboard) => leaderboard.tournamentId !== tournament.id),
      );
      setTournamentActionMessage(t('groups.tournamentAction.tournamentRemoved'));
    } catch (err) {
      setTournamentActionError(
        err instanceof Error ? err.message : t('groups.tournamentAction.removeFailed'),
      );
    } finally {
      setRemovingTournamentId(null);
    }
  }

  return {
    group,
    groupError,
    isLoadingGroup,
    members,
    membersError,
    isLoadingMembers,
    tournaments,
    tournamentsError,
    isLoadingTournaments,
    leaderboards,
    leaderboardsError,
    isLoadingLeaderboards,
    memberEmail,
    handleMemberEmailChange,
    memberActionMessage,
    memberActionError,
    resetMemberFeedback,
    isAddingMember,
    removingMemberUserId,
    availableTournaments,
    selectedTournamentId,
    handleTournamentSelectionChange,
    availableTournamentsError,
    isLoadingAvailableTournaments,
    tournamentActionMessage,
    tournamentActionError,
    resetTournamentFeedback,
    isAddingTournament,
    removingTournamentId,
    isAdmin,
    handleAddMember,
    handleAddTournament,
    handleRemoveMember,
    handleRemoveTournament,
  };
}
