import { useState } from 'react';

export type SelectedUser = {
  userId: string;
  userName: string;
  competitionCode: string;
  tournamentName: string;
};

export function useLeaderboardModal() {
  const [selectedUser, setSelectedUser] = useState<SelectedUser | null>(null);

  return {
    selectedUser,
    selectUser: (user: SelectedUser) => setSelectedUser(user),
    clearSelectedUser: () => setSelectedUser(null),
  };
}
