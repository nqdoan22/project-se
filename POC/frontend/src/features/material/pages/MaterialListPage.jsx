import React from 'react';
import { MaterialList } from '../components/MaterialList';

/**
 * MaterialListPage
 * Page component that displays the material list
 */
export const MaterialListPage = () => {
    return (
        <div className="container mx-auto py-8 px-4">
            <MaterialList />
        </div>
    );
};
