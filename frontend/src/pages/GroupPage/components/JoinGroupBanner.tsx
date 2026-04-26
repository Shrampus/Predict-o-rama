import { useTranslation } from 'react-i18next';

type JoinGroupBannerProps = {
  inviteCode: string;
  handleChange: (e: React.ChangeEvent<HTMLInputElement>) => void;
  handleSubmit: (e: React.FormEvent) => void;
};

function JoinGroupBanner({ inviteCode, handleChange, handleSubmit }: JoinGroupBannerProps) {
  const { t } = useTranslation();

  return (
    <div className="w-full bg-green-50 border border-green-200 rounded-2xl px-5 sm:px-6 py-5 shadow-sm">
      <p className="text-green-800 font-semibold mb-3">{t('groups.joinTitle')}</p>

      <form onSubmit={handleSubmit} className="flex gap-3 flex-wrap sm:flex-nowrap">
        <input
          type="text"
          name="inviteCode"
          value={inviteCode}
          onChange={handleChange}
          placeholder={t('groups.joinPlaceholder')}
          className="flex-1 min-w-[220px] border border-green-300 bg-white rounded-lg px-4 py-2 text-sm
                     focus:outline-none focus:ring-2 focus:border-green-300"
          required
        />
        <button
          type="submit"
          className="bg-green-700 text-white px-5 py-2 rounded-lg text-sm font-semibold
                     hover:bg-green-800 whitespace-nowrap"
        >
          {t('groups.viewGroup')}
        </button>
      </form>
    </div>
  );
}

export default JoinGroupBanner;
