import { useLocation, useParams } from 'react-router-dom';

import type { MyGroupsResponse } from '../../services/groupApi';
import { GroupDetailsHeader } from './components/GroupDetailsHeader';
import { GroupMembersSection } from './components/GroupMembersSection';
import { GroupTournamentsSection } from './components/GroupTournamentsSection';
import { useGroupDetails } from './hooks/useGroupDetails';

type GroupDetailsLocationState = {
  group?: MyGroupsResponse;
};

function isGroupDetailsLocationState(value: unknown): value is GroupDetailsLocationState {
  return typeof value === 'object' && value !== null && 'group' in value;
}

function GroupDetailsPageContent({ groupId }: { groupId: string }) {
  const location = useLocation();
  const groupState = isGroupDetailsLocationState(location.state) ? location.state : null;
  const initialGroup = groupState?.group ?? null;
  const {
    group,
    groupError,
    isLoadingGroup,
    members,
    membersError,
    isLoadingMembers,
    tournaments,
    tournamentsError,
    isLoadingTournaments,
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
  } = useGroupDetails(groupId);

  const linkedTournamentIds = new Set(tournaments.map((tournament) => tournament.id));
  const selectableTournaments = availableTournaments.filter(
    (tournament) => !linkedTournamentIds.has(tournament.id),
  );
  const groupName = group?.name ?? initialGroup?.name ?? 'Group';
  const groupDescription = group?.description ?? initialGroup?.description ?? 'No description yet.';

  return (
    <div className="max-w-5xl mx-auto px-4 sm:px-6 py-8 space-y-6">
      <GroupDetailsHeader
        name={groupName}
        description={groupDescription}
        isLoadingGroup={isLoadingGroup}
        groupError={groupError}
      />

      <GroupTournamentsSection
        groupId={groupId}
        tournaments={tournaments}
        isLoadingTournaments={isLoadingTournaments}
        tournamentsError={tournamentsError}
        isAdmin={isAdmin}
        removingTournamentId={removingTournamentId}
        selectableTournaments={selectableTournaments}
        selectedTournamentId={selectedTournamentId}
        isLoadingAvailableTournaments={isLoadingAvailableTournaments}
        availableTournamentsError={availableTournamentsError}
        isAddingTournament={isAddingTournament}
        tournamentActionMessage={tournamentActionMessage}
        tournamentActionError={tournamentActionError}
        onAddTournament={(event) => void handleAddTournament(event)}
        onSelectTournament={handleTournamentSelectionChange}
        onResetTournamentFeedback={resetTournamentFeedback}
        onRemoveTournament={(tournament) => void handleRemoveTournament(tournament)}
      />

      <GroupMembersSection
        members={members}
        isLoadingMembers={isLoadingMembers}
        membersError={membersError}
        isAdmin={isAdmin}
        removingMemberUserId={removingMemberUserId}
        memberEmail={memberEmail}
        isAddingMember={isAddingMember}
        memberActionMessage={memberActionMessage}
        memberActionError={memberActionError}
        onAddMember={(event) => void handleAddMember(event)}
        onMemberEmailChange={handleMemberEmailChange}
        onResetMemberFeedback={resetMemberFeedback}
        onRemoveMember={(member) => void handleRemoveMember(member)}
      />
    </div>
  );
}

function GroupDetailsPageRoute() {
  const { groupId } = useParams<{ groupId: string }>();

  if (!groupId) {
    return (
      <div className="max-w-5xl mx-auto px-4 sm:px-6 py-8">
        <p className="text-red-600">Invalid group URL</p>
      </div>
    );
  }

  return <GroupDetailsPageContent key={groupId} groupId={groupId} />;
}

export default GroupDetailsPageRoute;
