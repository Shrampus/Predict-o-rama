import { useState } from 'react';
import { Check, Copy } from 'lucide-react';

type CopyInviteButtonProps = {
  inviteCode: string;
};

function CopyInviteButton({ inviteCode }: CopyInviteButtonProps) {
  const [isCopied, setIsCopied] = useState(false);

  async function handleCopy() {
    await navigator.clipboard.writeText(inviteCode);
    setIsCopied(true);
    setTimeout(() => setIsCopied(false), 2000);
  }

  return (
    <button
      onClick={handleCopy}
      className="ml-1 flex items-center gap-1 hover:text-slate-600 transition-colors"
      title="Copy invite code"
    >
      {isCopied ? (
        <>
          <Check size={12} className="text-green-600" />
          <span className="text-green-600">Copied!</span>
        </>
      ) : (
        <Copy size={12} />
      )}
    </button>
  );
}

export default CopyInviteButton;
