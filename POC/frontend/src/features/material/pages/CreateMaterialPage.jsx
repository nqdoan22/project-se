import React from 'react';
import { MaterialForm } from '../components/MaterialForm';

/**
 * CreateMaterialPage
 * Page for creating a new material
 */
export const CreateMaterialPage = () => {
    return (
        <div className="container mx-auto py-8 px-4">
            <h1 className="text-3xl font-bold mb-6">Create New Material</h1>
            <MaterialForm />
        </div>
    );
};
