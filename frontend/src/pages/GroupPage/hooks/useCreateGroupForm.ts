import { useState } from 'react';

import { createGroup } from '../../../services/groupApi';
import type { GroupResponse } from '../../../services/groupApi';

interface CreateGroupForm {
  name: string;
  description: string;
}

interface UseCreateGroupReturn {
  formData: CreateGroupForm;
  isLoading: boolean;
  errorMessage: string;
  isFormOpen: boolean;
  createdGroup: GroupResponse | null;
  toggleForm: () => void;
  handleChange: (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => void;
  handleSubmit: (e: React.FormEvent) => void;
}

export function useCreateGroupForm(onSuccess: () => void): UseCreateGroupReturn {

  const [formData, setFormData] = useState<CreateGroupForm>({
    name: '',
    description: '',
  });
  const [isLoading, setIsLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');
  const [isFormOpen, setIsFormOpen] = useState(false);
  const [createdGroup, setCreatedGroup] = useState<GroupResponse | null>(null);

  function toggleForm() {
    setIsFormOpen((prev) => !prev)
  }

  function handleChange(e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setIsLoading(true);
    setErrorMessage('');

    try {
      const result = await createGroup(formData);
      setCreatedGroup(result);
      setFormData({ name: '', description: '' });
      setIsFormOpen(false);
      onSuccess();
    } catch (err) {
      setErrorMessage(err instanceof Error ? err.message : 'An unexpected error occurred');
    } finally {
      setIsLoading(false);

    }
  }

  return { formData, isLoading, errorMessage, isFormOpen, createdGroup, toggleForm, handleChange, handleSubmit };
}
