import type { FormEvent } from 'react';

import type { GroupMemberResponse } from '../../../services/groupApi';

type GroupMembersSectionProps = {
  members: GroupMemberResponse[];
  isLoadingMembers: boolean;
  membersError: string;
  isAdmin: boolean;
  removingMemberUserId: string | null;
  memberEmail: string;
  isAddingMember: boolean;
  memberActionMessage: string;
  memberActionError: string;
  onAddMember: (event: FormEvent<HTMLFormElement>) => void;
  onMemberEmailChange: (value: string) => void;
  onResetMemberFeedback: () => void;
  onRemoveMember: (member: GroupMemberResponse) => void;
};

export function GroupMembersSection({
  members,
  isLoadingMembers,
  membersError,
  isAdmin,
  removingMemberUserId,
  memberEmail,
  isAddingMember,
  memberActionMessage,
  memberActionError,
  onAddMember,
  onMemberEmailChange,
  onResetMemberFeedback,
  onRemoveMember,
}: GroupMembersSectionProps) {
  return (
    <section className="bg-white border border-slate-200 rounded-2xl p-5 sm:p-6 shadow-sm space-y-4">
      <h2 className="text-xl font-bold text-slate-900">Members</h2>
      {isLoadingMembers && <p className="text-sm text-slate-500">Loading members...</p>}
      {membersError && <p className="text-sm text-red-600">{membersError}</p>}
      {!isLoadingMembers && !membersError && members.length === 0 && (
        <p className="text-sm text-slate-500">No members found in this group yet.</p>
      )}
      {!isLoadingMembers && !membersError && members.length > 0 && (
        <ul className="space-y-2.5">
          {members.map((member) => (
            <li
              key={member.id}
              className="text-sm text-slate-700 flex items-center justify-between bg-slate-50/70 border border-slate-200 rounded-xl px-3.5 py-2.5"
            >
              <span className="text-sm font-medium text-slate-700 truncate">{member.name}</span>
              <div className="flex items-center gap-2.5">
                <span className="text-[11px] font-bold uppercase tracking-wide text-slate-500">
                  {member.memberRole}
                </span>
                {isAdmin && member.memberRole !== 'ADMIN' && (
                  <button
                    type="button"
                    onClick={() => onRemoveMember(member)}
                    disabled={removingMemberUserId === member.userId}
                    className="rounded-lg border border-red-200 bg-red-50 px-2.5 py-1 text-[11px] font-semibold uppercase tracking-wide text-red-700 hover:bg-red-100 disabled:opacity-60 disabled:hover:bg-red-50"
                  >
                    {removingMemberUserId === member.userId ? 'Removing...' : 'Remove'}
                  </button>
                )}
              </div>
            </li>
          ))}
        </ul>
      )}

      {isAdmin ? (
        <form onSubmit={onAddMember} className="pt-4 border-t border-slate-100 space-y-2.5">
          <label htmlFor="member-email" className="block text-sm font-semibold text-slate-700">
            Add member by email
          </label>
          <div className="flex gap-2">
            <input
              id="member-email"
              type="email"
              placeholder="teammate@example.com"
              value={memberEmail}
              onChange={(event) => {
                onMemberEmailChange(event.target.value);
                onResetMemberFeedback();
              }}
              disabled={isAddingMember}
              className="flex-1 border border-slate-300 rounded-lg px-3 py-2 text-sm bg-white text-slate-800 placeholder:text-slate-400 focus:outline-none focus:ring-2 focus:ring-green-600/20 focus:border-green-600"
            />
            <button
              type="submit"
              disabled={isAddingMember || memberEmail.trim().length === 0}
              className="rounded-lg bg-green-700 hover:bg-green-800 text-white px-4 py-2 text-sm font-semibold transition-colors disabled:opacity-50 disabled:hover:bg-green-700"
            >
              {isAddingMember ? 'Adding...' : 'Add'}
            </button>
          </div>
          {memberActionMessage && <p className="text-sm text-emerald-700">{memberActionMessage}</p>}
          {memberActionError && <p className="text-sm text-red-600">{memberActionError}</p>}
        </form>
      ) : (
        <p className="text-xs text-slate-500 pt-4 border-t border-slate-100">
          Admin-only action: adding members is only available to group admins.
        </p>
      )}
    </section>
  );
}
