import { Routes, Route } from 'react-router-dom';
import { useKeycloak } from '@react-keycloak/web';
import Home from './pages/HomePage';
import ToolInventoryPage from './pages/ToolInventoryPage';
import LoanPage from './pages/LoanPage';
import CustomerManagementPage from './pages/CustomerManagementPage';
import ToolUnitaryList from './components/List/ToolUnitaryList';
import KardexPage from './pages/KardexPage';
import MoveUnitaryList from './components/List/MoveUnitaryList'
import './App.css';
import MainLayout from './components/SideMenu';
import DeactivateUnusedTool from './components/Form/DeactivateUnusedToolForm';
import UpdateTypeToolForm from './components/Form/UpdateTypeToolForm';
import DebtList from './components/List/DebtList';
import NotFoundPage from './pages/NotFoundPage';
import { useEffect } from 'react';


function App() {
  const { keycloak, initialized } = useKeycloak();
  useEffect(() => {
    try {
      // Inyectamos el script de Clarity manualmente para poder detectar si fue bloqueado
      const script = document.createElement('script');
      script.src = 'https://www.clarity.ms/tag/j788054821';
      script.async = true;
      script.onload = () => {
        // El script cargó correctamente, Clarity ya se inicializa automáticamente
        console.log('Microsoft Clarity cargado correctamente.');
      };
      script.onerror = () => {
        // El script fue bloqueado por el navegador (ej: protección de seguimiento)
        console.warn('Microsoft Clarity bloqueado por el navegador. Las métricas no estarán disponibles.');
      };
      document.head.appendChild(script);
    } catch (error) {
      console.warn('Error al intentar cargar Clarity:', error);
    }
  }, []);
  if (!initialized) return <div>Cargando...</div>;

  const isLoggedIn = keycloak.authenticated;
  const roles = keycloak.tokenParsed?.realm_access?.roles || [];

  const PrivateRoute = ({ children, rolesAllowed }) => {
    if (!isLoggedIn) {
      keycloak.login();
      return null;
    }
    const tieneRol = rolesAllowed.some(r => roles.includes(r));
    if (rolesAllowed && !tieneRol) {
      return <h2>No tienes permiso para ver esta página</h2>;
    }
    return children;
  };

  if (!isLoggedIn) {
    keycloak.login();
    return null;
  }

  return (

    <Routes>
      <Route path="/" element={<MainLayout />}>

        <Route index element={<Home />} />

        <Route
          path="CustomerManagementPage"
          element={
            <PrivateRoute rolesAllowed={["USER", "ADMIN"]} >
              <CustomerManagementPage />
            </PrivateRoute>
          }
        />

        <Route
          path="ToolInventoryPage"
          element={
            <PrivateRoute rolesAllowed={['ADMIN']}>
              <ToolInventoryPage />
            </PrivateRoute>
          }
        />

        {/* Ruta LoanPage */}
        <Route
          path="LoanPage"
          element={
            <PrivateRoute rolesAllowed={["USER", "ADMIN"]} >
              <LoanPage />
            </PrivateRoute>
          }
        />

        <Route
          path="typeTool/:id"
          element={
            <PrivateRoute rolesAllowed={['ADMIN']}>
              <ToolUnitaryList />
            </PrivateRoute>
          }
        />

        <Route
          path="Kardex"
          element={
            <PrivateRoute rolesAllowed={['ADMIN']}>
              <KardexPage />
            </PrivateRoute>
          }
        />

        <Route
          path="DeacticateUnusedTool/:id"
          element={
            <PrivateRoute rolesAllowed={['ADMIN']}>
              <DeactivateUnusedTool />
            </PrivateRoute>
          }
        />

        <Route
          path="MovementsTool/:id"
          element={
            <PrivateRoute rolesAllowed={['ADMIN']}>
              <MoveUnitaryList />
            </PrivateRoute>
          }
        />

        <Route
          path="UpdateTypeTool/:id"
          element={
            <PrivateRoute rolesAllowed={['ADMIN']}>
              <UpdateTypeToolForm />
            </PrivateRoute>
          }
        />

        <Route
          path="DebtList/:id"
          element={
            <PrivateRoute rolesAllowed={['ADMIN']}>
              <DebtList />
            </PrivateRoute>
          }
        />

        <Route path="*" element={<NotFoundPage />} />

      </Route>
    </Routes>
  );
}

export default App;