import { Route, Routes } from 'react-router-dom';

import { MainLayout } from '../../components/layout/MainLayout';
import HomePage from '../../pages/HomePage/HomePage';
import PredictionsPage from '../../pages/PredictionsPage/PredictionsPage';

export function AppRouter() {
  return (
    <Routes>
      <Route element={<MainLayout />}>
        <Route path="/" element={<HomePage />} />
        <Route path="/Predictions" element={<PredictionsPage />} />
      </Route>
    </Routes>
  );
}