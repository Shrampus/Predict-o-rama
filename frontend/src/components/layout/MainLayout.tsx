import { NavLink, Outlet } from 'react-router-dom';

import { ROUTE_PATHS } from '../../app/routePaths';

export function MainLayout() {
  return (
    <div>
      <header>
        <div className="mx-auto flex max-w-5xl flex-wrap items-center justify-between gap-3 px-4 py-3">
          <h1 className="text-lg font-semibold">Predict-o-rama</h1>

          <nav aria-label="Primary" className="flex items-center gap-2">
            <NavLink
              to={ROUTE_PATHS.home}
              end
              className={({ isActive }) =>
                `nav-link ${isActive ? 'nav-link-active' : 'nav-link-default'}`
              }
            >
              Home
            </NavLink>

            <NavLink
              to={ROUTE_PATHS.predictions}
              className={({ isActive }) =>
                `nav-link ${isActive ? 'nav-link-active' : 'nav-link-default'}`
              }
            >
              Predictions
            </NavLink>

            <NavLink
              to={ROUTE_PATHS.tournaments}
              className={({ isActive }) =>
                `nav-link ${isActive ? 'nav-link-active' : 'nav-link-default'}`
              }
            >
              Tournaments
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