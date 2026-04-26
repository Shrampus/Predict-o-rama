import CreateGroupForm from './components/CreateGroupForm';
import JoinGroupBanner from './components/JoinGroupBanner';
import MyGroupsList from './components/MyGroupsList';
import { useCreateGroupForm } from './hooks/useCreateGroupForm';
import { useGroups } from './hooks/useGroups';
import { useJoinGroupBanner } from './hooks/useJoinGroupBanner';

function GroupsPage() {
  const myGroups = useGroups();
  const createForm = useCreateGroupForm(myGroups.refetch);
  const joinBanner = useJoinGroupBanner();

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 py-8 space-y-8">
      <div className="grid grid-cols-1 laptop:grid-cols-12 gap-8 items-start">
        <div className="laptop:col-span-4 space-y-6">
          <JoinGroupBanner
            inviteCode={joinBanner.inviteCode}
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
        </div>

        <div className="laptop:col-span-8">
          <MyGroupsList
            groups={myGroups.groups}
            isLoading={myGroups.isLoading}
            errorMessage={myGroups.errorMessage}
            onLeave={myGroups.leaveCurrentGroup}
          />
        </div>
      </div>
    </div>
  );
}

export default GroupsPage;
