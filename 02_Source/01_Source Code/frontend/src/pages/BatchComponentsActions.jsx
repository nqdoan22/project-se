import { useState } from 'react';
import Modal from '../components/Modal';

export function AddComponentForm({ batchId: _batchId, lots, onSubmit, onClose, loading, error }) {
  const [form, setForm] = useState({
    lotId: '',
    plannedQuantity: '',
    unitOfMeasure: '',
    addedBy: '',
  });
  const set = (k, v) => setForm((f) => ({ ...f, [k]: v }));
  const acceptedLots = lots.filter((l) => l.status === 'Accepted');

  return (
    <form onSubmit={(e) => {
      e.preventDefault();
      onSubmit({ ...form, plannedQuantity: parseFloat(form.plannedQuantity) });
    }}>
      <div className="modal-body">
        {error && <div className="alert alert-error">⚠ {error}</div>}
        <div className="form-grid">
          <div className="form-group form-full">
            <label className="form-label required">Lô nguyên liệu (Accepted)</label>
            <select
              className="form-control"
              value={form.lotId}
              onChange={(e) => set('lotId', e.target.value)}
              required
              id="comp-lotId"
            >
              <option value="">-- Chọn lô nguyên liệu --</option>
              {acceptedLots.map((l) => (
                <option key={l.lotId} value={l.lotId}>
                  {l.partNumber} — {l.materialName} | SL: {l.quantity} {l.unitOfMeasure}
                </option>
              ))}
            </select>
            {acceptedLots.length === 0 && (
              <span className="form-error">Không có lô nào ở trạng thái Accepted</span>
            )}
          </div>
          <div className="form-group">
            <label className="form-label required">Số lượng kế hoạch</label>
            <input
              className="form-control"
              type="number"
              min="0.001"
              step="0.001"
              value={form.plannedQuantity}
              onChange={(e) => set('plannedQuantity', e.target.value)}
              placeholder="VD: 100"
              required
              id="comp-plannedQty"
            />
          </div>
          <div className="form-group">
            <label className="form-label required">Đơn vị</label>
            <input
              className="form-control"
              value={form.unitOfMeasure}
              onChange={(e) => set('unitOfMeasure', e.target.value)}
              placeholder="kg / L / pcs"
              required
              id="comp-uom"
            />
          </div>
          <div className="form-group form-full">
            <label className="form-label">Người thêm</label>
            <input
              className="form-control"
              value={form.addedBy}
              onChange={(e) => set('addedBy', e.target.value)}
              placeholder="VD: operator01"
              id="comp-addedBy"
            />
          </div>
        </div>
      </div>
      <div className="modal-footer">
        <button type="button" className="btn btn-outline" onClick={onClose} disabled={loading}>Huỷ</button>
        <button type="submit" className="btn btn-primary" disabled={loading} id="btn-submit-component">
          {loading ? '⏳ Đang thêm...' : '➕ Thêm nguyên liệu'}
        </button>
      </div>
    </form>
  );
}

