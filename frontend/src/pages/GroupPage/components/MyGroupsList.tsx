import { LogOut } from 'lucide-react'
import { useState } from 'react';

import CopyInviteButton from '../../../components/ui/CopyInviteButton';
import { leaveGroup } from '../../../services/groupApi';
import type { MyGroupsResponse } from '../../../services/groupApi';

type MyGroupsListProps = {
  groups: MyGroupsResponse[];
  isLoading: boolean;
  errorMessage: string;
  onLeave: () => void;
};

function LeaveGroupButton({ groupId, onLeave }: { groupId: string; onLeave: () => void }) {
  const [isLeaving, setIsLeaving] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');

  async function handleLeave() {
    setIsLeaving(true);
    setErrorMessage('');
    try {
      await leaveGroup(groupId);
      onLeave();
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
  return (
    <div>
      <h2 className="text-lg font-semibold mb-3">My Groups</h2>

      {isLoading && <p className="text-sm text-slate-500">Loading...</p>}

      {errorMessage && <p className="text-sm text-red-600">{errorMessage}</p>}

      {!isLoading && groups.length === 0 && (
        <p className="text-sm text-slate-400">You are not a member of any groups yet.</p>
      )}

      <ul className="space-y-2">
        {groups.map((group) => (
          <li
            key={group.groupId}
            className="flex items-center justify-between bg-white border border-slate-200
                       rounded-xl px-5 py-4 shadow-sm"
          >
            <div>
              <p className="font-semibold">{group.name}</p>
              <p className="text-sm text-slate-500">{group.description}</p>
              {group.groupMemberRole === 'ADMIN' && (
                <p className="text-xs text-slate-400 mt-1 flex items-center gap-1">
                  Invite code: <span className="font-mono">{group.inviteCode}</span>
                  <CopyInviteButton inviteCode={group.inviteCode} />
                </p>
              )}
            </div>
            <div className="flex items-center gap-3">
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
    </div>
  );
}

export default MyGroupsList;
