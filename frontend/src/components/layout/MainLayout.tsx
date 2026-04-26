import { Outlet } from 'react-router-dom';

export function MainLayout() {
  return (
    <div className="mx-auto max-w-5xl px-4 py-6">
      <Outlet />
    </div>
  );
}
