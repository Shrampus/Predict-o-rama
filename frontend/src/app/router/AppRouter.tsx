import { Route, Routes } from 'react-router-dom';

import { MainLayout } from '../../components/layout/MainLayout';
import HomePage from '../../pages/HomePage/HomePage';
import PredictionsPage from '../../pages/PredictionsPage/PredictionsPage';
import TournamentPage from '../../pages/TournamentPage/TournamentPage';
import { ROUTE_PATHS } from '../routePaths';

export function AppRouter() {
  return (
    <Routes>
      <Route element={<MainLayout />}>
        <Route path={ROUTE_PATHS.home} element={<HomePage />} />
        <Route path={ROUTE_PATHS.predictions} element={<PredictionsPage />} />
        <Route path={ROUTE_PATHS.tournaments} element={<TournamentPage />} />
      </Route>
    </Routes>
  );
}