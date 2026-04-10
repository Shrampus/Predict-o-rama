type TimeBadgeProps = {
    time: string;
    timeStyle: string;
};

function TimeBadge({ time, timeStyle }: TimeBadgeProps) {
    return (
        <div
            className={`absolute top-4 right-4 text-[10px] font-bold px-2 py-0.5 rounded ${
                timeStyle === 'urgent' ? 'text-red-700 bg-red-100' : 'text-slate-500 bg-slate-100'
            }`}
        >
            {time}
        </div>
    );
}

export default TimeBadge;
