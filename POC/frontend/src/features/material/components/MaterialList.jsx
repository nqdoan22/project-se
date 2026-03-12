import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import { useMaterials } from '../hooks/useMaterials';
import { materialService } from '../services/materialService';

/**
 * MaterialList Component
 * Displays a table of all materials with options to view, edit, and create new materials
 * Features:
 *   - List all materials
 *   - Search/filter functionality
 *   - View material details
 *   - Edit material
 *   - Delete material (with confirmation)
 */
export const MaterialList = () => {
    const { materials, loading, error } = useMaterials();
    const [searchTerm, setSearchTerm] = useState('');
    const [filteredMaterials, setFilteredMaterials] = useState(materials);
    
    React.useEffect(() => {
        const filtered = materials.filter(m => 
            m.materialName.toLowerCase().includes(searchTerm.toLowerCase()) ||
            m.materialId.toLowerCase().includes(searchTerm.toLowerCase()) ||
            m.partNumber.toLowerCase().includes(searchTerm.toLowerCase())
        );
        setFilteredMaterials(filtered);
    }, [materials, searchTerm]);
    
    const handleDelete = async (materialId) => {
        if (window.confirm('Are you sure you want to delete this material?')) {
            try {
                await materialService.deleteMaterial(materialId);
                window.location.reload();
            } catch (err) {
                alert('Error deleting material: ' + err.message);
            }
        }
    };
    
    if (loading) return <div className="alert">Loading materials...</div>;
    if (error) return <div className="alert alert-error">Error: {error}</div>;
    
    return (
        <div className="material-list">
            <div className="flex justify-between items-center mb-4">
                <h2 className="text-2xl font-bold">Materials</h2>
                <Link to="/materials/create" className="btn btn-primary">
                    Add New Material
                </Link>
            </div>
            
            <div className="mb-4">
                <input
                    type="text"
                    placeholder="Search by name, ID, or part number..."
                    value={searchTerm}
                    onChange={(e) => setSearchTerm(e.target.value)}
                    className="input input-bordered w-full"
                />
            </div>
            
            {filteredMaterials.length === 0 ? (
                <p className="text-center text-gray-500">No materials found.</p>
            ) : (
                <div className="overflow-x-auto">
                    <table className="table table-striped w-full">
                        <thead>
                            <tr>
                                <th>Material ID</th>
                                <th>Part Number</th>
                                <th>Name</th>
                                <th>Type</th>
                                <th>Storage Conditions</th>
                                <th>Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            {filteredMaterials.map(material => (
                                <tr key={material.materialId}>
                                    <td>{material.materialId}</td>
                                    <td>{material.partNumber}</td>
                                    <td>{material.materialName}</td>
                                    <td>
                                        <span className="badge badge-primary">
                                            {material.materialType}
                                        </span>
                                    </td>
                                    <td>{material.storageConditions || 'N/A'}</td>
                                    <td className="space-x-2">
                                        <Link 
                                            to={`/materials/${material.materialId}`}
                                            className="btn btn-sm btn-info"
                                        >
                                            View
                                        </Link>
                                        <Link 
                                            to={`/materials/${material.materialId}/edit`}
                                            className="btn btn-sm btn-warning"
                                        >
                                            Edit
                                        </Link>
                                        <button
                                            onClick={() => handleDelete(material.materialId)}
                                            className="btn btn-sm btn-error"
                                        >
                                            Delete
                                        </button>
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>
            )}
        </div>
    );
};
