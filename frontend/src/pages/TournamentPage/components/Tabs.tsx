import { useTranslation } from 'react-i18next';

type Tab = 'matches' | 'results';

type Props = {
    activeTab: Tab;
    onTabChange: (tab: Tab) => void;
};

function Tabs({ activeTab, onTabChange }: Props) {
    const { t } = useTranslation();

    const tabs: { key: Tab; label: string }[] = [
        { key: 'matches', label: t('tabs.matches') },
        { key: 'results', label: t('tabs.results') },
    ];

    return (
        <div className="flex gap-2 mb-8 bg-slate-100 p-1.5 rounded-full w-fit mx-auto sm:mx-0">
            {tabs.map(({ key, label }) => (
                <button
                    key={key}
                    type="button"
                    onClick={() => onTabChange(key)}
                    className={`px-8 py-2.5 rounded-full font-bold text-sm transition-all ${
                        activeTab === key
                            ? 'bg-green-700 text-white shadow-lg'
                            : 'text-slate-500 hover:text-slate-800'
                    }`}
                >
                    {label}
                </button>
            ))}
        </div>
    );
}

export default Tabs;
