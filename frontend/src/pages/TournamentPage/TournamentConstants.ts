export type WinningTeam = 'Home' | 'Draw' | 'Away';
export type Prediction = { home: number; away: number; winningTeam: WinningTeam; saved: boolean };
export type Match = typeof matches[0];

export const matches = [
    {
        id: 1,
        group: 'A',
        homeTeam: { name: 'GERMANY', flag: '🇩🇪', isHost: true },
        awayTeam: { name: 'FRANCE', flag: '🇫🇷' },
        datetime: '2026-04-02T22:00',
        time: '22:00 CET',
        timeStyle: 'urgent',
    },
    {
        id: 2,
        group: 'B',
        homeTeam: { name: 'ENGLAND', flag: '🏴󠁧󠁢󠁥󠁮󠁧󠁿' },
        awayTeam: { name: 'SPAIN', flag: '🇪🇸' },
        datetime: '2026-04-03T18:00',
        time: 'TOMORROW 18:00',
        timeStyle: 'default',
    },
    {
        id: 3,
        group: 'A',
        homeTeam: { name: 'PORTUGAL', flag: '🇵🇹' },
        awayTeam: { name: 'NETHERLANDS', flag: '🇳🇱' },
        datetime: '2026-04-02T19:00',
        time: '19:00 CET',
        timeStyle: 'urgent',
    },
    {
        id: 4,
        group: 'B',
        homeTeam: { name: 'ITALY', flag: '🇮🇹' },
        awayTeam: { name: 'CROATIA', flag: '🇭🇷' },
        datetime: '2026-04-04T20:00',
        time: 'IN 2 DAYS',
        timeStyle: 'default',
    },
    {
        id: 5,
        group: 'C',
        homeTeam: { name: 'BELGIUM', flag: '🇧🇪' },
        awayTeam: { name: 'AUSTRIA', flag: '🇦🇹' },
        datetime: '2026-04-05T15:00',
        time: 'IN 3 DAYS',
        timeStyle: 'default',
    },
    {
        id: 6,
        group: 'C',
        homeTeam: { name: 'DENMARK', flag: '🇩🇰' },
        awayTeam: { name: 'TURKIYE', flag: '🇹🇷' },
        datetime: '2026-04-05T18:00',
        time: 'IN 3 DAYS',
        timeStyle: 'default',
    },
];

export function getTeamLabel(teamName: string, isHost?: boolean): string {
    if (isHost) return 'Host';
    const team = standings.find((t) => t.name.toUpperCase() === teamName);
    if (!team) return '';
    return `Group ${team.group} · Rank ${team.rank}`;
}

export const standings = [
    { group: 'A', rank: 1, name: 'Germany',     flag: '🇩🇪', pts: 9, gd: '+7', danger: false },
    { group: 'A', rank: 2, name: 'Switzerland', flag: '🇨🇭', pts: 5, gd: '+1', danger: false },
    { group: 'A', rank: 3, name: 'Hungary',     flag: '🇭🇺', pts: 3, gd: '-2', danger: false },
    { group: 'A', rank: 4, name: 'Scotland',    flag: '🏴󠁧󠁢󠁳󠁣󠁴󠁿', pts: 0, gd: '-6', danger: true  },
    { group: 'B', rank: 1, name: 'Spain',       flag: '🇪🇸', pts: 7, gd: '+5', danger: false },
    { group: 'B', rank: 2, name: 'Croatia',     flag: '🇭🇷', pts: 4, gd: '+0', danger: false },
    { group: 'B', rank: 3, name: 'Italy',       flag: '🇮🇹', pts: 4, gd: '-1', danger: false },
    { group: 'B', rank: 4, name: 'Albania',     flag: '🇦🇱', pts: 1, gd: '-4', danger: true  },
    { group: 'C', rank: 1, name: 'England',     flag: '🏴󠁧󠁢󠁥󠁮󠁧󠁿', pts: 6, gd: '+4', danger: false },
    { group: 'C', rank: 2, name: 'Denmark',     flag: '🇩🇰', pts: 5, gd: '+2', danger: false },
    { group: 'C', rank: 3, name: 'Slovenia',    flag: '🇸🇮', pts: 2, gd: '-1', danger: false },
    { group: 'C', rank: 4, name: 'Serbia',      flag: '🇷🇸', pts: 1, gd: '-5', danger: true  },
];