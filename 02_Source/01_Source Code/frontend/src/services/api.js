import axios from 'axios';
import keycloak from '../auth/keycloak';

const API_BASE = import.meta.env.VITE_API_BASE || 'https://project-se-24rr.onrender.com/api/v1';

const api = axios.create({
  baseURL: API_BASE,
  headers: { 'Content-Type': 'application/json' },
});

// Request interceptor — attach token
api.interceptors.request.use((config) => {
  if (keycloak.token) {
    config.headers.Authorization = `Bearer ${keycloak.token}`;
  }
  return config;
});

// Response interceptor — refresh token on 401
api.interceptors.response.use(
  (res) => res,
  async (err) => {
    if (err.response?.status === 401) {
      if (keycloak.authenticated) {
        try {
          await keycloak.updateToken(30);
          err.config.headers.Authorization = `Bearer ${keycloak.token}`;
          return api(err.config);
        } catch {
          // token refresh failed, reject
        }
      }
      return Promise.reject(err);
    }
    return Promise.reject(err);
  }
);

// ─── Materials ────────────────────────────────────────────────────────────────
export const materialApi = {
  getAll: (params = {}) => api.get('/materials', { params }),
  getById: (id) => api.get(`/materials/${id}`),
  create: (data) => api.post('/materials', data),
  update: (id, data) => api.put(`/materials/${id}`, data),
  delete: (id) => api.delete(`/materials/${id}`),
};

// ─── Inventory Lots ───────────────────────────────────────────────────────────
export const lotApi = {
  getAll: (params = {}) => api.get('/lots', { params }),
  getById: (id) => api.get(`/lots/${id}`),
  receive: (data) => api.post('/lots/receive', data),
  updateStatus: (id, status, performedBy = '') =>
    api.patch(`/lots/${id}/status`, { status, performedBy }),
  getTransactions: (id) => api.get(`/lots/${id}/transactions`),
};

// ─── QC Tests ─────────────────────────────────────────────────────────────────
export const qcTestApi = {
  getByLot: (lotId) => api.get('/qctests', { params: { lotId } }),
  getSummary: (lotId) => api.get('/qctests/summary', { params: { lotId } }),
  getById: (id) => api.get(`/qctests/${id}`),
  create: (data) => api.post('/qctests', data),
  update: (id, data) => api.put(`/qctests/${id}`, data),
};

// ─── Production Batches ───────────────────────────────────────────────────────
export const batchApi = {
  getAll: (params = {}) => api.get('/batches', { params }),
  getById: (id) => api.get(`/batches/${id}`),
  create: (data) => api.post('/batches', data),
  updateStatus: (id, status) => api.patch(`/batches/${id}/status`, { status }),
  addComponent: (id, data) => api.post(`/batches/${id}/components`, data),
  updateComponent: (componentId, data) => api.patch(`/batches/components/${componentId}`, data),
  confirmComponent: (componentId, actualQuantity, performedBy) =>
    api.patch(`/batches/components/${componentId}/confirm`, { actualQuantity, performedBy }),
  deleteComponent: (componentId) => api.delete(`/batches/components/${componentId}`),
};

// ─── Labels ───────────────────────────────────────────────────────────────────
export const labelApi = {
  getTemplates: () => api.get('/labels/templates'),
  getTemplateById: (id) => api.get(`/labels/templates/${id}`),
  createTemplate: (data) => api.post('/labels/templates', data),
  updateTemplate: (id, data) => api.put(`/labels/templates/${id}`, data),
  deleteTemplate: (id) => api.delete(`/labels/templates/${id}`),
  generate: (data) => api.post('/labels/generate', data),
};

// ─── Reports ──────────────────────────────────────────────────────────────────
export const reportApi = {
  getDashboard: () => api.get('/reports/dashboard'),
  getNearExpiry: (params = {}) => api.get('/reports/near-expiry', { params }),
  getTrace: (lotId) => api.get(`/reports/lots/${lotId}/trace`),
  getQCReport: (params = {}) => api.get('/reports/qc', { params }),
  getInventory: () => api.get('/reports/inventory'),
};

// ─── Users ────────────────────────────────────────────────────────────────────
export const userApi = {
  getAll: (params = {}) => api.get('/users', { params }),
  getById: (id) => api.get(`/users/${id}`),
  create: (data) => api.post('/users', data),
  update: (id, data) => api.put(`/users/${id}`, data),
  changePassword: (id, newPassword) => api.patch(`/users/${id}/password`, { newPassword }),
  deactivate: (id) => api.delete(`/users/${id}`),
};

export default api;
