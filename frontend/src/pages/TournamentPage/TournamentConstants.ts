export type WinningTeam = 'Home' | 'Draw' | 'Away';
export type Prediction = { home: number; away: number; winningTeam: WinningTeam; saved: boolean };
export type Match = typeof matches[0];

export const matches = [
    {
        id: 1,
        homeTeam: { name: 'GERMANY', flag: '🇩🇪', label: 'Host' },
        awayTeam: { name: 'FRANCE', flag: '🇫🇷', label: 'Rank 1' },
        time: '22:00 CET',
        timeStyle: 'urgent',
    },
    {
        id: 2,
        homeTeam: { name: 'ENGLAND', flag: '🏴󠁧󠁢󠁥󠁮󠁧󠁿', label: 'Seed 4' },
        awayTeam: { name: 'SPAIN', flag: '🇪🇸', label: 'Seed 2' },
        time: 'TOMORROW',
        timeStyle: 'default',
    },
];

export const standings = [
    { rank: 1, name: 'Germany', flag: '🇩🇪', pts: 9, gd: '+7', active: true, danger: false },
    { rank: 2, name: 'Switzerland', flag: '🇨🇭', pts: 5, gd: '+1', active: false, danger: false },
    { rank: 3, name: 'Hungary', flag: '🇭🇺', pts: 3, gd: '-2', active: false, danger: false },
    { rank: 4, name: 'Scotland', flag: '🏴󠁧󠁢󠁳󠁣󠁴󠁿', pts: 0, gd: '-6', active: false, danger: true },
];