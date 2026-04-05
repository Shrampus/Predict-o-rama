import { Route, Routes } from 'react-router-dom';

import { RequireAuth } from '../../components/auth/RequireAuth';
import { MainLayout } from '../../components/layout/MainLayout';
import HomePage from '../../pages/HomePage/HomePage';
import LoginPage from '../../pages/LoginPage/LoginPage';
import PredictionsPage from '../../pages/PredictionsPage/PredictionsPage';
import TournamentPage from '../../pages/TournamentPage/TournamentPage';
import { ROUTE_PATHS } from '../routePaths';

export function AppRouter() {
  return (
    <Routes>
      <Route path={ROUTE_PATHS.login} element={<LoginPage />} />
      <Route
        element={
          <RequireAuth>
            <MainLayout />
          </RequireAuth>
        }
      >
        <Route path={ROUTE_PATHS.home} element={<HomePage />} />
        <Route path={ROUTE_PATHS.predictions} element={<PredictionsPage />} />
        <Route path={ROUTE_PATHS.tournaments} element={<TournamentPage />} />
      </Route>
    </Routes>
  );
}