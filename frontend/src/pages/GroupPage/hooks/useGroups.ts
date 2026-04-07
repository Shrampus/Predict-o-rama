import { useCallback, useEffect, useState } from 'react';

import { getMyGroups } from '../../../services/groupApi';
import type { MyGroupsResponse } from '../../../services/groupApi';

interface UseGroupsReturn {
  groups: MyGroupsResponse[];
  isLoading: boolean;
  errorMessage: string;
  refetch: () => void;
}

export function useGroups(): UseGroupsReturn {
  const [groups, setGroups] = useState<MyGroupsResponse[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');

  const fetchGroups = useCallback(async () => {
    setIsLoading(true);
    setErrorMessage('');

    try {
      const result = await getMyGroups();
      const sorted = [...result].sort((a, b) => {
        if (a.groupMemberRole === b.groupMemberRole) return 0;
        return a.groupMemberRole === 'ADMIN' ? -1 : 1;
      });
      setGroups(sorted);
    } catch (err) {
      setErrorMessage(err instanceof Error ? err.message : 'Failed to load groups');
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchGroups();
  }, [fetchGroups]);

  return { groups, isLoading, errorMessage, refetch: fetchGroups };
}
