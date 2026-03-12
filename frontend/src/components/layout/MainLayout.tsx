
import { Outlet } from 'react-router-dom';

export function MainLayout() {
  return (
    <div>
      <header>
        <h1>Predict-o-rama</h1>
      </header>

      <main>
        <Outlet />
      </main>
    </div>
  );
}