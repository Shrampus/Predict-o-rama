type TeamDisplayProps = {
    imageUrl : string;
    label: string;
    name: string;
    align: 'left' | 'right';
};

function TeamDisplay({ imageUrl, label, name, align }: TeamDisplayProps) {
    const alignClasses = align === 'right'
        ? 'items-center sm:items-end text-center sm:text-right'
        : 'items-center sm:items-start text-center sm:text-left';

    return (
        <div className={`flex-1 flex flex-col gap-2 ${alignClasses}`}>
            <img src={imageUrl} alt={name} className="w-12 h-12 object-contain" />
            <div>
                <p className="text-xs font-bold uppercase tracking-widest text-slate-400 mb-1">{label}</p>
                <h3 className="text-xl font-black tracking-tight">{name}</h3>
            </div>
        </div>
    );
}

export default TeamDisplay;
