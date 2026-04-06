import { useEffect, useState } from 'react';

import type { CurrentUser } from '../../../services/authApi';
import { authApi } from '../../../services/authApi';

export function useCurrentUser() {
    const [currentUser, setCurrentUser] = useState<CurrentUser | null>(null);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

useEffect(() => {
    authApi.me()
        .then(user => {
            setError(null);
            setCurrentUser(user);
        })
        .catch(() => setError('Failed to fetch current user'))
        .finally(() => setIsLoading(false));
}, []);

return currentUser;
}