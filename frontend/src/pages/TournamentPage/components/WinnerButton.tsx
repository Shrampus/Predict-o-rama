import './MatchCard.css';

function WinnerButton({ isActive, onClick, children }: { isActive: boolean; onClick: () => void; children: React.ReactNode }) {
    return (
        <button
            onClick={onClick}
            className={`winnerBtnBase ${isActive ? 'winnerBtnActive' : 'winnerBtnInactive'}`}
        >
            {children}
        </button>
    );
}

export default WinnerButton;