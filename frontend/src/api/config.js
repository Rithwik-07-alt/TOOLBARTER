/**
 * Centralized API endpoints for TOOLBARTER.
 */
const rawBase = (import.meta.env.VITE_API_BASE_URL || '/api').trim().replace(/\/+$/, '');
export const API_BASE_URL = rawBase.endsWith('/api') ? rawBase : `${rawBase}/api`;

export const ENDPOINTS = {
  HEALTH: `${API_BASE_URL}/health`,
  AUTH: {
    REGISTER: `${API_BASE_URL}/auth/register`,
    LOGIN: `${API_BASE_URL}/auth/login`,
    ME: `${API_BASE_URL}/auth/me`,
  },
  ITEMS: {
    LIST: `${API_BASE_URL}/items`,
    NEARBY: `${API_BASE_URL}/items/nearby`,
    DETAIL: (id) => `${API_BASE_URL}/items/${id}`,
    CREATE: `${API_BASE_URL}/items`,
    UPDATE: (id) => `${API_BASE_URL}/items/${id}`,
    DELETE: (id) => `${API_BASE_URL}/items/${id}`,
    MY_ITEMS: `${API_BASE_URL}/items/my-items`,
    FAVORITES: `${API_BASE_URL}/items/favorites`,
    FAVORITE_TOGGLE: (id) => `${API_BASE_URL}/items/${id}/favorite`,
    SLOTS: (itemId) => `${API_BASE_URL}/items/${itemId}/slots`,
    REQUESTS: (itemId) => `${API_BASE_URL}/items/${itemId}/requests`,
  },
  SLOTS: {
    CREATE: (itemId) => `${API_BASE_URL}/items/${itemId}/slots`,
    LIST: (itemId) => `${API_BASE_URL}/items/${itemId}/slots`,
    DELETE: (slotId) => `${API_BASE_URL}/slots/${slotId}`,
  },
  REQUESTS: {
    BORROWER_LIST: `${API_BASE_URL}/requests/borrower`,
    OWNER_LIST: `${API_BASE_URL}/requests/owner`,
    DETAIL: (id) => `${API_BASE_URL}/requests/${id}`,
    APPROVE: (id) => `${API_BASE_URL}/requests/${id}/approve`,
    DECLINE: (id) => `${API_BASE_URL}/requests/${id}/decline`,
    CANCEL: (id) => `${API_BASE_URL}/requests/${id}/cancel`,
    PICKUP: (id) => `${API_BASE_URL}/requests/${id}/pickup`,
    RETURN: (id) => `${API_BASE_URL}/requests/${id}/return`,
    REVIEW: (id) => `${API_BASE_URL}/requests/${id}/review`,
  },
  USERS: {
    PROFILE: (id) => `${API_BASE_URL}/users/${id}`,
    UPDATE: (id) => `${API_BASE_URL}/users/${id}`,
    STATISTICS: (id) => `${API_BASE_URL}/users/${id}/statistics`,
    REVIEWS: (id) => `${API_BASE_URL}/users/${id}/reviews`,
  },
  STATS: {
    SUMMARY: `${API_BASE_URL}/stats/summary`,
  },
};
