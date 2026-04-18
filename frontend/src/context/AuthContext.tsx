import { createContext, useContext, useState, useEffect, ReactNode } from 'react';
import { apiClient } from '../services/apiClient';

// ── Types ──────────────────────────────────────────────────────────────────────

export interface AuthUser {
  username: string;
  email: string;
}

interface RegisterData {
  username: string;
  email: string;
  password: string;
}

interface LoginData {
  username: string;
  password: string;
}

interface AuthContextType {
  user:      AuthUser | null;
  isLoading: boolean;
  login:     (data: LoginData)    => Promise<void>;
  register:  (data: RegisterData) => Promise<void>;
  logout:    ()                   => Promise<void>;
}

// ── Context ────────────────────────────────────────────────────────────────────

export const AuthContext = createContext<AuthContextType | null>(null);

// ── Provider ───────────────────────────────────────────────────────────────────

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser]           = useState<AuthUser | null>(null);
  const [isLoading, setIsLoading] = useState(true);   // true while checking /me on mount

  // Re-hydrate session from the HttpOnly cookie on page load / refresh
  useEffect(() => {
    apiClient
      .get<AuthUser>('/api/auth/me')
      .then(setUser)
      .catch(() => setUser(null))
      .finally(() => setIsLoading(false));
  }, []);

  const register = async (data: RegisterData) => {
    const res = await apiClient.post<AuthUser>('/api/auth/register', data);
    setUser(res);
  };

  const login = async (data: LoginData) => {
    const res = await apiClient.post<AuthUser>('/api/auth/login', data);
    setUser(res);
  };

  const logout = async () => {
    await apiClient.post<void>('/api/auth/logout', {});
    setUser(null);
  };

  return (
    <AuthContext.Provider value={{ user, isLoading, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

// ── Hook ───────────────────────────────────────────────────────────────────────

export function useAuth(): AuthContextType {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used inside <AuthProvider>');
  return ctx;
}
