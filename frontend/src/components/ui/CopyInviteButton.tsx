import { Check, Copy } from 'lucide-react'
import { useState } from 'react';
import { useTranslation } from 'react-i18next';

type CopyInviteButtonProps = {
  inviteCode: string;
};

function CopyInviteButton({ inviteCode }: CopyInviteButtonProps) {
  const [isCopied, setIsCopied] = useState(false);
  const { t } = useTranslation();

  async function handleCopy() {
    const link = `${window.location.origin}/invite/${inviteCode}`;
    await navigator.clipboard.writeText(link);
    setIsCopied(true);
    setTimeout(() => setIsCopied(false), 2000);
  }

  return (
    <button
      onClick={handleCopy}
      className="ml-1 flex items-center gap-1 hover:text-slate-600 transition-colors"
      title={t('groups.copyInviteCode')}
    >
      {isCopied ? (
        <>
          <Check size={12} className="text-green-600" />
          <span className="text-green-600">{t('groups.copied')}</span>
        </>
      ) : (
        <Copy size={12} />
      )}
    </button>
  );
}

export default CopyInviteButton;
