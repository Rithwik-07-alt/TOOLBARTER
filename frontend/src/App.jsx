import { BrowserRouter, Routes, Route, Navigate, Outlet } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import Navbar from './components/Navbar';
import Footer from './components/Footer';
import LandingPage from './pages/LandingPage';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import DiscoverPage from './pages/DiscoverPage';
import ItemDetailsPage from './pages/ItemDetailsPage';
import ItemFormPage from './pages/ItemFormPage';
import BorrowerDashboard from './pages/BorrowerDashboard';
import OwnerDashboard from './pages/OwnerDashboard';
import ManageItemsPage from './pages/ManageItemsPage';
import ProfilePage from './pages/ProfilePage';

function ProtectedRoute() {
  const { user } = useAuth();
  if (!user) return <Navigate to="/login" replace />;
  return <Outlet />;
}

function AppLayout() {
  return (
    <div className="app-layout">
      <Navbar />
      <main className="main-content">
        <Routes>
          {/* Public routes */}
          <Route path="/" element={<LandingPage />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />
          <Route path="/discover" element={<DiscoverPage />} />
          <Route path="/items/:id" element={<ItemDetailsPage />} />

          {/* Protected routes */}
          <Route element={<ProtectedRoute />}>
            <Route path="/items/new" element={<ItemFormPage />} />
            <Route path="/items/:id/edit" element={<ItemFormPage />} />
            <Route path="/borrower/dashboard" element={<BorrowerDashboard />} />
            <Route path="/owner/dashboard" element={<OwnerDashboard />} />
            <Route path="/manage-items" element={<ManageItemsPage />} />
            <Route path="/profile" element={<ProfilePage />} />
          </Route>

          {/* Catch-all */}
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </main>
      <Footer />
    </div>
  );
}

export default function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <AppLayout />
      </AuthProvider>
    </BrowserRouter>
  );
}
