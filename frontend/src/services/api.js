import axios from 'axios';

// Default config for the axios instance
export const api = axios.create({
  // Use Vite's same-origin proxy locally. Set VITE_API_BASE_URL when the API
  // is hosted separately (for example, https://api.example.com/api/v1).
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api/v1',
  headers: {
    'Content-Type': 'application/json',
  },
});

// Add a request interceptor to automatically attach the token
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);
