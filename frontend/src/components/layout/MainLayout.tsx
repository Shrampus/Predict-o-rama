
import { NavLink, Outlet } from 'react-router-dom';

export function MainLayout() {
  return (
    <div>
      <header>
        <div className="mx-auto flex max-w-5xl flex-wrap items-center justify-between gap-3 px-4 py-3">
          <h1 className="text-lg font-semibold">Predict-o-rama</h1>

          <nav aria-label="Primary" className="flex items-center gap-2">
            <NavLink
              to="/"
              end
              className={({ isActive }) =>
                `nav-link ${isActive ? 'nav-link-active' : 'nav-link-default'}`
              }
            >
              Home
            </NavLink>

            <NavLink
              to="/Predictions"
              className={({ isActive }) =>
                `nav-link ${isActive ? 'nav-link-active' : 'nav-link-default'}`
              }
            >
              Predictions
            </NavLink>
          </nav>
        </div>
      </header>

      <main>
        <Outlet />
      </main>
    </div>
  );
}