import axios from 'axios';
import { ENDPOINTS } from './config';

const axiosInstance = axios.create({
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor to attach JWT token
axiosInstance.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('toolbarter_token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Response interceptor to format error responses
axiosInstance.interceptors.response.use(
  (response) => response.data,
  (error) => {
    const responseData = error.response ? error.response.data : null;
    let message = 'An error occurred. Please try again.';

    if (responseData) {
      if (typeof responseData === 'string') {
        message = responseData;
      } else if (responseData.message) {
        message = responseData.message;
      } else if (responseData.error) {
        message = responseData.error;
      } else if (responseData.errors) {
        // Collect field validation errors
        message = Object.values(responseData.errors).join(', ');
      }
    } else if (error.message) {
      message = error.message;
    }

    const customError = new Error(message);
    customError.status = error.response ? error.response.status : 500;
    customError.data = responseData;
    return Promise.reject(customError);
  }
);

export const apiClient = {
  get: (url, config) => axiosInstance.get(url, config),
  post: (url, data, config) => axiosInstance.post(url, data, config),
  put: (url, data, config) => axiosInstance.put(url, data, config),
  delete: (url, config) => axiosInstance.delete(url, config),

  // Health
  checkHealth: () => axiosInstance.get(ENDPOINTS.HEALTH),

  // Auth
  register: (userData) => axiosInstance.post(ENDPOINTS.AUTH.REGISTER, userData),
  login: (credentials) => axiosInstance.post(ENDPOINTS.AUTH.LOGIN, credentials),
  getMe: () => axiosInstance.get(ENDPOINTS.AUTH.ME),

  // Items
  getItems: (params) => axiosInstance.get(ENDPOINTS.ITEMS.LIST, { params }),
  getNearbyItems: (lat, lng, radius) =>
    axiosInstance.get(ENDPOINTS.ITEMS.NEARBY, { params: { lat, lng, radius } }),
  getItemById: (id, params) => axiosInstance.get(ENDPOINTS.ITEMS.DETAIL(id), { params }),
  createItem: (itemData) => axiosInstance.post(ENDPOINTS.ITEMS.CREATE, itemData),
  updateItem: (id, itemData) => axiosInstance.put(ENDPOINTS.ITEMS.UPDATE(id), itemData),
  deleteItem: (id) => axiosInstance.delete(ENDPOINTS.ITEMS.DELETE(id)),
  getMyItems: () => axiosInstance.get(ENDPOINTS.ITEMS.MY_ITEMS),
  toggleFavorite: (id) => axiosInstance.post(ENDPOINTS.ITEMS.FAVORITE_TOGGLE(id)),
  getFavorites: () => axiosInstance.get(ENDPOINTS.ITEMS.FAVORITES),

  // Slots
  createSlot: (itemId, slotData) => axiosInstance.post(ENDPOINTS.SLOTS.CREATE(itemId), slotData),
  getSlots: (itemId, openOnly = false) =>
    axiosInstance.get(ENDPOINTS.SLOTS.LIST(itemId), { params: { openOnly } }),
  deleteSlot: (slotId) => axiosInstance.delete(ENDPOINTS.SLOTS.DELETE(slotId)),

  // Requests
  createBorrowRequest: (itemId, requestData) =>
    axiosInstance.post(ENDPOINTS.ITEMS.REQUESTS(itemId), requestData),
  getBorrowerRequests: () => axiosInstance.get(ENDPOINTS.REQUESTS.BORROWER_LIST),
  getOwnerRequests: () => axiosInstance.get(ENDPOINTS.REQUESTS.OWNER_LIST),
  getRequestById: (id) => axiosInstance.get(ENDPOINTS.REQUESTS.DETAIL(id)),
  approveRequest: (id) => axiosInstance.put(ENDPOINTS.REQUESTS.APPROVE(id)),
  declineRequest: (id) => axiosInstance.put(ENDPOINTS.REQUESTS.DECLINE(id)),
  cancelRequest: (id) => axiosInstance.put(ENDPOINTS.REQUESTS.CANCEL(id)),
  confirmPickup: (id) => axiosInstance.put(ENDPOINTS.REQUESTS.PICKUP(id)),
  confirmReturn: (id) => axiosInstance.put(ENDPOINTS.REQUESTS.RETURN(id)),

  // Reviews
  submitReview: (requestId, reviewData) =>
    axiosInstance.post(ENDPOINTS.REQUESTS.REVIEW(requestId), reviewData),

  // User
  getUserProfile: (id) => axiosInstance.get(ENDPOINTS.USERS.PROFILE(id)),
  updateUserProfile: (id, data) => axiosInstance.put(ENDPOINTS.USERS.UPDATE(id), data),
  getUserStatistics: (id) => axiosInstance.get(ENDPOINTS.USERS.STATISTICS(id)),
  getUserReviews: (id) => axiosInstance.get(ENDPOINTS.USERS.REVIEWS(id)),

  // Stats
  getNeighbourhoodImpact: () => axiosInstance.get(ENDPOINTS.STATS.SUMMARY),
};

export default apiClient;
