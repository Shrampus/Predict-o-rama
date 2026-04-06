import { Minus, Plus } from 'lucide-react';

import CopyInviteButton from '../../../components/ui/CopyInviteButton';
import type { GroupResponse } from '../../../services/groupApi';

type CreateGroupFormProps = {
  name: string;
  description: string;
  isLoading: boolean;
  errorMessage: string;
  isFormOpen: boolean;
  createdGroup: GroupResponse | null;
  toggleForm: () => void;
  handleChange: (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => void;
  handleSubmit: (e: React.FormEvent) => void;
};

function CreateGroupForm({
  name,
  description,
  isLoading,
  errorMessage,
  isFormOpen,
  createdGroup,
  toggleForm,
  handleChange,
  handleSubmit,
}: CreateGroupFormProps) {
  return (
    <div className="bg-gray-50 border border-gray-200 rounded-2xl px-6 py-5 shadow-sm">
      <button
        onClick={toggleForm}
        className="flex items-center gap-1 text-gray-800 font-semibold hover:text-gray-600 transition-colors"
      >
        {isFormOpen ? <Minus size={16} /> : <Plus size={16} />}
        {isFormOpen ? 'Add new group' : 'Add new group'}
      </button>

      {isFormOpen && (
        <form onSubmit={handleSubmit} className="flex gap-3 flex-wrap mt-4">
          {errorMessage && <p className="w-full text-red-600 text-sm">{errorMessage}</p>}

          <input
            type="text"
            name="name"
            value={name}
            onChange={handleChange}
            placeholder="Group name"
            className="flex-1 min-w-0 border border-gray-300 bg-white rounded-lg px-4 py-2 text-sm
                       focus:outline-none focus:ring-2 focus:ring-gray-400"
            required
          />
          <input
            type="text"
            name="description"
            value={description}
            onChange={handleChange}
            placeholder="Description"
            className="flex-1 min-w-0 border border-gray-300 bg-white rounded-lg px-4 py-2 text-sm
                       focus:outline-none focus:ring-2 focus:ring-gray-400"
          />
          <button
            type="submit"
            disabled={isLoading}
            className="bg-gray-600 text-white px-5 py-2 rounded-lg text-sm font-semibold
                       hover:bg-gray-700 disabled:opacity-50 whitespace-nowrap"
          >
            {isLoading ? 'Creating...' : 'Create Group'}
          </button>
        </form>
      )}

      {createdGroup && (
        <div className="mt-3">
          <p className="text-green-700 text-sm font-medium">Group created!</p>
          <p className="text-sm text-slate-600 mt-1 flex items-center gap-1">
            Invite code: <span className="font-mono">{createdGroup.inviteCode}</span>
            <CopyInviteButton inviteCode={createdGroup.inviteCode} />
          </p>
          <p className="text-xs text-slate-400 mt-1">
            Share this code so others can join your group.
          </p>
        </div>
      )}
    </div>
  )
}

export default CreateGroupForm;
