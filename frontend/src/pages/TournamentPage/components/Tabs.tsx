import { useTranslation } from 'react-i18next';

function Tabs() {
    const { t } = useTranslation();

    return (
        <div className="flex gap-2 mb-8 bg-slate-100 p-1.5 rounded-full w-fit mx-auto sm:mx-0">
            <button
                type="button"
                className="px-8 py-2.5 rounded-full font-bold text-sm transition-all bg-green-700 text-white shadow-lg"
            >
                {t('tabs.matches')}
            </button>
        </div>
    );
}

export default Tabs;
