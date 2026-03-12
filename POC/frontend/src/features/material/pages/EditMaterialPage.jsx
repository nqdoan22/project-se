import React from 'react';
import { useParams } from 'react-router-dom';
import { MaterialForm } from '../components/MaterialForm';

/**
 * EditMaterialPage
 * Page for editing an existing material
 */
export const EditMaterialPage = () => {
    const { id } = useParams();
    
    return (
        <div className="container mx-auto py-8 px-4">
            <h1 className="text-3xl font-bold mb-6">Edit Material</h1>
            <MaterialForm materialId={id} />
        </div>
    );
};
