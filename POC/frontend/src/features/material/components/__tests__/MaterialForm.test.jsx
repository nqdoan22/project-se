import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import { MaterialForm } from '../MaterialForm';
import * as materialService from '../../services/materialService';

jest.mock('../../services/materialService');

const renderWithRouter = (component) => {
    return render(<BrowserRouter>{component}</BrowserRouter>);
};

describe('MaterialForm Component', () => {
    beforeEach(() => {
        jest.clearAllMocks();
    });
    
    it('should render form with all required fields', () => {
        renderWithRouter(<MaterialForm />);
        
        expect(screen.getByLabelText(/Material ID/i)).toBeInTheDocument();
        expect(screen.getByLabelText(/Part Number/i)).toBeInTheDocument();
        expect(screen.getByLabelText(/Material Name/i)).toBeInTheDocument();
        expect(screen.getByLabelText(/Material Type/i)).toBeInTheDocument();
    });
    
    it('should submit form with valid data', async () => {
        materialService.createMaterial.mockResolvedValue({ data: { materialId: 'MAT-001' } });
        
        renderWithRouter(<MaterialForm />);
        
        // Fill form fields
        fireEvent.change(screen.getByLabelText(/Material ID/i), { target: { value: 'MAT-001' } });
        fireEvent.change(screen.getByLabelText(/Part Number/i), { target: { value: 'PN-001' } });
        fireEvent.change(screen.getByLabelText(/Material Name/i), { target: { value: 'Test Material' } });
        fireEvent.change(screen.getByLabelText(/Material Type/i), { target: { value: 'API' } });
        
        // Submit form
        fireEvent.click(screen.getByText(/Create Material/i));
        
        // Assert service was called
        await waitFor(() => {
            expect(materialService.createMaterial).toHaveBeenCalled();
        });
    });
    
    it('should display validation errors for empty required fields', async () => {
        renderWithRouter(<MaterialForm />);
        
        // Try to submit without filling required fields
        fireEvent.click(screen.getByText(/Create Material/i));
        
        // Assert error messages are shown
        await waitFor(() => {
            expect(screen.getByText(/Material ID is required/i)).toBeInTheDocument();
        });
    });
    
    it('should load and display existing material for editing', async () => {
        const mockMaterial = {
            materialId: 'MAT-001',
            partNumber: 'PN-001',
            materialName: 'Existing Material',
            materialType: 'API'
        };
        
        materialService.getMaterialById.mockResolvedValue({ data: mockMaterial });
        
        renderWithRouter(<MaterialForm materialId="MAT-001" />);
        
        await waitFor(() => {
            expect(screen.getByDisplayValue('Existing Material')).toBeInTheDocument();
        });
    });
});
