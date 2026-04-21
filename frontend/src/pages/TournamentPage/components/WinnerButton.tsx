import type { ReactNode } from 'react';

type WinnerButtonProps = {
    isActive: boolean;
    onClick: () => void;
    disabled?: boolean;
    children: ReactNode;
};

function WinnerButton({
    isActive,
    onClick,
    disabled = false,
    children,
}: WinnerButtonProps) {
    const buttonClassName = [
        'flex-1 rounded-lg py-1 text-xs font-bold uppercase tracking-widest transition-colors',
        isActive ? 'bg-green-700 text-white shadow-sm' : 'bg-white text-slate-400 hover:bg-slate-100',
        disabled ? 'cursor-not-allowed opacity-60 hover:bg-white' : '',
    ].join(' ');

    return (
        <button
            type="button"
            onClick={onClick}
            disabled={disabled}
            className={buttonClassName}
        >
            {children}
        </button>
    );
}

export default WinnerButton;