import { Navigate, Outlet, useLocation } from "react-router-dom";
import { useAuth } from "./AuthContext.jsx";

export default function PublicOnlyRoute() {
  const { user, loading } = useAuth();
  const location = useLocation();
  if (loading) return <div className="auth-loading" role="status">Checking your session…</div>;
  if (user) {
    const destination = location.state?.from?.pathname ?? "/dashboard";
    return <Navigate to={destination} replace />;
  }
  return <Outlet />;
}
