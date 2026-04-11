import CreateGroupForm from './components/CreateGroupForm';
import JoinGroupBanner from './components/JoinGroupBanner';
import MyGroupsList from './components/MyGroupsList';
import { useCreateGroupForm } from './hooks/useCreateGroupForm';
import { useGroups } from './hooks/useGroups';
import { useJoinGroupBanner } from './hooks/useJoinGroupBanner';

function GroupsPage() {
  const myGroups = useGroups();
  const createForm = useCreateGroupForm(myGroups.refetch);
  const joinBanner = useJoinGroupBanner(myGroups.refetch);

  return (
    <div className="max-w-2xl mx-auto px-4 py-8 space-y-6">
      <JoinGroupBanner
        inviteCode={joinBanner.formData.inviteCode}
        isLoading={joinBanner.isLoading}
        errorMessage={joinBanner.errorMessage}
        joinedMember={joinBanner.joinedMember}
        handleChange={joinBanner.handleChange}
        handleSubmit={joinBanner.handleSubmit}
      />

      <CreateGroupForm
        name={createForm.formData.name}
        description={createForm.formData.description}
        isLoading={createForm.isLoading}
        errorMessage={createForm.errorMessage}
        isFormOpen={createForm.isFormOpen}
        createdGroup={createForm.createdGroup}
        toggleForm={createForm.toggleForm}
        handleChange={createForm.handleChange}
        handleSubmit={createForm.handleSubmit}
      />

      <MyGroupsList
        groups={myGroups.groups}
        isLoading={myGroups.isLoading}
        errorMessage={myGroups.errorMessage}
        onLeave={myGroups.refetch}
      />
    </div>
  );
}

export default GroupsPage;
