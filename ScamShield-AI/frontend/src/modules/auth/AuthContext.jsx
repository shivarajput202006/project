import { createContext, useCallback, useContext, useEffect, useMemo, useState } from "react";
import { authService } from "./authService.js";

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const [initializationError, setInitializationError] = useState("");

  useEffect(() => {
    let active = true;
    async function initialize() {
      try {
        await authService.initialize();
        const currentUser = await authService.getCurrentUser();
        if (active) setUser(currentUser);
      } catch {
        if (active) setInitializationError("Unable to connect to the authentication service.");
      } finally {
        if (active) setLoading(false);
      }
    }
    initialize();
    return () => { active = false; };
  }, []);

  const login = useCallback(async (email, password) => {
    const result = await authService.login(email, password);
    setUser(result.user);
    return result.user;
  }, []);

  const register = useCallback(async (fullName, email, password, confirmPassword) => {
    const result = await authService.register(fullName, email, password, confirmPassword);
    setUser(result.user);
    return result.user;
  }, []);

  const logout = useCallback(async () => {
    await authService.logout();
    setUser(null);
  }, []);

  const refreshUser = useCallback(async () => {
    const currentUser = await authService.getCurrentUser();
    setUser(currentUser);
    return currentUser;
  }, []);

  const invalidateSession = useCallback(() => {
    setUser(null);
  }, []);

  const value = useMemo(
    () => ({ user, loading, initializationError, login, register, logout, refreshUser, invalidateSession }),
    [user, loading, initializationError, login, register, logout, refreshUser, invalidateSession],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) throw new Error("useAuth must be used inside AuthProvider.");
  return context;
}
