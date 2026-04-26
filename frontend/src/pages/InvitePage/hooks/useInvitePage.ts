import { useEffect, useState } from 'react';
import { useLocation, useNavigate, useParams } from 'react-router-dom';

import { ROUTE_PATHS } from '../../../app/routePaths';
import { useAuth } from '../../../context/useAuth';
import { type GroupPreviewResponse, getGroupPreview, joinGroup } from '../../../services/groupApi';

type PageState =
  | { status: 'loading' }
  | { status: 'not_found' }
  | { status: 'load_error' }
  | { status: 'loaded'; preview: GroupPreviewResponse }
  | { status: 'already_member' }
  | { status: 'joined' };

export function useInvitePage() {
  const { inviteCode } = useParams<{ inviteCode: string }>();
  const { currentUser } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  const [pageState, setPageState] = useState<PageState>({ status: 'loading' });
  const [isJoining, setIsJoining] = useState(false);
  const [joinError, setJoinError] = useState('');

  useEffect(() => {
    if (!inviteCode) {
      setPageState({ status: 'not_found' });
      return;
    }
    getGroupPreview(inviteCode)
      .then(preview => setPageState({ status: 'loaded', preview }))
      .catch((err: unknown) => {
        const message = err instanceof Error ? err.message : '';
        setPageState({ status: message === 'INVITE_NOT_FOUND' ? 'not_found' : 'load_error' });
      });
  }, [inviteCode]);

  async function handleJoin() {
    if (!inviteCode) return;
    setIsJoining(true);
    setJoinError('');
    try {
      await joinGroup({ inviteCode });
      navigate(ROUTE_PATHS.groups);
    } catch (err: unknown) {
      const message = err instanceof Error ? err.message : '';
      if (message.includes('already a member')) {
        setPageState({ status: 'already_member' });
      } else {
        setJoinError('invite.joinError');
      }
    } finally {
      setIsJoining(false);
    }
  }

  function handleLoginToJoin() {
    navigate(ROUTE_PATHS.login, { state: { from: location.pathname } });
  }

  return {
    pageState,
    isJoining,
    joinError,
    isLoggedIn: currentUser !== null,
    handleJoin,
    handleLoginToJoin,
  };
}
