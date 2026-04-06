import { useEffect, useState } from 'react';

import type { CurrentUser } from '../../../services/authApi';
import { authApi } from '../../../services/authApi';

export function useCurrentUser() {
    const [currentUser, setCurrentUser] = useState<CurrentUser | null>(null);



useEffect(() => {
    authApi.me()
        .then(user => {
            setCurrentUser(user);
        })
}, []);

return currentUser;
}