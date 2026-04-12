import type { GroupMemberResponse } from '../../../services/groupApi';

type JoinGroupBannerProps = {
  inviteCode: string;
  isLoading: boolean;
  errorMessage: string;
  joinedMember: GroupMemberResponse | null;
  handleChange: (e: React.ChangeEvent<HTMLInputElement>) => void;
  handleSubmit: (e: React.FormEvent) => void;
};

function JoinGroupBanner({
  inviteCode,
  isLoading,
  errorMessage,
  joinedMember,
  handleChange,
  handleSubmit,
}: JoinGroupBannerProps) {
  return (
    <div className="bg-green-50 border border-green-200 rounded-2xl px-6 py-5 shadow-sm">
      <p className="text-green-800 font-semibold mb-3">Have an invite code? Join here:</p>

      {errorMessage && <p className="text-red-600 text-sm mb-3">{errorMessage}</p>}

      {joinedMember && (
        <p className="text-green-700 text-sm mb-3 font-medium">Successfully joined the group!</p>
      )}

      <form onSubmit={handleSubmit} className="flex gap-3 flex-wrap">
        <input
          type="text"
          name="inviteCode"
          value={inviteCode}
          onChange={handleChange}
          placeholder="Paste invite code"
          className="flex-1 min-w-0 border border-green-300 bg-white rounded-lg px-4 py-2 text-sm
                     focus:outline-none focus:ring-2 focus:border-green-300"
          required
        />
        <button
          type="submit"
          disabled={isLoading}
          className="bg-green-600 text-white px-5 py-2 rounded-lg text-sm font-semibold
                     hover:bg-green-700 disabled:opacity-50 whitespace-nowrap"
        >
          {isLoading ? 'Joining...' : 'Join Group'}
        </button>
      </form>
    </div>
  )
}

export default JoinGroupBanner;
