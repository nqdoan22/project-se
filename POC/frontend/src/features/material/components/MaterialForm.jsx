import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { materialService } from '../services/materialService';

const MATERIAL_TYPES = [
    'API',
    'EXCIPIENT',
    'DIETARY_SUPPLEMENT',
    'CONTAINER',
    'CLOSURE',
    'PROCESS_CHEMICAL',
    'TESTING_MATERIAL'
];

/**
 * MaterialForm Component
 * Handles both creation and editing of materials
 * Features:
 *   - Form validation
 *   - Error handling
 *   - Auto-load existing material data for edits
 *   - Material type selection dropdown
 *   - Submit and cancel buttons
 * 
 * @param {string} materialId - Optional material ID for editing
 * @param {function} onSuccess - Optional callback on successful submission
 */
export const MaterialForm = ({ materialId = null, onSuccess = null }) => {
    const navigate = useNavigate();
    const [formData, setFormData] = useState({
        materialId: '',
        partNumber: '',
        materialName: '',
        materialType: '',
        storageConditions: '',
        specificationDocument: ''
    });
    const [errors, setErrors] = useState({});
    const [error, setError] = useState(null);
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [isLoading, setIsLoading] = useState(false);
    
    // Load existing material if editing
    useEffect(() => {
        if (materialId) {
            setIsLoading(true);
            materialService.getMaterialById(materialId)
                .then(response => {
                    setFormData(response.data);
                    setError(null);
                })
                .catch(err => setError(err.message))
                .finally(() => setIsLoading(false));
        }
    }, [materialId]);
    
    /**
     * Validate form data
     */
    const validateForm = () => {
        const newErrors = {};
        
        if (!formData.materialId.trim()) {
            newErrors.materialId = 'Material ID is required';
        }
        if (!formData.partNumber.trim()) {
            newErrors.partNumber = 'Part number is required';
        }
        if (!formData.materialName.trim()) {
            newErrors.materialName = 'Material name is required';
        }
        if (!formData.materialType) {
            newErrors.materialType = 'Material type is required';
        }
        
        setErrors(newErrors);
        return Object.keys(newErrors).length === 0;
    };
    
    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({
            ...prev,
            [name]: value
        }));
        // Clear error for this field when user starts typing
        if (errors[name]) {
            setErrors(prev => ({
                ...prev,
                [name]: ''
            }));
        }
    };
    
    const handleSubmit = async (e) => {
        e.preventDefault();
        
        if (!validateForm()) {
            return;
        }
        
        setError(null);
        setIsSubmitting(true);
        
        try {
            if (materialId) {
                await materialService.updateMaterial(materialId, formData);
            } else {
                await materialService.createMaterial(formData);
            }
            
            if (onSuccess) {
                onSuccess();
            } else {
                navigate('/materials');
            }
        } catch (err) {
            setError(err.response?.data?.message || err.message);
        } finally {
            setIsSubmitting(false);
        }
    };
    
    if (isLoading) {
        return <div className="alert">Loading material...</div>;
    }
    
    return (
        <form onSubmit={handleSubmit} className="max-w-2xl">
            {error && <div className="alert alert-error mb-4">{error}</div>}
            
            <div className="form-group mb-4">
                <label htmlFor="materialId" className="form-label">Material ID *</label>
                <input
                    type="text"
                    id="materialId"
                    name="materialId"
                    value={formData.materialId}
                    onChange={handleChange}
                    disabled={!!materialId}
                    required
                    className={`form-input ${errors.materialId ? 'input-error' : ''}`}
                    placeholder="e.g., MAT-001"
                />
                {errors.materialId && <p className="text-error text-sm mt-1">{errors.materialId}</p>}
            </div>
            
            <div className="form-group mb-4">
                <label htmlFor="partNumber" className="form-label">Part Number *</label>
                <input
                    type="text"
                    id="partNumber"
                    name="partNumber"
                    value={formData.partNumber}
                    onChange={handleChange}
                    required
                    className={`form-input ${errors.partNumber ? 'input-error' : ''}`}
                    placeholder="e.g., PN-12345"
                />
                {errors.partNumber && <p className="text-error text-sm mt-1">{errors.partNumber}</p>}
            </div>
            
            <div className="form-group mb-4">
                <label htmlFor="materialName" className="form-label">Material Name *</label>
                <input
                    type="text"
                    id="materialName"
                    name="materialName"
                    value={formData.materialName}
                    onChange={handleChange}
                    required
                    className={`form-input ${errors.materialName ? 'input-error' : ''}`}
                    placeholder="e.g., Vitamin D3 100K"
                />
                {errors.materialName && <p className="text-error text-sm mt-1">{errors.materialName}</p>}
            </div>
            
            <div className="form-group mb-4">
                <label htmlFor="materialType" className="form-label">Material Type *</label>
                <select
                    id="materialType"
                    name="materialType"
                    value={formData.materialType}
                    onChange={handleChange}
                    required
                    className={`form-select ${errors.materialType ? 'select-error' : ''}`}
                >
                    <option value="">Select a type</option>
                    {MATERIAL_TYPES.map(type => (
                        <option key={type} value={type}>{type}</option>
                    ))}
                </select>
                {errors.materialType && <p className="text-error text-sm mt-1">{errors.materialType}</p>}
            </div>
            
            <div className="form-group mb-4">
                <label htmlFor="storageConditions" className="form-label">Storage Conditions</label>
                <input
                    type="text"
                    id="storageConditions"
                    name="storageConditions"
                    value={formData.storageConditions}
                    onChange={handleChange}
                    className="form-input"
                    placeholder="e.g., 2-8°C, protected from light"
                />
            </div>
            
            <div className="form-group mb-4">
                <label htmlFor="specificationDocument" className="form-label">Specification Document</label>
                <input
                    type="text"
                    id="specificationDocument"
                    name="specificationDocument"
                    value={formData.specificationDocument}
                    onChange={handleChange}
                    className="form-input"
                    placeholder="e.g., DOC-2025-001"
                />
            </div>
            
            <div className="flex gap-2 mt-6">
                <button
                    type="submit"
                    disabled={isSubmitting}
                    className="btn btn-primary"
                >
                    {isSubmitting ? 'Saving...' : (materialId ? 'Update Material' : 'Create Material')}
                </button>
                <button
                    type="button"
                    onClick={() => navigate('/materials')}
                    className="btn btn-secondary"
                >
                    Cancel
                </button>
            </div>
        </form>
    );
};
