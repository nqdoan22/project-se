import { useState } from 'react';
import { useMutation } from '@tanstack/react-query';
import axios from 'axios';

const AddProductionBatch = () => {
  const [formData, setFormData] = useState({
    product_id: '',
    batch_number: '',
    batch_size: '',
    unit_of_measure: '',
    manufacture_date: '',
    expiration_date: '',
    added_by: '',
  });

  const [components, setComponents] = useState([]);
  
  const [currentComponent, setCurrentComponent] = useState({
    lot_id: '',
    planned_quantity: '',
    unit_of_measure: '',
    added_by: '',
  });

  const mutation = useMutation({
    mutationFn: async (payload) => {
      const response = await axios.post('/api/production-batches', payload);
      return response.data;
    },
  });

  const handleFormChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));
  };

  const handleComponentChange = (e) => {
    const { name, value } = e.target;
    setCurrentComponent((prev) => ({
      ...prev,
      [name]: value,
    }));
  };

  const addComponent = () => {
    const componentToAdd = {
      lot_id: currentComponent.lot_id,
      planned_quantity: currentComponent.planned_quantity,
      unit_of_measure: currentComponent.unit_of_measure,
      ...(currentComponent.added_by && { added_by: currentComponent.added_by }),
    };
    
    setComponents((prev) => [...prev, componentToAdd]);
    setCurrentComponent({
      lot_id: '',
      planned_quantity: '',
      unit_of_measure: '',
      added_by: '',
    });
  };

  const removeComponent = (index) => {
    setComponents((prev) => prev.filter((_, i) => i !== index));
  };

  const handleSubmit = (e) => {
    e.preventDefault();

    const payload = {
      product_id: formData.product_id,
      batch_number: formData.batch_number,
      batch_size: formData.batch_size,
      unit_of_measure: formData.unit_of_measure,
      manufacture_date: formData.manufacture_date,
      expiration_date: formData.expiration_date,
      components: components,
      ...(formData.added_by && { added_by: formData.added_by }),
    };

    mutation.mutate(payload);
  };

  return (
    <form onSubmit={handleSubmit}>
      <div>
        <label htmlFor="product_id">Product ID</label>
        <input
          type="text"
          id="product_id"
          name="product_id"
          value={formData.product_id}
          onChange={handleFormChange}
          required
        />
      </div>

      <div>
        <label htmlFor="batch_number">Batch Number</label>
        <input
          type="text"
          id="batch_number"
          name="batch_number"
          value={formData.batch_number}
          onChange={handleFormChange}
          required
        />
      </div>

      <div>
        <label htmlFor="batch_size">Batch Size</label>
        <input
          type="number"
          id="batch_size"
          name="batch_size"
          value={formData.batch_size}
          onChange={handleFormChange}
          required
        />
      </div>

      <div>
        <label htmlFor="unit_of_measure">Unit of Measure</label>
        <input
          type="text"
          id="unit_of_measure"
          name="unit_of_measure"
          value={formData.unit_of_measure}
          onChange={handleFormChange}
          required
        />
      </div>

      <div>
        <label htmlFor="manufacture_date">Manufacture Date</label>
        <input
          type="date"
          id="manufacture_date"
          name="manufacture_date"
          value={formData.manufacture_date}
          onChange={handleFormChange}
          required
        />
      </div>

      <div>
        <label htmlFor="expiration_date">Expiration Date</label>
        <input
          type="date"
          id="expiration_date"
          name="expiration_date"
          value={formData.expiration_date}
          onChange={handleFormChange}
          required
        />
      </div>

      <div>
        <label htmlFor="added_by">Added By (Optional)</label>
        <input
          type="text"
          id="added_by"
          name="added_by"
          value={formData.added_by}
          onChange={handleFormChange}
        />
      </div>

      <fieldset>
        <legend>Components</legend>

        <div>
          <label htmlFor="lot_id">Lot ID</label>
          <input
            type="text"
            id="lot_id"
            name="lot_id"
            value={currentComponent.lot_id}
            onChange={handleComponentChange}
          />
        </div>

        <div>
          <label htmlFor="planned_quantity">Planned Quantity</label>
          <input
            type="number"
            id="planned_quantity"
            name="planned_quantity"
            value={currentComponent.planned_quantity}
            onChange={handleComponentChange}
          />
        </div>

        <div>
          <label htmlFor="component_unit_of_measure">Unit of Measure</label>
          <input
            type="text"
            id="component_unit_of_measure"
            name="unit_of_measure"
            value={currentComponent.unit_of_measure}
            onChange={handleComponentChange}
          />
        </div>

        <div>
          <label htmlFor="component_added_by">Added By (Optional)</label>
          <input
            type="text"
            id="component_added_by"
            name="added_by"
            value={currentComponent.added_by}
            onChange={handleComponentChange}
          />
        </div>

        <button type="button" onClick={addComponent}>
          Add Component
        </button>

        {components.length > 0 && (
          <div>
            <h4>Added Components</h4>
            <ul>
              {components.map((component, index) => (
                <li key={index}>
                  Lot ID: {component.lot_id}, Quantity: {component.planned_quantity}, UOM: {component.unit_of_measure}
                  {component.added_by && `, Added By: ${component.added_by}`}
                  <button
                    type="button"
                    onClick={() => removeComponent(index)}
                  >
                    Remove
                  </button>
                </li>
              ))}
            </ul>
          </div>
        )}
      </fieldset>

      <button type="submit" disabled={mutation.isPending}>
        {mutation.isPending ? 'Submitting...' : 'Submit'}
      </button>

      {mutation.isError && (
        <div>
          Error submitting form: {mutation.error?.message}
        </div>
      )}

      {mutation.isSuccess && (
        <div>
          Production batch created successfully!
        </div>
      )}
    </form>
  );
};

export default AddProductionBatch;
