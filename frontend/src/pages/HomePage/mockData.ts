import type { UpcomingMatch } from './types';

export const MOCK_UPCOMING_MATCHES: UpcomingMatch[] = [
  {
    matchId: 'mock-match-1',
    externalMatchId: 'ext-001',
    homeTeamName: 'Arsenal',
    awayTeamName: 'Chelsea',
    homeTeamImage: 'https://crests.football-data.org/57.png',
    awayTeamImage: 'https://crests.football-data.org/61.png',
    kickoffTime: '2026-04-20T19:45:00Z',
    groups: [
      { groupId: 'group-1', groupName: 'Office League', competitionId: 'PL' },
      { groupId: 'group-2', groupName: 'Friends Cup',   competitionId: 'PL' },
    ],
  },
  {
    matchId: 'mock-match-2',
    externalMatchId: 'ext-002',
    homeTeamName: 'Real Madrid',
    awayTeamName: 'Bayern Munich',
    homeTeamImage: 'https://crests.football-data.org/86.png',
    awayTeamImage: 'https://crests.football-data.org/5.png',
    kickoffTime: '2026-04-22T20:00:00Z',
    groups: [
      { groupId: 'group-1', groupName: 'Office League', competitionId: 'CL' },
    ],
  },
];
