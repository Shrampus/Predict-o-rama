function TimeBadgeFunction(Match: any) {
    return (
              <div
        className={`absolute top-4 right-4 text-[10px] font-bold px-2 py-0.5 rounded ${
          Match.timeStyle === 'urgent' ? 'text-red-700 bg-red-100' : 'text-slate-500 bg-slate-100'
        }`}
      >
        {Match.time}
      </div>
    )
}

export default TimeBadgeFunction