type GroupDetailsHeaderProps = {
  name: string;
  description: string;
  isLoadingGroup: boolean;
  groupError: string;
};

export function GroupDetailsHeader({
  name,
  description,
  isLoadingGroup,
  groupError,
}: GroupDetailsHeaderProps) {
  return (
    <header className="relative overflow-hidden rounded-2xl shadow-lg">
      <div className="absolute inset-0 bg-gradient-to-r from-green-900 via-green-800 to-emerald-700" />
      <div className="relative z-10 p-6 sm:p-7">
        <p className="text-xs font-bold uppercase tracking-[0.14em] text-green-200">Group Details</p>
        <h1 className="text-3xl sm:text-4xl font-black tracking-tight text-white mt-2">{name}</h1>
        {isLoadingGroup && <p className="text-sm text-green-100/90 mt-3">Loading group details...</p>}
        {groupError && <p className="text-sm text-red-100 mt-3">{groupError}</p>}
        {!isLoadingGroup && !groupError && (
          <p className="text-sm sm:text-base text-green-100/95 mt-3 max-w-3xl">{description}</p>
        )}
      </div>
    </header>
  );
}