export function ModifyComponentForm({ component, lots, onSubmit, onClose, loading, error }) {
  const [form, setForm] = useState({
    lotId: component.lotId || '',
    plannedQuantity: component.plannedQuantity?.toString() || '',
    unitOfMeasure: component.unitOfMeasure || '',
    actualQuantity: component.actualQuantity?.toString() || '',
    addedBy: component.addedBy || '',
  });
  const [validationError, setValidationError] = useState('');
  const set = (k, v) => setForm((f) => ({ ...f, [k]: v }));
  const acceptedLots = lots.filter((l) => l.status === 'Accepted');
  const selectedLot = acceptedLots.find((l) => l.lotId === form.lotId);
  const remainingQuantity = selectedLot?.quantity ?? 0;
  const plannedQty = parseFloat(form.plannedQuantity) || 0;
  const actualQty = parseFloat(form.actualQuantity) || 0;
  const exceedsPlanned = actualQty > plannedQty && form.actualQuantity !== '';
  const exceedsRemaining = plannedQty > remainingQuantity && form.plannedQuantity !== '';

  return (
    <form onSubmit={(e) => {
      e.preventDefault();
      if (exceedsRemaining) {
        setValidationError(`Số lượng kế hoạch không thể vượt quá ${remainingQuantity} ${selectedLot?.unitOfMeasure}`);
        return;
      }
      if (exceedsPlanned) {
        setValidationError(`Số lượng thực tế không thể vượt quá số lượng kế hoạch (${plannedQty})`);
        return;
      }
      onSubmit({
        ...form,
        plannedQuantity: parseFloat(form.plannedQuantity),
        actualQuantity: form.actualQuantity ? parseFloat(form.actualQuantity) : null,
      });
    }}>
      <div className="modal-body">
        {error && <div className="alert alert-error">⚠ {error}</div>}
        {validationError && <div className="alert alert-error">⚠ {validationError}</div>}
        <div className="form-grid">
          <div className="form-group form-full">
            <label className="form-label required">Lô nguyên liệu (Accepted)</label>
            <select
              className="form-control"
              value={form.lotId}
              onChange={(e) => set('lotId', e.target.value)}
              required
              disabled={true}
              id="modify-comp-lotId"
            >
              <option value="">-- Chọn lô nguyên liệu --</option>
              {acceptedLots.map((l) => (
                <option key={l.lotId} value={l.lotId}>
                  {l.partNumber} — {l.materialName} | SL: {l.quantity} {l.unitOfMeasure}
                </option>
              ))}
            </select>
          </div>
          <div className="form-group">
            <label className="form-label required">Số lượng kế hoạch {selectedLot && `(Tối đa: ${remainingQuantity} ${selectedLot.unitOfMeasure})`}</label>
            <input
              className="form-control"
              type="number"
              min="0.001"
              step="0.001"
              value={form.plannedQuantity}
              onChange={(e) => { setValidationError(''); set('plannedQuantity', e.target.value); }}
              placeholder="VD: 100"
              required
              id="modify-comp-plannedQty"
              style={exceedsRemaining ? { borderColor: 'var(--error)' } : {}}
            />
            {exceedsRemaining && <span className="form-error">Số lượng vượt quá lượng sẵn có ({remainingQuantity} {selectedLot?.unitOfMeasure})</span>}
          </div>
          <div className="form-group">
            <label className="form-label required">Đơn vị</label>
            <input
              className="form-control"
              value={form.unitOfMeasure}
              onChange={(e) => set('unitOfMeasure', e.target.value)}
              placeholder="kg / L / pcs"
              required
              id="modify-comp-uom"
            />
          </div>
          <div className="form-group">
            <label className="form-label">Số lượng thực tế sử dụng {form.plannedQuantity && `(Tối đa: ${plannedQty})`}</label>
            <input
              className="form-control"
              type="number"
              min="0"
              step="0.001"
              value={form.actualQuantity}
              onChange={(e) => { setValidationError(''); set('actualQuantity', e.target.value); }}
              placeholder="VD: 95.5"
              id="modify-comp-actualQty"
              style={exceedsPlanned ? { borderColor: 'var(--error)' } : {}}
            />
            {exceedsPlanned && <span className="form-error">Không thể vượt quá số lượng kế hoạch ({plannedQty})</span>}
          </div>
          <div className="form-group form-full">
            <label className="form-label">Người thêm</label>
            <input
              className="form-control"
              value={form.addedBy}
              onChange={(e) => set('addedBy', e.target.value)}
              placeholder="VD: operator01"
              id="modify-comp-addedBy"
            />
          </div>
        </div>
      </div>
      <div className="modal-footer">
        <button type="button" className="btn btn-outline" onClick={onClose} disabled={loading}>Huỷ</button>
        <button type="submit" className="btn btn-primary" disabled={loading || exceedsRemaining || exceedsPlanned} id="btn-modify-component">
          {loading ? '⏳ Đang lưu...' : '💾 Lưu thay đổi'}
        </button>
      </div>
    </form>
  );
}

