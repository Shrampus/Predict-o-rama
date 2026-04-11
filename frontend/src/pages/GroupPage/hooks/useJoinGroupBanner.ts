import { useState } from 'react'

import { joinGroup } from '../../../services/groupApi'
import type { GroupMemberResponse } from '../../../services/groupApi'

interface JoinGroupBanner {
  inviteCode: string
}

interface UseJoinGroupBannerReturn {
  formData: JoinGroupBanner
  isLoading: boolean
  errorMessage: string
  joinedMember: GroupMemberResponse | null
  handleChange: (e: React.ChangeEvent<HTMLInputElement>) => void
  handleSubmit: (e: React.FormEvent) => void
}

export function useJoinGroupBanner(onSuccess:() => void): UseJoinGroupBannerReturn {

  const [formData, setFormData] = useState<JoinGroupBanner>({
    inviteCode: '',
  })
  const [isLoading, setIsLoading] = useState(false)
  const [errorMessage, setErrorMessage] = useState('')
  const [joinedMember, setJoinedMember] = useState<GroupMemberResponse | null>(null)

  function handleChange(e: React.ChangeEvent<HTMLInputElement>) {
    const { name, value } = e.target
    setFormData((prev) => ({ ...prev, [name]: value }))
  }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    setIsLoading(true)
    setErrorMessage('')

    try {
      const result = await joinGroup({ inviteCode: formData.inviteCode })
      setJoinedMember(result)
      onSuccess()
    } catch (err) {
      setErrorMessage(err instanceof Error ? err.message : 'An unexpected error occurred')
    } finally {
      setIsLoading(false)
    }
  }

  return { formData, isLoading, errorMessage, joinedMember, handleChange, handleSubmit }
}
