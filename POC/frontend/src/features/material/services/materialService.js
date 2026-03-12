import axios from 'axios';
import { API_BASE_URL } from '../../../utils/apiConfig';

const API_URL = `${API_BASE_URL}/materials`;

export const materialService = {
    /**
     * Create new material
     * @param {Object} materialData - Material data to create
     * @returns {Promise} Response with created material
     */
    createMaterial: async (materialData) => {
        return axios.post(API_URL, materialData);
    },
    
    /**
     * Get material by ID
     * @param {string} materialId - ID of the material
     * @returns {Promise} Response with material details
     */
    getMaterialById: async (materialId) => {
        return axios.get(`${API_URL}/${materialId}`);
    },
    
    /**
     * Get all materials
     * @returns {Promise} Response with array of materials
     */
    getAllMaterials: async () => {
        return axios.get(API_URL);
    },
    
    /**
     * Get materials filtered by type
     * @param {string} type - Material type filter
     * @returns {Promise} Response with filtered materials
     */
    getMaterialsByType: async (type) => {
        return axios.get(API_URL, { params: { type } });
    },
    
    /**
     * Search materials by name
     * @param {string} searchTerm - Search term
     * @returns {Promise} Response with matching materials
     */
    searchMaterials: async (searchTerm) => {
        return axios.get(API_URL, { params: { search: searchTerm } });
    },
    
    /**
     * Update material
     * @param {string} materialId - ID of material to update
     * @param {Object} materialData - Updated material data
     * @returns {Promise} Response with updated material
     */
    updateMaterial: async (materialId, materialData) => {
        return axios.put(`${API_URL}/${materialId}`, materialData);
    },
    
    /**
     * Delete material
     * @param {string} materialId - ID of material to delete
     * @returns {Promise} Response from delete operation
     */
    deleteMaterial: async (materialId) => {
        return axios.delete(`${API_URL}/${materialId}`);
    }
};
