import { useState } from 'react'
import { useNavigate } from 'react-router-dom'

interface UseJoinGroupBannerReturn {
  inviteCode: string
  handleChange: (e: React.ChangeEvent<HTMLInputElement>) => void
  handleSubmit: (e: React.FormEvent) => void
}

function extractInviteCode(input: string): string {
  try {
    const url = new URL(input)
    const match = url.pathname.match(/\/invite\/([^/]+)$/)
    if (match) return match[1]
  } catch {
    // not a URL — use as-is
  }
  return input.trim()
}

export function useJoinGroupBanner(): UseJoinGroupBannerReturn {
  const [inviteCode, setInviteCode] = useState('')
  const navigate = useNavigate()

  function handleChange(e: React.ChangeEvent<HTMLInputElement>) {
    setInviteCode(e.target.value)
  }

  function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    const code = extractInviteCode(inviteCode)
    if (code) navigate(`/invite/${code}`)
  }

  return { inviteCode, handleChange, handleSubmit }
}
