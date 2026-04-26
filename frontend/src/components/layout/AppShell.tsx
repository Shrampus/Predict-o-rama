import { Outlet } from 'react-router-dom';

import { AppNavbar } from './AppNavbar';

export function AppShell() {
  return (
    <div className="min-h-screen bg-gray-50 text-gray-900">
      <AppNavbar />
      <main>
        <Outlet />
      </main>
    </div>
  );
}
