import React from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useMaterialById } from '../hooks/useMaterials';
import { materialService } from '../services/materialService';

/**
 * MaterialDetail Component
 * Displays detailed information about a single material
 * Features:
 *   - Display all material information
 *   - Navigate to edit page
 *   - Delete material with confirmation
 *   - Back navigation
 */
export const MaterialDetail = () => {
    const { id } = useParams();
    const navigate = useNavigate();
    const { material, loading, error } = useMaterialById(id);
    
    const handleDelete = async () => {
        if (window.confirm('Are you sure you want to delete this material?')) {
            try {
                await materialService.deleteMaterial(id);
                navigate('/materials');
            } catch (err) {
                alert('Error deleting material: ' + err.message);
            }
        }
    };
    
    if (loading) return <div className="alert">Loading...</div>;
    if (error) return <div className="alert alert-error">{error}</div>;
    if (!material) return <div className="alert alert-warning">Material not found</div>;
    
    return (
        <div className="material-detail">
            <div className="card">
                <div className="flex justify-between items-start mb-6">
                    <div>
                        <h2 className="text-3xl font-bold">{material.materialName}</h2>
                        <p className="text-gray-500 text-lg">ID: {material.materialId}</p>
                    </div>
                    <button
                        onClick={() => navigate(`/materials/${id}/edit`)}
                        className="btn btn-primary"
                    >
                        Edit
                    </button>
                </div>
                
                <div className="grid grid-cols-2 gap-6">
                    <div className="card-body">
                        <p className="font-semibold text-lg">Part Number</p>
                        <p className="text-base">{material.partNumber}</p>
                    </div>
                    <div className="card-body">
                        <p className="font-semibold text-lg">Type</p>
                        <p>
                            <span className="badge badge-primary badge-lg">{material.materialType}</span>
                        </p>
                    </div>
                    <div className="card-body">
                        <p className="font-semibold text-lg">Storage Conditions</p>
                        <p className="text-base">{material.storageConditions || 'N/A'}</p>
                    </div>
                    <div className="card-body">
                        <p className="font-semibold text-lg">Specification Document</p>
                        <p className="text-base">{material.specificationDocument || 'N/A'}</p>
                    </div>
                    <div className="card-body">
                        <p className="font-semibold text-lg">Created Date</p>
                        <p className="text-base">{new Date(material.createdDate).toLocaleDateString()}</p>
                    </div>
                    <div className="card-body">
                        <p className="font-semibold text-lg">Modified Date</p>
                        <p className="text-base">{new Date(material.modifiedDate).toLocaleDateString()}</p>
                    </div>
                </div>
                
                <div className="flex gap-2 mt-8">
                    <button
                        onClick={handleDelete}
                        className="btn btn-error"
                    >
                        Delete Material
                    </button>
                    <button
                        onClick={() => navigate('/materials')}
                        className="btn btn-secondary"
                    >
                        Back to List
                    </button>
                </div>
            </div>
        </div>
    );
};
