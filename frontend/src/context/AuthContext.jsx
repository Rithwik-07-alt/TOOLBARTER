import React, { createContext, useContext, useState, useEffect } from 'react';
import { apiClient } from '../api/apiClient';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [token, setToken] = useState(() => localStorage.getItem('toolbarter_token'));
  const [loading, setLoading] = useState(true);
  const [activeRole, setActiveRole] = useState(() => {
    return localStorage.getItem('toolbarter_active_role') || 'BORROWER';
  });

  useEffect(() => {
    async function loadUser() {
      if (token) {
        try {
          const userData = await apiClient.getMe();
          setUser(userData);
          // Set active role if not set or not in user's roles
          const roles = userData.roles || [];
          const hasOwner = roles.includes('ROLE_OWNER');
          const hasBorrower = roles.includes('ROLE_BORROWER');
          if (activeRole === 'OWNER' && !hasOwner && hasBorrower) {
            setActiveRole('BORROWER');
          } else if (activeRole === 'BORROWER' && !hasBorrower && hasOwner) {
            setActiveRole('OWNER');
          }
        } catch (err) {
          console.error('Failed to restore session:', err);
          logout();
        }
      }
      setLoading(false);
    }
    loadUser();
  }, [token]);

  const login = async (email, password) => {
    const data = await apiClient.login({ email, password });
    localStorage.setItem('toolbarter_token', data.token);
    setToken(data.token);
    setUser(data.user);
    if (data.user.roles && data.user.roles.includes('ROLE_OWNER')) {
      switchRole('OWNER');
    } else {
      switchRole('BORROWER');
    }
    return data.user;
  };

  const register = async (userData) => {
    const data = await apiClient.register(userData);
    localStorage.setItem('toolbarter_token', data.token);
    setToken(data.token);
    setUser(data.user);
    switchRole('BORROWER');
    return data.user;
  };

  const logout = () => {
    localStorage.removeItem('toolbarter_token');
    localStorage.removeItem('toolbarter_active_role');
    setToken(null);
    setUser(null);
  };

  const refreshUser = async () => {
    if (token) {
      try {
        const userData = await apiClient.getMe();
        setUser(userData);
      } catch (err) {
        console.error('Failed to refresh user:', err);
      }
    }
  };

  const switchRole = (newRole) => {
    setActiveRole(newRole);
    localStorage.setItem('toolbarter_active_role', newRole);
  };

  const isOwner = user?.roles?.includes('ROLE_OWNER');
  const isBorrower = user?.roles?.includes('ROLE_BORROWER');

  return (
    <AuthContext.Provider
      value={{
        user,
        token,
        loading,
        activeRole,
        isOwner,
        isBorrower,
        login,
        register,
        logout,
        refreshUser,
        switchRole,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}