export function ConfirmUsageModal({ component, onSubmit, onClose, loading, error }) {
  const [actualQuantity, setActualQuantity] = useState(component.plannedQuantity?.toString() || '');
  const [performedBy, setPerformedBy] = useState('');
  const [validationError, setValidationError] = useState('');
  const actualQty = parseFloat(actualQuantity) || 0;
  const plannedQty = component.plannedQuantity || 0;
  const exceedsPlanned = actualQty > plannedQty && actualQuantity !== '';

  return (
    <Modal
      title="✔ Xác nhận lượng sử dụng thực tế"
      onClose={onClose}
      footer={
        <>
          <button className="btn btn-outline" onClick={onClose} disabled={loading}>Huỷ</button>
          <button
            id="btn-confirm-usage"
            className="btn btn-primary"
            disabled={loading || !actualQuantity || !performedBy || exceedsPlanned}
            onClick={() => {
              if (exceedsPlanned) {
                setValidationError(`Số lượng thực tế không thể vượt quá số lượng kế hoạch (${plannedQty})`);
                return;
              }
              onSubmit(parseFloat(actualQuantity), performedBy);
            }}
          >
            {loading ? 'Đang xác nhận...' : '✔ Xác nhận'}
          </button>
        </>
      }
    >
      {error && <div className="alert alert-error">⚠ {error}</div>}
      {validationError && <div className="alert alert-error">⚠ {validationError}</div>}
      <p style={{ marginBottom: 12 }}>
        Nguyên liệu: <strong>{component.materialName}</strong> ({component.partNumber || 'N/A'})
      </p>
      <p style={{ marginBottom: 16 }} className="text-muted">
        Số lượng kế hoạch: <strong>{component.plannedQuantity} {component.unitOfMeasure}</strong>
      </p>
      <div className="form-group">
        <label className="form-label required">Số lượng thực tế sử dụng (Tối đa: {plannedQty})</label>
        <input
          className="form-control"
          type="number"
          min="0"
          step="0.001"
          value={actualQuantity}
          onChange={(e) => { setValidationError(''); setActualQuantity(e.target.value); }}
          id="actual-quantity"
          required
          style={exceedsPlanned ? { borderColor: 'var(--error)' } : {}}
        />
        {exceedsPlanned && <span className="form-error">Không thể vượt quá số lượng kế hoạch ({plannedQty})</span>}
      </div>
      <div className="form-group">
        <label className="form-label required">Người thực hiện</label>
        <input
          className="form-control"
          type="text"
          placeholder='Tên người xác nhận component'
          value={performedBy}
          onChange={(e) => setPerformedBy(e.target.value)}
          id="performed-by"
          required
        />
      </div>
    </Modal>
  );
}

export function DeleteConfirmationModal({ component, onConfirm, onCancel, loading, error }) {
  return (
    <Modal
      title="🗑️ Xóa nguyên liệu"
      onClose={onCancel}
      footer={
        <>
          <button className="btn btn-outline" onClick={onCancel} disabled={loading}>
            Huỷ
          </button>
          <button
            id="btn-confirm-delete"
            className="btn btn-danger"
            disabled={loading}
            onClick={onConfirm}
          >
            {loading ? 'Đang xóa...' : '🗑️ Xóa'}
          </button>
        </>
      }
    >
      {error && <div className="alert alert-error">⚠ {error}</div>}
      <p style={{ marginBottom: 16 }}>
        Bạn có chắc chắn muốn xóa nguyên liệu sau?
      </p>
      <div style={{ padding: '12px', backgroundColor: 'var(--bg-secondary)', borderRadius: '4px' }}>
        <p style={{ margin: '4px 0' }}>
          <strong>Nguyên liệu:</strong> {component.materialName}
        </p>
        <p style={{ margin: '4px 0' }}>
          <strong>Part Number:</strong> {component.partNumber || 'N/A'}
        </p>
        <p style={{ margin: '4px 0' }}>
          <strong>Số lượng kế hoạch:</strong> {component.plannedQuantity} {component.unitOfMeasure}
        </p>
        {component.actualQuantity != null && (
          <p style={{ margin: '4px 0' }}>
            <strong>Số lượng thực tế:</strong> {component.actualQuantity} {component.unitOfMeasure}
          </p>
        )}
      </div>
      <p style={{ marginTop: 16, color: 'var(--error)', fontSize: '14px' }}>
        ⚠️ Thao tác này không thể hoàn tác!
      </p>
    </Modal>
  );
}
