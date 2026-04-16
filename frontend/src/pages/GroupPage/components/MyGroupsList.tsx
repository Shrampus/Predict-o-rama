import { LogOut } from 'lucide-react';
import { useState } from 'react';
import { useNavigate } from 'react-router-dom';

import { ROUTE_PATHS } from '../../../app/routePaths';
import CopyInviteButton from '../../../components/ui/CopyInviteButton';
import type { MyGroupsResponse } from '../../../services/groupApi';

type MyGroupsListProps = {
  groups: MyGroupsResponse[];
  isLoading: boolean;
  errorMessage: string;
  onLeave: (groupId: string) => Promise<void>;
};

function LeaveGroupButton({
  groupId,
  onLeave,
}: {
  groupId: string;
  onLeave: (groupId: string) => Promise<void>;
}) {
  const [isLeaving, setIsLeaving] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');

  async function handleLeave() {
    setIsLeaving(true);
    setErrorMessage('');
    try {
      await onLeave(groupId);
    } catch (err) {
      setErrorMessage(err instanceof Error ? err.message : 'Failed to leave group');
    } finally {
      setIsLeaving(false);
    }
  }

  return (
    <div className="flex flex-col items-end gap-1">
      <button
        onClick={handleLeave}
        disabled={isLeaving}
        title="Leave group"
        className="flex items-center gap-1 text-slate-400 hover:text-red-500 transition-colors disabled:opacity-50"
      >
        <LogOut size={16} />
      </button>
      {errorMessage && <p className="text-xs text-red-600">{errorMessage}</p>}
    </div>
  );
}

function MyGroupsList({ groups, isLoading, errorMessage, onLeave }: MyGroupsListProps) {
  const navigate = useNavigate();

  function getGroupDetailsPath(groupId: string) {
    return ROUTE_PATHS.groupDetails.replace(':groupId', encodeURIComponent(groupId));
  }

  function openGroupDetails(group: MyGroupsResponse) {
    navigate(getGroupDetailsPath(group.groupId), { state: { group } });
  }

  function shouldIgnoreRowNavigation(target: EventTarget | null): boolean {
    if (!(target instanceof HTMLElement)) {
      return false;
    }

    return target.closest('button, a') !== null;
  }

  return (
    <section className="bg-white border border-slate-200 rounded-2xl shadow-sm p-5 sm:p-6">
      <div className="flex items-center justify-between mb-5">
        <h2 className="text-lg sm:text-xl font-semibold text-slate-900">My Groups</h2>
        <span className="text-sm text-slate-500">
          {groups.length} {groups.length === 1 ? 'group' : 'groups'}
        </span>
      </div>

      {isLoading && <p className="text-sm text-slate-500">Loading...</p>}

      {errorMessage && <p className="text-sm text-red-600">{errorMessage}</p>}

      {!isLoading && groups.length === 0 && (
        <p className="text-sm text-slate-400">You are not a member of any groups yet.</p>
      )}

      <ul className="space-y-3">
        {groups.map((group) => (
          <li
            key={group.groupId}
            onClick={(event) => {
              if (shouldIgnoreRowNavigation(event.target)) {
                return;
              }

              openGroupDetails(group);
            }}
            onKeyDown={(event) => {
              if (shouldIgnoreRowNavigation(event.target)) {
                return;
              }

              if (event.key === 'Enter' || event.key === ' ') {
                event.preventDefault();
                openGroupDetails(group);
              }
            }}
            role="button"
            tabIndex={0}
            className="flex items-center justify-between bg-slate-50 border border-slate-200
                       rounded-xl px-5 py-4 cursor-pointer transition-colors hover:bg-slate-100
                       focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-blue-500"
          >
            <div>
              <p className="font-semibold text-slate-900">{group.name}</p>
              <p className="text-sm text-slate-500">{group.description}</p>
              {group.groupMemberRole === 'ADMIN' && (
                <p className="text-xs text-slate-400 mt-1 flex items-center gap-1">
                  Invite code: <span className="font-mono">{group.inviteCode}</span>
                  <CopyInviteButton inviteCode={group.inviteCode} />
                </p>
              )}
            </div>
            <div className="flex items-center gap-3">
              <button
                type="button"
                onClick={() => openGroupDetails(group)}
                className="text-xs font-semibold text-blue-600 hover:text-blue-700"
              >
                View details
              </button>
              <span
                className={`text-xs font-bold uppercase tracking-wide px-3 py-1 rounded-full ${
                  group.groupMemberRole === 'ADMIN'
                    ? 'bg-green-100 text-green-700'
                    : 'bg-slate-100 text-slate-500'
                }`}
              >
                {group.groupMemberRole}
              </span>
              {group.groupMemberRole !== 'ADMIN' && (
                <LeaveGroupButton groupId={group.groupId} onLeave={onLeave} />
              )}
            </div>
          </li>
        ))}
      </ul>
    </section>
  );
}

export default MyGroupsList;
