import { useState, useEffect } from 'react';
import { materialService } from '../services/materialService';

/**
 * Hook to fetch all materials
 * @returns {Object} { materials, loading, error }
 */
export const useMaterials = () => {
    const [materials, setMaterials] = useState([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    
    useEffect(() => {
        const fetchMaterials = async () => {
            setLoading(true);
            try {
                const response = await materialService.getAllMaterials();
                setMaterials(response.data);
                setError(null);
            } catch (err) {
                setError(err.message);
            } finally {
                setLoading(false);
            }
        };
        
        fetchMaterials();
    }, []);
    
    return { materials, loading, error };
};

/**
 * Hook to fetch a single material by ID
 * @param {string} materialId - ID of material to fetch
 * @returns {Object} { material, loading, error }
 */
export const useMaterialById = (materialId) => {
    const [material, setMaterial] = useState(null);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    
    useEffect(() => {
        if (!materialId) return;
        
        const fetchMaterial = async () => {
            setLoading(true);
            try {
                const response = await materialService.getMaterialById(materialId);
                setMaterial(response.data);
                setError(null);
            } catch (err) {
                setError(err.message);
            } finally {
                setLoading(false);
            }
        };
        
        fetchMaterial();
    }, [materialId]);
    
    return { material, loading, error };
};
